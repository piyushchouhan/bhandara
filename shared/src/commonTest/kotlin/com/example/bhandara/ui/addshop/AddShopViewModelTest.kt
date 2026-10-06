package com.example.bhandara.ui.addshop

import com.example.bhandara.data.location.DeviceLocation
import com.example.bhandara.data.location.GeoLocation
import com.example.bhandara.data.location.LocationService
import com.example.bhandara.data.location.TimedLocation
import com.example.bhandara.data.models.api.MenuItemRequest
import com.example.bhandara.data.network.ApiClient
import com.example.bhandara.data.network.MenuApi
import com.example.bhandara.data.network.ShopApi
import com.example.bhandara.data.settings.AddShopDraft
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
import kotlinx.serialization.json.boolean
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
 * Adding a shop, the same on Android and iOS. The phone's location and settings, photo uploads and the backend
 * are fakes.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class AddShopViewModelTest {

    private val dispatcher = StandardTestDispatcher()

    @BeforeTest
    fun setUp() = Dispatchers.setMain(dispatcher)

    @AfterTest
    fun tearDown() = Dispatchers.resetMain()

    // ==================== Fakes ====================

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
    private val settings = AppSettings(object : Settings.Factory {
        private val stores = mutableMapOf<String?, Settings>()
        override fun create(name: String?): Settings = stores.getOrPut(name) { MapSettings() }
    })
    private val uploaded = mutableListOf<List<String>>()
    private var uploadFails = false
    private val requests = mutableListOf<HttpRequestData>()
    private var createStatus = HttpStatusCode.Created
    private var menuStatus = HttpStatusCode.Created

    private val client = ApiClient("https://api.test/", { "id-token" }, MockEngine(MockEngineConfig().apply {
        dispatcher = this@AddShopViewModelTest.dispatcher
        addHandler { request ->
            requests += request
            val (status, body) = when {
                request.url.encodedPath == "/api/localshops" ->
                    createStatus to (if (createStatus == HttpStatusCode.Created) """{"id":"42","shopName":"Sharma Chaat","latitude":18.58,"longitude":73.88}""" else "")
                request.url.encodedPath.startsWith("/api/menu-items/manual/shop/") -> menuStatus to "[]"
                else -> HttpStatusCode.NotFound to ""
            }
            respond(body, status, headersOf(HttpHeaders.ContentType, "application/json"))
        }
    }))

    private fun viewModel(accountEmail: String? = null) = AddShopViewModel(
        ShopApi(client),
        MenuApi(client),
        uploadPhotos = { photos, onProgress ->
            uploaded += photos.map { (it as Photo).name }
            if (uploadFails) throw PhotoUploadException("Permission denied")
            onProgress(100)
            photos.map { "https://img.test/${(it as Photo).name}.webp" }
        },
        LocationService(phone, nowMillis = { 0 }),
        settings,
        currentUid = { "owner-1" },
        accountEmail = accountEmail,
    )

    /** Enough to add a shop: a fixed shop with a name, a kind and a dish */
    private fun AddShopViewModel.fillIn() {
        setMovingCart(false)
        onShopNameChange("Sharma Chaat")
        onShopTypeChange("Street Food")
        onMenuInputChange("Pani Puri, Bhel, ")
    }

    private fun requestTo(path: String) = requests.single { it.url.encodedPath == path }

    private fun HttpRequestData.json(): JsonObject =
        Json.parseToJsonElement((body as OutgoingContent.ByteArrayContent).bytes().decodeToString()).jsonObject

    private fun TestScope.submitted(viewModel: AddShopViewModel) = viewModel.also {
        it.submit()
        advanceUntilIdle()
    }

    // ==================== The form's rules ====================

    @Test
    fun eachStepSaysWhatIsMissing() {
        assertEquals("Say whether it's a moving cart or a shop", ShopForm().problem()?.message)
        val shop = ShopForm(isMovingCart = false)
        assertEquals("Enter the shop's name", shop.problem()?.message)
        assertEquals("The shop's name is too short", shop.copy(shopName = "S").problem()?.message)
        assertEquals("Choose the kind of shop", shop.copy(shopName = "Sharma Chaat").problem()?.message)
        assertEquals(
            "Add at least one dish it serves",
            shop.copy(shopName = "Sharma Chaat", shopType = "Street Food").problem()?.message,
        )
        // A dish from the detailed menu counts
        assertNull(
            shop.copy(shopName = "Sharma Chaat", shopType = "Street Food", detailedMenu = listOf(MenuItemRequest("Chai", "VEG"))).problem()
        )
    }

    @Test
    fun optionalDetailsAreCheckedOnlyWhenGiven() {
        val basics = ShopForm(isMovingCart = false, shopName = "Sharma Chaat", shopType = "Street Food", menuNames = listOf("Chai"))

        assertEquals("Enter a phone number with 10 to 15 digits", basics.copy(phone = "12345").problem()?.message)
        assertEquals("Check the email address", basics.copy(email = "sharma@").problem()?.message)
        assertEquals("Enter the cost for two in rupees, from 1 to 50000", basics.copy(costForTwo = "0").problem()?.message)
        assertNull(basics.copy(phone = "98765 43210", email = "sharma@example.com", costForTwo = "200").problem())
    }

    @Test
    fun dishesAreListedOnceAcrossTheQuickListAndTheDetailedMenu() {
        val form = ShopForm(menuNames = listOf("Chai", "Samosa"), menuInput = " chai ", detailedMenu = listOf(MenuItemRequest("SAMOSA", "VEG"), MenuItemRequest("Bun Maska", "VEG")))

        assertEquals(listOf("Chai", "Samosa", "Bun Maska"), form.allMenuNames)
    }

    @Test
    fun adminsAreRecognisedByTheirEmail() {
        val admins = listOf(" piyush@example.com", "")

        assertTrue(isAdminEmail("Piyush@Example.com", admins))
        assertFalse(isAdminEmail("someone@example.com", admins))
        assertFalse(isAdminEmail(null, admins))
        assertFalse(isAdminEmail("", listOf("")))
    }

    // ==================== Steps and the kept draft ====================

    @Test
    fun theStepsGoInOrderAndBackLeavesFromTheFirst() {
        val viewModel = viewModel()

        viewModel.next()
        assertEquals(ShopStep.KIND, viewModel.state.value.step)
        assertEquals("Say whether it's a moving cart or a shop", viewModel.state.value.error)

        viewModel.fillIn()
        viewModel.next()
        viewModel.next()
        assertEquals(ShopStep.MENU, viewModel.state.value.step)
        viewModel.next()
        assertEquals(ShopStep.DETAILS, viewModel.state.value.step)
        assertTrue(viewModel.state.value.isLastStep)

        repeat(3) { assertTrue(viewModel.back()) }
        assertFalse(viewModel.back())
    }

    @Test
    fun whatIsTypedIsKeptForNextTime() {
        // The user leaves (or the app is closed) halfway through
        val first = viewModel()
        first.fillIn()
        first.onAddressChange("Dhanori Road")
        first.setDetailedMenu(listOf(MenuItemRequest("Pani Puri", "VEG", 30.0)))

        val again = viewModel().state.value.form

        assertEquals("Sharma Chaat", again.shopName)
        assertEquals(false, again.isMovingCart)
        assertEquals(listOf("Pani Puri", "Bhel"), again.menuNames)
        assertEquals("Dhanori Road", again.address)
        assertEquals(listOf(MenuItemRequest("Pani Puri", "VEG", 30.0)), again.detailedMenu)
    }

    @Test
    fun theAccountsEmailIsSuggestedButAKeptOneWins() {
        assertEquals("piyush@example.com", viewModel(accountEmail = "piyush@example.com").state.value.form.email)

        settings.addShopDraft = AddShopDraft(ownerEmail = "shop@example.com")
        assertEquals("shop@example.com", viewModel(accountEmail = "piyush@example.com").state.value.form.email)
    }

    @Test
    fun clearingForgetsTheKeptDraft() {
        val viewModel = viewModel()
        viewModel.fillIn()
        viewModel.setDetailedMenu(listOf(MenuItemRequest("Chai", "VEG")))

        viewModel.clearAll()

        assertEquals(ShopForm(), viewModel.state.value.form)
        assertNull(settings.addShopDraft)
        assertTrue(settings.draftMenuItems.isEmpty())
    }

    // ==================== Adding the shop ====================

    @Test
    fun aShopIsAddedWhereTheUserIsAndItsMenuSaved() = runTest(dispatcher) {
        val viewModel = viewModel()
        viewModel.fillIn()
        viewModel.addPhotos(listOf(Photo("front")))
        viewModel.setDetailedMenu(listOf(MenuItemRequest("Pani Puri", "VEG", 30.0)))
        viewModel.onPhoneChange("98765 43210")
        viewModel.onCostForTwoChange("₹200")
        viewModel.setHomeDelivery(true)

        submitted(viewModel)

        val shop = requestTo("/api/localshops").json()
        assertEquals("Sharma Chaat", shop["shopName"]!!.jsonPrimitive.content)
        assertEquals(listOf("Pani Puri", "Bhel"), shop["menuItems"]!!.jsonArray.map { it.jsonPrimitive.content })
        assertEquals(listOf("https://img.test/front.webp"), shop["imageUrls"]!!.jsonArray.map { it.jsonPrimitive.content })
        assertEquals(18.5823, shop["latitude"]!!.jsonPrimitive.double)
        assertEquals("9876543210", shop["ownerPhone"]!!.jsonPrimitive.content)
        assertEquals(200, shop["averageCostForTwo"]!!.jsonPrimitive.int)
        assertTrue(shop["homeDelivery"]!!.jsonPrimitive.boolean)
        assertFalse(shop["isMovingCart"]!!.jsonPrimitive.boolean)
        assertNotNull(requestTo("/api/menu-items/manual/shop/42"))
        assertEquals(AddedShop("42", menuSaved = true), viewModel.state.value.added)
        // The drafts are done with
        assertNull(settings.addShopDraft)
        assertTrue(settings.draftMenuItems.isEmpty())
    }

    @Test
    fun aShopWithoutPhotosIsFine() = runTest(dispatcher) {
        val viewModel = viewModel()
        viewModel.fillIn()

        submitted(viewModel)

        assertTrue(uploaded.isEmpty())
        assertEquals(null, requestTo("/api/localshops").json()["imageUrls"])
        assertEquals("42", viewModel.state.value.added?.id)
    }

    @Test
    fun aMovingCartIsRememberedForVendorMode() = runTest(dispatcher) {
        val viewModel = viewModel()
        viewModel.fillIn()
        viewModel.setMovingCart(true)

        submitted(viewModel)

        assertTrue(requestTo("/api/localshops").json()["isMovingCart"]!!.jsonPrimitive.boolean)
        assertEquals(42L, settings.vendorShopId)
        assertEquals("owner-1", settings.vendorOwnerUid)
    }

    @Test
    fun ifOnlyTheMenuFailsTheShopIsStillAddedAndTheUserTold() = runTest(dispatcher) {
        // The menu is saved after the shop; losing it silently would leave the owner wondering
        menuStatus = HttpStatusCode.ServiceUnavailable
        val viewModel = viewModel()
        viewModel.fillIn()
        viewModel.setDetailedMenu(listOf(MenuItemRequest("Pani Puri", "VEG", 30.0)))

        submitted(viewModel)

        assertEquals(AddedShop("42", menuSaved = false), viewModel.state.value.added)
    }

    @Test
    fun nothingIsUploadedOrAddedWithoutTheUsersLocation() = runTest(dispatcher) {
        phone.permitted = false
        val viewModel = viewModel()
        viewModel.fillIn()
        viewModel.addPhotos(listOf(Photo("front")))

        submitted(viewModel)

        assertTrue(uploaded.isEmpty())
        assertTrue(requests.isEmpty())
        assertTrue(viewModel.state.value.needsLocation)
        // The draft stays for when they allow it
        assertEquals("Sharma Chaat", settings.addShopDraft?.shopName)
    }

    @Test
    fun ifPhotosFailToUploadNoShopIsAdded() = runTest(dispatcher) {
        uploadFails = true
        val viewModel = viewModel()
        viewModel.fillIn()
        viewModel.addPhotos(listOf(Photo("front")))

        submitted(viewModel)

        assertTrue(requests.isEmpty())
        assertEquals("Couldn't upload the photos. Check your connection and try again.", viewModel.state.value.error)
        assertNull(viewModel.state.value.added)
    }

    @Test
    fun aRefusalIsExplainedAndTheDraftKept() = runTest(dispatcher) {
        createStatus = HttpStatusCode.ServiceUnavailable
        val viewModel = viewModel()
        viewModel.fillIn()

        submitted(viewModel)

        assertNotNull(viewModel.state.value.error)
        assertNull(viewModel.state.value.added)
        assertEquals("Sharma Chaat", settings.addShopDraft?.shopName)
    }

    @Test
    fun anIncompleteFormGoesBackToWhatIsMissing() = runTest(dispatcher) {
        val viewModel = viewModel()
        viewModel.setMovingCart(false)
        viewModel.next()
        viewModel.onShopNameChange("Sharma Chaat")

        submitted(viewModel)

        assertEquals(ShopStep.BASICS, viewModel.state.value.step)
        assertEquals("Choose the kind of shop", viewModel.state.value.error)
        assertTrue(requests.isEmpty())
    }

    @Test
    fun tappingAddTwiceAddsOneShop() = runTest(dispatcher) {
        val viewModel = viewModel()
        viewModel.fillIn()

        viewModel.submit()
        viewModel.submit()
        advanceUntilIdle()
        viewModel.submit()
        advanceUntilIdle()

        assertEquals(1, requests.count { it.url.encodedPath == "/api/localshops" })
    }

    @Test
    fun atMostTwentyPhotos() {
        val viewModel = viewModel()

        viewModel.addPhotos((1..25).map { Photo("p$it") })

        assertEquals(20, viewModel.state.value.form.photos.size)
        assertFalse(viewModel.state.value.canAddPhotos)
    }
}
