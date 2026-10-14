package com.example.bhandara.ui.reviews

import com.example.bhandara.data.network.ApiClient
import com.example.bhandara.data.network.ReviewApi
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
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.int
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * My photos (Profile → My activity), the same on Android and iOS: the photos in the user's reviews, grouped by
 * shop; deleting photos takes them out of their review and keeps the rest of it. The backend is a fake.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class MyPhotosViewModelTest {

    private val dispatcher = StandardTestDispatcher()

    @BeforeTest
    fun setUp() = Dispatchers.setMain(dispatcher)

    @AfterTest
    fun tearDown() = Dispatchers.resetMain()

    // ==================== Fakes ====================

    private val requests = mutableListOf<Pair<HttpRequestData, String>>()
    private var mine = "[]"
    private val failingReviews = mutableSetOf<String>()

    // The fake backend answers on the test's dispatcher, so advanceUntilIdle() also waits for its answers
    private val client = ApiClient("https://api.test/", { "id-token" }, MockEngine(MockEngineConfig().apply {
        dispatcher = this@MyPhotosViewModelTest.dispatcher
        addHandler { request ->
            val body = (request.body as? OutgoingContent.ByteArrayContent)?.bytes()?.decodeToString().orEmpty()
            requests += request to body
            val path = request.url.encodedPath
            val (status, answer) = when {
                request.method == HttpMethod.Get && path == "/api/reviews/mine" -> HttpStatusCode.OK to mine
                request.method == HttpMethod.Put && path.removePrefix("/api/reviews/") in failingReviews ->
                    HttpStatusCode.BadRequest to """{"message":"Couldn't update the review"}"""
                request.method == HttpMethod.Put -> HttpStatusCode.OK to
                    """{"id":"${path.removePrefix("/api/reviews/")}","shopId":"s","reviewerUid":"me","rating":4}"""
                else -> HttpStatusCode.NotFound to ""
            }
            respond(answer, status, headersOf(HttpHeaders.ContentType, "application/json"))
        }
    }))

    private fun TestScope.opened(): MyPhotosViewModel = MyPhotosViewModel(ReviewApi(client)).also { advanceUntilIdle() }

    /** The review updates sent, as JSON */
    private fun updates(): List<Pair<String, JsonObject>> = requests
        .filter { it.first.method == HttpMethod.Put }
        .map { (request, body) -> request.url.encodedPath.removePrefix("/api/reviews/") to Json.parseToJsonElement(body).jsonObject }

    init {
        mine = listOf(
            review("3", "Sai Amruttulya", photos = emptyList()),
            review("2", "Annapurna Misal", photos = listOf("misal")),
            review("1", "Shri Krishna", photos = listOf("sev", "pani", "stall")),
            review("4", "Hidden Snacks", photos = listOf("spam"), removed = true),
        ).joinToString(",", "[", "]")
    }

    // ==================== Tests ====================

    @Test
    fun photosAreGroupedByShopLeavingOutReviewsWithoutPhotosAndRemovedOnes() = runTest(dispatcher) {
        val state = opened().state.value

        assertEquals(listOf("Annapurna Misal", "Shri Krishna"), state.groups.map { it.review.shopName })
        assertEquals(listOf("sev", "pani", "stall").map(::url), state.groups[1].photos.map { it.url })
        assertEquals(4, state.photos.size)
        assertEquals(2, state.placeCount)
    }

    @Test
    fun deletingAPhotoKeepsTheReviewsStarsTextAndOtherPhotos() = runTest(dispatcher) {
        val viewModel = opened()

        viewModel.delete(setOf(url("pani")))
        advanceUntilIdle()

        val (id, update) = updates().single()
        assertEquals("1", id)
        assertEquals(4, update["rating"]!!.jsonPrimitive.int)
        assertEquals("Lovely", update["comment"]!!.jsonPrimitive.content)
        assertEquals(listOf("sev", "stall").map(::url), update["imageUrls"]!!.jsonArray.map { it.jsonPrimitive.content })
        assertEquals(listOf("misal", "sev", "stall").map(::url), viewModel.state.value.photos.map { it.url })
        assertEquals(1, viewModel.state.value.deletedCount)
    }

    @Test
    fun selectedPhotosFromSeveralShopsAreDeletedInOneGo() = runTest(dispatcher) {
        val viewModel = opened()
        viewModel.startSelecting()
        viewModel.toggle(url("misal"))
        viewModel.toggle(url("sev"))
        viewModel.toggle(url("stall"))
        viewModel.toggle(url("stall")) // picked by mistake, unpicked

        viewModel.deleteSelected()
        advanceUntilIdle()

        assertEquals(setOf("2", "1"), updates().map { it.first }.toSet())
        assertTrue(updates().first { it.first == "2" }.second["imageUrls"]!!.jsonArray.isEmpty())
        val state = viewModel.state.value
        assertEquals(listOf("pani", "stall").map(::url), state.photos.map { it.url })
        assertEquals(listOf("Shri Krishna"), state.groups.map { it.review.shopName })
        assertEquals(2, state.deletedCount)
        assertFalse(state.selecting)
        assertTrue(state.selected.isEmpty())
    }

    @Test
    fun selectAllPicksEveryPhotoAndStoppingClearsThePicks() = runTest(dispatcher) {
        val viewModel = opened()
        viewModel.startSelecting()

        viewModel.selectAll()
        assertEquals(4, viewModel.state.value.selected.size)

        viewModel.stopSelecting()
        assertFalse(viewModel.state.value.selecting)
        assertTrue(viewModel.state.value.selected.isEmpty())
    }

    @Test
    fun whenAReviewCannotBeUpdatedItsPhotosStayAndStaySelected() = runTest(dispatcher) {
        failingReviews += "1"
        val viewModel = opened()
        viewModel.startSelecting()
        viewModel.toggle(url("misal"))
        viewModel.toggle(url("sev"))

        viewModel.deleteSelected()
        advanceUntilIdle()

        val state = viewModel.state.value
        assertEquals(listOf("sev", "pani", "stall").map(::url), state.photos.map { it.url })
        assertEquals(setOf(url("sev")), state.selected)
        assertTrue(state.selecting)
        assertEquals(1, state.deletedCount)
        assertNotNull(state.message)
    }

    // ==================== Test data ====================

    @Test
    fun eachPhotosViewsAndTheirTotalAreShown() = runTest(dispatcher) {
        mine = """[{"id":"1","shopId":"s1","reviewerUid":"me","rating":4,"shopName":"Sharma Chaat","isActive":true,""" +
            """"imageUrls":["${url("a")}","${url("b")}"],"photoViews":{"${url("a")}":640,"${url("b")}":0}},""" +
            """{"id":"2","shopId":"s2","reviewerUid":"me","rating":3,"shopName":"Momo Corner","isActive":true,""" +
            """"imageUrls":["${url("c")}"],"photoViews":{"${url("c")}":12}},""" +
            """{"id":"3","shopId":"s3","reviewerUid":"me","rating":1,"shopName":"Gone","isActive":false,"removedByModerator":true,""" +
            """"imageUrls":["${url("d")}"],"photoViews":{"${url("d")}":999}}]"""

        val state = opened().state.value

        assertEquals(listOf(640L, 0L, 12L), state.photos.map { it.views })
        assertEquals(652L, state.viewCount, "photos in removed reviews aren't shown, so they aren't counted")
    }

    @Test
    fun photosFromOlderBackendsHaveNoViews() = runTest(dispatcher) {
        mine = "[${review("1", "Sharma Chaat", listOf("a"))}]"

        assertEquals(0L, opened().state.value.viewCount)
    }

    private companion object {
        fun url(name: String) = "https://img.test/$name.webp"

        fun review(id: String, shopName: String, photos: List<String>, removed: Boolean = false) =
            """{"id":"$id","shopId":"s$id","reviewerUid":"me","rating":4,"comment":"Lovely","shopName":"$shopName",""" +
                """"imageUrls":[${photos.joinToString(",") { "\"${url(it)}\"" }}],"removedByModerator":$removed,"isActive":${!removed}}"""
    }
}
