package com.example.bhandara.ui.addshop

import io.ktor.http.HttpMethod
import kotlinx.datetime.LocalDate
import com.example.bhandara.ui.fssai.FssaiForm
import com.example.bhandara.data.network.FssaiApi
import kotlinx.datetime.LocalTime
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
import kotlinx.serialization.json.JsonNull
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
    private var fssaiStatus = HttpStatusCode.Created

    private val client = ApiClient("https://api.test/", { "id-token" }, MockEngine(MockEngineConfig().apply {
        dispatcher = this@AddShopViewModelTest.dispatcher
        addHandler { request ->
            requests += request
            val (status, body) = when {
                request.url.encodedPath == "/api/localshops" ->
                    createStatus to (if (createStatus == HttpStatusCode.Created) """{"id":"42","shopName":"Sharma Chaat","latitude":18.58,"longitude":73.88}""" else "")
                request.url.encodedPath.startsWith("/api/menu-items/manual/shop/") -> menuStatus to "[]"
                request.url.encodedPath == "/api/fssai/shops/42" -> fssaiStatus to
                    """{"id":1,"shopId":42,"licenceNumber":"11523026000123","validTill":"2028-03-12","status":"PENDING_REVIEW"}"""
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
        fssaiApi = FssaiApi(client),
    )

    /** A certificate page that can be sent */
    private class Page(val name: String) : PickedPhoto {
        override val preview: Any = name
        override suspend fun compressed() = CompressedPhoto(name.encodeToByteArray(), "jpg", "image/jpeg")
    }

    private val licence = FssaiForm(listOf(Page("certificate")), "11523026000123", LocalDate(2028, 3, 12))

    /** Enough to add a shop: a fixed shop with a name, a kind and a dish */
    private fun AddShopViewModel.fillIn() {
        setMovingCart(false)
        onShopNameChange("Sharma Chaat")
        onShopTypeChange("Street Food")
        onMenuInputChange("Pani Puri, Bhel, ")
    }

    private fun AddShopViewModel.fillInCart() {
        setMovingCart(true)
        onShopNameChange("Ramesh Bhel")
        toggleCartSells("Chaat & snacks")
        setNoFixedRoute(true)
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
    fun aMovingCartAlsoHasARouteAndItsMenuIsItsItems() {
        val viewModel = viewModel()
        viewModel.setMovingCart(true)
        assertEquals(listOf(ShopStep.KIND, ShopStep.BASICS, ShopStep.ROUTE, ShopStep.MENU, ShopStep.DETAILS), viewModel.state.value.steps)

        viewModel.next()
        viewModel.next()
        assertEquals(ShopStep.BASICS, viewModel.state.value.step)
        assertEquals("Enter the cart's name", viewModel.state.value.error)

        viewModel.onShopNameChange("Ramesh Bhel")
        viewModel.next()
        assertEquals("Pick what it sells", viewModel.state.value.error)

        viewModel.toggleCartSells("Sabzi")
        viewModel.next()
        assertEquals(ShopStep.ROUTE, viewModel.state.value.step)
        viewModel.next()
        assertEquals("Add a stop, or say it has no fixed route", viewModel.state.value.error)

        viewModel.saveStop(CartStop(1, "Kothrud bus depot"))
        viewModel.next()
        assertEquals(ShopStep.MENU, viewModel.state.value.step)
        viewModel.next()
        assertTrue(viewModel.state.value.isLastStep)
        repeat(4) { assertTrue(viewModel.back()) }
        assertFalse(viewModel.back())
    }

    @Test
    fun aShopHasNoRouteStep() {
        val viewModel = viewModel()
        viewModel.setMovingCart(false)
        assertFalse(ShopStep.ROUTE in viewModel.state.value.steps)
    }

    @Test
    fun stopsCanBeChangedReorderedAndDroppedForNoFixedRoute() {
        val viewModel = viewModel()
        viewModel.setMovingCart(true)
        viewModel.saveStop(CartStop(1, "Kothrud bus depot"))
        viewModel.saveStop(CartStop(2, "MIT College gate"))
        viewModel.saveStop(CartStop(3, "Karve Road signal"))

        viewModel.saveStop(CartStop(2, "MIT gate"))
        viewModel.moveStop(2, 0)
        assertEquals(listOf("Karve Road signal", "Kothrud bus depot", "MIT gate"), viewModel.state.value.form.stops.map { it.name })

        viewModel.removeStop(1)
        assertEquals(listOf("Karve Road signal", "MIT gate"), viewModel.state.value.form.stops.map { it.name })

        viewModel.setNoFixedRoute(true)
        assertTrue(viewModel.state.value.form.stops.isEmpty())
        assertNull(viewModel.state.value.form.problem(listOf(ShopStep.ROUTE)))

        // Adding a stop again means it has a route after all
        viewModel.saveStop(CartStop(4, "Paud Phata"))
        assertFalse(viewModel.state.value.form.noFixedRoute)
    }

    @Test
    fun aRouteNeedsItsDays() {
        val cart = ShopForm(isMovingCart = true, shopName = "Ramesh Bhel", cartSells = listOf("Sabzi"), stops = listOf(CartStop(1, "Kothrud")))
        assertNull(cart.problem(listOf(ShopStep.ROUTE)))
        assertEquals("Pick the days it's out", cart.copy(daysOut = emptySet()).problem(listOf(ShopStep.ROUTE))?.message)
    }

    @Test
    fun aCartsTypeRouteDaysPaymentsAndLooksAreSentWithIt() = runTest(dispatcher) {
        val viewModel = viewModel()
        viewModel.setMovingCart(true)
        viewModel.onShopNameChange("Ramesh Bhel")
        viewModel.setCartType("CYCLE")
        viewModel.toggleCartSells("Chaat & snacks")
        viewModel.saveStop(CartStop(1, "Kothrud depot", from = LocalTime(7, 0), to = LocalTime(9, 30)))
        viewModel.saveStop(CartStop(2, "MIT gate", timesVary = true))
        viewModel.toggleDay(5) // not on Saturdays (out Monday to Friday)
        viewModel.togglePayment("CARD")
        viewModel.setTakesOrders(true)
        viewModel.onSpotHintChange(" Blue umbrella ")
        viewModel.setCartOption("Chaat & snacks", OPTION_JAIN, true)

        submitted(viewModel)

        val cart = requestTo("/api/localshops").json()["cartDetails"]!!.jsonObject
        assertEquals("CYCLE", cart["cartType"]!!.jsonPrimitive.content)
        assertEquals(listOf("Chaat & snacks"), cart["sells"]!!.jsonArray.map { it.jsonPrimitive.content })
        val stops = cart["stops"]!!.jsonArray.map { it.jsonObject }
        assertEquals(listOf("Kothrud depot", "MIT gate"), stops.map { it["name"]!!.jsonPrimitive.content })
        assertEquals("07:00:00", stops[0]["from"]!!.jsonPrimitive.content)
        assertEquals("09:30:00", stops[0]["to"]!!.jsonPrimitive.content)
        assertEquals("true", stops[1]["timesVary"]!!.jsonPrimitive.content)
        assertTrue(stops[1]["from"].let { it == null || it is JsonNull }, "a stop whose times vary has none")
        assertEquals(listOf("MON", "TUE", "WED", "THU", "FRI"), cart["daysOut"]!!.jsonArray.map { it.jsonPrimitive.content })
        assertEquals(listOf("CASH", "UPI", "CARD"), cart["payments"]!!.jsonArray.map { it.jsonPrimitive.content })
        assertEquals("true", cart["takesOrders"]!!.jsonPrimitive.content)
        assertEquals("Blue umbrella", cart["spotHint"]!!.jsonPrimitive.content)
        // Chaat's settings as picked, or as its preset has them
        val options = cart["options"]!!.jsonObject
        assertEquals("true", options["Chaat & snacks/$OPTION_JAIN"]!!.jsonPrimitive.content)
        assertEquals("true", options["Chaat & snacks/$OPTION_PURE_VEG"]!!.jsonPrimitive.content)
    }

    @Test
    fun aCartWithNoFixedRouteSendsNoStops() = runTest(dispatcher) {
        val viewModel = viewModel()
        viewModel.setMovingCart(true)
        viewModel.saveStop(CartStop(1, "Kothrud depot"))
        viewModel.fillInCart() // says it has no fixed route

        submitted(viewModel)

        val cart = requestTo("/api/localshops").json()["cartDetails"]!!.jsonObject
        assertEquals("true", cart["noFixedRoute"]!!.jsonPrimitive.content)
        assertTrue(cart["stops"]!!.jsonArray.isEmpty())
    }

    @Test
    fun aFixedShopSendsNoCartDetails() = runTest(dispatcher) {
        val viewModel = viewModel()
        viewModel.fillIn()

        submitted(viewModel)

        assertTrue(requestTo("/api/localshops").json()["cartDetails"].let { it == null || it is JsonNull })
    }

    @Test
    fun whatACartSellsIsListedAsItsDishes() = runTest(dispatcher) {
        val viewModel = viewModel()
        viewModel.fillInCart()
        viewModel.toggleCartSells("Chai & drinks")

        submitted(viewModel)

        val shop = requestTo("/api/localshops").json()
        assertEquals(listOf("Chaat & snacks", "Chai & drinks"), shop["menuItems"]!!.jsonArray.map { it.jsonPrimitive.content })
        assertEquals("Street Food", shop["shopType"]!!.jsonPrimitive.content)
    }

    @Test
    fun pickingWhatACartSellsSuggestsItsItemsAndDroppingItDropsThem() {
        val viewModel = viewModel()
        viewModel.setMovingCart(true)

        viewModel.toggleCartSells("Sabzi")
        val sabzi = viewModel.state.value.form.cartItems
        assertEquals(listOf("Tomato", "Onion", "Methi", "Kothimbir"), sabzi.map { it.name })
        assertEquals(listOf("kg", "kg", "bunch", "bunch"), sabzi.map { it.unit })
        assertTrue(viewModel.state.value.form.cartOption("Sabzi", OPTION_DAILY_RATES))

        viewModel.toggleCartSells("Sabzi")
        assertTrue(viewModel.state.value.form.cartItems.isEmpty())
        assertTrue(viewModel.state.value.form.cartSells.isEmpty())
    }

    @Test
    fun itemsCanBeAddedPricedAndRemoved() {
        val viewModel = viewModel()
        viewModel.setMovingCart(true)
        viewModel.toggleCartSells("Milk & dairy")
        viewModel.addCartItem("Milk & dairy", PresetItem("Ghee", "kg"))
        viewModel.addCartItem("Milk & dairy")
        val form = viewModel.state.value.form
        val own = form.cartItems.last()
        assertTrue(own.custom)
        assertEquals("litre", own.unit)

        viewModel.onCartItemName(own.id, "Malai")
        viewModel.onCartItemPrice(form.cartItems.first().id, "60.505")
        viewModel.onCartItemUnit(form.cartItems.first().id, "½ L")
        viewModel.removeCartItem(form.cartItems[1].id)

        val items = viewModel.state.value.form.cartItems
        assertEquals(listOf("Cow milk", "Dahi", "Ghee", "Malai"), items.map { it.name })
        assertEquals("60.50", items.first().price)
        assertEquals("½ L", items.first().unit)
    }

    @Test
    fun aCategoryTheOwnerNamesHasNoSuggestions() {
        val viewModel = viewModel()
        viewModel.setMovingCart(true)
        viewModel.addCustomCategory("  Flowers ")
        viewModel.addCustomCategory("flowers")

        assertEquals(listOf("Flowers"), viewModel.state.value.form.cartSells)
        assertTrue(viewModel.state.value.form.cartItems.isEmpty())
    }

    @Test
    fun aDrinksCartKnowsGanneRasIsSeasonal() {
        val viewModel = viewModel()
        viewModel.setMovingCart(true)
        viewModel.toggleCartSells("Chai & drinks")
        val form = viewModel.state.value.form
        assertEquals(2 to 5, form.seasons["Chai & drinks"])
        assertEquals(listOf("Ganne ras"), form.cartItems.filter { it.id in form.seasonalItems }.map { it.name })

        val naralPani = form.cartItems.single { it.name == "Naral pani" }.id
        viewModel.setSeason("Chai & drinks", 3, 6, setOf(naralPani))
        assertEquals(3 to 6, viewModel.state.value.form.seasons["Chai & drinks"])
        assertEquals(setOf(naralPani), viewModel.state.value.form.seasonalItems)
    }

    @Test
    fun aCartsItemsAreSavedAsItsMenuWithTheirUnits() = runTest(dispatcher) {
        val viewModel = viewModel()
        viewModel.fillInCart()
        val items = viewModel.state.value.form.cartItems
        viewModel.onCartItemPrice(items.first { it.name == "Bhel" }.id, "40")
        viewModel.addCartItem("Chaat & snacks")

        submitted(viewModel)

        val menu = requestTo("/api/menu-items/manual/shop/42").json()["items"]!!.jsonArray.map { it.jsonObject }
        assertEquals(listOf("Bhel", "Pani puri", "Vada pav"), menu.map { it["name"]!!.jsonPrimitive.content })
        assertEquals(listOf("plate", "6 pcs", "piece"), menu.map { it["unit"]!!.jsonPrimitive.content })
        assertTrue(menu.all { it["category"]!!.jsonPrimitive.content == "Chaat & snacks" })
        assertEquals(40.0, menu.first()["price"]!!.jsonPrimitive.content.toDouble())
    }

    @Test
    fun seasonalItemsAreSavedWithTheirMonthsAndTheRestAllYear() = runTest(dispatcher) {
        val viewModel = viewModel()
        viewModel.fillInCart()
        viewModel.toggleCartSells("Chai & drinks")
        val drinks = viewModel.state.value.form.cartItems.filter { it.category == "Chai & drinks" }
        val naralPani = drinks.single { it.name == "Naral pani" }.id
        // March to June (0 = January on the phone)
        viewModel.setSeason("Chai & drinks", 2, 5, setOf(naralPani))

        submitted(viewModel)

        val menu = requestTo("/api/menu-items/manual/shop/42").json()["items"]!!.jsonArray.map { it.jsonObject }
        val naral = menu.single { it["name"]!!.jsonPrimitive.content == "Naral pani" }
        assertEquals(3, naral["seasonFromMonth"]!!.jsonPrimitive.content.toInt())
        assertEquals(6, naral["seasonToMonth"]!!.jsonPrimitive.content.toInt())
        val allYear = menu.filter { it["name"]!!.jsonPrimitive.content != "Naral pani" }
        assertTrue(allYear.isNotEmpty())
        assertTrue(allYear.all { (it["seasonFromMonth"] ?: JsonNull) is JsonNull })
    }

    @Test
    fun theSameItemByTwoUnitsIsSavedTwice() {
        val form = ShopForm(
            isMovingCart = true,
            cartItems = listOf(
                CartItem(1, "Eggs", "Eggs", "dozen", "84"),
                CartItem(2, "Eggs", "Eggs", "tray of 30", "200"),
                CartItem(3, "Eggs", "eggs", "Dozen", "90"),
            ),
        )

        assertEquals(listOf("dozen", "tray of 30"), form.cartMenu.map { it.unit })
    }

    @Test
    fun editInRouteGoesBackToTheRoute() {
        val viewModel = viewModel()
        viewModel.fillInCart()
        viewModel.next()
        viewModel.next()
        viewModel.next()
        assertEquals(ShopStep.MENU, viewModel.state.value.step)

        viewModel.goBackTo(ShopStep.ROUTE)
        assertEquals(ShopStep.ROUTE, viewModel.state.value.step)
        // Never forward
        viewModel.goBackTo(ShopStep.DETAILS)
        assertEquals(ShopStep.ROUTE, viewModel.state.value.step)
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
        viewModel.fillInCart()

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
    fun aShopGoesWhereItsPinWasPutNotWhereThePhoneIs() = runTest(dispatcher) {
        val viewModel = viewModel()
        viewModel.fillIn()
        viewModel.setPin(GeoLocation(18.5901, 73.8912))

        submitted(viewModel)

        val shop = requestTo("/api/localshops").json()
        assertEquals(18.5901, shop["latitude"]!!.jsonPrimitive.double)
        assertEquals(73.8912, shop["longitude"]!!.jsonPrimitive.double)
    }

    @Test
    fun aPinPutByHandNeedsNoLocationAccess() = runTest(dispatcher) {
        // Added later from home: the owner found the shop on the map
        phone.permitted = false
        val viewModel = viewModel()
        viewModel.fillIn()
        viewModel.setPin(GeoLocation(18.5901, 73.8912))

        submitted(viewModel)

        assertFalse(viewModel.state.value.needsLocation)
        assertEquals(18.5901, requestTo("/api/localshops").json()["latitude"]!!.jsonPrimitive.double)
    }

    @Test
    fun thePinIsKeptForNextTimeAndClearingForgetsIt() {
        viewModel().apply {
            fillIn()
            setPin(GeoLocation(18.5901, 73.8912))
        }

        val again = viewModel()
        assertEquals(GeoLocation(18.5901, 73.8912), again.state.value.form.pin)

        again.clearAll()
        assertNull(viewModel().state.value.form.pin)
    }

    @Test
    fun theFssaiLicenceIsSentForTheNewShop() = runTest(dispatcher) {
        val viewModel = viewModel()
        viewModel.fillIn()
        viewModel.setFssai(licence)

        submitted(viewModel)

        val sent = requestTo("/api/fssai/shops/42")
        assertEquals(HttpMethod.Post, sent.method)
        assertEquals(AddedShop("42", menuSaved = true, fssaiSent = true), viewModel.state.value.added)
    }

    @Test
    fun ifOnlyTheLicenceFailsTheShopIsStillAddedAndTheUserTold() = runTest(dispatcher) {
        fssaiStatus = HttpStatusCode.ServiceUnavailable
        val viewModel = viewModel()
        viewModel.fillIn()
        viewModel.setFssai(licence)

        submitted(viewModel)

        assertEquals(AddedShop("42", menuSaved = true, fssaiSent = false), viewModel.state.value.added)
    }

    @Test
    fun aMovingCartSendsNoLicence() = runTest(dispatcher) {
        val viewModel = viewModel()
        viewModel.fillInCart()
        viewModel.setFssai(licence)

        submitted(viewModel)

        assertTrue(requests.none { it.url.encodedPath.startsWith("/api/fssai") })
        assertEquals(true, viewModel.state.value.added?.fssaiSent)
    }

    @Test
    fun withoutALicenceNothingIsSentForIt() = runTest(dispatcher) {
        val viewModel = viewModel()
        viewModel.fillIn()

        submitted(viewModel)

        assertTrue(requests.none { it.url.encodedPath.startsWith("/api/fssai") })
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
