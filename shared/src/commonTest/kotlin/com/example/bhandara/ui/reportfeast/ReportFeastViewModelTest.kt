package com.example.bhandara.ui.reportfeast

import com.example.bhandara.data.location.DeviceLocation
import com.example.bhandara.data.location.GeoLocation
import com.example.bhandara.data.location.LocationService
import com.example.bhandara.data.location.TimedLocation
import com.example.bhandara.data.network.ApiClient
import com.example.bhandara.data.network.FeastApi
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
import io.ktor.http.HttpMethod
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.plus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.double
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
 * Posting a community feast, the same on Android and iOS. The phone's location, the clock, photo uploads and the
 * backend are fakes. It's 4 Oct 2026, 2 PM.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class ReportFeastViewModelTest {

    private val dispatcher = StandardTestDispatcher()

    @BeforeTest
    fun setUp() = Dispatchers.setMain(dispatcher)

    @AfterTest
    fun tearDown() = Dispatchers.resetMain()

    // ==================== Fakes ====================

    private val today = LocalDate(2026, 10, 4)
    private val now = LocalDateTime(today, LocalTime(14, 0))

    private class FakePhone(var permitted: Boolean = true, var location: GeoLocation? = GeoLocation(18.5823, 73.8845)) : DeviceLocation {
        override fun hasPermission() = permitted
        override suspend fun current() = location
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
    private var createStatus = HttpStatusCode.Created
    private var createBody = """{"id":"77","menuItems":["Puri"],"feastDate":"2026-10-04","startTime":"18:00:00","endTime":"22:00:00","latitude":18.58,"longitude":73.88,"estimatedCapacity":0}"""

    private val client = ApiClient("https://api.test/", { "id-token" }, MockEngine(MockEngineConfig().apply {
        dispatcher = this@ReportFeastViewModelTest.dispatcher
        addHandler { request ->
            requests += request
            respond(createBody, createStatus, headersOf(HttpHeaders.ContentType, "application/json"))
        }
    }))

    private fun viewModel(start: FeastFormStart = FeastFormStart.New) = ReportFeastViewModel(
        FeastApi(client),
        uploadPhotos = { photos, onProgress ->
            uploaded += photos.map { (it as Photo).name }
            if (uploadFails) throw PhotoUploadException("Permission denied")
            onProgress(100)
            photos.map { "https://img.test/${(it as Photo).name}.webp" }
        },
        LocationService(phone, nowMillis = { 0 }),
        currentUid = { "me-1" },
        now = { now },
        start = start,
    )

    /** A form filled in correctly: a langar from 6 to 10 PM today */
    private fun ReportFeastViewModel.fillIn() {
        addPhotos(listOf(Photo("p1")))
        onMenuInputChange("Puri, Halwa, ")
        onDateChange(today)
        onStartTimeChange(LocalTime(18, 0))
        onEndTimeChange(LocalTime(22, 0))
    }

    private fun HttpRequestData.json(): JsonObject =
        Json.parseToJsonElement((body as OutgoingContent.ByteArrayContent).bytes().decodeToString()).jsonObject

    private fun TestScope.submitted(viewModel: ReportFeastViewModel = viewModel()): ReportFeastViewModel =
        viewModel.also {
            it.submit()
            advanceUntilIdle()
        }

    // ==================== Filling in ====================

    @Test
    fun typingCommasAddsMenuItems() {
        val viewModel = viewModel()

        viewModel.onMenuInputChange("Puri, Halwa,  Chana")

        val form = viewModel.state.value.form
        assertEquals(listOf("Puri", "Halwa"), form.menuItems)
        assertEquals("Chana", form.menuInput)
        assertEquals(listOf("Puri", "Halwa", "Chana"), form.allMenuItems)
    }

    @Test
    fun theSameItemIsListedOnce() {
        val viewModel = viewModel()

        viewModel.onMenuInputChange("Puri, puri, ,Halwa,")
        viewModel.onMenuInputChange("Halwa")
        viewModel.addMenuInput()

        assertEquals(listOf("Puri", "Halwa"), viewModel.state.value.form.menuItems)
        assertEquals("", viewModel.state.value.form.menuInput)
    }

    @Test
    fun aFeastHasAtMostTenPhotos() {
        val viewModel = viewModel()

        viewModel.addPhotos((1..8).map { Photo("p$it") })
        viewModel.addPhotos((9..12).map { Photo("p$it") })

        assertEquals(10, viewModel.state.value.form.photos.size)
        assertFalse(viewModel.state.value.canAddPhotos)
    }

    @Test
    fun capacityTakesOnlyDigits() {
        val viewModel = viewModel()

        viewModel.onCapacityChange("about 200 people")

        assertEquals("200", viewModel.state.value.form.capacity)
    }

    // ==================== Steps ====================

    @Test
    fun theFirstStepNeedsAPhotoAndWhatIsServed() {
        val viewModel = viewModel()
        assertFalse(viewModel.canGoOn())

        viewModel.next()
        assertEquals(FeastStep.FOOD, viewModel.state.value.step)
        assertEquals("Add at least one photo of the food or the place", viewModel.state.value.error)

        viewModel.addPhotos(listOf(Photo("p1")))
        viewModel.onMenuInputChange("Puri")
        // An item still being typed counts
        assertTrue(viewModel.canGoOn())
        viewModel.next()

        assertEquals(FeastStep.WHEN, viewModel.state.value.step)
        assertEquals(listOf("Puri"), viewModel.state.value.form.menuItems)
    }

    @Test
    fun goingBackFromTheFirstStepLeavesTheScreen() {
        val viewModel = viewModel()
        viewModel.addPhotos(listOf(Photo("p1")))
        viewModel.onMenuInputChange("Puri")
        viewModel.next()

        assertTrue(viewModel.back())
        assertEquals(FeastStep.FOOD, viewModel.state.value.step)
        assertFalse(viewModel.back())
    }

    @Test
    fun clearingStartsOver() {
        val viewModel = viewModel()
        viewModel.fillIn()
        viewModel.next()

        viewModel.clearAll()

        assertEquals(ReportFeastState(), viewModel.state.value)
    }

    // ==================== When ====================

    @Test
    fun aFeastCannotBeInThePast() {
        val problem = FeastForm(
            photos = listOf(Photo("p")), menuItems = listOf("Puri"),
            date = LocalDate(2026, 10, 3), startTime = LocalTime(18, 0), endTime = LocalTime(22, 0),
        ).problem(now)

        assertEquals(FormProblem(FeastStep.WHEN, "The date can't be in the past"), problem)
    }

    @Test
    fun aFeastThatAlreadyEndedTodayIsNotPosted() {
        val form = FeastForm(photos = listOf(Photo("p")), menuItems = listOf("Puri"), date = today)

        assertEquals(
            "This feast would already be over",
            form.copy(startTime = LocalTime(9, 0), endTime = LocalTime(13, 0)).problem(now)?.message,
        )
        // On now, or later today: fine
        assertNull(form.copy(startTime = LocalTime(12, 0), endTime = LocalTime(15, 0)).problem(now))
        assertNull(form.copy(startTime = LocalTime(18, 0), endTime = LocalTime(22, 0)).problem(now))
    }

    @Test
    fun aFeastCanRunPastMidnight() {
        // 8 PM to 1 AM: ends tomorrow, even though 1 AM is "earlier" than now
        val form = FeastForm(
            photos = listOf(Photo("p")), menuItems = listOf("Puri"),
            date = today, startTime = LocalTime(20, 0), endTime = LocalTime(1, 0),
        )

        assertNull(form.problem(now))
    }

    @Test
    fun aFeastMustLastSomeTime() {
        val form = FeastForm(
            photos = listOf(Photo("p")), menuItems = listOf("Puri"),
            date = today, startTime = LocalTime(18, 0), endTime = LocalTime(18, 0),
        )

        assertEquals("It can't end when it starts", form.problem(now)?.message)
    }

    @Test
    fun phoneNumbersAreAcceptedAsPeopleTypeThem() {
        assertEquals("9876543210", normalizePhone("98765 43210"))
        assertEquals("+919876543210", normalizePhone("+91-98765-43210"))
        assertEquals("09876543210", normalizePhone("(0) 98765 43210"))
        assertNull(normalizePhone("12345"))
        assertNull(normalizePhone("call me"))
        assertNull(normalizePhone(""))
    }

    @Test
    fun aWrongPhoneNumberIsCaughtBeforePosting() = runTest(dispatcher) {
        val viewModel = viewModel()
        viewModel.fillIn()
        viewModel.onContactPhoneChange("12345")

        submitted(viewModel)

        assertEquals(FeastStep.WHEN, viewModel.state.value.step)
        assertEquals("Enter a phone number with 10 to 15 digits", viewModel.state.value.error)
        assertTrue(requests.isEmpty())
    }

    // ==================== Posting ====================

    @Test
    fun aFilledInFeastIsPostedFromWhereTheUserIs() = runTest(dispatcher) {
        val viewModel = viewModel()
        viewModel.fillIn()
        viewModel.onOrganizerNameChange("  Gurudwara Sahib ")
        viewModel.onContactPhoneChange("98765 43210")
        viewModel.onAddressChange("Dhanori Road")
        viewModel.onCapacityChange("200")

        submitted(viewModel)

        val body = requests.single().json()
        assertEquals("/api/feasts", requests.single().url.encodedPath)
        assertEquals(listOf("Puri", "Halwa"), body["menuItems"]!!.jsonArray.map { it.jsonPrimitive.content })
        assertEquals(listOf("https://img.test/p1.webp"), body["imageUrls"]!!.jsonArray.map { it.jsonPrimitive.content })
        assertEquals("2026-10-04", body["feastDate"]!!.jsonPrimitive.content)
        assertEquals("18:00:00", body["startTime"]!!.jsonPrimitive.content)
        assertEquals("22:00:00", body["endTime"]!!.jsonPrimitive.content)
        assertEquals(18.5823, body["latitude"]!!.jsonPrimitive.double)
        assertEquals("Gurudwara Sahib", body["organizerName"]!!.jsonPrimitive.content)
        assertEquals("9876543210", body["contactPhone"]!!.jsonPrimitive.content)
        assertEquals(200, body["estimatedCapacity"]!!.jsonPrimitive.int)
        // No landmark given: none is sent
        assertTrue(body["landmark"] == null || body["landmark"] == JsonNull)
        assertEquals("77", viewModel.state.value.postedFeastId)
        assertFalse(viewModel.state.value.submitting)
    }

    @Test
    fun nothingIsUploadedOrPostedWithoutTheUsersLocation() = runTest(dispatcher) {
        // Photos would be uploaded for nothing, and a feast without a place can't go on the map
        phone.permitted = false
        val viewModel = viewModel()
        viewModel.fillIn()

        submitted(viewModel)

        assertTrue(uploaded.isEmpty())
        assertTrue(requests.isEmpty())
        assertTrue(viewModel.state.value.needsLocation)
        assertNotNull(viewModel.state.value.error)

        // The user allows location and tries again
        phone.permitted = true
        submitted(viewModel)
        assertEquals("77", viewModel.state.value.postedFeastId)
        assertFalse(viewModel.state.value.needsLocation)
    }

    @Test
    fun ifPhotosFailToUploadNothingIsPostedAndTheFormStays() = runTest(dispatcher) {
        uploadFails = true
        val viewModel = viewModel()
        viewModel.fillIn()

        submitted(viewModel)

        assertTrue(requests.isEmpty())
        assertEquals("Couldn't upload the photos. Check your connection and try again.", viewModel.state.value.error)
        assertEquals(1, viewModel.state.value.form.photos.size)
        assertNull(viewModel.state.value.postedFeastId)
    }

    @Test
    fun aRefusalIsExplained() = runTest(dispatcher) {
        // Posting is limited to a few feasts an hour
        createStatus = HttpStatusCode.TooManyRequests
        createBody = ""
        val viewModel = viewModel()
        viewModel.fillIn()

        submitted(viewModel)

        assertEquals("You're doing that too often. Please wait a while and try again.", viewModel.state.value.error)
        assertNull(viewModel.state.value.postedFeastId)
        assertFalse(viewModel.state.value.submitting)
    }

    @Test
    fun tappingPostTwiceSendsOneFeast() = runTest(dispatcher) {
        val viewModel = viewModel()
        viewModel.fillIn()

        viewModel.submit()
        viewModel.submit()
        advanceUntilIdle()
        viewModel.submit()
        advanceUntilIdle()

        assertEquals(1, requests.size)
        assertEquals(1, uploaded.size)
    }

    @Test
    fun anIncompleteFormGoesBackToWhatIsMissing() = runTest(dispatcher) {
        val viewModel = viewModel()
        viewModel.addPhotos(listOf(Photo("p1")))
        viewModel.onMenuInputChange("Puri")
        viewModel.next()
        viewModel.next() // no date yet: stays

        submitted(viewModel)

        assertEquals(FeastStep.WHEN, viewModel.state.value.step)
        assertEquals("Choose the date", viewModel.state.value.error)
        assertTrue(requests.isEmpty())
    }

    @Test
    fun theFormCannotBeChangedWhilePosting() = runTest(dispatcher) {
        val viewModel = viewModel()
        viewModel.fillIn()

        viewModel.submit()
        viewModel.onMenuInputChange("Kheer,")
        viewModel.clearAll()
        advanceUntilIdle()

        assertEquals(listOf("Puri", "Halwa"), requests.single().json()["menuItems"]!!.jsonArray.map { it.jsonPrimitive.content })
    }

    // ==================== Editing and reposting ====================

    /** One of the user's feasts, tomorrow 12 to 3 PM at Karve Road, with two photos */
    private val myFeast = """{"id":"50","organizerName":"Ganesh Mandal","contactPhone":"9876543210","menuItems":["Puri","Kheer"],""" +
        """"foodType":"Vegetarian","description":"All welcome","imageUrls":["https://img.test/old1.webp","https://img.test/old2.webp"],""" +
        """"feastDate":"2026-10-05","startTime":"12:00:00","endTime":"15:00:00","latitude":18.51,"longitude":73.81,""" +
        """"address":"Karve Road","landmark":"Near the temple","estimatedCapacity":200,"status":"UPCOMING"}"""

    @Test
    fun editingStartsFromTheFeastWithItsPhotosAndDate() = runTest(dispatcher) {
        createBody = myFeast
        val viewModel = viewModel(FeastFormStart.Edit("50"))
        assertTrue(viewModel.state.value.loading)
        advanceUntilIdle()

        val form = viewModel.state.value.form
        assertFalse(viewModel.state.value.loading)
        assertTrue(viewModel.state.value.isEditing)
        assertEquals("/api/feasts/50", requests.single().url.encodedPath)
        assertEquals(listOf("https://img.test/old1.webp", "https://img.test/old2.webp"), form.photoUrls)
        assertEquals(listOf("Puri", "Kheer"), form.menuItems)
        assertEquals(LocalDate(2026, 10, 5), form.date)
        assertEquals(LocalTime(12, 0), form.startTime)
        assertEquals("Ganesh Mandal", form.organizerName)
        assertEquals("Karve Road", form.address)
        assertEquals("200", form.capacity)
    }

    @Test
    fun savingAnEditKeepsThePlaceUploadsOnlyNewPhotosAndChangesTheSameFeast() = runTest(dispatcher) {
        createBody = myFeast
        phone.location = null // where the user is doesn't matter for an edit
        val viewModel = viewModel(FeastFormStart.Edit("50"))
        advanceUntilIdle()
        viewModel.removePhotoUrl("https://img.test/old2.webp")
        viewModel.addPhotos(listOf(Photo("new")))
        viewModel.onStartTimeChange(LocalTime(18, 0))
        viewModel.onEndTimeChange(LocalTime(21, 0))

        submitted(viewModel)

        val put = requests.last()
        assertEquals(HttpMethod.Put, put.method)
        assertEquals("/api/feasts/50", put.url.encodedPath)
        val body = put.json()
        assertEquals(listOf("https://img.test/old1.webp", "https://img.test/new.webp"), body["imageUrls"]!!.jsonArray.map { it.jsonPrimitive.content })
        assertEquals(listOf(listOf("new")), uploaded)
        assertEquals(18.51, body["latitude"]!!.jsonPrimitive.content.toDouble())
        assertEquals(73.81, body["longitude"]!!.jsonPrimitive.content.toDouble())
        assertEquals("18:00:00", body["startTime"]!!.jsonPrimitive.content)
        assertEquals("50", viewModel.state.value.postedFeastId)
    }

    @Test
    fun anEditWithoutNewPhotosUploadsNothing() = runTest(dispatcher) {
        createBody = myFeast
        val viewModel = viewModel(FeastFormStart.Edit("50"))
        advanceUntilIdle()

        submitted(viewModel)

        assertTrue(uploaded.isEmpty())
        assertEquals(HttpMethod.Put, requests.last().method)
    }

    @Test
    fun aFeastWhosePhotosWereAllRemovedNeedsOne() = runTest(dispatcher) {
        createBody = myFeast
        val viewModel = viewModel(FeastFormStart.Edit("50"))
        advanceUntilIdle()
        viewModel.removePhotoUrl("https://img.test/old1.webp")
        viewModel.removePhotoUrl("https://img.test/old2.webp")

        submitted(viewModel)

        assertEquals(FeastStep.FOOD, viewModel.state.value.step)
        assertEquals("Add at least one photo of the food or the place", viewModel.state.value.error)
        assertTrue(requests.none { it.method == HttpMethod.Put })
    }

    @Test
    fun repostingKeepsTheDetailsButAsksForANewDateAndPostsWhereTheUserIs() = runTest(dispatcher) {
        createBody = myFeast
        val viewModel = viewModel(FeastFormStart.Repost("50"))
        advanceUntilIdle()
        assertNull(viewModel.state.value.form.date)
        assertEquals(LocalTime(12, 0), viewModel.state.value.form.startTime)
        assertFalse(viewModel.state.value.isEditing)

        viewModel.onDateChange(today.plus(DatePeriod(days = 7)))
        submitted(viewModel)

        val post = requests.last()
        assertEquals(HttpMethod.Post, post.method)
        assertEquals("/api/feasts", post.url.encodedPath)
        val body = post.json()
        assertEquals("2026-10-11", body["feastDate"]!!.jsonPrimitive.content)
        assertEquals(18.5823, body["latitude"]!!.jsonPrimitive.content.toDouble())
        assertEquals(listOf("https://img.test/old1.webp", "https://img.test/old2.webp"), body["imageUrls"]!!.jsonArray.map { it.jsonPrimitive.content })
        assertEquals("Ganesh Mandal", body["organizerName"]!!.jsonPrimitive.content)
    }

    @Test
    fun clearingAllOnlyAppliesToANewFeast() = runTest(dispatcher) {
        createBody = myFeast
        val viewModel = viewModel(FeastFormStart.Edit("50"))
        advanceUntilIdle()

        viewModel.clearAll()

        assertEquals(listOf("Puri", "Kheer"), viewModel.state.value.form.menuItems)
    }
}
