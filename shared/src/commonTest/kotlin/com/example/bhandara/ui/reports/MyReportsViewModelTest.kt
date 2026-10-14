package com.example.bhandara.ui.reports

import com.example.bhandara.data.network.ApiClient
import com.example.bhandara.data.network.ReportApi
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
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * My reports (Profile → My activity), the same on Android and iOS: the user's reports with their status, filtered,
 * opened, and withdrawn while still in review. The backend is a fake.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class MyReportsViewModelTest {

    private val dispatcher = StandardTestDispatcher()

    @BeforeTest
    fun setUp() = Dispatchers.setMain(dispatcher)

    @AfterTest
    fun tearDown() = Dispatchers.resetMain()

    // ==================== Fakes ====================

    private val requests = mutableListOf<HttpRequestData>()
    private var mine = "[]"
    private var withdrawStatus = HttpStatusCode.NoContent

    // The fake backend answers on the test's dispatcher, so advanceUntilIdle() also waits for its answers
    private val client = ApiClient("https://api.test/", { "id-token" }, MockEngine(MockEngineConfig().apply {
        dispatcher = this@MyReportsViewModelTest.dispatcher
        addHandler { request ->
            requests += request
            val (status, body) = when (request.method) {
                HttpMethod.Get -> HttpStatusCode.OK to mine
                HttpMethod.Delete -> withdrawStatus to if (withdrawStatus == HttpStatusCode.NoContent) "" else """{"message":"Already dealt with"}"""
                else -> HttpStatusCode.NotFound to ""
            }
            respond(body, status, headersOf(HttpHeaders.ContentType, "application/json"))
        }
    }))

    private fun TestScope.opened(): MyReportsViewModel = MyReportsViewModel(ReportApi(client)).also { advanceUntilIdle() }

    private fun myReportsAre(vararg reports: String) {
        mine = reports.joinToString(",", "[", "]")
    }

    init {
        myReportsAre(
            report("SHOP", "1", "IN_REVIEW", reason = "WRONG_LOCATION"),
            report("SHOP", "2", "FIXED", reason = "PERMANENTLY_CLOSED"),
            report("REVIEW", "2", "NO_ACTION"),
        )
    }

    // ==================== Tests ====================

    @Test
    fun reportsAreLoadedWithHowManyLedToAFix() = runTest(dispatcher) {
        val state = opened().state.value

        assertEquals(3, state.reports.size)
        assertEquals(1, state.fixedCount)
        assertEquals(1, state.inReviewCount)
        assertEquals(2, state.closedCount)
        assertEquals("/api/reports/mine", requests.single().url.encodedPath)
    }

    @Test
    fun reportsCanBeFilteredByWhetherTheyAreStillInReview() = runTest(dispatcher) {
        val viewModel = opened()

        viewModel.onFilter(ReportsFilter.IN_REVIEW)
        assertEquals(listOf("SHOP:1"), viewModel.state.value.shown().map { it.key })

        viewModel.onFilter(ReportsFilter.CLOSED)
        assertEquals(listOf("SHOP:2", "REVIEW:2"), viewModel.state.value.shown().map { it.key })
    }

    @Test
    fun aShopReportAndAReviewReportWithTheSameIdAreDifferentReports() = runTest(dispatcher) {
        val viewModel = opened()
        val review = viewModel.state.value.reports.single { it.kind == "REVIEW" }

        viewModel.open(review)

        assertEquals("REVIEW", viewModel.state.value.open?.kind)
        viewModel.close()
        assertNull(viewModel.state.value.open)
    }

    @Test
    fun withdrawingAReportInReviewRemovesItAndGoesBackToTheList() = runTest(dispatcher) {
        val viewModel = opened()
        val pending = viewModel.state.value.reports.first { it.isInReview }
        viewModel.open(pending)

        viewModel.withdraw(pending)
        advanceUntilIdle()

        val delete = requests.last()
        assertEquals(HttpMethod.Delete, delete.method)
        assertEquals("/api/reports/SHOP/1", delete.url.encodedPath)
        val state = viewModel.state.value
        assertEquals(listOf("SHOP:2", "REVIEW:2"), state.reports.map { it.key })
        assertNull(state.open)
        assertTrue(state.withdrawn)

        viewModel.onMessageShown()
        assertFalse(viewModel.state.value.withdrawn)
    }

    @Test
    fun aReportAlreadyDealtWithIsNotSentToBeWithdrawn() = runTest(dispatcher) {
        val viewModel = opened()

        viewModel.withdraw(viewModel.state.value.reports.single { it.status == "FIXED" })
        advanceUntilIdle()

        assertTrue(requests.none { it.method == HttpMethod.Delete })
    }

    @Test
    fun whenWithdrawingFailsTheReportStaysAndTheUserSeesWhy() = runTest(dispatcher) {
        withdrawStatus = HttpStatusCode.Conflict
        val viewModel = opened()
        val pending = viewModel.state.value.reports.first { it.isInReview }
        viewModel.open(pending)

        viewModel.withdraw(pending)
        advanceUntilIdle()

        assertEquals(3, viewModel.state.value.reports.size)
        assertEquals(pending.key, viewModel.state.value.open?.key)
        assertNotNull(viewModel.state.value.message)
        assertFalse(viewModel.state.value.withdrawing)
    }

    @Test
    fun whenReportsCannotBeLoadedTheUserCanRetry() = runTest(dispatcher) {
        mine = "not json"
        val viewModel = opened()
        assertNotNull(viewModel.state.value.error)

        myReportsAre(report("FEAST", "7", "IN_REVIEW"))
        viewModel.load()
        advanceUntilIdle()

        assertNull(viewModel.state.value.error)
        assertEquals(listOf("FEAST:7"), viewModel.state.value.reports.map { it.key })
    }

    // ==================== Test data ====================

    private companion object {
        fun report(kind: String, id: String, status: String, reason: String? = null) =
            """{"kind":"$kind","reportId":"$id","status":"$status","reason":${reason?.let { "\"$it\"" } ?: "null"},""" +
                """"targetName":"Sai Amruttulya","shopId":"5","reporterCount":1,"createdAt":"2026-10-12T09:12:00"}"""
    }
}
