package com.example.bhandara.ui.feasts

import com.example.bhandara.data.network.ActivityApi
import io.ktor.http.content.TextContent
import com.example.bhandara.data.location.DeviceLocation
import com.example.bhandara.data.location.GeoLocation
import com.example.bhandara.data.location.LocationService
import com.example.bhandara.data.location.TimedLocation
import com.example.bhandara.data.network.ApiClient
import com.example.bhandara.data.network.FeastApi
import com.example.bhandara.data.models.api.FeastReportReason
import com.example.bhandara.photos.CompressedPhoto
import com.example.bhandara.photos.PhotoUploadException
import com.example.bhandara.photos.PickedPhoto
import io.ktor.http.content.OutgoingContent
import com.example.bhandara.data.settings.AppSettings
import com.example.bhandara.ui.shopsmap.MapLocationState
import com.russhwolf.settings.MapSettings
import com.russhwolf.settings.Settings
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.MockEngineConfig
import io.ktor.client.engine.mock.respond
import io.ktor.client.request.HttpRequestData
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
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
import kotlin.test.assertSame
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Community feasts, the same on Android and iOS: the "I'm hungry" map of feasts around the user, and one feast's
 * page with reporting. The phone's location, the clock and the backend are fakes.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class FeastViewModelsTest {

    private val dispatcher = StandardTestDispatcher()

    @BeforeTest
    fun setUp() = Dispatchers.setMain(dispatcher)

    @AfterTest
    fun tearDown() = Dispatchers.resetMain()

    // ==================== Fakes ====================

    private class FakePhone(var permitted: Boolean = true, var location: GeoLocation? = null) : DeviceLocation {
        override fun hasPermission() = permitted
        override suspend fun current() = location
        override suspend fun lastKnown(): TimedLocation? = null
    }

    private val here = GeoLocation(18.5823, 73.8845)
    private val phone = FakePhone(location = here)
    private var now = LocalDateTime(LocalDate(2026, 10, 4), LocalTime(19, 0))
    private var signedInUid: String? = "me-1"
    private val requests = mutableListOf<HttpRequestData>()
    private val responses = mutableMapOf<Pair<HttpMethod, String>, Pair<HttpStatusCode, String>>()

    private val settings = AppSettings(object : Settings.Factory {
        private val stores = mutableMapOf<String?, Settings>()
        override fun create(name: String?): Settings = stores.getOrPut(name) { MapSettings() }
    })

    // The fake backend answers on the test's dispatcher, so advanceUntilIdle() also waits for its answers
    private val client = ApiClient("https://api.test/", { "id-token" }, MockEngine(MockEngineConfig().apply {
        dispatcher = this@FeastViewModelsTest.dispatcher
        addHandler { request ->
            requests += request
            val (status, body) = responses[request.method to request.url.encodedPath] ?: (HttpStatusCode.NotFound to "")
            respond(body, status, headersOf(HttpHeaders.ContentType, "application/json"))
        }
    }))

    private fun answer(method: HttpMethod, path: String, body: String, status: HttpStatusCode = HttpStatusCode.OK) {
        responses[method to "/api/$path"] = status to body
    }

    private fun nearbyAre(vararg feasts: String) = answer(HttpMethod.Get, "feasts/nearby", "[${feasts.joinToString(",")}]")

    init {
        nearbyAre(feast("1", "Gurudwara langar"), feast("2", null, start = "20:00:00", end = "23:00:00"))
        answer(HttpMethod.Get, "feasts/1", feast("1", "Gurudwara langar"))
    }

    private fun TestScope.mapStarted(): FeastsMapViewModel =
        FeastsMapViewModel(LocationService(phone, nowMillis = { 0 }), FeastApi(client), { now }).also {
            it.start()
            advanceUntilIdle()
        }

    private fun TestScope.feastOpened(id: String = "1"): FeastDetailsViewModel =
        FeastDetailsViewModel(id, FeastApi(client), settings, { signedInUid }, LocationService(phone, nowMillis = { 0 }), { now })
            .also { advanceUntilIdle() }

    private fun requestsTo(method: HttpMethod, path: String) =
        requests.filter { it.method == method && it.url.encodedPath == "/api/$path" }

    // ==================== The feasts map ====================

    @Test
    fun comingBackToTheMapShowsAFeastPostedMeanwhileWithoutMovingTheMap() = runTest(dispatcher) {
        val viewModel = mapStarted()
        val camera = viewModel.state.value.camera

        nearbyAre(feast("1", "Gurudwara langar"), feast("2", null, start = "20:00:00", end = "23:00:00"), feast("3", "Mandir bhandara"))
        viewModel.start()
        advanceUntilIdle()

        assertEquals(listOf("1", "2", "3"), viewModel.state.value.feasts.map { it.id })
        assertSame(camera, viewModel.state.value.camera)
    }

    @Test
    fun theMapCentresOnTheUserAndShowsFeastsAroundThem() = runTest(dispatcher) {
        val state = mapStarted().state.value

        assertEquals(MapLocationState.Found(here), state.location)
        assertEquals(here, state.camera.center)
        val pins = state.pins(unnamed = "Community feast")
        assertEquals(listOf("Gurudwara langar", "Community feast"), pins.map { it.label })
        assertTrue(pins.all { it.emoji == "🍲" })
        val url = requestsTo(HttpMethod.Get, "feasts/nearby").single().url
        assertEquals("18.5823", url.parameters["lat"])
        assertEquals("73.8845", url.parameters["lon"])
        assertEquals("5000.0", url.parameters["radius"])
    }

    @Test
    fun feastsMapFindsTheUserWhenLocationIsAllowedInAnotherDialog() = runTest(dispatcher) {
        phone.permitted = false
        val viewModel = mapStarted()
        viewModel.onLocationPermissionResult(false)
        advanceUntilIdle()

        phone.permitted = true
        viewModel.onLocationAccessChanged(true)
        advanceUntilIdle()

        assertEquals(MapLocationState.Found(here), viewModel.state.value.location)
    }

    @Test
    fun withoutPermissionTheUserIsAskedAndNothingIsLoaded() = runTest(dispatcher) {
        phone.permitted = false
        val viewModel = mapStarted()

        assertEquals(MapLocationState.PermissionNeeded, viewModel.state.value.location)
        assertTrue(requestsTo(HttpMethod.Get, "feasts/nearby").isEmpty())

        phone.permitted = true
        viewModel.onLocationPermissionResult(granted = true)
        advanceUntilIdle()

        assertEquals(2, viewModel.state.value.pins("").size)
    }

    @Test
    fun whenTheUserRefusesLocationTheMapSaysSo() = runTest(dispatcher) {
        phone.permitted = false
        val viewModel = mapStarted()

        viewModel.onLocationPermissionResult(granted = false)

        assertEquals(MapLocationState.PermissionDenied, viewModel.state.value.location)
    }

    @Test
    fun ifThePhoneCannotFindTheUserTheMapSaysSoInsteadOfShowingAnotherCity() = runTest(dispatcher) {
        phone.location = null

        val state = mapStarted().state.value

        assertEquals(MapLocationState.Unavailable, state.location)
        assertEquals(FeastsMapState().camera.center, state.camera.center)
    }

    @Test
    fun feastsThatFailToLoadAreExplainedAndCanBeRetried() = runTest(dispatcher) {
        answer(HttpMethod.Get, "feasts/nearby", "", HttpStatusCode.ServiceUnavailable)
        val viewModel = mapStarted()

        assertNotNull(viewModel.state.value.error)

        nearbyAre(feast("1", "Gurudwara langar"))
        viewModel.retry()
        advanceUntilIdle()

        assertNull(viewModel.state.value.error)
        assertEquals(1, viewModel.state.value.pins("").size)
    }

    @Test
    fun noFeastsNearbyIsSaidPlainly() = runTest(dispatcher) {
        nearbyAre()

        val state = mapStarted().state.value

        assertTrue(state.noFeastsNearby)
    }

    @Test
    fun feastsThatEndedSinceTheyLoadedAreHidden() = runTest(dispatcher) {
        // Loaded at 7 PM; the langar ends at 10 PM, the other one at 11 PM
        val viewModel = mapStarted()
        assertEquals(2, viewModel.state.value.pins("").size)

        now = LocalDateTime(LocalDate(2026, 10, 4), LocalTime(22, 30))
        viewModel.recenter()
        advanceUntilIdle()

        assertEquals(listOf("2"), viewModel.state.value.pins("").map { it.id })
    }

    @Test
    fun aTappedPinShowsItsFeastAndWhenItIs() = runTest(dispatcher) {
        val viewModel = mapStarted()
        val pin = viewModel.state.value.pins("").first { it.id == "2" }

        viewModel.onPinClick(pin)

        val selected = assertNotNull(viewModel.state.value.selectedFeast())
        assertEquals("2", selected.id)
        assertEquals(FeastTiming.Today(LocalTime(20, 0)), viewModel.state.value.timing(selected))

        viewModel.clearSelection()
        assertNull(viewModel.state.value.selectedFeast())
    }

    // ==================== A feast's page ====================

    @Test
    fun theFeastIsShownWithWhenItIsAndHowFar() = runTest(dispatcher) {
        answer(HttpMethod.Get, "feasts/1", feast("1", "Gurudwara langar", lat = 18.5823, lng = 73.8945))

        val state = feastOpened().state.value

        assertFalse(state.loading)
        assertEquals("Gurudwara langar", state.feast?.organizerName)
        assertEquals(FeastTiming.Now(LocalTime(22, 0)), state.timing)
        // The backend sends 0 for one feast, so the app measures from where the user is (about 1 km)
        val distance = assertNotNull(state.distanceMeters)
        assertTrue(distance in 1000.0..1100.0, "was $distance")
    }

    @Test
    fun aFeastThatNoLongerExistsSaysSoAndCanBeRetried() = runTest(dispatcher) {
        answer(HttpMethod.Get, "feasts/1", "", HttpStatusCode.NotFound)
        val viewModel = feastOpened()

        assertEquals("This is no longer available. It may have been removed.", viewModel.state.value.error)

        answer(HttpMethod.Get, "feasts/1", feast("1", "Gurudwara langar"))
        viewModel.retry()
        advanceUntilIdle()

        assertNotNull(viewModel.state.value.feast)
    }

    @Test
    fun reportingIsRememberedOnlyWhenItWorked() = runTest(dispatcher) {
        answer(HttpMethod.Put, "feasts/1/report", "", HttpStatusCode.TooManyRequests)
        val viewModel = feastOpened()

        viewModel.report(FeastReportReason.WRONG_LOCATION)
        advanceUntilIdle()

        assertTrue(viewModel.state.value.canReport)
        assertFalse(settings.hasReportedFeast("1"))

        answer(HttpMethod.Put, "feasts/1/report", feast("1", "Gurudwara langar"))
        viewModel.report(FeastReportReason.WRONG_LOCATION)
        advanceUntilIdle()

        assertFalse(viewModel.state.value.canReport)
        assertTrue(settings.hasReportedFeast("1"))
        assertEquals("Thanks for reporting. A moderator will take a look.", viewModel.state.value.message)
        // Opening it again still shows it as reported
        assertTrue(feastOpened().state.value.hasReported)
    }

    @Test
    fun aReportSaysWhyAndCarriesTheNoteAndPhotos() = runTest(dispatcher) {
        answer(HttpMethod.Put, "feasts/1/report", feast("1", "Gurudwara langar"))
        val viewModel = FeastDetailsViewModel(
            "1", FeastApi(client), settings, { signedInUid }, LocationService(phone, nowMillis = { 0 }), { now },
            uploadPhotos = { photos -> photos.map { "https://img.test/${it.preview}.webp" } },
        )
        advanceUntilIdle()

        viewModel.report(FeastReportReason.NOT_HAPPENING, "  Nobody here  ", listOf(Photo("gate")))
        advanceUntilIdle()

        val body = (requestsTo(HttpMethod.Put, "feasts/1/report").single().body as OutgoingContent.ByteArrayContent).bytes().decodeToString()
        assertEquals("""{"reason":"NOT_HAPPENING","note":"Nobody here","photoUrls":["https://img.test/gate.webp"]}""", body)
    }

    @Test
    fun ifTheReportsPhotosFailToUploadNothingIsSent() = runTest(dispatcher) {
        val viewModel = FeastDetailsViewModel(
            "1", FeastApi(client), settings, { signedInUid }, LocationService(phone, nowMillis = { 0 }), { now },
            uploadPhotos = { throw PhotoUploadException("Upload failed") },
        )
        advanceUntilIdle()

        viewModel.report(FeastReportReason.NOT_HAPPENING, "", listOf(Photo("gate")))
        advanceUntilIdle()

        assertTrue(requestsTo(HttpMethod.Put, "feasts/1/report").isEmpty())
        assertEquals("Couldn't upload the photos. Please try again.", viewModel.state.value.message)
        assertTrue(viewModel.state.value.canReport)
    }

    private data class Photo(val name: String) : PickedPhoto {
        override val preview: Any = name
        override suspend fun compressed(): CompressedPhoto? = null
    }

    @Test
    fun whenEnoughReportsTakeTheFeastDownThePageSaysSo() = runTest(dispatcher) {
        answer(HttpMethod.Put, "feasts/1/report", feast("1", "Gurudwara langar", active = false))
        val viewModel = feastOpened()

        viewModel.report(FeastReportReason.WRONG_LOCATION)
        advanceUntilIdle()

        assertEquals(false, viewModel.state.value.feast?.isActive)
        assertEquals("Thanks for reporting. This feast has been taken down.", viewModel.state.value.message)
    }

    @Test
    fun tappingTwiceSendsOneReport() = runTest(dispatcher) {
        answer(HttpMethod.Put, "feasts/1/report", feast("1", "Gurudwara langar"))
        val viewModel = feastOpened()

        viewModel.report(FeastReportReason.WRONG_LOCATION)
        viewModel.report(FeastReportReason.WRONG_LOCATION)
        advanceUntilIdle()

        assertEquals(1, requestsTo(HttpMethod.Put, "feasts/1/report").size)
    }

    @Test
    fun theOrganizerCannotReportTheirOwnFeast() = runTest(dispatcher) {
        answer(HttpMethod.Get, "feasts/1", feast("1", "Gurudwara langar", organizerUid = "me-1"))
        val viewModel = feastOpened()

        assertTrue(viewModel.state.value.isOrganizer)
        assertFalse(viewModel.state.value.canReport)
        viewModel.report(FeastReportReason.WRONG_LOCATION)
        advanceUntilIdle()

        assertTrue(requestsTo(HttpMethod.Put, "feasts/1/report").isEmpty())
    }

    @Test
    fun anEndedOrTakenDownFeastCannotBeCalledOrReported() = runTest(dispatcher) {
        answer(HttpMethod.Get, "feasts/1", feast("1", "Gurudwara langar", phone = "9876543210", active = false))
        val takenDown = feastOpened().state.value
        assertFalse(takenDown.canReport)
        assertFalse(takenDown.canCall)

        answer(HttpMethod.Get, "feasts/1", feast("1", "Gurudwara langar", phone = "9876543210"))
        now = LocalDateTime(LocalDate(2026, 10, 5), LocalTime(9, 0))
        val ended = feastOpened().state.value
        assertEquals(FeastTiming.Ended, ended.timing)
        assertFalse(ended.canCall)

        now = LocalDateTime(LocalDate(2026, 10, 4), LocalTime(19, 0))
        assertTrue(feastOpened().state.value.canCall)
    }

    private companion object {
        fun feast(
            id: String,
            organizer: String?,
            start: String = "18:00:00",
            end: String = "22:00:00",
            active: Boolean = true,
            organizerUid: String = "someone",
            phone: String? = null,
            lat: Double = 18.58,
            lng: Double = 73.88,
        ) = """{"id":"$id","firebaseUid":"$organizerUid","menuItems":["Puri","Halwa"],"feastDate":"2026-10-04",""" +
            """"startTime":"$start","endTime":"$end","latitude":$lat,"longitude":$lng,"estimatedCapacity":200,""" +
            """"isActive":$active""" +
            (organizer?.let { ""","organizerName":"$it"""" } ?: "") +
            (phone?.let { ""","contactPhone":"$it"""" } ?: "") + "}"
    }

    @Test
    fun aFeastCanBeSavedToTheUsersPlacesAndUnsaved() = runTest(dispatcher) {
        val viewModel = feastOpened("1")
        assertFalse(viewModel.state.value.isSaved)

        viewModel.toggleSaved()
        assertTrue(viewModel.state.value.isSaved)
        assertTrue(settings.isFeastSaved("1"))
        assertEquals("Saved to your places", viewModel.state.value.message)

        // Opened again later, it's still saved
        assertTrue(feastOpened("1").state.value.isSaved)

        viewModel.toggleSaved()
        assertFalse(settings.isFeastSaved("1"))
    }    // ==================== Counting for the organizer ====================

    @Test
    fun openingAFeastCountsOneViewAndDirectionsAreCounted() = runTest(dispatcher) {
        answer(HttpMethod.Post, "activity", "", HttpStatusCode.NoContent)
        val viewModel = FeastDetailsViewModel(
            "1", FeastApi(client), settings, { signedInUid }, LocationService(phone, nowMillis = { 0 }), { now },
            activityApi = ActivityApi(client),
        )
        advanceUntilIdle()

        viewModel.onDirections()
        advanceUntilIdle()

        val sent = requests.filter { it.url.encodedPath == "/api/activity" }.map { (it.body as TextContent).text }
        assertEquals(
            listOf("""{"targetKind":"FEAST","targetId":1,"action":"VIEW"}""", """{"targetKind":"FEAST","targetId":1,"action":"DIRECTIONS"}"""),
            sent,
        )
    }


}
