package com.example.bhandara.ui.claim

import com.example.bhandara.data.location.DeviceLocation
import com.example.bhandara.data.location.GeoLocation
import com.example.bhandara.data.location.LocationService
import com.example.bhandara.data.location.TimedLocation
import com.example.bhandara.data.network.ApiClient
import com.example.bhandara.data.network.ClaimApi
import com.example.bhandara.photos.CompressedPhoto
import com.example.bhandara.photos.PhotoUploadException
import com.example.bhandara.photos.PickedPhoto
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
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.double
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.long
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Claiming a shop the platform listed, the same on Android and iOS. The phone's location, photo uploads and the
 * backend are fakes.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class ClaimShopViewModelTest {

    private val dispatcher = StandardTestDispatcher()

    @BeforeTest
    fun setUp() = Dispatchers.setMain(dispatcher)

    @AfterTest
    fun tearDown() = Dispatchers.resetMain()

    private class FakePhone(var permitted: Boolean = true) : DeviceLocation {
        override fun hasPermission() = permitted
        override suspend fun current() = GeoLocation(18.5823, 73.8845)
        override suspend fun lastKnown(): TimedLocation? = null
    }

    private data class Photo(val name: String) : PickedPhoto {
        override val preview: Any = name
        override suspend fun compressed(): CompressedPhoto? = null
    }

    private val phone = FakePhone()
    private val uploaded = mutableListOf<List<String>>()
    private var uploadFails = false
    private val requests = mutableListOf<HttpRequestData>()
    private var claimStatus = HttpStatusCode.Created
    private var claimBody = """{"id":1,"shopId":22,"status":"APPROVED","distanceMeters":12.0}"""

    private val client = ApiClient("https://api.test/", { "id-token" }, MockEngine(MockEngineConfig().apply {
        dispatcher = this@ClaimShopViewModelTest.dispatcher
        addHandler { request ->
            requests += request
            respond(claimBody, claimStatus, headersOf(HttpHeaders.ContentType, "application/json"))
        }
    }))

    private fun viewModel() = ClaimShopViewModel(
        "22",
        ClaimApi(client),
        uploadPhotos = { photos, onProgress ->
            uploaded += photos.map { (it as Photo).name }
            if (uploadFails) throw PhotoUploadException("Permission denied")
            onProgress(100)
            photos.map { "https://img.test/${(it as Photo).name}.webp" }
        },
        LocationService(phone, nowMillis = { 0 }),
    )

    private fun TestScope.submitted(viewModel: ClaimShopViewModel) = viewModel.also {
        it.submit()
        advanceUntilIdle()
    }

    @Test
    fun aClaimSendsThePhotosAndWhereTheUserIs() = runTest(dispatcher) {
        val viewModel = viewModel()
        viewModel.addPhotos(listOf(Photo("front"), Photo("board")))
        viewModel.onPhoneChange("98765 43210")

        submitted(viewModel)

        val body = Json.parseToJsonElement((requests.single().body as OutgoingContent.ByteArrayContent).bytes().decodeToString()).jsonObject
        assertEquals("/api/shop-claims", requests.single().url.encodedPath)
        assertEquals(22, body["shopId"]!!.jsonPrimitive.long)
        assertEquals(listOf("https://img.test/front.webp", "https://img.test/board.webp"), body["imageUrls"]!!.jsonArray.map { it.jsonPrimitive.content })
        assertEquals(18.5823, body["latitude"]!!.jsonPrimitive.double)
        assertEquals("9876543210", body["ownerPhone"]!!.jsonPrimitive.content)
        assertEquals(ClaimOutcome.APPROVED, viewModel.state.value.outcome)
        assertFalse(viewModel.state.value.submitting)
    }

    @Test
    fun eachDecisionIsShownForWhatItIs() = runTest(dispatcher) {
        claimBody = """{"id":1,"shopId":22,"status":"PENDING_REVIEW"}"""
        val inReview = viewModel().apply { addPhotos(listOf(Photo("front"))) }
        assertEquals(ClaimOutcome.IN_REVIEW, submitted(inReview).state.value.outcome)

        claimBody = """{"id":2,"shopId":22,"status":"REJECTED","decisionReason":"You seem to be far from the shop"}"""
        val rejected = viewModel().apply { addPhotos(listOf(Photo("front"))) }
        submitted(rejected)
        assertEquals(ClaimOutcome.REJECTED, rejected.state.value.outcome)
        assertEquals("You seem to be far from the shop", rejected.state.value.decision?.decisionReason)
    }

    @Test
    fun afterARejectionTheUserCanTryAgainWithNewPhotos() = runTest(dispatcher) {
        claimBody = """{"id":2,"shopId":22,"status":"REJECTED"}"""
        val viewModel = viewModel()
        viewModel.addPhotos(listOf(Photo("blurry")))
        viewModel.onPhoneChange("9876543210")
        submitted(viewModel)

        viewModel.tryAgain()

        assertNull(viewModel.state.value.decision)
        assertTrue(viewModel.state.value.photos.isEmpty())
        assertEquals("9876543210", viewModel.state.value.phone)
    }

    @Test
    fun aClaimNeedsAPhotoOfTheShop() = runTest(dispatcher) {
        val viewModel = viewModel()

        submitted(viewModel)

        assertEquals("Take at least one photo of the shop front", viewModel.state.value.error)
        assertTrue(requests.isEmpty())
    }

    @Test
    fun atMostThreePhotos() {
        val viewModel = viewModel()

        viewModel.addPhotos((1..5).map { Photo("p$it") })

        assertEquals(3, viewModel.state.value.photos.size)
        assertFalse(viewModel.state.value.canAddPhotos)
    }

    @Test
    fun aWrongPhoneNumberIsCaughtBeforeSending() = runTest(dispatcher) {
        val viewModel = viewModel()
        viewModel.addPhotos(listOf(Photo("front")))
        viewModel.onPhoneChange("12345")

        submitted(viewModel)

        assertEquals("Enter a phone number with 10 to 15 digits", viewModel.state.value.error)
        assertTrue(uploaded.isEmpty())
    }

    @Test
    fun nothingIsUploadedOrSentWithoutTheUsersLocation() = runTest(dispatcher) {
        phone.permitted = false
        val viewModel = viewModel()
        viewModel.addPhotos(listOf(Photo("front")))

        submitted(viewModel)

        assertTrue(uploaded.isEmpty())
        assertTrue(requests.isEmpty())
        assertTrue(viewModel.state.value.needsLocation)
        assertEquals(1, viewModel.state.value.photos.size)
    }

    @Test
    fun ifPhotosFailToUploadNoClaimIsSent() = runTest(dispatcher) {
        uploadFails = true
        val viewModel = viewModel()
        viewModel.addPhotos(listOf(Photo("front")))

        submitted(viewModel)

        assertTrue(requests.isEmpty())
        assertEquals("Couldn't upload the photos. Check your connection and try again.", viewModel.state.value.error)
    }

    @Test
    fun aRefusalIsExplainedInTheBackendsWords() = runTest(dispatcher) {
        // e.g. someone else claimed it, or a claim is already waiting for review
        claimStatus = HttpStatusCode.Conflict
        claimBody = """{"message":"This shop already has an owner. Contact support if you believe this is a mistake."}"""
        val viewModel = viewModel()
        viewModel.addPhotos(listOf(Photo("front")))

        submitted(viewModel)

        assertEquals("This shop already has an owner. Contact support if you believe this is a mistake.", viewModel.state.value.error)
        assertNull(viewModel.state.value.outcome)
        assertFalse(viewModel.state.value.submitting)
    }

    @Test
    fun tappingSubmitTwiceSendsOneClaim() = runTest(dispatcher) {
        val viewModel = viewModel()
        viewModel.addPhotos(listOf(Photo("front")))

        viewModel.submit()
        viewModel.submit()
        advanceUntilIdle()
        viewModel.submit()
        advanceUntilIdle()

        assertEquals(1, requests.size)
    }
}
