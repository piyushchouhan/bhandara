package com.example.bhandara.ui.fssai

import com.example.bhandara.data.network.ApiClient
import com.example.bhandara.data.network.FssaiApi
import com.example.bhandara.photos.CompressedPhoto
import com.example.bhandara.photos.PickedPdf
import com.example.bhandara.photos.PickedPhoto
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.MockEngineConfig
import io.ktor.client.engine.mock.respond
import io.ktor.client.request.HttpRequestData
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.content.OutgoingContent
import io.ktor.http.headersOf
import io.ktor.utils.io.ByteChannel
import io.ktor.utils.io.toByteArray
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
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
 * Adding an FSSAI licence (5a): the owner adds the certificate (1–2 photos, or a PDF), AI reads the number and expiry
 * off it for them to check, and "Submit for check" sends it — or, while adding a shop, keeps it to send with the
 * shop. The backend is a fake.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class FssaiViewModelTest {

    private val dispatcher = StandardTestDispatcher()

    @BeforeTest
    fun setUp() = Dispatchers.setMain(dispatcher)

    @AfterTest
    fun tearDown() = Dispatchers.resetMain()

    // ==================== Fakes ====================

    private class Photo(val name: String, val readable: Boolean = true) : PickedPhoto {
        override val preview: Any = name
        override suspend fun compressed() = if (readable) CompressedPhoto(name.encodeToByteArray(), "jpg", "image/jpeg") else null
    }

    private val today = LocalDate(2026, 10, 15)
    private val requests = mutableListOf<HttpRequestData>()
    private var readAnswer = HttpStatusCode.OK to """{"licenceNumber":"11523026000123","validTill":"2028-03-12","kind":"LICENCE"}"""
    private var submitAnswer = HttpStatusCode.Created to
        """{"id":1,"shopId":42,"licenceNumber":"11523026000123","kind":"LICENCE","validTill":"2028-03-12","status":"PENDING_REVIEW"}"""

    private val client = ApiClient("https://api.test/", { "id-token" }, MockEngine(MockEngineConfig().apply {
        dispatcher = this@FssaiViewModelTest.dispatcher
        addHandler { request ->
            requests += request
            val (status, body) = when (request.url.encodedPath) {
                "/api/fssai/read" -> readAnswer
                "/api/fssai/shops/42" -> submitAnswer
                else -> HttpStatusCode.NotFound to ""
            }
            respond(body, status, headersOf(HttpHeaders.ContentType, "application/json"))
        }
    }))

    private fun viewModel(shopId: String? = "42", initial: FssaiForm? = null) = FssaiViewModel(FssaiApi(client), shopId, initial, today = { today })

    private fun requestsTo(path: String) = requests.filter { it.url.encodedPath == path }

    private suspend fun HttpRequestData.sent(): String = (body as OutgoingContent.WriteChannelContent).let { content ->
        val channel = ByteChannel()
        content.writeTo(channel)
        channel.close()
        channel.toByteArray().decodeToString()
    }

    // ==================== Reading the certificate ====================

    @Test
    fun theNumberAndExpiryAreReadOffTheCertificateForTheOwnerToCheck() = runTest(dispatcher) {
        val viewModel = viewModel()

        viewModel.addPages(listOf(Photo("front")))
        assertTrue(viewModel.state.value.reading)
        advanceUntilIdle()

        val state = viewModel.state.value
        assertFalse(state.reading)
        assertEquals("11523026000123", state.form.number)
        assertEquals(LocalDate(2028, 3, 12), state.form.validTill)
        assertTrue(state.readFromPhoto)
    }

    @Test
    fun whatTheOwnerTypedStays() = runTest(dispatcher) {
        val viewModel = viewModel()
        viewModel.onNumberChange("1152 3026 0001 99")

        viewModel.addPages(listOf(Photo("front")))
        advanceUntilIdle()

        assertEquals("11523026000199", viewModel.state.value.form.number)
        assertEquals(LocalDate(2028, 3, 12), viewModel.state.value.form.validTill)
    }

    @Test
    fun aSecondPageIsReadTogetherWithTheFirst() = runTest(dispatcher) {
        val viewModel = viewModel()
        viewModel.addPages(listOf(Photo("front")))
        advanceUntilIdle()

        viewModel.addPages(listOf(Photo("back")))
        advanceUntilIdle()

        val sent = requestsTo("/api/fssai/read").last().sent()
        assertEquals(2, Regex("name=\"?pages\"?").findAll(sent).count(), sent)
    }

    @Test
    fun aCertificateHasAtMostTwoPages() = runTest(dispatcher) {
        val viewModel = viewModel()

        viewModel.addPages(listOf(Photo("1"), Photo("2"), Photo("3")))
        advanceUntilIdle()

        assertEquals(2, viewModel.state.value.form.pages.size)
    }

    @Test
    fun ifItCantBeReadTheOwnerIsToldAndCanTypeItIn() = runTest(dispatcher) {
        readAnswer = HttpStatusCode.BadRequest to """{"message":"This is a menu, not a certificate"}"""
        val viewModel = viewModel()

        viewModel.addPages(listOf(Photo("menu")))
        advanceUntilIdle()

        val state = viewModel.state.value
        assertEquals("This is a menu, not a certificate", state.readProblem)
        assertFalse(state.readFromPhoto)
        assertEquals("", state.form.number)
    }

    @Test
    fun aCertificateWithNothingReadableSaysToTypeIt() = runTest(dispatcher) {
        readAnswer = HttpStatusCode.OK to """{"licenceNumber":null,"validTill":null}"""
        val viewModel = viewModel()

        viewModel.addPages(listOf(Photo("blurry")))
        advanceUntilIdle()

        assertNotNull(viewModel.state.value.readProblem)
        assertFalse(viewModel.state.value.readFromPhoto)
    }

    @Test
    fun removingAPageKeepsWhatWasFilledIn() = runTest(dispatcher) {
        val viewModel = viewModel()
        viewModel.addPages(listOf(Photo("front"), Photo("back")))
        advanceUntilIdle()

        viewModel.removePage(1)

        assertEquals(listOf("front"), viewModel.state.value.form.pages.map { (it as Photo).name })
        assertEquals("11523026000123", viewModel.state.value.form.number)
    }

    @Test
    fun theNumberIsOnlyDigitsAndAtMostFourteen() {
        val viewModel = viewModel()

        viewModel.onNumberChange("1152-3026 0001 2345 67")

        assertEquals("11523026000123", viewModel.state.value.form.number)
    }

    // ==================== Sending it ====================

    @Test
    fun itIsSentWithTheNumberExpiryAndPages() = runTest(dispatcher) {
        val viewModel = viewModel()
        viewModel.addPages(listOf(Photo("front")))
        advanceUntilIdle()

        viewModel.submit()
        advanceUntilIdle()

        assertTrue(viewModel.state.value.done)
        val sent = requestsTo("/api/fssai/shops/42").single().sent()
        assertTrue("11523026000123" in sent && "2028-03-12" in sent, sent)
        assertEquals(1, Regex("name=\"?pages\"?").findAll(sent).count(), sent)
    }

    @Test
    fun aPdfIsSentAsAPdf() = runTest(dispatcher) {
        val viewModel = viewModel()
        viewModel.addPages(listOf(PickedPdf("licence.pdf") { "%PDF-1.4".encodeToByteArray() }))
        advanceUntilIdle()

        viewModel.submit()
        advanceUntilIdle()

        assertTrue("application/pdf" in requestsTo("/api/fssai/shops/42").single().sent())
    }

    @Test
    fun whatIsMissingIsSaidAndNothingIsSent() = runTest(dispatcher) {
        readAnswer = HttpStatusCode.OK to """{"licenceNumber":"1152302600","validTill":null}"""
        val viewModel = viewModel()

        viewModel.submit()
        assertEquals("Add a photo or PDF of the certificate.", viewModel.state.value.error)

        viewModel.addPages(listOf(Photo("front")))
        advanceUntilIdle()
        viewModel.onNumberChange("1152302600")
        viewModel.submit()
        assertEquals("The licence number has 14 digits.", viewModel.state.value.error)

        viewModel.onNumberChange("11523026000123")
        viewModel.submit()
        assertEquals("Add the date it's valid till.", viewModel.state.value.error)

        assertTrue(requestsTo("/api/fssai/shops/42").isEmpty())
    }

    @Test
    fun anExpiredLicenceIsntSent() = runTest(dispatcher) {
        val viewModel = viewModel()
        viewModel.addPages(listOf(Photo("front")))
        advanceUntilIdle()

        viewModel.setValidTill(LocalDate(2026, 10, 14))
        viewModel.submit()
        advanceUntilIdle()

        assertEquals("This licence has expired.", viewModel.state.value.error)
        assertTrue(requestsTo("/api/fssai/shops/42").isEmpty())
    }

    @Test
    fun aRefusalIsShownAndItCanBeTriedAgain() = runTest(dispatcher) {
        submitAnswer = HttpStatusCode.TooManyRequests to """{"message":"Too many licences sent today. Please try again tomorrow."}"""
        val viewModel = viewModel()
        viewModel.addPages(listOf(Photo("front")))
        advanceUntilIdle()

        viewModel.submit()
        advanceUntilIdle()

        assertFalse(viewModel.state.value.done)
        assertFalse(viewModel.state.value.submitting)
        assertNotNull(viewModel.state.value.error)
    }

    @Test
    fun anUnreadablePageStopsItBeforeAnythingIsSent() = runTest(dispatcher) {
        val viewModel = viewModel(initial = FssaiForm(listOf(Photo("broken", readable = false)), "11523026000123", LocalDate(2028, 3, 12)))

        viewModel.submit()
        advanceUntilIdle()

        assertNotNull(viewModel.state.value.error)
        assertTrue(requests.isEmpty())
    }

    @Test
    fun whileAddingAShopItIsKeptToSendWithTheShop() = runTest(dispatcher) {
        val viewModel = viewModel(shopId = null)
        viewModel.addPages(listOf(Photo("front")))
        advanceUntilIdle()

        viewModel.submit()
        advanceUntilIdle()

        assertTrue(viewModel.state.value.done)
        assertTrue(requestsTo("/api/fssai/shops/42").isEmpty())
        assertEquals("11523026000123", viewModel.state.value.form.number)
    }

    @Test
    fun comingBackToChangeItShowsWhatWasFilledIn() {
        val earlier = FssaiForm(listOf(Photo("front")), "11523026000123", LocalDate(2028, 3, 12))

        val state = viewModel(shopId = null, initial = earlier).state.value

        assertEquals(earlier, state.form)
        assertNull(state.error)
    }

    // ==================== How it's shown ====================

    @Test
    fun theNumberIsShownInGroupsAsOnTheCertificate() {
        val shown = FssaiNumberGroups.filter(androidx.compose.ui.text.AnnotatedString("11523026000123"))

        assertEquals("1152 3026 0001 23", shown.text.text)
        assertEquals(17, shown.offsetMapping.originalToTransformed(14))
        assertEquals(6, shown.offsetMapping.originalToTransformed(5))
        assertEquals(14, shown.offsetMapping.transformedToOriginal(17))
        assertEquals(4, shown.offsetMapping.transformedToOriginal(5))
    }

    @Test
    fun datesReadAsOnTheCertificate() {
        assertEquals("12 Mar 2028", fssaiDate(LocalDate(2028, 3, 12)))
        assertEquals("12 Mar 2028", fssaiDate("2028-03-12"))
        assertEquals("1152 3026 0001 23", fssaiNumber("11523026000123"))
    }
}
