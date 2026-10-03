package com.example.bhandara.data.network

import com.example.bhandara.data.models.api.ReviewRequest
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.MockRequestHandleScope
import io.ktor.client.engine.mock.respond
import io.ktor.client.request.HttpRequestData
import io.ktor.client.request.HttpResponseData
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.http.content.OutgoingContent
import io.ktor.http.headersOf
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.test.fail

/**
 * Talking to the backend, from both Android and iOS: requests go out right, answers come back as data, and every
 * kind of failure becomes a message the user can understand, using the backend's own wording when it is meant
 * for users. Runs against a fake server (Ktor's MockEngine).
 */
class ApiClientTest {

    private val requests = mutableListOf<HttpRequestData>()

    private fun client(
        token: String? = "id-token",
        reply: suspend MockRequestHandleScope.(HttpRequestData) -> HttpResponseData,
    ): ApiClient {
        val engine = MockEngine { request ->
            requests += request
            reply(request)
        }
        return ApiClient(baseUrl = "https://api.test/", idTokenProvider = { token }, engine = engine)
    }

    private fun MockRequestHandleScope.json(body: String, status: HttpStatusCode = HttpStatusCode.OK) =
        respond(body, status, headersOf(HttpHeaders.ContentType, "application/json"))

    private fun <T> ApiResult<T>.value(): T = when (this) {
        is ApiResult.Success -> value
        is ApiResult.Failure -> fail("Expected success but got $error")
    }

    private fun <T> ApiResult<T>.error(): ApiError = when (this) {
        is ApiResult.Success -> fail("Expected a failure but got $value")
        is ApiResult.Failure -> error
    }

    private val shopJson = """
        {"id": "5", "shopName": "Sharma Chaat", "latitude": 18.58, "longitude": 73.88,
         "averageRating": 4.5, "someFieldAddedLater": true}
    """

    // ==================== Successful calls ====================

    @Test
    fun aShopIsReadFromTheResponseEvenIfTheBackendAddsNewFields() = runTest {
        val shop = ShopApi(client { json(shopJson) }).getShop("5").value()

        assertEquals("Sharma Chaat", shop.shopName)
        assertEquals("https://api.test/api/localshops/5", requests.single().url.toString())
        assertEquals(HttpMethod.Get, requests.single().method)
    }

    @Test
    fun theSignedInUsersTokenIsSentAndNothingWhenSignedOut() = runTest {
        ShopApi(client(token = "id-token") { json(shopJson) }).getShop("5")
        ShopApi(client(token = null) { json(shopJson) }).getShop("5")

        assertEquals("Bearer id-token", requests[0].headers[HttpHeaders.Authorization])
        assertNull(requests[1].headers[HttpHeaders.Authorization])
    }

    @Test
    fun aReviewIsSentAsJson() = runTest {
        val api = ReviewApi(client {
            json("""{"id": "9", "shopId": "5", "reviewerUid": "me", "rating": 4, "comment": "Tasty"}""")
        })

        val saved = api.createReview(ReviewRequest(shopId = "5", rating = 4, comment = "Tasty")).value()

        assertEquals("9", saved.id)
        val request = requests.single()
        assertEquals(HttpMethod.Post, request.method)
        assertEquals("https://api.test/api/reviews", request.url.toString())
        val body = (request.body as OutgoingContent.ByteArrayContent).bytes().decodeToString()
        assertTrue(body.contains("\"rating\":4") && body.contains("\"comment\":\"Tasty\""), body)
    }

    @Test
    fun deletingAReviewNeedsNothingBack() = runTest {
        val result = ReviewApi(client { json("""{"message": "Review deleted successfully"}""") }).deleteReview("9")

        assertIs<ApiResult.Success<Unit>>(result)
        assertEquals(HttpMethod.Delete, requests.single().method)
    }

    // ==================== Failures the backend explains to the user ====================

    @Test
    fun aConflictShowsTheBackendsExplanation() = runTest {
        val error = ReviewApi(client {
            json("""{"message": "You have already reviewed this shop. Please update your existing review.",
                     "statusCode": 409}""", HttpStatusCode.Conflict)
        }).createReview(ReviewRequest(shopId = "5", rating = 4)).error()

        assertEquals(ApiError.Kind.CONFLICT, error.kind)
        assertEquals("You have already reviewed this shop. Please update your existing review.", error.message)
    }

