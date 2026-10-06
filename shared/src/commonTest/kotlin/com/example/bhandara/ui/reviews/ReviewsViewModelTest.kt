package com.example.bhandara.ui.reviews

import com.example.bhandara.data.network.ApiClient
import com.example.bhandara.data.network.ReviewApi
import com.example.bhandara.data.settings.AppSettings
import com.example.bhandara.photos.CompressedPhoto
import com.example.bhandara.photos.PhotoUploadException
import com.example.bhandara.photos.PickedPhoto
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
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonNull
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
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * A shop's reviews, the same on Android and iOS: read them, write one review per shop (or edit / delete your own),
 * with optional photos, and report someone else's review once. The backend and photo uploads are fakes.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class ReviewsViewModelTest {

    private val dispatcher = StandardTestDispatcher()

    @BeforeTest
    fun setUp() = Dispatchers.setMain(dispatcher)

    @AfterTest
    fun tearDown() = Dispatchers.resetMain()

    // ==================== Fakes ====================

    private var signedInUid: String? = ME
    private val requests = mutableListOf<HttpRequestData>()
    private val responses = mutableMapOf<Pair<HttpMethod, String>, Pair<HttpStatusCode, String>>()

    private val settings = AppSettings(object : Settings.Factory {
        private val stores = mutableMapOf<String?, Settings>()
        override fun create(name: String?): Settings = stores.getOrPut(name) { MapSettings() }
    })

    // The fake backend answers on the test's dispatcher, so advanceUntilIdle() also waits for its answers
    private val client = ApiClient("https://api.test/", { "id-token" }, MockEngine(MockEngineConfig().apply {
        dispatcher = this@ReviewsViewModelTest.dispatcher
        addHandler { request ->
            requests += request
            val (status, body) = responses[request.method to request.url.encodedPath] ?: (HttpStatusCode.NotFound to "")
            respond(body, status, headersOf(HttpHeaders.ContentType, "application/json"))
        }
    }))

    private fun answer(method: HttpMethod, path: String, body: String, status: HttpStatusCode = HttpStatusCode.OK) {
        responses[method to "/api/$path"] = status to body
    }

    private fun reviewsAre(vararg reviews: String) = answer(HttpMethod.Get, "reviews/shop/5", "[${reviews.joinToString(",")}]")

    /** A photo picked on the phone, named for the test */
    private data class Photo(val name: String) : PickedPhoto {
        override val preview: Any = name
        override suspend fun compressed(): CompressedPhoto? = null
    }

    private fun photos(vararg names: String) = names.map { Photo(it) }

    /** Photos "uploaded" so far, and whether the next upload fails */
    private val uploaded = mutableListOf<List<String>>()
    private var uploadFails = false
    private val uploader: suspend (List<PickedPhoto>, (Int) -> Unit) -> List<String> = { picked, onProgress ->
        val names = picked.map { (it as Photo).name }
        uploaded += names
        if (uploadFails) throw PhotoUploadException("Upload failed")
        onProgress(100)
        names.map { "https://img.test/$it.webp" }
    }

    init {
        reviewsAre(review("1", OTHER, 4, "Tasty"), review("2", "someone", 2, "Too oily"))
    }

    private fun TestScope.opened(photos: Boolean = true): ReviewsViewModel =
        ReviewsViewModel("5", ReviewApi(client), settings, { signedInUid }, if (photos) uploader else null)
            .also { advanceUntilIdle() }

    private fun requestsTo(method: HttpMethod, path: String) =
        requests.filter { it.method == method && it.url.encodedPath == "/api/$path" }

    // ==================== Reading reviews ====================

    @Test
    fun theShopsReviewsAreShownWithTheirAverage() = runTest(dispatcher) {
        val state = opened().state.value

        assertFalse(state.loading)
        assertEquals(listOf("Tasty", "Too oily"), state.reviews.map { it.comment })
        assertEquals(3.0, state.averageRating)
    }

    @Test
    fun theCountIsNotShownAsZeroWhileReviewsLoad() = runTest(dispatcher) {
        val viewModel = ReviewsViewModel("5", ReviewApi(client), settings, { signedInUid }, uploader)

        assertNull(viewModel.state.value.countToShow)
        advanceUntilIdle()
        assertEquals(2, viewModel.state.value.countToShow)
    }

    @Test
    fun withoutReviewsThereIsNoAverage() = runTest(dispatcher) {
        reviewsAre()

        val state = opened().state.value

        assertTrue(state.reviews.isEmpty())
        assertNull(state.averageRating)
    }

    @Test
    fun reviewsThatFailToLoadCanBeRetried() = runTest(dispatcher) {
        answer(HttpMethod.Get, "reviews/shop/5", "", HttpStatusCode.InternalServerError)
        val viewModel = opened()

        assertNotNull(viewModel.state.value.error)
        assertFalse(viewModel.state.value.canWrite)

        reviewsAre(review("1", OTHER, 5, "Great"))
        viewModel.retry()
        advanceUntilIdle()

        assertNull(viewModel.state.value.error)
        assertEquals(1, viewModel.state.value.reviews.size)
    }

    // ==================== Writing a review ====================

    @Test
    fun aNewReviewStartsWithoutStarsSoTheUserMustPickARating() = runTest(dispatcher) {
        val viewModel = opened()

        viewModel.startWriting()

        val draft = assertNotNull(viewModel.state.value.draft)
        assertNull(draft.reviewId)
        assertEquals(0, draft.rating)
        assertFalse(draft.canSubmit)

        viewModel.submit()
        advanceUntilIdle()
        assertTrue(requestsTo(HttpMethod.Post, "reviews").isEmpty())
    }

    @Test
    fun aNewReviewIsPostedAndTheListReloaded() = runTest(dispatcher) {
        answer(HttpMethod.Post, "reviews", review("3", ME, 4, "Great chaat"))
        val viewModel = opened()

        viewModel.startWriting()
        viewModel.setRating(4)
        viewModel.setComment("  Great chaat  ")
        reviewsAre(review("3", ME, 4, "Great chaat"), review("1", OTHER, 4, "Tasty"))
        viewModel.submit()
        advanceUntilIdle()

        val body = requestsTo(HttpMethod.Post, "reviews").single().json()
        assertEquals("5", body["shopId"]?.jsonPrimitive?.content)
        assertEquals(4, body["rating"]?.jsonPrimitive?.int)
        assertEquals("Great chaat", body["comment"]?.jsonPrimitive?.content)
        val state = viewModel.state.value
        assertNull(state.draft)
        assertEquals("Thanks for your review!", state.message)
        assertEquals("Great chaat", state.myReview?.comment)
    }

    @Test
    fun aBlankCommentIsSentAsNoComment() = runTest(dispatcher) {
        answer(HttpMethod.Post, "reviews", review("3", ME, 5, null))
        val viewModel = opened()

        viewModel.startWriting()
        viewModel.setRating(5)
        viewModel.setComment("   ")
        viewModel.submit()
        advanceUntilIdle()

        val comment = requestsTo(HttpMethod.Post, "reviews").single().json()["comment"]
        assertTrue(comment == null || comment == JsonNull)
    }

    @Test
    fun commentsAreLimitedTo1000Characters() = runTest(dispatcher) {
        val viewModel = opened()
        viewModel.startWriting()

        viewModel.setComment("a".repeat(1200))

        assertEquals(1000, viewModel.state.value.draft?.comment?.length)
    }

    @Test
    fun ratingsOutside1To5AreIgnored() = runTest(dispatcher) {
        val viewModel = opened()
        viewModel.startWriting()

        viewModel.setRating(6)
        viewModel.setRating(-1)

        assertEquals(0, viewModel.state.value.draft?.rating)
    }

    @Test
    fun someoneWhoAlreadyReviewedEditsTheirReviewInsteadOfWritingAnother() = runTest(dispatcher) {
        // The backend allows one review per person per shop
        reviewsAre(review("1", OTHER, 4, "Tasty"), review("7", ME, 3, "Okay", images = listOf("https://img.test/a.webp")))
        answer(HttpMethod.Put, "reviews/7", review("7", ME, 5, "Better now"))
        val viewModel = opened()

        assertEquals("7", viewModel.state.value.myReview?.id)
        viewModel.startWriting()
        val draft = assertNotNull(viewModel.state.value.draft)
        assertEquals("7", draft.reviewId)
        assertEquals(3, draft.rating)
        assertEquals("Okay", draft.comment)
        assertEquals(listOf("https://img.test/a.webp"), draft.keptImageUrls)

        viewModel.setRating(5)
        viewModel.setComment("Better now")
        viewModel.submit()
        advanceUntilIdle()

        assertTrue(requestsTo(HttpMethod.Post, "reviews").isEmpty())
        val body = requestsTo(HttpMethod.Put, "reviews/7").single().json()
        assertEquals(5, body["rating"]?.jsonPrimitive?.int)
        assertEquals(listOf("https://img.test/a.webp"), body["imageUrls"]?.jsonArray?.map { it.jsonPrimitive.content })
        assertEquals("Review updated", viewModel.state.value.message)
    }

    @Test
    fun signedOutUsersCannotWriteOrReport() = runTest(dispatcher) {
        signedInUid = null

        val state = opened().state.value

        assertFalse(state.canWrite)
        assertTrue(state.reviews.none { state.canReport(it) })
    }

    @Test
    fun aRejectedReviewStaysOpenWithTheReason() = runTest(dispatcher) {
        answer(
            HttpMethod.Post, "reviews",
            """{"message":"Validation failed","fieldErrors":{"comment":"Review comment cannot exceed 1000 characters"}}""",
            HttpStatusCode.BadRequest,
        )
        val viewModel = opened()

        viewModel.startWriting()
        viewModel.setRating(4)
        viewModel.submit()
        advanceUntilIdle()

        assertNotNull(viewModel.state.value.draft)
        assertFalse(viewModel.state.value.submitting)
        assertNotNull(viewModel.state.value.message)
    }

    @Test
    fun ifTheUserAlreadyReviewedElsewhereTheirReviewIsLoadedToEdit() = runTest(dispatcher) {
        // E.g. reviewed on another phone after this list was loaded
        answer(HttpMethod.Post, "reviews", """{"message":"You have already reviewed this shop. Please update your existing review."}""", HttpStatusCode.Conflict)
        val viewModel = opened()

        viewModel.startWriting()
        viewModel.setRating(4)
        reviewsAre(review("7", ME, 2, "Meh"))
        viewModel.submit()
        advanceUntilIdle()

        assertEquals("You have already reviewed this shop. Please update your existing review.", viewModel.state.value.message)
        assertNull(viewModel.state.value.draft)
        assertEquals("7", viewModel.state.value.myReview?.id)
    }

    @Test
    fun tappingSubmitTwiceSendsOneReview() = runTest(dispatcher) {
        answer(HttpMethod.Post, "reviews", review("3", ME, 4, null))
        val viewModel = opened()

        viewModel.startWriting()
        viewModel.setRating(4)
        viewModel.submit()
        viewModel.submit()
        advanceUntilIdle()

        assertEquals(1, requestsTo(HttpMethod.Post, "reviews").size)
    }

    @Test
    fun aDraftCanBeCancelled() = runTest(dispatcher) {
        val viewModel = opened()
        viewModel.startWriting()

        viewModel.cancelDraft()

        assertNull(viewModel.state.value.draft)
    }

    // ==================== Photos ====================

    @Test
    fun newPhotosAreUploadedAndSentWithTheOnesKept() = runTest(dispatcher) {
        reviewsAre(review("7", ME, 3, "Okay", images = listOf("https://img.test/old1.webp", "https://img.test/old2.webp")))
        answer(HttpMethod.Put, "reviews/7", review("7", ME, 3, "Okay"))
        val viewModel = opened()

        viewModel.startWriting()
        viewModel.removeKeptPhoto("https://img.test/old1.webp")
        viewModel.addPhotos(photos("new1"))
        viewModel.submit()
        advanceUntilIdle()

        assertEquals(listOf(listOf("new1")), uploaded)
        val urls = requestsTo(HttpMethod.Put, "reviews/7").single().json()["imageUrls"]?.jsonArray?.map { it.jsonPrimitive.content }
        assertEquals(listOf("https://img.test/old2.webp", "https://img.test/new1.webp"), urls)
    }

    @Test
    fun aReviewHasAtMostFivePhotos() = runTest(dispatcher) {
        reviewsAre(review("7", ME, 3, "Okay", images = listOf("https://img.test/a.webp", "https://img.test/b.webp")))
        val viewModel = opened()
        viewModel.startWriting()

        viewModel.addPhotos(photos("p1", "p2", "p3", "p4"))

        val draft = assertNotNull(viewModel.state.value.draft)
        assertEquals(photos("p1", "p2", "p3"), draft.newPhotos)
        assertFalse(draft.canAddPhotos)

        viewModel.removeNewPhoto(Photo("p2"))
        assertTrue(viewModel.state.value.draft!!.canAddPhotos)
    }

    @Test
    fun ifAPhotoFailsToUploadNothingIsPostedAndTheDraftStays() = runTest(dispatcher) {
        // Posting without the photo would silently lose it
        uploadFails = true
        val viewModel = opened()

        viewModel.startWriting()
        viewModel.setRating(5)
        viewModel.addPhotos(photos("p1"))
        viewModel.submit()
        advanceUntilIdle()

        assertTrue(requestsTo(HttpMethod.Post, "reviews").isEmpty())
        assertEquals(photos("p1"), viewModel.state.value.draft?.newPhotos)
        assertEquals("Couldn't upload the photos. Please try again.", viewModel.state.value.message)
        assertFalse(viewModel.state.value.submitting)
    }

    @Test
    fun withoutPhotoSupportPhotosCannotBeAdded() = runTest(dispatcher) {
        val viewModel = opened(photos = false)
        viewModel.startWriting()

        viewModel.addPhotos(photos("p1"))

        assertFalse(viewModel.state.value.photosSupported)
        assertTrue(viewModel.state.value.draft!!.newPhotos.isEmpty())
    }

    // ==================== Deleting your review ====================

    @Test
    fun theAuthorCanDeleteTheirReview() = runTest(dispatcher) {
        reviewsAre(review("7", ME, 3, "Okay"), review("1", OTHER, 4, "Tasty"))
        answer(HttpMethod.Delete, "reviews/7", "", HttpStatusCode.NoContent)
        val viewModel = opened()

        reviewsAre(review("1", OTHER, 4, "Tasty"))
        viewModel.delete("7")
        advanceUntilIdle()

        assertEquals(1, requestsTo(HttpMethod.Delete, "reviews/7").size)
        assertNull(viewModel.state.value.myReview)
        assertEquals("Review deleted", viewModel.state.value.message)
    }

    @Test
    fun aFailedDeleteIsExplainedAndTheReviewStays() = runTest(dispatcher) {
        reviewsAre(review("7", ME, 3, "Okay"))
        answer(HttpMethod.Delete, "reviews/7", "", HttpStatusCode.ServiceUnavailable)
        val viewModel = opened()

        viewModel.delete("7")
        advanceUntilIdle()

        assertEquals("7", viewModel.state.value.myReview?.id)
        assertNotNull(viewModel.state.value.message)
    }

    @Test
    fun onlyTheAuthorCanDelete() = runTest(dispatcher) {
        val viewModel = opened()

        viewModel.delete("1")
        advanceUntilIdle()

        assertTrue(requestsTo(HttpMethod.Delete, "reviews/1").isEmpty())
    }

    // ==================== Reporting someone else's review ====================

    @Test
    fun reportingAReviewIsRememberedOnlyWhenItWorked() = runTest(dispatcher) {
        answer(HttpMethod.Put, "reviews/1/report", "", HttpStatusCode.TooManyRequests)
        val viewModel = opened()
        val tasty = viewModel.state.value.reviews.first { it.id == "1" }

        viewModel.report("1")
        advanceUntilIdle()

        assertTrue(viewModel.state.value.canReport(tasty))
        assertFalse(settings.hasReportedReview("1"))
        assertEquals("You're doing that too often. Please wait a while and try again.", viewModel.state.value.message)

        answer(HttpMethod.Put, "reviews/1/report", review("1", OTHER, 4, "Tasty"))
        viewModel.report("1")
        advanceUntilIdle()

        assertFalse(viewModel.state.value.canReport(tasty))
        assertTrue(settings.hasReportedReview("1"))
        assertEquals("Thanks for reporting. A moderator will take a look.", viewModel.state.value.message)
    }

    @Test
    fun aReviewAlreadyReportedIsNotReportedAgain() = runTest(dispatcher) {
        settings.markReportedReview("1")
        val viewModel = opened()
        val tasty = viewModel.state.value.reviews.first { it.id == "1" }

        assertFalse(viewModel.state.value.canReport(tasty))
        viewModel.report("1")
        advanceUntilIdle()

        assertTrue(requestsTo(HttpMethod.Put, "reviews/1/report").isEmpty())
    }

    @Test
    fun noOneReportsTheirOwnReview() = runTest(dispatcher) {
        reviewsAre(review("7", ME, 3, "Okay"))
        val viewModel = opened()

        assertFalse(viewModel.state.value.canReport(viewModel.state.value.myReview!!))
        viewModel.report("7")
        advanceUntilIdle()

        assertTrue(requestsTo(HttpMethod.Put, "reviews/7/report").isEmpty())
    }

    @Test
    fun aMessageIsShownOnce() = runTest(dispatcher) {
        answer(HttpMethod.Put, "reviews/1/report", review("1", OTHER, 4, "Tasty"))
        val viewModel = opened()
        viewModel.report("1")
        advanceUntilIdle()

        viewModel.onMessageShown()

        assertNull(viewModel.state.value.message)
    }

    private companion object {
        const val ME = "me-1"
        const val OTHER = "other-1"

        fun review(id: String, author: String, rating: Int, comment: String?, images: List<String> = emptyList()) =
            """{"id":"$id","shopId":"5","reviewerUid":"$author","rating":$rating,"isActive":true""" +
                (comment?.let { ""","comment":"$it"""" } ?: "") +
                ""","imageUrls":[${images.joinToString(",") { "\"$it\"" }}],"createdAt":"2026-10-03T16:42:24"}"""

        fun HttpRequestData.json(): JsonObject {
            val text = (body as? OutgoingContent.ByteArrayContent)?.bytes()?.decodeToString().orEmpty()
            return Json.parseToJsonElement(text).jsonObject
        }
    }
}
