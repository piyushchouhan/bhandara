package com.example.bhandara.ui.drawer

import com.example.bhandara.auth.AuthService
import com.example.bhandara.auth.PlatformAuth
import com.example.bhandara.auth.UserAccount
import com.example.bhandara.data.network.ApiClient
import com.example.bhandara.data.network.ClaimApi
import com.example.bhandara.data.network.ShopApi
import com.example.bhandara.data.network.SupportApi
import com.example.bhandara.ui.profile.ProfileViewModel
import com.example.bhandara.ui.profile.SignInOutcome
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
 * What the side menu leads to, the same on Android and iOS: reporting a problem or suggesting a feature, and the
 * profile with signing in and out. The backend and Firebase are fakes.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class DrawerViewModelsTest {

    private val dispatcher = StandardTestDispatcher()

    @BeforeTest
    fun setUp() = Dispatchers.setMain(dispatcher)

    @AfterTest
    fun tearDown() = Dispatchers.resetMain()

    // ==================== Fakes ====================

    private val requests = mutableListOf<HttpRequestData>()
    private var supportStatus = HttpStatusCode.Created
    /** Answers for particular paths (e.g. the user's claims); anything else gets the support answer */
    private val bodies = mutableMapOf<String, String>()

    private val client = ApiClient("https://api.test/", { "id-token" }, MockEngine(MockEngineConfig().apply {
        dispatcher = this@DrawerViewModelsTest.dispatcher
        addHandler { request ->
            requests += request
            val body = bodies[request.url.encodedPath]
            if (body != null) {
                respond(body, HttpStatusCode.OK, headersOf(HttpHeaders.ContentType, "application/json"))
            } else {
                respond("{}", supportStatus, headersOf(HttpHeaders.ContentType, "application/json"))
            }
        }
    }))

    /** Firebase stand-in: whoever is signed in, and anonymous sign-in that succeeds straight away */
    private class FakeFirebase(var account: UserAccount?) : PlatformAuth {
        var anonymousSignIns = 0
        override fun currentUid() = account?.uid
        override fun currentAccount() = account
        override fun signInAnonymously(onResult: (uid: String?, error: String?) -> Unit) {
            anonymousSignIns++
            account = UserAccount(uid = "anon-2", isAnonymous = true)
            onResult("anon-2", null)
        }
        override fun idToken(forceRefresh: Boolean, onResult: (token: String?, error: String?) -> Unit) = onResult("t", null)
    }

    private val guest = UserAccount(uid = "anon-1", isAnonymous = true)
    private val google = UserAccount(uid = "g-1", isAnonymous = false, name = "Piyush", email = "p@example.com")

    private fun HttpRequestData.bodyText() = (body as? OutgoingContent.ByteArrayContent)?.bytes()?.decodeToString().orEmpty()

    // ==================== Report an issue / suggest a feature ====================

    @Test
    fun aProblemReportIsSentTrimmed() = runTest(dispatcher) {
        val viewModel = FeedbackViewModel(FeedbackKind.BUG, SupportApi(client))

        viewModel.onTextChange("  Map doesn't load  ")
        viewModel.send()
        advanceUntilIdle()

        val request = requests.single()
        assertEquals("/api/support/bugs", request.url.encodedPath)
        assertEquals("""{"bug":"Map doesn't load"}""", request.bodyText())
        assertTrue(viewModel.state.value.sent)
        assertFalse(viewModel.state.value.canSend)
    }

    @Test
    fun aFeatureIdeaGoesToFeatures() = runTest(dispatcher) {
        val viewModel = FeedbackViewModel(FeedbackKind.FEATURE, SupportApi(client))

        viewModel.onTextChange("Dark map")
        viewModel.send()
        advanceUntilIdle()

        assertEquals("/api/support/features", requests.single().url.encodedPath)
        assertEquals("""{"feature":"Dark map"}""", requests.single().bodyText())
    }

    @Test
    fun nothingIsSentWithoutText() = runTest(dispatcher) {
        val viewModel = FeedbackViewModel(FeedbackKind.BUG, SupportApi(client))

        viewModel.onTextChange("   ")
        viewModel.send()
        advanceUntilIdle()

        assertFalse(viewModel.state.value.canSend)
        assertTrue(requests.isEmpty())
    }

    @Test
    fun textIsLimitedToWhatTheBackendAccepts() {
        val viewModel = FeedbackViewModel(FeedbackKind.BUG, SupportApi(client))

        viewModel.onTextChange("a".repeat(2500))

        assertEquals(MAX_FEEDBACK_LENGTH, viewModel.state.value.text.length)
    }

    @Test
    fun aFailedSendIsExplainedAndCanBeTriedAgain() = runTest(dispatcher) {
        supportStatus = HttpStatusCode.ServiceUnavailable
        val viewModel = FeedbackViewModel(FeedbackKind.BUG, SupportApi(client))
        viewModel.onTextChange("Crash on start")

        viewModel.send()
        advanceUntilIdle()

        assertNotNull(viewModel.state.value.error)
        assertFalse(viewModel.state.value.sent)
        assertTrue(viewModel.state.value.canSend)

        supportStatus = HttpStatusCode.Created
        viewModel.send()
        advanceUntilIdle()
        assertTrue(viewModel.state.value.sent)
        assertNull(viewModel.state.value.error)
    }

    @Test
    fun tappingSendTwiceSendsOnce() = runTest(dispatcher) {
        val viewModel = FeedbackViewModel(FeedbackKind.BUG, SupportApi(client))
        viewModel.onTextChange("Crash on start")

        viewModel.send()
        viewModel.send()
        advanceUntilIdle()

        assertEquals(1, requests.size)
    }

    /** A screenshot picked on the phone */
    private object Screenshot : com.example.bhandara.photos.PickedPhoto {
        override val preview: Any = "shot"
        override suspend fun compressed(): com.example.bhandara.photos.CompressedPhoto? = null
    }

    @Test
    fun theTopicAndScreenshotGoWithTheReport() = runTest(dispatcher) {
        val viewModel = FeedbackViewModel(FeedbackKind.BUG, SupportApi(client)) { _, _ -> listOf("https://img.test/shot.webp") }

        viewModel.onTopicClick(FeedbackTopic.MAP)
        viewModel.onScreenshot(Screenshot)
        viewModel.onTextChange("Pins don't show")
        viewModel.send()
        advanceUntilIdle()

        assertEquals(
            """{"bug":"[Map] Pins don't show\n\nScreenshot: https://img.test/shot.webp"}""",
            requests.single().bodyText(),
        )
        assertTrue(viewModel.state.value.sent)
    }

    @Test
    fun tappingTheSameTopicAgainClearsIt() {
        val viewModel = FeedbackViewModel(FeedbackKind.BUG, SupportApi(client))

        viewModel.onTopicClick(FeedbackTopic.LOGIN)
        viewModel.onTopicClick(FeedbackTopic.FEASTS)
        assertEquals(FeedbackTopic.FEASTS, viewModel.state.value.topic)
        viewModel.onTopicClick(FeedbackTopic.FEASTS)
        assertNull(viewModel.state.value.topic)
    }

    @Test
    fun aScreenshotThatFailsToUploadStopsTheReportSoItCanBeRetried() = runTest(dispatcher) {
        val viewModel = FeedbackViewModel(FeedbackKind.BUG, SupportApi(client)) { _, _ -> throw IllegalStateException("offline") }
        viewModel.onScreenshot(Screenshot)
        viewModel.onTextChange("Crash")

        viewModel.send()
        advanceUntilIdle()

        assertTrue(requests.isEmpty(), "no report without the screenshot the user added")
        assertNotNull(viewModel.state.value.error)
        assertTrue(viewModel.state.value.canSend)
    }

    @Test
    fun aLongReportStillFitsTheBackendWithItsTopicAndLink() {
        val text = feedbackText("a".repeat(MAX_FEEDBACK_LENGTH), FeedbackTopic.SHOP, "https://img.test/x.webp")

        assertEquals(MAX_FEEDBACK_LENGTH, text.length)
        assertTrue(text.startsWith("[Shop details] aaa"))
        assertTrue(text.endsWith("Screenshot: https://img.test/x.webp"))
    }

    // ==================== Profile ====================

    @Test
    fun aGuestIsShownAsAGuest() {
        val state = ProfileViewModel(AuthService(FakeFirebase(guest)), signOut = {}).state.value

        assertTrue(state.isGuest)
    }

    @Test
    fun signingInShowsTheAccount() {
        val firebase = FakeFirebase(guest)
        val viewModel = ProfileViewModel(AuthService(firebase), signOut = {})

        viewModel.onSignInStarted()
        assertTrue(viewModel.state.value.busy)
        firebase.account = google
        viewModel.onSignInResult(SignInOutcome.SignedIn)

        val state = viewModel.state.value
        assertFalse(state.busy)
        assertFalse(state.isGuest)
        assertEquals("Piyush", state.account?.name)
        assertEquals("Signed in successfully!", state.message)
    }

    private fun claim(shopId: Long, status: String, at: String) =
        """{"id":$shopId,"shopId":$shopId,"status":"$status","createdAt":"$at"}"""

    private fun shop(id: Long, name: String) = """{"id":"$id","shopName":"$name","latitude":18.5,"longitude":73.8}"""

    @Test
    fun theProfileListsTheShopsWhoseClaimsWereApprovedNewestFirst() = runTest(dispatcher) {
        bodies["/api/shop-claims/mine"] = "[${claim(1, "APPROVED", "2026-10-01")}, ${claim(2, "REJECTED", "2026-10-02")}, ${claim(3, "APPROVED", "2026-10-05")}, ${claim(4, "PENDING_REVIEW", "2026-10-06")}]"
        bodies["/api/localshops/1"] = shop(1, "Sharma Chaat")
        bodies["/api/localshops/3"] = shop(3, "Gupta Snacks")

        val viewModel = ProfileViewModel(AuthService(FakeFirebase(google)), signOut = {}, ClaimApi(client), ShopApi(client))
        advanceUntilIdle()

        assertEquals(listOf("Gupta Snacks", "Sharma Chaat"), viewModel.state.value.myShops.map { it.shopName })
        assertTrue(viewModel.state.value.isShopOwner)
    }

    @Test
    fun aGuestHasNoShopsAndTheirClaimsAreNotAskedFor() = runTest(dispatcher) {
        val viewModel = ProfileViewModel(AuthService(FakeFirebase(guest)), signOut = {}, ClaimApi(client), ShopApi(client))
        advanceUntilIdle()

        assertTrue(viewModel.state.value.myShops.isEmpty())
        assertTrue(requests.none { it.url.encodedPath == "/api/shop-claims/mine" })
    }

    @Test
    fun signingInFindsTheAccountsShops() = runTest(dispatcher) {
        bodies["/api/shop-claims/mine"] = "[${claim(1, "APPROVED", "2026-10-01")}]"
        bodies["/api/localshops/1"] = shop(1, "Sharma Chaat")
        val firebase = FakeFirebase(guest)
        val viewModel = ProfileViewModel(AuthService(firebase), signOut = {}, ClaimApi(client), ShopApi(client))
        advanceUntilIdle()

        firebase.account = google
        viewModel.onSignInResult(SignInOutcome.SignedIn)
        advanceUntilIdle()

        assertEquals(listOf("Sharma Chaat"), viewModel.state.value.myShops.map { it.shopName })
    }

    @Test
    fun aShopThatWasDeactivatedSinceTheLastLookIsAnnouncedOnce() = runTest(dispatcher) {
        bodies["/api/shop-claims/mine"] = "[${claim(1, "APPROVED", "2026-10-01")}]"
        bodies["/api/localshops/1"] = shop(1, "Sharma Chaat")
        val viewModel = ProfileViewModel(AuthService(FakeFirebase(google)), signOut = {}, ClaimApi(client), ShopApi(client))
        advanceUntilIdle()
        assertEquals(null, viewModel.state.value.justDeactivated)

        bodies["/api/localshops/1"] = """{"id":"1","shopName":"Sharma Chaat","latitude":18.5,"longitude":73.8,"isActive":false}"""
        viewModel.loadMyShops()
        advanceUntilIdle()
        assertEquals("Sharma Chaat", viewModel.state.value.justDeactivated?.shopName)

        viewModel.onBannerShown()
        viewModel.loadMyShops()
        advanceUntilIdle()
        assertEquals(null, viewModel.state.value.justDeactivated)
    }

    @Test
    fun aShopAlreadyHiddenWhenTheProfileOpensIsNotAnnounced() = runTest(dispatcher) {
        bodies["/api/shop-claims/mine"] = "[${claim(1, "APPROVED", "2026-10-01")}]"
        bodies["/api/localshops/1"] = """{"id":"1","shopName":"Sharma Chaat","latitude":18.5,"longitude":73.8,"isActive":false}"""
        val viewModel = ProfileViewModel(AuthService(FakeFirebase(google)), signOut = {}, ClaimApi(client), ShopApi(client))
        advanceUntilIdle()

        assertEquals(1, viewModel.state.value.myShops.size)
        assertEquals(null, viewModel.state.value.justDeactivated)
    }

    @Test
    fun closingTheAccountPickerIsNotAnError() {
        val viewModel = ProfileViewModel(AuthService(FakeFirebase(guest)), signOut = {})

        viewModel.onSignInStarted()
        viewModel.onSignInResult(SignInOutcome.Cancelled)

        assertFalse(viewModel.state.value.busy)
        assertNull(viewModel.state.value.error)
    }

    @Test
    fun aFailedSignInIsExplainedWithoutTechnicalDetails() {
        val viewModel = ProfileViewModel(AuthService(FakeFirebase(guest)), signOut = {})

        viewModel.onSignInStarted()
        viewModel.onSignInResult(SignInOutcome.Failed("ApiException: 7: NETWORK_ERROR"))

        assertEquals("Couldn't sign in. Please check your connection and try again.", viewModel.state.value.error)
        assertTrue(viewModel.state.value.isGuest)
    }

    @Test
    fun signingOutLeavesTheUserSignedInAnonymously() = runTest(dispatcher) {
        // The backend needs to know who is using the app, so a guest account takes over
        val firebase = FakeFirebase(google)
        var signedOut = false
        val viewModel = ProfileViewModel(AuthService(firebase), signOut = { signedOut = true; firebase.account = null })

        viewModel.signOut()
        advanceUntilIdle()

        assertTrue(signedOut)
        assertEquals(1, firebase.anonymousSignIns)
        assertTrue(viewModel.state.value.isGuest)
        assertEquals("anon-2", viewModel.state.value.account?.uid)
        assertEquals("Signed out", viewModel.state.value.message)
    }

    @Test
    fun aFailedSignOutKeepsTheAccount() = runTest(dispatcher) {
        val viewModel = ProfileViewModel(AuthService(FakeFirebase(google)), signOut = { error("offline") })

        viewModel.signOut()
        advanceUntilIdle()

        assertFalse(viewModel.state.value.isGuest)
        assertEquals("Couldn't sign out. Please try again.", viewModel.state.value.error)
        assertFalse(viewModel.state.value.busy)
    }

    @Test
    fun theProfileShowsTheNameAndEmailEachOnce() {
        val named = ProfileViewModel(AuthService(FakeFirebase(google)), signOut = {}).state.value
        assertEquals("Piyush", named.accountTitle)
        assertEquals("p@example.com", named.accountSubtitle)

        // Some Google accounts come without a name: the email is shown once, as the title
        val unnamed = ProfileViewModel(AuthService(FakeFirebase(google.copy(name = null))), signOut = {}).state.value
        assertEquals("p@example.com", unnamed.accountTitle)
        assertNull(unnamed.accountSubtitle)
    }
}
