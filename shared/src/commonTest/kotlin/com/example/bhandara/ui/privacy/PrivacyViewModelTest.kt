package com.example.bhandara.ui.privacy

import com.example.bhandara.auth.UserAccount
import com.example.bhandara.data.network.ApiClient
import com.example.bhandara.data.network.ClaimApi
import com.example.bhandara.data.network.FeastApi
import com.example.bhandara.data.network.ReportApi
import com.example.bhandara.data.network.ReviewApi
import com.example.bhandara.data.network.UserApi
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
import io.ktor.http.headersOf
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Privacy and data (Profile → Account), the same on Android and iOS: the user's data as a file with the parts they
 * pick, and deleting the account (only after typing DELETE), which also forgets what's kept on the phone and signs
 * out. The backend is a fake.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class PrivacyViewModelTest {

    private val dispatcher = StandardTestDispatcher()

    @BeforeTest
    fun setUp() = Dispatchers.setMain(dispatcher)

    @AfterTest
    fun tearDown() = Dispatchers.resetMain()

    // ==================== Fakes ====================

    private val requests = mutableListOf<HttpRequestData>()
    private var reviewsStatus = HttpStatusCode.OK
    private var deleteStatus = HttpStatusCode.NoContent
    private var signedOut = 0

    private val settings = AppSettings(object : Settings.Factory {
        private val stores = mutableMapOf<String?, Settings>()
        override fun create(name: String?): Settings = stores.getOrPut(name) { MapSettings() }
    })

    // The fake backend answers on the test's dispatcher, so advanceUntilIdle() also waits for its answers
    private val client = ApiClient("https://api.test/", { "id-token" }, MockEngine(MockEngineConfig().apply {
        dispatcher = this@PrivacyViewModelTest.dispatcher
        addHandler { request ->
            requests += request
            val (status, body) = when (request.url.encodedPath) {
                "/api/reviews/mine" -> reviewsStatus to if (reviewsStatus == HttpStatusCode.OK) REVIEWS else """{"message":"Server error"}"""
                "/api/feasts/mine" -> HttpStatusCode.OK to FEASTS
                "/api/reports/mine" -> HttpStatusCode.OK to REPORTS
                "/api/shop-claims/mine" -> HttpStatusCode.OK to "[]"
                "/api/users/me" -> deleteStatus to if (deleteStatus == HttpStatusCode.NoContent) "" else """{"message":"Couldn't delete"}"""
                else -> HttpStatusCode.OK to "[]"
            }
            respond(body, status, headersOf(HttpHeaders.ContentType, "application/json"))
        }
    }))

    private fun TestScope.opened(startAt: PrivacyPage = PrivacyPage.MAIN): PrivacyViewModel = PrivacyViewModel(
        ReviewApi(client), FeastApi(client), ReportApi(client), ClaimApi(client), UserApi(client), settings,
        account = { UserAccount(uid = "me-1", isAnonymous = false, name = "Piyush", email = "p@test.local") },
        signOut = { signedOut++ },
        startAt = startAt,
    ).also { advanceUntilIdle() }

    // ==================== Tests ====================

    @Test
    fun theMainPageFetchesNothingUntilItIsNeeded() = runTest(dispatcher) {
        val viewModel = opened()

        assertTrue(requests.isEmpty())

        viewModel.open(PrivacyPage.DOWNLOAD)
        advanceUntilIdle()
        val data = viewModel.state.value.data
        assertNotNull(data)
        assertEquals(2, data.reviews.size)
        assertEquals(3, data.photos.size)
        assertEquals(1, data.feasts.size)
        assertEquals(1, data.reports.size)
    }

    @Test
    fun theFileHasOnlyThePartsPicked() = runTest(dispatcher) {
        settings.setShopSaved("5", true)
        val viewModel = opened()
        viewModel.open(PrivacyPage.DOWNLOAD)
        advanceUntilIdle()
        // Starts with account, reviews, photos and feasts; the user swaps feasts for saved places
        viewModel.toggle(DataPart.FEASTS)
        viewModel.toggle(DataPart.SAVED)

        viewModel.makeFile(appLanguage = "hi")

        val state = viewModel.state.value
        assertEquals(PrivacyPage.READY, state.page)
        val file = Json.parseToJsonElement(state.file!!).jsonObject
        assertEquals("Piyush", file["account"]!!.jsonObject["name"]!!.jsonPrimitive.content)
        assertEquals("hi", file["account"]!!.jsonObject["appLanguage"]!!.jsonPrimitive.content)
        assertEquals(2, file["reviews"]!!.jsonArray.size)
        assertEquals(3, file["photos"]!!.jsonArray.size)
        assertEquals(listOf("5"), file["savedPlaces"]!!.jsonObject["shopIds"]!!.jsonArray.map { it.jsonPrimitive.content })
        assertNull(file["feastsPosted"])
        assertNull(file["reports"])
    }

    @Test
    fun noFileIsMadeUntilTheDataIsInOrWithNothingPicked() = runTest(dispatcher) {
        reviewsStatus = HttpStatusCode.InternalServerError
        val viewModel = opened()
        viewModel.open(PrivacyPage.DOWNLOAD)
        advanceUntilIdle()
        assertNotNull(viewModel.state.value.dataError)

        viewModel.makeFile("en")
        assertNull(viewModel.state.value.file)

        reviewsStatus = HttpStatusCode.OK
        viewModel.loadData()
        advanceUntilIdle()
        DataPart.entries.filter { it in viewModel.state.value.parts }.forEach(viewModel::toggle)
        viewModel.makeFile("en")
        assertNull(viewModel.state.value.file)
    }

    @Test
    fun theAccountIsOnlyDeletedOnceDeleteIsTyped() = runTest(dispatcher) {
        val viewModel = opened(PrivacyPage.DELETE)

        viewModel.onDeleteTextChange("delet")
        assertFalse(viewModel.state.value.canDelete)
        viewModel.deleteAccount()
        advanceUntilIdle()
        assertTrue(requests.none { it.method == HttpMethod.Delete })

        viewModel.onDeleteTextChange("delete ")
        assertTrue(viewModel.state.value.canDelete)
    }

    @Test
    fun deletingTheAccountForgetsWhatIsOnThePhoneAndSignsOut() = runTest(dispatcher) {
        settings.setShopSaved("5", true)
        settings.setFeastSaved("9", true)
        val viewModel = opened(PrivacyPage.DELETE)
        viewModel.onDeleteTextChange("DELETE")

        viewModel.deleteAccount()
        advanceUntilIdle()

        val delete = requests.single { it.method == HttpMethod.Delete }
        assertEquals("/api/users/me", delete.url.encodedPath)
        assertTrue(viewModel.state.value.deleted)
        assertEquals(0, settings.savedPlaceCount)
        assertEquals(1, signedOut)
    }

    @Test
    fun whenDeletingFailsNothingOnThePhoneChangesAndTheUserSeesWhy() = runTest(dispatcher) {
        deleteStatus = HttpStatusCode.InternalServerError
        settings.setShopSaved("5", true)
        val viewModel = opened(PrivacyPage.DELETE)
        viewModel.onDeleteTextChange("DELETE")

        viewModel.deleteAccount()
        advanceUntilIdle()

        assertFalse(viewModel.state.value.deleted)
        assertNotNull(viewModel.state.value.message)
        assertEquals(1, settings.savedPlaceCount)
        assertEquals(0, signedOut)
    }

    @Test
    fun backGoesFromASubPageToTheMainPageAndThenLeaves() = runTest(dispatcher) {
        val viewModel = opened()
        viewModel.open(PrivacyPage.LOCATION)

        assertTrue(viewModel.back())
        assertEquals(PrivacyPage.MAIN, viewModel.state.value.page)
        assertFalse(viewModel.back())
    }

    // ==================== Test data ====================

    private companion object {
        const val REVIEWS = """[{"id":"1","shopId":"5","reviewerUid":"me-1","rating":5,"imageUrls":["https://img.test/a.webp","https://img.test/b.webp"]},
            {"id":"2","shopId":"6","reviewerUid":"me-1","rating":3,"imageUrls":["https://img.test/c.webp"]}]"""
        const val FEASTS = """[{"id":"9","organizerName":"Ganesh Mandal","menuItems":["Puri"],"feastDate":"2026-10-12","startTime":"12:00:00","endTime":"15:00:00","latitude":18.5,"longitude":73.8,"estimatedCapacity":100}]"""
        const val REPORTS = """[{"kind":"SHOP","reportId":"3","status":"IN_REVIEW","reason":"WRONG_LOCATION"}]"""
    }
}