    @Test
    fun invalidInputShowsWhatIsWrongWithTheField() = runTest {
        val error = ReviewApi(client {
            json("""{"message": "Validation failed", "errors": {"rating": "Rating must be between 1 and 5"},
                     "statusCode": 400}""", HttpStatusCode.BadRequest)
        }).createReview(ReviewRequest(shopId = "5", rating = 9)).error()

        assertEquals(ApiError.Kind.INVALID, error.kind)
        assertEquals("Rating must be between 1 and 5", error.message)
        assertEquals(mapOf("rating" to "Rating must be between 1 and 5"), error.fieldErrors)
    }

    @Test
    fun aRefusalShowsTheBackendsReason() = runTest {
        val error = ReviewApi(client {
            json("""{"message": "Your review for this shop was removed by a moderator, so you can't post another one."}""",
                HttpStatusCode.Forbidden)
        }).createReview(ReviewRequest(shopId = "5", rating = 1)).error()

        assertEquals(ApiError.Kind.FORBIDDEN, error.kind)
        assertEquals("Your review for this shop was removed by a moderator, so you can't post another one.", error.message)
    }

    @Test
    fun aTemporarilyUnavailableServiceShowsTheBackendsMessage() = runTest {
        val error = ShopApi(client {
            json("""{"message": "The AI service is unavailable right now. Please try again in a moment."}""",
                HttpStatusCode.ServiceUnavailable)
        }).getShop("5").error()

        assertEquals(ApiError.Kind.UNAVAILABLE, error.kind)
        assertEquals("The AI service is unavailable right now. Please try again in a moment.", error.message)
    }

    // ==================== Failures with a friendly message instead ====================

    @Test
    fun somethingThatNoLongerExistsGetsAFriendlyMessageNotTheBackendsTechnicalOne() = runTest {
        val error = ShopApi(client {
            json("""{"message": "Local shop not found with ID: 5"}""", HttpStatusCode.NotFound)
        }).getShop("5").error()

        assertEquals(ApiError.Kind.NOT_FOUND, error.kind)
        assertEquals("This is no longer available. It may have been removed.", error.message)
    }

    @Test
    fun doingSomethingTooOftenSaysToWait() = runTest {
        val error = ShopApi(client {
            json("""{"error": "Too Many Requests", "message": "Rate limit exceeded. Please try again later."}""",
                HttpStatusCode.TooManyRequests)
        }).reportShop("5").error()

        assertEquals(ApiError.Kind.RATE_LIMITED, error.kind)
        assertEquals("You're doing that too often. Please wait a while and try again.", error.message)
    }

    @Test
    fun anExpiredSignInAsksToSignInAgain() = runTest {
        val error = ShopApi(client { json("", HttpStatusCode.Unauthorized) }).deactivateShop("5").error()

        assertEquals(ApiError.Kind.UNAUTHORIZED, error.kind)
        assertEquals("Please sign in again.", error.message)
    }

    @Test
    fun aServerCrashNeverShowsItsInternals() = runTest {
        val error = ShopApi(client {
            json("java.lang.NullPointerException at com.localfeast.backend.Foo(Foo.java:42)",
                HttpStatusCode.InternalServerError)
        }).getShop("5").error()

        assertEquals(ApiError.Kind.SERVER, error.kind)
        assertEquals("Something went wrong on our side. Please try again.", error.message)
    }

    @Test
    fun noConnectionSaysSo() = runTest {
        val error = ShopApi(client { throw RuntimeException("Unable to resolve host api.test") }).getShop("5").error()

        assertEquals(ApiError.Kind.NETWORK, error.kind)
        assertEquals("No internet connection. Check your connection and try again.", error.message)
    }

    @Test
    fun anAnswerTheAppCannotReadIsAFailureNotACrash() = runTest {
        val error = ShopApi(client { json("<html>Bad gateway</html>") }).getShop("5").error()

        assertEquals(ApiError.Kind.UNEXPECTED_RESPONSE, error.kind)
        assertEquals("Something went wrong. Please try again.", error.message)
    }
}
