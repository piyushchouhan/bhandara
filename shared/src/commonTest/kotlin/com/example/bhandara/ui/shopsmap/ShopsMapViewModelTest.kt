package com.example.bhandara.ui.shopsmap

import com.example.bhandara.data.location.DeviceLocation
import com.example.bhandara.data.location.GeoLocation
import com.example.bhandara.data.location.LocationService
import com.example.bhandara.data.location.TimedLocation
import com.example.bhandara.data.network.ApiClient
import com.example.bhandara.data.network.FeedApi
import com.example.bhandara.data.network.ShopApi
import com.example.bhandara.data.settings.AppSettings
import com.russhwolf.settings.MapSettings
import com.russhwolf.settings.Settings
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.MockEngineConfig
import io.ktor.client.engine.mock.respond
import io.ktor.client.request.HttpRequestData
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.content.OutgoingContent
import io.ktor.http.headersOf
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertSame
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * The shops map, the same on Android and iOS: find the user, load the shops and carts around them, show them as
 * pins (filtered by search, more as the user zooms in), and ask nearby users "Is this place real?" once.
 * The phone's location and the backend are fakes.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class ShopsMapViewModelTest {

    private val dispatcher = StandardTestDispatcher()

    @BeforeTest
    fun setUp() = Dispatchers.setMain(dispatcher)

    @AfterTest
    fun tearDown() = Dispatchers.resetMain()

    private val here = GeoLocation(18.5823, 73.8845)

    // ==================== Fakes ====================

    private class FakePhone(var permitted: Boolean = true, var location: GeoLocation? = null) : DeviceLocation {
        override fun hasPermission() = permitted
        override suspend fun current() = location
        override suspend fun lastKnown(): TimedLocation? = null
    }

    private val phone = FakePhone(location = here)
    private val requests = mutableListOf<HttpRequestData>()
    private var feedStatus = HttpStatusCode.OK
    private var feedBody = feed(shops = listOf(shop("1", "Sharma Chaat")))
    private var voteStatus = HttpStatusCode.OK
    private var voteBody = ""

    private val settings = AppSettings(object : Settings.Factory {
        private val stores = mutableMapOf<String?, Settings>()
        override fun create(name: String?): Settings = stores.getOrPut(name) { MapSettings() }
    })

    // The fake backend answers on the test's dispatcher, so advanceUntilIdle() also waits for its answers
    private val client = ApiClient("https://api.test/", { "id-token" }, MockEngine(MockEngineConfig().apply {
        dispatcher = this@ShopsMapViewModelTest.dispatcher
        addHandler { request ->
            requests += request
            val path = request.url.encodedPath
            val (status, body) = when {
                path == "/api/feed" -> feedStatus to feedBody
                path.endsWith("/verify") -> voteStatus to voteBody
                else -> HttpStatusCode.NotFound to ""
            }
            respond(body, status, headersOf(HttpHeaders.ContentType, "application/json"))
        }
    }))

    private fun viewModel() = ShopsMapViewModel(
        LocationService(phone, nowMillis = { 0 }), FeedApi(client), ShopApi(client), settings,
    )

    /** A view model that has located the user and loaded the map */
    private fun TestScope.started(): ShopsMapViewModel = viewModel().also {
        it.start()
        advanceUntilIdle()
    }

    private fun feedRequests() = requests.filter { it.url.encodedPath == "/api/feed" }

    // ==================== Locating the user and loading the map ====================

    @Test
    fun comingBackToTheMapShowsAShopAddedMeanwhileWithoutMovingTheMap() = runTest(dispatcher) {
        val viewModel = started()
        val camera = viewModel.state.value.camera

        // The user added a shop, then came back to the map
        feedBody = feed(shops = listOf(shop("1", "Sharma Chaat"), shop("30", "McDonald's")))
        viewModel.start()
        advanceUntilIdle()

        assertEquals(listOf("Sharma Chaat", "McDonald's"), viewModel.state.value.shops.map { it.shopName })
        assertSame(camera, viewModel.state.value.camera)
        assertEquals(2, feedRequests().size)
    }

    @Test
    fun theMapCentresOnTheUserAndShowsTheShopsAroundThem() = runTest(dispatcher) {
        feedBody = feed(shops = listOf(shop("1", "Sharma Chaat"), shop("2", "Gupta Snacks")), carts = listOf(cart("9", "Momo Cart")))

        val state = started().state.value

        assertEquals(MapLocationState.Found(here), state.location)
        assertEquals(here, state.camera.center)
        assertEquals(14f, state.camera.zoom)
        assertEquals(listOf("Sharma Chaat", "Gupta Snacks"), state.pins().map { it.label })
        assertEquals(listOf("📍", "📍"), state.pins().map { it.emoji })
        val url = feedRequests().single().url
        assertEquals("18.5823", url.parameters["lat"])
        assertEquals("73.8845", url.parameters["lon"])
    }

    @Test
    fun shopsAreSearchedWithinTheRadiusPickedInSettings() = runTest(dispatcher) {
        started()
        assertEquals("5000.0", feedRequests().single().url.parameters["radius"], "5 km unless the user picks another")

        settings.preferences.updateSearchRadius(500)
        started()

        assertEquals("500.0", feedRequests().last().url.parameters["radius"])
    }

    @Test
    fun locationAllowedInAnotherDialogStillFindsTheUser() = runTest(dispatcher) {
        // The app's own start-up request is showing, so the map's request answers "denied" at once
        phone.permitted = false
        val viewModel = started()
        viewModel.onLocationPermissionResult(false)
        advanceUntilIdle()
        assertEquals(MapLocationState.PermissionDenied, viewModel.state.value.location)

        // The user allows location in the other dialog; the app comes back to the front
        phone.permitted = true
        viewModel.onLocationAccessChanged(true)
        advanceUntilIdle()

        assertEquals(MapLocationState.Found(here), viewModel.state.value.location)
        assertEquals(1, feedRequests().size)
    }

    @Test
    fun comingBackToTheAppDoesNotLookForTheUserAgainOnceFound() = runTest(dispatcher) {
        val viewModel = started()

        viewModel.onLocationAccessChanged(true)
        advanceUntilIdle()

        assertEquals(1, feedRequests().size)
    }

    @Test
    fun locationStillOffChangesNothing() = runTest(dispatcher) {
        phone.permitted = false
        val viewModel = started()

        viewModel.onLocationAccessChanged(false)
        advanceUntilIdle()

        assertEquals(MapLocationState.PermissionNeeded, viewModel.state.value.location)
        assertTrue(feedRequests().isEmpty())
    }

    @Test
    fun withoutPermissionTheUserIsAskedAndNothingIsLoadedYet() = runTest(dispatcher) {
        phone.permitted = false
        val viewModel = started()

        assertEquals(MapLocationState.PermissionNeeded, viewModel.state.value.location)
        assertTrue(feedRequests().isEmpty())

        // The user allows it
        phone.permitted = true
        viewModel.onLocationPermissionResult(granted = true)
        advanceUntilIdle()

        assertEquals(MapLocationState.Found(here), viewModel.state.value.location)
        assertEquals(1, feedRequests().size)
    }

    @Test
    fun whenTheUserRefusesLocationTheMapSaysSo() = runTest(dispatcher) {
        phone.permitted = false
        val viewModel = started()

        viewModel.onLocationPermissionResult(granted = false)
        advanceUntilIdle()

        assertEquals(MapLocationState.PermissionDenied, viewModel.state.value.location)
        assertTrue(feedRequests().isEmpty())
    }

    @Test
    fun whenThePhoneCannotTellWhereItIsTheMapDoesNotPretendToBeSomewhereElse() = runTest(dispatcher) {
        // Before, the map opened on Delhi with no shops when the phone had no last location
        phone.location = null
        val viewModel = started()

        assertEquals(MapLocationState.Unavailable, viewModel.state.value.location)
        assertTrue(feedRequests().isEmpty())
        assertTrue(viewModel.state.value.camera.zoom < 6f, "zoomed out over the country, not on a city")

        // Location comes back, the user retries
        phone.location = here
        viewModel.retry()
        advanceUntilIdle()

        assertEquals(MapLocationState.Found(here), viewModel.state.value.location)
        assertEquals(1, feedRequests().size)
    }

    @Test
    fun whenShopsCannotBeLoadedTheUserSeesWhyAndCanRetry() = runTest(dispatcher) {
        feedStatus = HttpStatusCode.ServiceUnavailable
        feedBody = """{"message": "Service is down"}"""
        val viewModel = started()

        assertEquals("Service is down", viewModel.state.value.error)
        assertEquals(MapLocationState.Found(here), viewModel.state.value.location)

        feedStatus = HttpStatusCode.OK
        feedBody = feed(shops = listOf(shop("1", "Sharma Chaat")))
        viewModel.retry()
        advanceUntilIdle()

        assertNull(viewModel.state.value.error)
        assertEquals(listOf("Sharma Chaat"), viewModel.state.value.pins().map { it.label })
    }

    @Test
    fun recentringMovesTheMapBackToTheUser() = runTest(dispatcher) {
        val viewModel = started()
        val first = viewModel.state.value.camera

        val moved = GeoLocation(18.5900, 73.8900)
        phone.location = moved
        viewModel.recenter()
        advanceUntilIdle()

        assertEquals(moved, viewModel.state.value.camera.center)
        assertNotEquals(first.id, viewModel.state.value.camera.id)
    }

    // ==================== What the map shows ====================

    @Test
    fun searchMatchesNameCuisineTypeOrMenuIgnoringCase() = runTest(dispatcher) {
        feedBody = feed(shops = listOf(
            shop("1", "Sharma Chaat", cuisine = "Street Food", menu = listOf("Pani Puri")),
            shop("2", "Gupta Sweets", cuisine = "Mithai", menu = listOf("Jalebi")),
            shop("3", "Momo Point", type = "Fast Food", menu = listOf("Veg Momos")),
        ))
        val viewModel = started()
        viewModel.onZoomChanged(16f)

        fun search(query: String): List<String> {
            viewModel.onSearchQueryChange(query)
            return viewModel.state.value.pins().map { it.label }
        }

        assertEquals(listOf("Sharma Chaat"), search("sharma"))
        assertEquals(listOf("Gupta Sweets"), search("MITHAI"))
        assertEquals(listOf("Momo Point"), search("fast food"))
        assertEquals(listOf("Sharma Chaat"), search("pani puri"))
        assertEquals(3, search("").size)
    }

    @Test
    fun movingVendorModeShowsOnlyTheLiveCartsInGreen() = runTest(dispatcher) {
        feedBody = feed(shops = listOf(shop("1", "Sharma Chaat")), carts = listOf(cart("9", "Momo Cart")))
        val viewModel = started()

        viewModel.onShowMovingVendorsChange(true)

        val pins = viewModel.state.value.pins()
        assertEquals(listOf("Momo Cart"), pins.map { it.label })
        assertTrue(pins.single().isLiveCart)
        assertEquals("🚚", pins.single().emoji)
    }

    @Test
    fun moreShopsAppearAsTheUserZoomsIn() = runTest(dispatcher) {
        // The backend lists shops best first; zoomed out, only the best few are shown so the map stays readable
        feedBody = feed(shops = (1..12).map { shop("$it", "Shop $it") })
        val viewModel = started()

        assertEquals((1..5).map { "Shop $it" }, viewModel.state.value.pins().map { it.label })
        viewModel.onZoomChanged(15f)
        assertEquals(10, viewModel.state.value.pins().size)
        viewModel.onZoomChanged(16f)
        assertEquals(12, viewModel.state.value.pins().size)
    }

    @Test
    fun tappingAPinShowsThatPlaceAndSwitchingModesClosesIt() = runTest(dispatcher) {
        feedBody = feed(shops = listOf(shop("1", "Sharma Chaat")), carts = listOf(cart("9", "Momo Cart")))
        val viewModel = started()

        viewModel.onPinClick(viewModel.state.value.pins().single())
        assertEquals("Sharma Chaat", viewModel.state.value.selectedPlace()?.shopName)

        viewModel.onShowMovingVendorsChange(true)
        assertNull(viewModel.state.value.selectedPlace(), "the shop isn't on the map any more")

        viewModel.onPinClick(viewModel.state.value.pins().single())
        assertEquals("Momo Cart", viewModel.state.value.selectedPlace()?.shopName)
        viewModel.clearSelection()
        assertNull(viewModel.state.value.selectedPlace())
    }

    @Test
    fun theTappedPinStandsOutAndTheOthersFadeUntilItIsClosed() = runTest(dispatcher) {
        feedBody = feed(shops = listOf(shop("1", "Sharma Chaat"), shop("2", "Gupta Sweets")))
        val viewModel = started()

        viewModel.onPinClick(viewModel.state.value.pins().last())

        val pins = viewModel.state.value.pins()
        assertEquals(listOf(false, true), pins.map { it.selected })
        assertEquals(listOf(true, false), pins.map { it.dimmed })
        viewModel.clearSelection()
        assertTrue(viewModel.state.value.pins().none { it.selected || it.dimmed })
    }

    @Test
    fun aSelectedShopThatASearchHidesIsNoLongerShown() = runTest(dispatcher) {
        feedBody = feed(shops = listOf(shop("1", "Sharma Chaat"), shop("2", "Gupta Sweets")))
        val viewModel = started()
        viewModel.onPinClick(viewModel.state.value.pins().first())

        viewModel.onSearchQueryChange("gupta")

        assertNull(viewModel.state.value.selectedPlace())
    }

    // ==================== "Is this place real?" ====================

    @Test
    fun nearbyUsersAreAskedAboutAProvisionalShopOnce() = runTest(dispatcher) {
        feedBody = feed(shops = listOf(shop("1", "Sharma Chaat")), prompt = shop("5", "New Chaat Stall"))

        assertEquals("New Chaat Stall", started().state.value.verificationPrompt?.shopName)

        // Already answered on this phone: not asked again
        settings.markAnsweredVerification("5")
        assertNull(started().state.value.verificationPrompt)
    }

    @Test
    fun anAnswerIsSentWithWhereTheUserIsAndThanked() = runTest(dispatcher) {
        feedBody = feed(shops = emptyList(), prompt = shop("5", "New Chaat Stall"))
        val viewModel = started()

        viewModel.answerVerification("YES")
        advanceUntilIdle()

        val vote = requests.last()
        assertEquals("/api/localshops/5/verify", vote.url.encodedPath)
        val body = (vote.body as OutgoingContent.ByteArrayContent).bytes().decodeToString()
        assertEquals("""{"vote":"YES","latitude":18.5823,"longitude":73.8845}""", body)
        assertNull(viewModel.state.value.verificationPrompt)
        assertEquals("Thanks for verifying!", viewModel.state.value.message)
        assertTrue(settings.hasAnsweredVerification("5"))
    }

    @Test
    fun whenTheBackendSaysTheUserShouldNotBeAskedAgainTheyAreNot() = runTest(dispatcher) {
        // e.g. already voted on another phone, or it's their own shop
        feedBody = feed(shops = emptyList(), prompt = shop("5", "New Chaat Stall"))
        voteStatus = HttpStatusCode.Conflict
        voteBody = """{"message": "User has already voted for this shop."}"""
        val viewModel = started()

        viewModel.answerVerification("NO")
        advanceUntilIdle()

        assertNull(viewModel.state.value.verificationPrompt)
        assertEquals("User has already voted for this shop.", viewModel.state.value.message)
        assertTrue(settings.hasAnsweredVerification("5"))
    }

    @Test
    fun anAnswerFromTooFarAwayCanBeGivenAgainLater() = runTest(dispatcher) {
        feedBody = feed(shops = emptyList(), prompt = shop("5", "New Chaat Stall"))
        voteStatus = HttpStatusCode.BadRequest
        voteBody = """{"message": "You need to be near the shop to vote"}"""
        val viewModel = started()

        viewModel.answerVerification("YES")
        advanceUntilIdle()

        assertNull(viewModel.state.value.verificationPrompt)
        assertEquals("You need to be near the shop to vote", viewModel.state.value.message)
        assertFalse(settings.hasAnsweredVerification("5"))
    }

    @Test
    fun aMessageIsShownOnce() = runTest(dispatcher) {
        feedBody = feed(shops = emptyList(), prompt = shop("5", "New Chaat Stall"))
        val viewModel = started()
        viewModel.answerVerification("YES")
        advanceUntilIdle()

        viewModel.onMessageShown()

        assertNull(viewModel.state.value.message)
    }

    // ==================== Shop zones ====================

    /** Six chaat stalls standing together (zone z1, "Chaat Gali") and one shop on its own */
    private fun givenAChaatZone(name: String? = "Chaat Gali") {
        val stalls = (1..6).map { shop("$it", "Stall $it") }
        feedBody = feed(
            shops = stalls + shop("7", "Lonely Chai"),
            zones = listOf(zone("z1", shopIds = (1..6).map { "$it" }, name = name)),
        )
    }

    @Test
    fun zoomedOutAZonesShopsAreReplacedByOneLabelAndItsShape() = runTest(dispatcher) {
        givenAChaatZone()
        val viewModel = started()

        val state = viewModel.state.value
        val zonePin = state.pins().single { it.zoneCount != null }
        assertEquals(6, zonePin.zoneCount)
        assertEquals("Chaat Gali", zonePin.label)
        assertEquals("🍢", zonePin.emoji)
        assertEquals(GeoLocation(18.5801, 73.8802), zonePin.position)
        // The zone's own shops aren't drawn; the shop outside it is
        assertEquals(listOf("7"), state.pins().filter { it.zoneCount == null }.map { it.id })

        val shape = state.mapZones().single()
        assertEquals("z1", shape.id)
        assertEquals(listOf(GeoLocation(18.58, 73.88), GeoLocation(18.5802, 73.88), GeoLocation(18.5802, 73.8804)), shape.outline)
        assertFalse(shape.faint)
    }

    @Test
    fun zoomedInEveryShopHasItsPinAndTheShapeIsFaint() = runTest(dispatcher) {
        givenAChaatZone()
        val viewModel = started()

        // Still a flag at 16, where shops metres apart would sit on top of each other
        viewModel.onZoomChanged(16f)
        assertEquals(1, viewModel.state.value.pins().count { it.zoneCount != null })
        assertFalse(viewModel.state.value.mapZones().single().faint)

        viewModel.onZoomChanged(17f)

        val state = viewModel.state.value
        assertEquals((1..7).map { "$it" }, state.pins().map { it.id })
        assertTrue(state.pins().none { it.zoneCount != null })
        assertTrue(state.mapZones().single().faint)
    }

    @Test
    fun aTappedShopInsideAZoneKeepsItsPinWhenZoomingOut() = runTest(dispatcher) {
        givenAChaatZone()
        val viewModel = started()
        viewModel.onZoomChanged(17f)
        viewModel.onPinClick(viewModel.state.value.pins().first { it.id == "3" })

        viewModel.onZoomChanged(14f)

        val state = viewModel.state.value
        assertTrue(state.pins().single { it.id == "3" }.selected)
        assertEquals("Stall 3", state.selectedPlace()?.shopName)
        assertEquals(1, state.pins().count { it.zoneCount != null })
    }

    @Test
    fun tappingAZoneOrItsLabelZoomsInOnIt() = runTest(dispatcher) {
        givenAChaatZone()
        val viewModel = started()

        viewModel.onZoneClick("z1")

        val camera = viewModel.state.value.camera
        assertEquals(GeoLocation(18.5801, 73.8802), camera.center)
        assertEquals(18f, camera.zoom)
        assertNull(viewModel.state.value.selectedId)

        // The label does the same, and opens no shop card
        val before = viewModel.state.value.camera.id
        viewModel.onPinClick(viewModel.state.value.pins().single { it.zoneCount != null })
        assertNotEquals(before, viewModel.state.value.camera.id)
        assertEquals(18f, viewModel.state.value.camera.zoom)
        assertNull(viewModel.state.value.selectedId)
    }

    @Test
    fun searchingOrShowingMovingVendorsShowsPlacesOneByOneWithoutZones() = runTest(dispatcher) {
        givenAChaatZone()
        val viewModel = started()

        viewModel.onSearchQueryChange("Stall")
        assertTrue(viewModel.state.value.mapZones().isEmpty())
        assertTrue(viewModel.state.value.pins().none { it.zoneCount != null })
        assertEquals(5, viewModel.state.value.pins().size) // the usual zoomed-out limit

        viewModel.onSearchQueryChange("")
        viewModel.onShowMovingVendorsChange(true)
        assertTrue(viewModel.state.value.mapZones().isEmpty())
        assertTrue(viewModel.state.value.pins().none { it.zoneCount != null })
    }

    @Test
    fun aZoneIsNamedInTheAppsLanguageAndAnUnnamedOneShowsOnlyItsCount() = runTest(dispatcher) {
        givenAChaatZone()
        val viewModel = started()

        assertEquals("चाट गली", viewModel.state.value.pins("hi").single { it.zoneCount != null }.label)
        assertEquals("चाट गल्ली", viewModel.state.value.pins("mr").single { it.zoneCount != null }.label)

        givenAChaatZone(name = null)
        val unnamed = started()
        val pin = unnamed.state.value.pins("hi").single { it.zoneCount != null }
        assertEquals("", pin.label)
        assertEquals("🏪", pin.emoji)
    }

    @Test
    fun aFeedWithoutZonesStillLoads() = runTest(dispatcher) {
        val viewModel = started()

        assertEquals(listOf("Sharma Chaat"), viewModel.state.value.shops.map { it.shopName })
        assertTrue(viewModel.state.value.zones.isEmpty())
        assertTrue(viewModel.state.value.mapZones().isEmpty())
    }

    // ==================== Test data ====================

    private companion object {
        fun shop(id: String, name: String, cuisine: String? = null, type: String? = null, menu: List<String> = emptyList()) =
            """{"id":"$id","shopName":"$name","latitude":18.58,"longitude":73.88,"isMovingCart":false""" +
                (cuisine?.let { ""","cuisineType":"$it"""" } ?: "") +
                (type?.let { ""","shopType":"$it"""" } ?: "") +
                ""","menuItems":[${menu.joinToString(",") { "\"$it\"" }}]}"""

        fun cart(id: String, name: String) =
            """{"id":"$id","shopName":"$name","latitude":18.59,"longitude":73.89,"isMovingCart":true}"""

        fun feed(shops: List<String>, carts: List<String> = emptyList(), prompt: String? = null, zones: List<String>? = null) =
            """{"activeShops":[${(shops + carts).joinToString(",")}],"verificationPrompt":${prompt ?: "null"}""" +
                (zones?.let { ""","zones":[${it.joinToString(",")}]""" } ?: "") + "}"

        /** A street-food zone centred at 18.5801, 73.8802 with a three-corner outline */
        fun zone(id: String, shopIds: List<String>, name: String?) =
            """{"id":"$id","shopIds":[${shopIds.joinToString(",") { "\"$it\"" }}],"shopCount":${shopIds.size},""" +
                """"centerLatitude":18.5801,"centerLongitude":73.8802,""" +
                """"outline":[[18.58,73.88],[18.5802,73.88],[18.5802,73.8804]],""" +
                (name?.let { """"name":"$it","nameHi":"चाट गली","nameMr":"चाट गल्ली","type":"STREET_FOOD"""" } ?: """"name":null""") + "}"
    }
}
