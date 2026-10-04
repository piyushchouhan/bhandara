package com.example.bhandara.ui.shopdetails

import com.example.bhandara.data.location.DeviceLocation
import com.example.bhandara.data.location.GeoLocation
import com.example.bhandara.data.location.LocationService
import com.example.bhandara.data.location.TimedLocation
import com.example.bhandara.data.models.api.ShopReportReason
import com.example.bhandara.data.network.ApiClient
import com.example.bhandara.data.network.ShopApi
import com.example.bhandara.data.settings.AppSettings
import com.russhwolf.settings.MapSettings
import com.russhwolf.settings.Settings
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.MockEngineConfig
import io.ktor.client.engine.mock.respond
import io.ktor.client.request.HttpRequestData
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
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
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * One shop's page, the same on Android and iOS: the shop with its menu and how far away it is, and what people can
 * do with it — report it, suggest deleting it (once each), or, for its owner, take it off the map.
 * The backend and the phone's location are fakes.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class ShopDetailsViewModelTest {

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

    private val phone = FakePhone(permitted = false)
    private var signedInUid: String? = "customer-1"
    private val requests = mutableListOf<HttpRequestData>()
    private val responses = mutableMapOf<String, Pair<HttpStatusCode, String>>()

    private val settings = AppSettings(object : Settings.Factory {
        private val stores = mutableMapOf<String?, Settings>()
        override fun create(name: String?): Settings = stores.getOrPut(name) { MapSettings() }
    })

    // The fake backend answers on the test's dispatcher, so advanceUntilIdle() also waits for its answers
    private val client = ApiClient("https://api.test/", { "id-token" }, MockEngine(MockEngineConfig().apply {
        dispatcher = this@ShopDetailsViewModelTest.dispatcher
        addHandler { request ->
            requests += request
            val (status, body) = responses[request.url.encodedPath] ?: (HttpStatusCode.NotFound to "")
            respond(body, status, headersOf(HttpHeaders.ContentType, "application/json"))
        }
    }))

    private fun answer(path: String, body: String, status: HttpStatusCode = HttpStatusCode.OK) {
        responses["/api/$path"] = status to body
    }

    init {
        answer("localshops/5", shop())
        answer("menu-items/shop/5", "[]")
    }

    private fun TestScope.opened(): ShopDetailsViewModel =
        ShopDetailsViewModel("5", ShopApi(client), settings, { signedInUid }, LocationService(phone, nowMillis = { 0 }))
            .also { advanceUntilIdle() }

    private fun requestsTo(path: String) = requests.filter { it.url.encodedPath == "/api/$path" }

    // ==================== Loading the shop ====================

    @Test
    fun theShopAndItsPricedMenuAreShown() = runTest(dispatcher) {
        answer("localshops/5", shop(name = "Sharma Chaat", crowd = "BUSY"))
        answer("menu-items/shop/5", """[${menuItem("Pani Puri", 30.0)}, ${menuItem("Samosa", 15.5)}]""")

        val state = opened().state.value

        assertFalse(state.loading)
        assertNull(state.error)
        assertEquals("Sharma Chaat", state.shop?.shopName)
        assertEquals("BUSY", state.shop?.crowdLevel)
        assertEquals(listOf("Pani Puri", "Samosa"), state.menu.map { it.name })
    }

    @Test
    fun itIsLoadingUntilTheBackendAnswers() = runTest(dispatcher) {
        val viewModel = ShopDetailsViewModel("5", ShopApi(client), settings, { signedInUid }, null)

        assertTrue(viewModel.state.value.loading)
        advanceUntilIdle()
        assertFalse(viewModel.state.value.loading)
    }

    @Test
    fun aShopThatNoLongerExistsSaysSoAndCanBeRetried() = runTest(dispatcher) {
        answer("localshops/5", "", HttpStatusCode.NotFound)
        val viewModel = opened()

        assertNull(viewModel.state.value.shop)
        assertEquals("This is no longer available. It may have been removed.", viewModel.state.value.error)

        answer("localshops/5", shop())
        viewModel.retry()
        advanceUntilIdle()

        assertNull(viewModel.state.value.error)
        assertNotNull(viewModel.state.value.shop)
    }

    @Test
    fun withoutAConnectionTheUserIsToldAndCanRetry() = runTest(dispatcher) {
        responses.clear()
        val failing = ApiClient("https://api.test/", { null }, MockEngine(MockEngineConfig().apply {
            dispatcher = this@ShopDetailsViewModelTest.dispatcher
            addHandler { throw RuntimeException("Unable to resolve host api.test") }
        }))
        val viewModel = ShopDetailsViewModel("5", ShopApi(failing), settings, { signedInUid }, null)
        advanceUntilIdle()

        assertNull(viewModel.state.value.shop)
        assertNotNull(viewModel.state.value.error)
        assertFalse(viewModel.state.value.loading)
    }

    @Test
    fun ifOnlyTheMenuFailsTheShopIsStillShown() = runTest(dispatcher) {
        answer("menu-items/shop/5", "", HttpStatusCode.InternalServerError)

        val state = opened().state.value

        assertNotNull(state.shop)
        assertNull(state.error)
        assertTrue(state.menu.isEmpty())
    }

    @Test
    fun theDistanceIsMeasuredFromWhereTheUserIs() = runTest(dispatcher) {
        // The backend doesn't know where the user is when it sends one shop, so the app works it out
        answer("localshops/5", shop(lat = 18.5823, lng = 73.8845))
        phone.permitted = true
        phone.location = GeoLocation(18.5823, 73.8945) // about 1 km east

        val distance = opened().state.value.distanceMeters

        assertNotNull(distance)
        assertTrue(distance in 1000.0..1100.0, "was $distance")
    }

    @Test
    fun withoutLocationPermissionNoDistanceIsShown() = runTest(dispatcher) {
        phone.permitted = false

        assertNull(opened().state.value.distanceMeters)
    }

    // ==================== Who can do what ====================

    @Test
    fun aCustomerCanReportSuggestDeletingAndClaimAnUnclaimedShop() = runTest(dispatcher) {
        answer("localshops/5", shop(ownerUid = "platform", claimed = false))

        val state = opened().state.value

        assertFalse(state.isOwner)
        assertTrue(state.canReport)
        assertTrue(state.canSuggestDelete)
        assertTrue(state.canClaim)
        assertFalse(state.canDeactivate)
        assertFalse(state.canEditMenu)
    }

    @Test
    fun theOwnerManagesTheirShopInsteadOfFlaggingIt() = runTest(dispatcher) {
        signedInUid = "owner-1"
        answer("localshops/5", shop(ownerUid = "owner-1", claimed = true))

        val state = opened().state.value

        assertTrue(state.isOwner)
        assertTrue(state.canDeactivate)
        assertTrue(state.canEditMenu)
        assertFalse(state.canReport)
        assertFalse(state.canSuggestDelete)
        assertFalse(state.canClaim)
    }

    @Test
    fun noOneIsTheOwnerWhileSignedOut() = runTest(dispatcher) {
        // A shop without an owner must not look owned by a user who isn't signed in yet
        signedInUid = null
        answer("localshops/5", shop(ownerUid = null))

        assertFalse(opened().state.value.isOwner)
    }

    @Test
    fun anInactiveShopCannotBeCalledClaimedEditedOrDeactivatedAgain() = runTest(dispatcher) {
        signedInUid = "owner-1"
        answer("localshops/5", shop(ownerUid = "owner-1", active = false, phone = "9876543210"))

        val state = opened().state.value

        assertFalse(state.isActive)
        assertFalse(state.canDeactivate)
        assertFalse(state.canEditMenu)
        assertFalse(state.canCall)
    }

    @Test
    fun theShopCanBeCalledOnlyWhenItHasAPhoneNumber() = runTest(dispatcher) {
        answer("localshops/5", shop(phone = "9876543210"))
        assertTrue(opened().state.value.canCall)

        answer("localshops/5", shop(phone = " "))
        assertFalse(opened().state.value.canCall)
    }

    // ==================== Reporting and suggesting deletion ====================

    @Test
    fun reportingSendsTheReasonAndIsRememberedForThisShop() = runTest(dispatcher) {
        answer("localshops/5/report", shop())
        val viewModel = opened()

        viewModel.report(ShopReportReason.WRONG_LOCATION)
        advanceUntilIdle()

        val request = requestsTo("localshops/5/report").single()
        assertEquals(HttpMethod.Put, request.method)
        assertEquals("""{"reason":"WRONG_LOCATION"}""", request.bodyText())
        assertTrue(viewModel.state.value.hasReported)
        assertFalse(viewModel.state.value.canReport)
        assertEquals("Thanks for reporting. A moderator will take a look.", viewModel.state.value.message)
        assertTrue(settings.hasReportedShop("5"))
        // Opening the shop again still shows it as reported
        assertTrue(opened().state.value.hasReported)
    }

    @Test
    fun reportingKeepsWhatThePageAlreadyShows() = runTest(dispatcher) {
        // The backend's answer to a report has no crowd level; the badge must not disappear
        answer("localshops/5", shop(crowd = "BUSY"))
        answer("localshops/5/report", shop(crowd = null))
        val viewModel = opened()

        viewModel.report(ShopReportReason.INAPPROPRIATE_CONTENT)
        advanceUntilIdle()

        assertEquals("BUSY", viewModel.state.value.shop?.crowdLevel)
    }

    @Test
    fun aFailedReportIsExplainedAndCanBeTriedAgain() = runTest(dispatcher) {
        answer("localshops/5/report", "", HttpStatusCode.TooManyRequests)
        val viewModel = opened()

        viewModel.report(ShopReportReason.NOT_FOOD_SHOP)
        advanceUntilIdle()

        assertEquals("You're doing that too often. Please wait a while and try again.", viewModel.state.value.message)
        assertFalse(viewModel.state.value.hasReported)
        assertFalse(settings.hasReportedShop("5"))
        assertTrue(viewModel.state.value.canReport)
    }

    @Test
    fun tappingTwiceSendsOneReport() = runTest(dispatcher) {
        answer("localshops/5/report", shop())
        val viewModel = opened()

        viewModel.report(ShopReportReason.NOT_FOOD_SHOP)
        viewModel.report(ShopReportReason.NOT_FOOD_SHOP)
        advanceUntilIdle()

        assertEquals(1, requestsTo("localshops/5/report").size)
    }

    @Test
    fun aShopAlreadyReportedIsNotReportedAgain() = runTest(dispatcher) {
        settings.markReportedShop("5")
        val viewModel = opened()

        viewModel.report(ShopReportReason.NOT_FOOD_SHOP)
        advanceUntilIdle()

        assertTrue(requestsTo("localshops/5/report").isEmpty())
    }

    @Test
    fun suggestingDeletionIsRememberedForThisShop() = runTest(dispatcher) {
        answer("localshops/5/suggest-delete", shop())
        val viewModel = opened()

        viewModel.suggestDelete()
        advanceUntilIdle()

        assertEquals(1, requestsTo("localshops/5/suggest-delete").size)
        assertTrue(viewModel.state.value.hasSuggestedDelete)
        assertFalse(viewModel.state.value.canSuggestDelete)
        assertEquals("Thanks. A moderator will review this shop.", viewModel.state.value.message)
        assertTrue(settings.hasSuggestedDeletingShop("5"))

        viewModel.suggestDelete()
        advanceUntilIdle()
        assertEquals(1, requestsTo("localshops/5/suggest-delete").size)
    }

    @Test
    fun aFailedDeletionSuggestionIsExplained() = runTest(dispatcher) {
        answer("localshops/5/suggest-delete", "", HttpStatusCode.ServiceUnavailable)
        val viewModel = opened()

        viewModel.suggestDelete()
        advanceUntilIdle()

        assertNotNull(viewModel.state.value.message)
        assertFalse(viewModel.state.value.hasSuggestedDelete)
        assertFalse(settings.hasSuggestedDeletingShop("5"))
    }

    @Test
    fun theOwnerCannotFlagTheirOwnShop() = runTest(dispatcher) {
        signedInUid = "owner-1"
        answer("localshops/5", shop(ownerUid = "owner-1"))
        val viewModel = opened()

        viewModel.report(ShopReportReason.NOT_FOOD_SHOP)
        viewModel.suggestDelete()
        advanceUntilIdle()

        assertTrue(requestsTo("localshops/5/report").isEmpty())
        assertTrue(requestsTo("localshops/5/suggest-delete").isEmpty())
    }

    // ==================== The owner taking the shop off the map ====================

    @Test
    fun theOwnerCanDeactivateTheirShop() = runTest(dispatcher) {
        signedInUid = "owner-1"
        answer("localshops/5", shop(ownerUid = "owner-1"))
        answer("localshops/5/deactivate", shop(ownerUid = "owner-1", active = false))
        val viewModel = opened()

        viewModel.deactivate()
        advanceUntilIdle()

        assertEquals(HttpMethod.Put, requestsTo("localshops/5/deactivate").single().method)
        assertFalse(viewModel.state.value.isActive)
        assertFalse(viewModel.state.value.canDeactivate)
        assertEquals("Shop deactivated. It's no longer shown to others.", viewModel.state.value.message)
    }

    @Test
    fun aRefusedDeactivationLeavesTheShopActive() = runTest(dispatcher) {
        signedInUid = "owner-1"
        answer("localshops/5", shop(ownerUid = "owner-1"))
        answer("localshops/5/deactivate", """{"message":"You can only change your own shop"}""", HttpStatusCode.Forbidden)
        val viewModel = opened()

        viewModel.deactivate()
        advanceUntilIdle()

        assertTrue(viewModel.state.value.isActive)
        assertEquals("You can only change your own shop", viewModel.state.value.message)
    }

    @Test
    fun onlyTheOwnerCanDeactivate() = runTest(dispatcher) {
        val viewModel = opened()

        viewModel.deactivate()
        advanceUntilIdle()

        assertTrue(requestsTo("localshops/5/deactivate").isEmpty())
    }

    // ==================== Other ====================

    @Test
    fun theMenuIsReloadedAfterTheOwnerAddsItems() = runTest(dispatcher) {
        val viewModel = opened()
        answer("menu-items/shop/5", "[${menuItem("Vada Pav", 20.0)}]")

        viewModel.refreshMenu()
        advanceUntilIdle()

        assertEquals(listOf("Vada Pav"), viewModel.state.value.menu.map { it.name })
    }

    @Test
    fun aMessageIsShownOnce() = runTest(dispatcher) {
        answer("localshops/5/report", shop())
        val viewModel = opened()
        viewModel.report(ShopReportReason.NOT_FOOD_SHOP)
        advanceUntilIdle()

        viewModel.onMessageShown()

        assertNull(viewModel.state.value.message)
    }

    private companion object {
        fun shop(
            name: String = "Sharma Chaat",
            ownerUid: String? = "someone-else",
            claimed: Boolean? = true,
            active: Boolean = true,
            phone: String? = null,
            crowd: String? = null,
            lat: Double = 18.58,
            lng: Double = 73.88,
        ) = """{"id":"5","shopName":"$name","latitude":$lat,"longitude":$lng,"isActive":$active""" +
            (ownerUid?.let { ""","ownerUid":"$it"""" } ?: "") +
            (claimed?.let { ""","ownerClaimed":$it""" } ?: "") +
            (phone?.let { ""","ownerPhone":"$it"""" } ?: "") +
            (crowd?.let { ""","crowdLevel":"$it"""" } ?: "") + "}"

        fun menuItem(name: String, price: Double) =
            """{"id":"$name","shopId":"5","name":"$name","foodType":"VEG","price":$price,"isAvailable":true}"""

        fun HttpRequestData.bodyText(): String = when (val content = body) {
            is OutgoingContent.ByteArrayContent -> content.bytes().decodeToString()
            else -> ""
        }
    }
}
