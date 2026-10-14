package com.example.bhandara.ui.menu

import com.example.bhandara.data.models.api.MenuItemRequest
import com.example.bhandara.data.network.ApiClient
import com.example.bhandara.data.network.MenuApi
import com.example.bhandara.photos.CompressedPhoto
import com.example.bhandara.photos.PickedPhoto
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.MockEngineConfig
import io.ktor.client.engine.mock.respond
import io.ktor.client.request.HttpRequestData
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.content.OutgoingContent
import io.ktor.http.headersOf
import io.ktor.utils.io.ByteChannel
import io.ktor.utils.io.toByteArray
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
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Adding items to a menu, the same on Android and iOS: typed in or read from menu photos by AI, saved to the
 * owner's shop or handed back as a draft. The backend is a fake.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class AddMenuItemsViewModelTest {

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

    private val requests = mutableListOf<HttpRequestData>()
    private var saveStatus = HttpStatusCode.Created
    private var saveBody = "[]"
    private var scanStatus = HttpStatusCode.OK
    private var scanBody = """{"items":[{"name":"Masala Dosa","foodType":"VEG","price":80.0},{"name":"Egg Roll","foodType":"EGG"}]}"""

    private val client = ApiClient("https://api.test/", { "id-token" }, MockEngine(MockEngineConfig().apply {
        dispatcher = this@AddMenuItemsViewModelTest.dispatcher
        addHandler { request ->
            requests += request
            val (status, body) = if (request.url.encodedPath.endsWith("/image/extract")) scanStatus to scanBody else saveStatus to saveBody
            respond(body, status, headersOf(HttpHeaders.ContentType, "application/json"))
        }
    }))

    private fun viewModel(shopId: String? = "5", existing: List<String> = emptyList(), initial: List<MenuItemRequest> = emptyList()) =
        AddMenuItemsViewModel(shopId, existing, initial, MenuApi(client))

    private fun AddMenuItemsViewModel.type(vararg items: Pair<String, String>) {
        items.forEachIndexed { index, (name, price) ->
            if (index > 0) addRow()
            val row = state.value.rows.last()
            onNameChange(row.id, name)
            onPriceChange(row.id, price)
        }
    }

    private fun HttpRequestData.savedNames(): List<String> =
        Json.parseToJsonElement((body as OutgoingContent.ByteArrayContent).bytes().decodeToString())
            .jsonObject["items"]!!.jsonArray.map { it.jsonObject["name"]!!.jsonPrimitive.content }

    private fun TestScope.saved(viewModel: AddMenuItemsViewModel) = viewModel.also {
        it.save()
        advanceUntilIdle()
    }

    // ==================== Rules for items ====================

    @Test
    fun pricesAreShownAndTypedSimply() {
        assertEquals("20", priceText(20.0))
        assertEquals("12.5", priceText(12.5))
        assertEquals("", priceText(null))
        assertEquals("12.5", cleanPrice("₹12.5"))
        assertEquals("12.55", cleanPrice("12.559"))
        assertEquals("1.50", cleanPrice("1.5.0")) // a second point is dropped
        assertEquals("", cleanPrice("free"))
    }

    @Test
    fun blankRowsAreSkippedAndNamesTrimmed() {
        val rows = listOf(MenuRow(1, " Chai ", "10"), MenuRow(2), MenuRow(3, "Samosa", "", "VEG"))

        val items = menuItemsFrom(rows, emptyList()).getOrThrow()

        assertEquals(listOf(MenuItemRequest("Chai", "VEG", 10.0), MenuItemRequest("Samosa", "VEG", null)), items)
    }

    @Test
    fun namesTheBackendWouldRefuseAreCaughtFirst() {
        fun problem(rows: List<MenuRow>, existing: List<String> = emptyList()) = menuItemsFrom(rows, existing).exceptionOrNull()?.message

        assertEquals("Add at least one item", problem(listOf(MenuRow(1))))
        assertEquals("Every item needs a name", problem(listOf(MenuRow(1, "", "20"))))
        assertEquals("\"T\" is too short for an item name", problem(listOf(MenuRow(1, "T"))))
        assertEquals("\"Chai\" is already on the menu", problem(listOf(MenuRow(1, "chai ")).map { it.copy(name = "Chai") }, listOf("CHAI")))
        // Names the repeat, as typed
        assertEquals("\"vada pav\" is listed more than once", problem(listOf(MenuRow(1, "Vada Pav"), MenuRow(2, "vada pav"))))
        assertEquals("Check the price of \"Chai\"", problem(listOf(MenuRow(1, "Chai", "0"))))
        assertEquals("Check the price of \"Chai\"", problem(listOf(MenuRow(1, "Chai", "."))))
    }

    // ==================== Typing items ====================

    @Test
    fun theEditorStartsWithOneRowOrTheDraft() {
        assertEquals(1, viewModel().state.value.rows.size)

        val draft = viewModel(shopId = null, initial = listOf(MenuItemRequest("Chai", "VEG", 10.0), MenuItemRequest("Omelette", "EGG")))

        assertEquals(listOf("Chai" to "10", "Omelette" to ""), draft.state.value.rows.map { it.name to it.price })
        assertEquals("EGG", draft.state.value.rows[1].foodType)
    }

    @Test
    fun thereIsAlwaysARowToTypeIn() {
        val viewModel = viewModel()

        viewModel.removeRow(viewModel.state.value.rows.single().id)

        assertEquals(1, viewModel.state.value.rows.size)
    }

    // ==================== Saving ====================

    @Test
    fun theOwnersItemsAreSavedToTheirShop() = runTest(dispatcher) {
        val viewModel = viewModel()
        viewModel.type("Chai" to "10", "Bun Maska" to "25.5")
        viewModel.onFoodTypeChange(viewModel.state.value.rows[1].id, "VEG")

        saved(viewModel)

        val request = requests.single()
        assertEquals("/api/menu-items/manual/shop/5", request.url.encodedPath)
        assertEquals(listOf("Chai", "Bun Maska"), request.savedNames())
        assertEquals(listOf("Chai", "Bun Maska"), viewModel.state.value.savedItems?.map { it.name })
    }

    @Test
    fun aDraftIsHandedBackWithoutSaving() = runTest(dispatcher) {
        // Adding a shop: the menu is saved with the shop later
        val viewModel = viewModel(shopId = null)
        viewModel.type("Chai" to "10")

        saved(viewModel)

        assertTrue(requests.isEmpty())
        assertEquals(listOf(MenuItemRequest("Chai", "VEG", 10.0)), viewModel.state.value.savedItems)
    }

    @Test
    fun aProblemIsShownAndNothingSaved() = runTest(dispatcher) {
        val viewModel = viewModel(existing = listOf("Chai"))
        viewModel.type("chai" to "10")

        saved(viewModel)

        assertEquals("\"chai\" is already on the menu", viewModel.state.value.error)
        assertTrue(requests.isEmpty())
        assertNull(viewModel.state.value.savedItems)
    }

    @Test
    fun aRefusedSaveIsExplainedAndTheItemsStay() = runTest(dispatcher) {
        // e.g. someone else added it from another phone meanwhile
        saveStatus = HttpStatusCode.Conflict
        saveBody = """{"message":"\"Chai\" is already on the menu"}"""
        val viewModel = viewModel()
        viewModel.type("Chai" to "10")

        saved(viewModel)

        assertEquals("\"Chai\" is already on the menu", viewModel.state.value.error)
        assertEquals("Chai", viewModel.state.value.rows.single().name)
        assertFalse(viewModel.state.value.saving)
    }

    @Test
    fun tappingSaveTwiceSavesOnce() = runTest(dispatcher) {
        val viewModel = viewModel()
        viewModel.type("Chai" to "10")

        viewModel.save()
        viewModel.save()
        advanceUntilIdle()
        viewModel.save()
        advanceUntilIdle()

        assertEquals(1, requests.size)
    }

    // ==================== Reading a menu photo ====================

    @Test
    fun menuPhotosAreSentAsImagesForAiToRead() = runTest(dispatcher) {
        val viewModel = viewModel()
        viewModel.addMenuPhotos(listOf(Photo("page1"), Photo("page2")))

        viewModel.scan()
        advanceUntilIdle()

        val request = requests.single()
        assertEquals("/api/menu-items/image/extract", request.url.encodedPath)
        val body = request.body
        assertTrue(body.contentType?.match(ContentType.MultiPart.FormData) == true, "was ${body.contentType}")
        val sent = (body as OutgoingContent.WriteChannelContent).let { content ->
            val channel = ByteChannel()
            content.writeTo(channel)
            channel.close()
            channel.toByteArray().decodeToString()
        }
        assertEquals(2, Regex("name=\"?images\"?").findAll(sent).count(), sent)
        assertTrue("page1" in sent && "page2" in sent)
        assertTrue(ContentType.Image.JPEG.withoutParameters().toString() in sent)
    }

    @Test
    fun itemsReadFromPhotosAreAddedForTheOwnerToCheck() = runTest(dispatcher) {
        val viewModel = viewModel()
        viewModel.addMenuPhotos(listOf(Photo("page1")))

        viewModel.scan()
        advanceUntilIdle()

        val state = viewModel.state.value
        // The untouched blank row is replaced by what was read
        assertEquals(listOf("Masala Dosa" to "80", "Egg Roll" to ""), state.rows.map { it.name to it.price })
        assertEquals("EGG", state.rows[1].foodType)
        assertEquals(2, state.scanFound)
        assertTrue(state.menuPhotos.isEmpty())
        // Nothing is saved until the owner says so
        assertEquals(1, requests.size)
    }

    @Test
    fun itemsAlreadyListedAreNotReadInAgain() = runTest(dispatcher) {
        val viewModel = viewModel(existing = listOf("masala dosa"))
        viewModel.type("Egg Roll" to "40")
        viewModel.addMenuPhotos(listOf(Photo("page1")))

        viewModel.scan()
        advanceUntilIdle()

        assertEquals("Everything in these photos is already listed.", viewModel.state.value.error)
        assertEquals(listOf("Egg Roll"), viewModel.state.value.rows.map { it.name })
    }

    @Test
    fun aPhotoWithNoMenuSaysSo() = runTest(dispatcher) {
        scanBody = """{"items":[]}"""
        val viewModel = viewModel()
        viewModel.addMenuPhotos(listOf(Photo("cat")))

        viewModel.scan()
        advanceUntilIdle()

        assertEquals("No menu items could be read from these photos. Try clearer photos.", viewModel.state.value.error)
        assertEquals(1, viewModel.state.value.menuPhotos.size)
    }

    @Test
    fun scanningTooOftenIsExplained() = runTest(dispatcher) {
        // Each scan is a paid AI call, so the backend limits them
        scanStatus = HttpStatusCode.TooManyRequests
        scanBody = ""
        val viewModel = viewModel()
        viewModel.addMenuPhotos(listOf(Photo("page1")))

        viewModel.scan()
        advanceUntilIdle()

        assertEquals("You're doing that too often. Please wait a while and try again.", viewModel.state.value.error)
        assertFalse(viewModel.state.value.scanning)
    }

    @Test
    fun anUnreadablePhotoIsNotSent() = runTest(dispatcher) {
        val viewModel = viewModel()
        viewModel.addMenuPhotos(listOf(Photo("page1"), Photo("broken", readable = false)))

        viewModel.scan()
        advanceUntilIdle()

        assertTrue(requests.isEmpty())
        assertEquals("Couldn't read one of the photos. Try another photo.", viewModel.state.value.error)
    }

    @Test
    fun atMostTenMenuPhotos() {
        val viewModel = viewModel()

        viewModel.addMenuPhotos((1..12).map { Photo("p$it") })

        assertEquals(10, viewModel.state.value.menuPhotos.size)
        assertFalse(viewModel.state.value.canAddMenuPhotos)
    }

    @Test
    fun theDraftCanBeCleared() {
        val viewModel = viewModel(shopId = null, initial = listOf(MenuItemRequest("Chai", "VEG", 10.0)))

        viewModel.clearAll()

        assertTrue(viewModel.state.value.rows.single().isBlank)
    }
}
