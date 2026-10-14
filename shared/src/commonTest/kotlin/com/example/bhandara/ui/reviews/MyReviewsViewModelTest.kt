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
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * My reviews (Profile → My activity), the same on Android and iOS: the user's reviews with a summary, filtered by
 * status (published, or removed by a moderator), sorted, and deleted. The backend is a fake.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class MyReviewsViewModelTest {

    private val dispatcher = StandardTestDispatcher()

    @BeforeTest
    fun setUp() = Dispatchers.setMain(dispatcher)

    @AfterTest
    fun tearDown() = Dispatchers.resetMain()

    // ==================== Fakes ====================

    private val requests = mutableListOf<HttpRequestData>()
    private var mine: Pair<HttpStatusCode, String> = HttpStatusCode.OK to "[]"
    private var deleteStatus = HttpStatusCode.OK

    // The fake backend answers on the test's dispatcher, so advanceUntilIdle() also waits for its answers
    private val client = ApiClient("https://api.test/", { "id-token" }, MockEngine(MockEngineConfig().apply {
        dispatcher = this@MyReviewsViewModelTest.dispatcher
        addHandler { request ->
            requests += request
            val (status, body) = when {
                request.method == HttpMethod.Get && request.url.encodedPath == "/api/reviews/mine" -> mine
                request.method == HttpMethod.Delete -> deleteStatus to
                    if (deleteStatus == HttpStatusCode.OK) """{"message":"Review deleted successfully"}""" else """{"message":"Review not found"}"""
                else -> HttpStatusCode.NotFound to ""
            }
            respond(body, status, headersOf(HttpHeaders.ContentType, "application/json"))
        }
    }))

    private fun myReviewsAre(vararg reviews: String) {
        mine = HttpStatusCode.OK to "[${reviews.joinToString(",")}]"
    }

    private fun TestScope.opened(): MyReviewsViewModel = MyReviewsViewModel(ReviewApi(client)).also { advanceUntilIdle() }

    // ==================== Tests ====================

    @Test
    fun theUsersReviewsAreLoadedWithASummary() = runTest(dispatcher) {
        myReviewsAre(review("3", 3, "Sai Amruttulya"), review("2", 4, "Annapurna Misal"), review("1", 5, "Shri Krishna"))

        val state = opened().state.value

        assertFalse(state.loading)
        assertEquals(listOf("3", "2", "1"), state.shown().map { it.id })
        assertEquals("Sai Amruttulya", state.shown().first().shopName)
        assertEquals(4.0, state.averageGiven)
        assertEquals(3, state.publishedCount)
        assertEquals(0, state.removedCount)
        assertTrue(requests.single().headers[HttpHeaders.Authorization]!!.endsWith("id-token"))
    }

    @Test
    fun withNoReviewsTheListIsEmptyAndThereIsNoAverage() = runTest(dispatcher) {
        val state = opened().state.value

        assertTrue(state.reviews.isEmpty())
        assertNull(state.averageGiven)
        assertNull(state.error)
    }

    @Test
    fun whenTheReviewsCannotBeLoadedTheUserSeesWhyAndCanRetry() = runTest(dispatcher) {
        mine = HttpStatusCode.InternalServerError to """{"message":"Something went wrong"}"""
        val viewModel = opened()
        // The client's own wording for a server error
        assertNotNull(viewModel.state.value.error)

        myReviewsAre(review("1", 5, "Shri Krishna"))
        viewModel.load()
        advanceUntilIdle()

        assertNull(viewModel.state.value.error)
        assertEquals(1, viewModel.state.value.reviews.size)
    }

    @Test
    fun reviewsAModeratorRemovedCanBeListedOnTheirOwn() = runTest(dispatcher) {
        myReviewsAre(review("2", 1, "Hidden Snacks", removed = true), review("1", 5, "Shri Krishna"))
        val viewModel = opened()
        assertEquals(1, viewModel.state.value.publishedCount)
        assertEquals(1, viewModel.state.value.removedCount)

        viewModel.onFilter(MyReviewsFilter.PUBLISHED)
        assertEquals(listOf("1"), viewModel.state.value.shown().map { it.id })

        viewModel.onFilter(MyReviewsFilter.REMOVED)
        assertEquals(listOf("2"), viewModel.state.value.shown().map { it.id })
        assertTrue(viewModel.state.value.shown().single().isRemoved)
    }

    @Test
    fun reviewsCanBeSortedByTheStarsGiven() = runTest(dispatcher) {
        myReviewsAre(review("3", 3, "C"), review("2", 5, "B"), review("1", 1, "A"))
        val viewModel = opened()

        viewModel.onSort(MyReviewsSort.HIGHEST)
        assertEquals(listOf("2", "3", "1"), viewModel.state.value.shown().map { it.id })

        viewModel.onSort(MyReviewsSort.LOWEST)
        assertEquals(listOf("1", "3", "2"), viewModel.state.value.shown().map { it.id })

        viewModel.onSort(MyReviewsSort.NEWEST)
        assertEquals(listOf("3", "2", "1"), viewModel.state.value.shown().map { it.id })
    }

    @Test
    fun aDeletedReviewLeavesTheListAndTheUserIsTold() = runTest(dispatcher) {
        myReviewsAre(review("2", 4, "Annapurna Misal"), review("1", 5, "Shri Krishna"))
        val viewModel = opened()

        viewModel.delete("2")
        advanceUntilIdle()

        assertEquals("/api/reviews/2", requests.last { it.method == HttpMethod.Delete }.url.encodedPath)
        assertEquals(listOf("1"), viewModel.state.value.shown().map { it.id })
        assertTrue(viewModel.state.value.deleted)
        assertEquals(5.0, viewModel.state.value.averageGiven)

        viewModel.onMessageShown()
        assertFalse(viewModel.state.value.deleted)
    }

    @Test
    fun whenDeletingFailsTheReviewStaysAndTheUserSeesWhy() = runTest(dispatcher) {
        myReviewsAre(review("1", 5, "Shri Krishna"))
        deleteStatus = HttpStatusCode.NotFound
        val viewModel = opened()

        viewModel.delete("1")
        advanceUntilIdle()

        assertEquals(1, viewModel.state.value.reviews.size)
        assertFalse(viewModel.state.value.deleted)
        assertNotNull(viewModel.state.value.message)
        assertNull(viewModel.state.value.deletingId)
    }

    @Test
    fun theRemovedFilterFallsBackToAllOnceNothingIsRemoved() = runTest(dispatcher) {
        myReviewsAre(review("2", 1, "Hidden Snacks", removed = true), review("1", 5, "Shri Krishna"))
        val viewModel = opened()
        viewModel.onFilter(MyReviewsFilter.REMOVED)

        // e.g. the moderator's removal was undone and the list reloaded
        myReviewsAre(review("1", 5, "Shri Krishna"))
        viewModel.load()
        advanceUntilIdle()

        assertEquals(MyReviewsFilter.ALL, viewModel.state.value.activeFilter)
        assertEquals(listOf("1"), viewModel.state.value.shown().map { it.id })
    }

    @Test
    fun reviewsSayHowLongAgoTheyWereWritten() {
        val today = LocalDate(2026, 10, 12)

        assertEquals(ReviewAge.Today, reviewAge("2026-10-12T08:15:00", today))
        assertEquals(ReviewAge.Today, reviewAge("2026-10-13T00:05:00", today)) // the server's clock a little ahead
        assertEquals(ReviewAge.Yesterday, reviewAge("2026-10-11T22:00:00", today))
        assertEquals(ReviewAge.DaysAgo(2), reviewAge("2026-10-10T12:00:00", today))
        assertEquals(ReviewAge.DaysAgo(6), reviewAge("2026-10-06T12:00:00", today))
        assertEquals(ReviewAge.WeeksAgo(1), reviewAge("2026-10-05T12:00:00", today))
        assertEquals(ReviewAge.WeeksAgo(4), reviewAge("2026-09-13T12:00:00", today))
        assertEquals(ReviewAge.On("12 Sep 2026"), reviewAge("2026-09-12T12:00:00", today))
        assertNull(reviewAge(null, today))
        assertNull(reviewAge("not a date", today))
    }

    // ==================== Test data ====================

    private companion object {
        fun review(id: String, rating: Int, shopName: String, removed: Boolean = false) =
            """{"id":"$id","shopId":"s$id","reviewerUid":"me","rating":$rating,"comment":"Nice","shopName":"$shopName",""" +
                """"shopArea":"Kothrud","removedByModerator":$removed,"isActive":${!removed},"createdAt":"2026-10-0${id}T10:00:00"}"""
    }
}
