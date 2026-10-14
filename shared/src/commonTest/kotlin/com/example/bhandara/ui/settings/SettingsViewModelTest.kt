package com.example.bhandara.ui.settings

import com.example.bhandara.data.network.ApiClient
import com.example.bhandara.data.network.UserApi
import com.example.bhandara.data.settings.AppPreferences
import com.example.bhandara.data.settings.NotificationKind
import com.example.bhandara.navigation.Route
import com.example.bhandara.ui.privacy.PrivacyPage
import com.example.bhandara.ui.privacy.privacyStart
import com.russhwolf.settings.MapSettings
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.MockEngineConfig
import io.ktor.client.engine.mock.respond
import io.ktor.client.request.HttpRequestData
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.http.content.TextContent
import io.ktor.http.headersOf
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Settings, the same on Android and iOS: pages, and "New feasts near me", which the backend also keeps. The
 * backend is a fake.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class SettingsViewModelTest {

    private val dispatcher = StandardTestDispatcher()

    @BeforeTest
    fun setUp() = Dispatchers.setMain(dispatcher)

    @AfterTest
    fun tearDown() = Dispatchers.resetMain()

    // ==================== Fakes ====================

    private val requests = mutableListOf<HttpRequestData>()
    private var status = HttpStatusCode.NoContent
    /** The account's notification settings, as GET answers them */
    private var accountSettings = """{"nearbyFeasts":true,"reportUpdates":true,"shopReviews":true,"claimStatus":true,"quietHours":false}"""

    private val client = ApiClient("https://api.test/", { "id-token" }, MockEngine(MockEngineConfig().apply {
        dispatcher = this@SettingsViewModelTest.dispatcher
        addHandler { request ->
            requests += request
            if (request.method == HttpMethod.Get) {
                respond(accountSettings, HttpStatusCode.OK, headersOf(HttpHeaders.ContentType, "application/json"))
            } else {
                respond(if (status == HttpStatusCode.NoContent) "" else """{"message":"No"}""", status, headersOf(HttpHeaders.ContentType, "application/json"))
            }
        }
    }))

    private val preferences = AppPreferences(MapSettings())
    private val viewModel = SettingsViewModel(preferences, UserApi(client))

    private fun HttpRequestData.text() = (body as TextContent).text

    // ==================== Tests ====================

    @Test
    fun turningOffFeastAlertsTellsTheBackend() = runTest(dispatcher) {
        viewModel.setNotifyNearbyFeasts(false)
        assertFalse(preferences.notifyNearbyFeasts, "the switch moves at once")
        advanceUntilIdle()

        val put = requests.single()
        assertEquals(HttpMethod.Put, put.method)
        assertEquals("/api/users/notifications", put.url.encodedPath)
        assertEquals("""{"nearbyFeasts":false}""", put.text())
        assertFalse(preferences.notifyNearbyFeasts)
        assertFalse(viewModel.state.value.notificationsFailed)
    }

    @Test
    fun whenTheBackendCannotBeToldTheSwitchGoesBackAndTheUserIsTold() = runTest(dispatcher) {
        status = HttpStatusCode.NotFound

        viewModel.setNotifyNearbyFeasts(false)
        advanceUntilIdle()

        assertTrue(preferences.notifyNearbyFeasts)
        assertTrue(viewModel.state.value.notificationsFailed)
        viewModel.onMessageShown()
        assertFalse(viewModel.state.value.notificationsFailed)
    }

    @Test
    fun tapsWhileAChangeIsOnItsWayAreIgnored() = runTest(dispatcher) {
        viewModel.setNotifyNearbyFeasts(false)
        viewModel.setNotifyNearbyFeasts(true)
        viewModel.setNotifyNearbyFeasts(false)
        advanceUntilIdle()

        assertEquals(1, requests.size)
        assertFalse(preferences.notifyNearbyFeasts)

        // Asking for what's already set sends nothing
        viewModel.setNotifyNearbyFeasts(false)
        advanceUntilIdle()
        assertEquals(1, requests.size)
    }

    @Test
    fun eachNotificationSettingIsSentOnItsOwnSoTheOthersStay() = runTest(dispatcher) {
        viewModel.setNotification(NotificationKind.SHOP_REVIEWS, false)
        viewModel.setNotification(NotificationKind.QUIET_HOURS, true)
        advanceUntilIdle()

        assertEquals(listOf("""{"shopReviews":false}""", """{"quietHours":true}"""), requests.map { it.text() })
        assertFalse(preferences.notifyShopReviews)
        assertTrue(preferences.quietHours)
        assertTrue(preferences.notifyReportUpdates)
    }

    @Test
    fun whenOneSettingFailsOnlyThatOneGoesBack() = runTest(dispatcher) {
        status = HttpStatusCode.InternalServerError

        viewModel.setNotification(NotificationKind.CLAIM_STATUS, false)
        advanceUntilIdle()

        assertTrue(preferences.notifyClaimStatus)
        assertTrue(viewModel.state.value.notificationsFailed)
        assertTrue(viewModel.state.value.saving.isEmpty())
    }

    @Test
    fun openingNotificationsTakesTheAccountsSettingsFromAnotherPhone() = runTest(dispatcher) {
        accountSettings = """{"nearbyFeasts":false,"reportUpdates":true,"shopReviews":false,"claimStatus":true,"quietHours":true}"""

        viewModel.open(SettingsPage.NOTIFICATIONS)
        advanceUntilIdle()

        assertEquals("/api/users/notifications", requests.single().url.encodedPath)
        assertFalse(preferences.notifyNearbyFeasts)
        assertFalse(preferences.notifyShopReviews)
        assertTrue(preferences.quietHours)
    }

    @Test
    fun withoutTheAccountsSettingsThePhonesStay() = runTest(dispatcher) {
        accountSettings = "not json"
        preferences.updateNotification(NotificationKind.REPORT_UPDATES, false)

        viewModel.open(SettingsPage.NOTIFICATIONS)
        advanceUntilIdle()

        assertFalse(preferences.notifyReportUpdates)
        assertTrue(preferences.notifyNearbyFeasts)
    }

    @Test
    fun backGoesFromASubPageToTheMainPageAndThenLeaves() {
        viewModel.open(SettingsPage.APPEARANCE)

        assertTrue(viewModel.back())
        assertEquals(SettingsPage.MAIN, viewModel.state.value.page)
        assertFalse(viewModel.back())
    }

    @Test
    fun locationAccessAndDeleteAccountOpenTheirPrivacyPages() {
        assertEquals(PrivacyPage.LOCATION, privacyStart(Route.Privacy(startAtLocation = true)))
        assertEquals(PrivacyPage.DELETE, privacyStart(Route.Privacy(startAtDelete = true)))
        assertEquals(PrivacyPage.MAIN, privacyStart(Route.Privacy()))
    }

    @Test
    fun theCacheSizeReadsInMegabytes() {
        assertEquals("0 MB", megabytes(0))
        assertEquals("1 MB", megabytes(1024 * 1024))
        assertEquals("0.5 MB", megabytes(512 * 1024))
        assertEquals("2.3 MB", megabytes(2_400_000))
        assertEquals("24 MB", megabytes(24L * 1024 * 1024))
    }

    @Test
    fun savedFeastAlertsAreTheirOwnSetting() = runTest(dispatcher) {
        assertTrue(preferences.notifySavedFeasts, "on unless turned off")

        viewModel.setNotification(NotificationKind.SAVED_FEASTS, false)
        advanceUntilIdle()

        assertEquals("""{"savedFeasts":false}""", requests.single().text())
        assertFalse(preferences.notifySavedFeasts)
    }

}
