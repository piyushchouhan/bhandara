package com.example.bhandara.ui.saved

import com.example.bhandara.data.location.DeviceLocation
import com.example.bhandara.data.location.GeoLocation
import com.example.bhandara.data.location.LocationService
import com.example.bhandara.data.location.TimedLocation
import com.example.bhandara.data.network.ApiClient
import com.example.bhandara.data.network.FeastApi
import com.example.bhandara.data.network.ShopApi
import com.example.bhandara.data.settings.AppSettings
import com.russhwolf.settings.MapSettings
import com.russhwolf.settings.Settings
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.MockEngineConfig
import io.ktor.client.engine.mock.respond
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Saved places (Profile → My activity), the same on Android and iOS: the shops and feasts saved on the phone, as
 * they are now (open, closed, gone, over), filtered, sorted, on a map, and unsaved with Undo. The backend is a fake.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class SavedPlacesViewModelTest {

    private val dispatcher = StandardTestDispatcher()

    @BeforeTest
    fun setUp() = Dispatchers.setMain(dispatcher)

    @AfterTest
    fun tearDown() = Dispatchers.resetMain()

    // ==================== Fakes ====================

    private val here = GeoLocation(18.5823, 73.8845)
    private val now = LocalDateTime(LocalDate(2026, 10, 12), LocalTime(13, 0))

    private class FakePhone(var location: GeoLocation?) : DeviceLocation {
        override fun hasPermission() = location != null
        override suspend fun current() = location
        override suspend fun lastKnown(): TimedLocation? = null
    }

    private val phone = FakePhone(here)
    private var clock = 1_000L
    private val settings = AppSettings(object : Settings.Factory {
        private val stores = mutableMapOf<String?, Settings>()
        override fun create(name: String?): Settings = stores.getOrPut(name) { MapSettings() }
    }, nowMillis = { clock })

    /** What the fake backend answers, by path */
    private val answers = mutableMapOf<String, Pair<HttpStatusCode, String>>()

    // The fake backend answers on the test's dispatcher, so advanceUntilIdle() also waits for its answers
    private val client = ApiClient("https://api.test/", { "id-token" }, MockEngine(MockEngineConfig().apply {
        dispatcher = this@SavedPlacesViewModelTest.dispatcher
        addHandler { request ->
            val (status, body) = answers[request.url.encodedPath] ?: (HttpStatusCode.NotFound to """{"message":"Not found"}""")
            respond(body, status, headersOf(HttpHeaders.ContentType, "application/json"))
        }
    }))

    private fun TestScope.opened(): SavedPlacesViewModel =
        SavedPlacesViewModel(settings, ShopApi(client), FeastApi(client), LocationService(phone, nowMillis = { 0 }), now = { now })
            .also { advanceUntilIdle() }

    private fun savedShop(id: String, json: String) {
        clock += 1
        settings.setShopSaved(id, true)
        answers["/api/localshops/$id"] = HttpStatusCode.OK to json
    }

    private fun savedFeast(id: String, json: String) {
        clock += 1
        settings.setFeastSaved(id, true)
        answers["/api/feasts/$id"] = HttpStatusCode.OK to json
    }

    private fun SavedPlacesState.names(): List<String> = shown().map {
        when (it) {
            is SavedPlace.Shop -> it.shop?.shopName ?: "gone shop ${it.id}"
            is SavedPlace.Feast -> it.feast?.organizerName ?: "gone feast ${it.id}"
        }
    }

    // ==================== Tests ====================

    @Test
    fun savedShopsAndFeastsAreShownLatestSavedFirstWithDistance() = runTest(dispatcher) {
        savedShop("1", shop("1", "Shri Krishna", lat = 18.5833))
        savedFeast("9", feast("9", "Ganesh Mandal", date = "2026-10-12", start = "12:00:00", end = "15:00:00"))
        savedShop("2", shop("2", "Annapurna Misal", lat = 18.5900))

        val state = opened().state.value

        assertEquals(listOf("Annapurna Misal", "Ganesh Mandal", "Shri Krishna"), state.names())
        val near = state.places.first { it.id == "1" }
        assertTrue(near.distanceMeters!! in 100.0..120.0)
    }

    @Test
    fun openNowCountsOpenShopsAndFeastsOnNow() = runTest(dispatcher) {
        savedShop("1", shop("1", "Open Chaat", open = true))
        savedShop("2", shop("2", "Closed Thali", open = false))
        savedFeast("9", feast("9", "Lunch Langar", date = "2026-10-12", start = "12:00:00", end = "15:00:00"))
        savedFeast("8", feast("8", "Tomorrow Langar", date = "2026-10-13", start = "12:00:00", end = "15:00:00"))
        val viewModel = opened()

        assertEquals(2, viewModel.state.value.openNowCount)
        assertEquals(2, viewModel.state.value.feastCount)

        viewModel.onFilter(SavedFilter.OPEN_NOW)
        assertEquals(setOf("Open Chaat", "Lunch Langar"), viewModel.state.value.names().toSet())

        viewModel.onFilter(SavedFilter.FEASTS)
        assertEquals(setOf("Lunch Langar", "Tomorrow Langar"), viewModel.state.value.names().toSet())
    }

    @Test
    fun placesThatAreGoneAreListedLastAsGone() = runTest(dispatcher) {
        savedShop("1", shop("1", "Closed Down", active = false))
        savedShop("2", shop("2", "Rejected Stall", status = "REJECTED"))
        settings.setShopSaved("3", true) // the backend no longer has it at all
        savedFeast("9", feast("9", "Last Week Langar", date = "2026-10-05", start = "12:00:00", end = "15:00:00"))
        savedShop("4", shop("4", "Still Here"))

        val state = opened().state.value

        assertEquals("Still Here", state.names().first())
        assertTrue(state.shown().drop(1).all { it.isGone })
        assertNull((state.places.single { it.id == "3" } as SavedPlace.Shop).shop)
        assertNull(state.error)
    }

    @Test
    fun aConnectionProblemIsNotMistakenForAPlaceBeingGone() = runTest(dispatcher) {
        settings.setShopSaved("1", true)
        answers["/api/localshops/1"] = HttpStatusCode.InternalServerError to """{"message":"Server error"}"""

        val viewModel = opened()

        assertNotNull(viewModel.state.value.error)
        assertTrue(viewModel.state.value.places.isEmpty())

        answers["/api/localshops/1"] = HttpStatusCode.OK to shop("1", "Shri Krishna")
        viewModel.load()
        advanceUntilIdle()
        assertNull(viewModel.state.value.error)
        assertEquals(listOf("Shri Krishna"), viewModel.state.value.names())
    }

    @Test
    fun placesCanBeSortedByDistanceOrRating() = runTest(dispatcher) {
        savedShop("1", shop("1", "Far But Great", lat = 18.60, rating = 4.9))
        savedShop("2", shop("2", "Near And Fine", lat = 18.5824, rating = 3.9))
        val viewModel = opened()

        viewModel.onSort(SavedSort.NEAREST)
        assertEquals(listOf("Near And Fine", "Far But Great"), viewModel.state.value.names())

        viewModel.onSort(SavedSort.RATING)
        assertEquals(listOf("Far But Great", "Near And Fine"), viewModel.state.value.names())
    }

    @Test
    fun unsavingTakesThePlaceOffThePhoneAndUndoPutsItBack() = runTest(dispatcher) {
        savedShop("1", shop("1", "Shri Krishna"))
        savedFeast("9", feast("9", "Ganesh Mandal", date = "2026-10-12", start = "12:00:00", end = "15:00:00"))
        val viewModel = opened()
        val shop = viewModel.state.value.places.single { it.id == "1" }

        viewModel.remove(shop)
        assertFalse(settings.isShopSaved("1"))
        assertEquals(listOf("Ganesh Mandal"), viewModel.state.value.names())
        assertEquals(shop, viewModel.state.value.removed)

        viewModel.undo()
        assertTrue(settings.isShopSaved("1"))
        assertEquals(setOf("Shri Krishna", "Ganesh Mandal"), viewModel.state.value.names().toSet())
        assertNull(viewModel.state.value.removed)
    }

    @Test
    fun onTheMapOnlyPlacesStillThereAreShownAndOneCanBePicked() = runTest(dispatcher) {
        savedShop("1", shop("1", "Shri Krishna"))
        savedShop("2", shop("2", "Closed Down", active = false))
        val viewModel = opened()
        viewModel.onShowMap(true)

        val onMap = viewModel.state.value.onMap()
        assertEquals(listOf("1"), onMap.map { it.id })

        viewModel.select(onMap.single())
        assertEquals("1", viewModel.state.value.selected?.id)

        viewModel.onShowMap(false)
        assertNull(viewModel.state.value.selected)
    }

    @Test
    fun withoutLocationPlacesStillLoadWithoutDistances() = runTest(dispatcher) {
        phone.location = null
        savedShop("1", shop("1", "Shri Krishna"))

        val state = opened().state.value

        assertEquals(listOf("Shri Krishna"), state.names())
        assertNull(state.places.single().distanceMeters)
    }

    // ==================== Test data ====================

    private companion object {
        fun shop(
            id: String, name: String, lat: Double = 18.5833, open: Boolean = true, active: Boolean = true,
            status: String = "VERIFIED", rating: Double = 4.5,
        ) = """{"id":"$id","shopName":"$name","shopType":"Street Food","latitude":$lat,"longitude":73.8845,""" +
            """"isCurrentlyOpen":$open,"isActive":$active,"verificationStatus":"$status","averageRating":$rating,"openingTime":"11:00:00"}"""

        fun feast(id: String, name: String, date: String, start: String, end: String) =
            """{"id":"$id","organizerName":"$name","menuItems":["Puri"],"feastDate":"$date","startTime":"$start","endTime":"$end",""" +
                """"latitude":18.59,"longitude":73.88,"estimatedCapacity":100,"isActive":true}"""
    }
}
