package com.example.bhandara.ui.feasts

import com.example.bhandara.data.network.ApiClient
import com.example.bhandara.data.network.FeastApi
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
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Feasts I posted (Profile → My activity), the same on Android and iOS: the user's feasts by status, ending one
 * early and taking one down. The backend is a fake.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class MyFeastsViewModelTest {

    private val dispatcher = StandardTestDispatcher()

    @BeforeTest
    fun setUp() = Dispatchers.setMain(dispatcher)

    @AfterTest
    fun tearDown() = Dispatchers.resetMain()

    // ==================== Fakes ====================

    private val requests = mutableListOf<HttpRequestData>()
    private var mine = "[]"
    private var actionStatus = HttpStatusCode.OK

    // The fake backend answers on the test's dispatcher, so advanceUntilIdle() also waits for its answers
    private val client = ApiClient("https://api.test/", { "id-token" }, MockEngine(MockEngineConfig().apply {
        dispatcher = this@MyFeastsViewModelTest.dispatcher
        addHandler { request ->
            requests += request
            val path = request.url.encodedPath
            val (status, body) = when {
                request.method == HttpMethod.Get && path == "/api/feasts/mine" -> HttpStatusCode.OK to mine
                actionStatus != HttpStatusCode.OK -> actionStatus to """{"message":"This feast has already ended"}"""
                request.method == HttpMethod.Post && path.endsWith("/end") ->
                    HttpStatusCode.OK to feast(path.removePrefix("/api/feasts/").removeSuffix("/end"), "Ended Langar", "2026-10-12", "PAST", endedEarly = true)
                request.method == HttpMethod.Delete -> HttpStatusCode.NoContent to ""
                else -> HttpStatusCode.NotFound to ""
            }
            respond(body, status, headersOf(HttpHeaders.ContentType, "application/json"))
        }
    }))

    private fun TestScope.opened(): MyFeastsViewModel = MyFeastsViewModel(FeastApi(client)).also { advanceUntilIdle() }

    private fun myFeastsAre(vararg feasts: String) {
        mine = feasts.joinToString(",", "[", "]")
    }

    // ==================== Tests ====================

    @Test
    fun feastsAreSplitIntoUpcomingSoonestFirstPastAndTakenDown() = runTest(dispatcher) {
        // The backend sends the latest date first
        myFeastsAre(
            feast("4", "Diwali Langar", "2026-11-01", "UPCOMING"),
            feast("3", "Ganesh Mandal", "2026-10-14", "UPCOMING"),
            feast("2", "Fake Feast", "2026-10-10", "TAKEN_DOWN"),
            feast("1", "Ganeshotsav Prasad", "2026-08-27", "PAST"),
        )

        val state = opened().state.value

        assertEquals(listOf("3", "4"), state.upcoming.map { it.id })
        assertEquals(listOf("1"), state.past.map { it.id })
        assertEquals(listOf("2"), state.takenDown.map { it.id })
        assertEquals(MyFeastsTab.UPCOMING, state.activeTab)
    }

    @Test
    fun withNothingUpcomingTheListOpensOnThePastOnes() = runTest(dispatcher) {
        myFeastsAre(feast("1", "Ganeshotsav Prasad", "2026-08-27", "PAST"))

        val viewModel = opened()
        assertEquals(MyFeastsTab.PAST, viewModel.state.value.activeTab)

        viewModel.onTab(MyFeastsTab.TAKEN_DOWN)
        assertEquals(MyFeastsTab.TAKEN_DOWN, viewModel.state.value.activeTab)
    }

    @Test
    fun endingAFeastEarlyMovesItToThePast() = runTest(dispatcher) {
        myFeastsAre(feast("3", "Ganesh Mandal", "2026-10-12", "UPCOMING"))
        val viewModel = opened()

        viewModel.endEarly("3")
        advanceUntilIdle()

        val request = requests.last()
        assertEquals(HttpMethod.Post, request.method)
        assertEquals("/api/feasts/3/end", request.url.encodedPath)
        val state = viewModel.state.value
        assertTrue(state.upcoming.isEmpty())
        assertEquals(listOf("3"), state.past.map { it.id })
        assertNotNull(state.past.single().endedEarlyAt)
        assertEquals(MyFeastsDone.ENDED, state.done)
        assertNull(state.busyId)
    }

    @Test
    fun takingAFeastDownRemovesItFromTheList() = runTest(dispatcher) {
        myFeastsAre(feast("3", "Ganesh Mandal", "2026-10-14", "UPCOMING"), feast("1", "Ganeshotsav Prasad", "2026-08-27", "PAST"))
        val viewModel = opened()

        viewModel.takeDown("3")
        advanceUntilIdle()

        assertEquals("/api/feasts/3", requests.last { it.method == HttpMethod.Delete }.url.encodedPath)
        assertEquals(listOf("1"), viewModel.state.value.feasts.map { it.id })
        assertEquals(MyFeastsDone.TAKEN_DOWN, viewModel.state.value.done)

        viewModel.onMessageShown()
        assertNull(viewModel.state.value.done)
    }

    @Test
    fun whenTheBackendRefusesTheFeastStaysAndTheUserSeesWhy() = runTest(dispatcher) {
        myFeastsAre(feast("3", "Ganesh Mandal", "2026-10-14", "UPCOMING"))
        val viewModel = opened()
        actionStatus = HttpStatusCode.Conflict

        viewModel.endEarly("3")
        advanceUntilIdle()

        assertEquals(listOf("3"), viewModel.state.value.upcoming.map { it.id })
        assertNotNull(viewModel.state.value.message)
        assertNull(viewModel.state.value.done)
    }

    @Test
    fun whenTheFeastsCannotBeLoadedTheUserCanRetry() = runTest(dispatcher) {
        mine = "not json"
        val viewModel = opened()
        assertNotNull(viewModel.state.value.error)

        myFeastsAre(feast("1", "Ganeshotsav Prasad", "2026-08-27", "PAST"))
        viewModel.load()
        advanceUntilIdle()

        assertNull(viewModel.state.value.error)
        assertEquals(1, viewModel.state.value.feasts.size)
    }

    // ==================== Test data ====================

    private companion object {
        fun feast(id: String, name: String, date: String, status: String, endedEarly: Boolean = false) =
            """{"id":"$id","organizerName":"$name","menuItems":["Puri"],"feastDate":"$date","startTime":"12:00:00",""" +
                """"endTime":"15:00:00","latitude":18.58,"longitude":73.88,"estimatedCapacity":100,"status":"$status"""" +
                (if (endedEarly) ""","endedEarlyAt":"2026-10-12T13:00:00"""" else "") + "}"
    }
}
