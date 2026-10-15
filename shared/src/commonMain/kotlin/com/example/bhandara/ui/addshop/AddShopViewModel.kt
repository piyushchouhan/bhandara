package com.example.bhandara.ui.addshop

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bhandara.data.location.GeoLocation
import com.example.bhandara.data.location.LocationResult
import com.example.bhandara.data.location.LocationService
import com.example.bhandara.data.models.api.LocalShopRequest
import com.example.bhandara.data.models.api.MenuItemRequest
import com.example.bhandara.data.network.ApiResult
import com.example.bhandara.data.network.MenuApi
import com.example.bhandara.data.network.ShopApi
import com.example.bhandara.data.settings.AppSettings
import com.example.bhandara.photos.PhotoUploadException
import com.example.bhandara.photos.PickedPhoto
import com.example.bhandara.ui.menu.cleanPrice
import com.example.bhandara.ui.reportfeast.normalizePhone
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * The shop just added.
 *
 * @param menuSaved false when the shop was added but its detailed menu couldn't be saved (the owner can add it
 *                  from the shop's page)
 */
data class AddedShop(val id: String, val menuSaved: Boolean)

/**
 * @param error          what stops the user, ready to show
 * @param needsLocation  the shop couldn't be added without the user's location (permission or location off)
 * @param uploadProgress 0-100 while photos upload
 * @param added          the new shop, once added; the screen opens it
 */
data class AddShopState(
    val step: ShopStep = ShopStep.KIND,
    val form: ShopForm = ShopForm(),
    val error: String? = null,
    val needsLocation: Boolean = false,
    val submitting: Boolean = false,
    val uploadProgress: Int? = null,
    val added: AddedShop? = null,
) {
    /** The steps for what's being added: a moving cart also has its Route */
    val steps: List<ShopStep> get() = stepsFor(form.isMovingCart)
    val isLastStep: Boolean get() = step == steps.last()
    val canAddPhotos: Boolean get() = form.photos.size + form.cartPhotos.size < MAX_SHOP_PHOTOS
}

/**
 * Adding a shop, the same on Android and iOS: whether it's a cart or a shop, the basics (with photos), an optional
 * detailed menu, and the rest. The shop goes on the map where the user is when they add it. The form is kept on the
 * phone as it's filled in, so nothing is lost if the user leaves; it's cleared once the shop is added.
 *
 * @param accountEmail the signed-in account's email, suggested as the shop's email
 */
class AddShopViewModel(
    private val shopApi: ShopApi,
    private val menuApi: MenuApi,
    private val uploadPhotos: suspend (List<PickedPhoto>, (Int) -> Unit) -> List<String>,
    private val locationService: LocationService,
    private val settings: AppSettings,
    private val currentUid: () -> String?,
    accountEmail: String?,
) : ViewModel() {

    private val _state = MutableStateFlow(
        AddShopState(
            form = ShopForm.fromDraft(settings.addShopDraft, settings.draftMenuItems).let { form ->
                if (form.email.isBlank() && accountEmail != null) form.copy(email = accountEmail) else form
            }
        )
    )
    val state: StateFlow<AddShopState> = _state.asStateFlow()

    // ==================== Filling in the form ====================

    fun setMovingCart(isCart: Boolean) = edit { it.copy(isMovingCart = isCart) }
    fun onShopNameChange(text: String) = edit { it.copy(shopName = text.take(255)) }
    fun onShopTypeChange(type: String) = edit { it.copy(shopType = type) }

    /** Typing a dish; a comma adds what's before it */
    fun onMenuInputChange(text: String) = edit { form ->
        if (',' !in text) return@edit form.copy(menuInput = text)
        val parts = text.split(',')
        form.copy(menuNames = withNames(form.menuNames, parts.dropLast(1)), menuInput = parts.last().trimStart())
    }

    fun addMenuInput() = edit { it.copy(menuNames = withNames(it.menuNames, listOf(it.menuInput)), menuInput = "") }
    fun removeMenuName(name: String) = edit { it.copy(menuNames = it.menuNames - name) }

    fun addPhotos(photos: List<PickedPhoto>) =
        edit { it.copy(photos = it.photos + photos.take(MAX_SHOP_PHOTOS - it.photos.size - it.cartPhotos.size)) }
    fun removePhoto(photo: PickedPhoto) = edit { it.copy(photos = it.photos - photo) }

    // ==================== A moving cart's parts ====================

    fun setCartType(type: String) = edit { it.copy(cartType = type) }

    /** Picks or drops something the cart sells; picking it fills its suggested items, dropping it drops its items */
    fun toggleCartSells(category: String) = edit { form ->
        if (category in form.cartSells) form.withoutCategory(category) else form.withCategory(category)
    }

    /** "Something else": a category the owner names, with no suggestions */
    fun addCustomCategory(name: String) = edit { form ->
        val category = name.trim().take(40)
        if (category.isEmpty() || form.cartSells.any { it.equals(category, ignoreCase = true) }) form else form.withCategory(category)
    }

    /** Adds a suggested item, or (with no [preset]) an empty row to type one in */
    fun addCartItem(category: String, preset: PresetItem? = null) = edit { form ->
        val item = CartItem(
            id = form.nextItemId(),
            category = category,
            name = preset?.name.orEmpty(),
            unit = preset?.unit ?: presetFor(category).units.first(),
            custom = preset == null,
        )
        form.copy(cartItems = form.cartItems + item)
    }

    fun onCartItemName(id: Long, name: String) = editItem(id) { it.copy(name = name.take(60)) }
    fun onCartItemUnit(id: Long, unit: String) = editItem(id) { it.copy(unit = unit) }
    fun onCartItemPrice(id: Long, price: String) = editItem(id) { it.copy(price = cleanPrice(price).take(7)) }
    fun removeCartItem(id: Long) = edit { form -> form.copy(cartItems = form.cartItems.filterNot { it.id == id }, seasonalItems = form.seasonalItems - id) }

    fun setCartOption(category: String, option: String, on: Boolean) = edit { it.copy(cartOptions = it.cartOptions + ("$category/$option" to on)) }

    /** The months a category is out, and (for drinks) which of its items only then */
    fun setSeason(category: String, from: Int, to: Int, seasonalItems: Set<Long>? = null) = edit { form ->
        val inCategory = form.cartItems.filter { it.category == category }.map { it.id }.toSet()
        form.copy(
            seasons = form.seasons + (category to (from to to)),
            seasonalItems = if (seasonalItems == null) form.seasonalItems else form.seasonalItems - inCategory + seasonalItems,
        )
    }

    private fun editItem(id: Long, change: (CartItem) -> CartItem) =
        edit { form -> form.copy(cartItems = form.cartItems.map { if (it.id == id) change(it) else it }) }

    private fun ShopForm.nextItemId(): Long = (cartItems.maxOfOrNull { it.id } ?: 0L) + 1

    private fun ShopForm.withCategory(category: String): ShopForm {
        val preset = presetFor(category)
        var id = nextItemId()
        val starters = if (cartItems.any { it.category == category }) emptyList() else preset.starters.map { CartItem(id++, category, it.name, it.unit) }
        val season = if (preset.seasonFrom != null && preset.seasonTo != null) mapOf(category to (preset.seasonFrom to preset.seasonTo)) else emptyMap()
        return copy(
            cartSells = cartSells + category,
            cartItems = cartItems + starters,
            seasons = season + seasons,
            seasonalItems = seasonalItems + starters.filter { it.name in SEASONAL_BY_DEFAULT }.map { it.id },
        )
    }

    private fun ShopForm.withoutCategory(category: String): ShopForm {
        val dropped = cartItems.filter { it.category == category }.map { it.id }.toSet()
        return copy(
            cartSells = cartSells - category,
            cartItems = cartItems.filterNot { it.id in dropped },
            seasonalItems = seasonalItems - dropped,
            cartOptions = cartOptions.filterKeys { !it.startsWith("$category/") },
            seasons = seasons - category,
        )
    }

    fun addCartPhotos(photos: List<PickedPhoto>) =
        edit { it.copy(cartPhotos = it.cartPhotos + photos.take(MAX_SHOP_PHOTOS - it.photos.size - it.cartPhotos.size)) }

    fun removeCartPhoto(photo: PickedPhoto) = edit { it.copy(cartPhotos = it.cartPhotos - photo) }

    /** Adds a stop, or changes the one with the same id; the cart then has a route */
    fun saveStop(stop: CartStop) = edit { form ->
        val stops = if (form.stops.any { it.id == stop.id }) form.stops.map { if (it.id == stop.id) stop else it } else form.stops + stop
        form.copy(stops = stops, noFixedRoute = false)
    }

    fun removeStop(id: Long) = edit { form -> form.copy(stops = form.stops.filterNot { it.id == id }) }

    /** Moves a stop to another place in the round (dragging it in the list) */
    fun moveStop(from: Int, to: Int) = edit { form ->
        if (from !in form.stops.indices || to !in form.stops.indices) return@edit form
        form.copy(stops = form.stops.toMutableList().apply { add(to, removeAt(from)) })
    }

    /** "It has no fixed route": it goes wherever, so its stops are dropped */
    fun setNoFixedRoute(on: Boolean) = edit { it.copy(noFixedRoute = on, stops = if (on) emptyList() else it.stops) }

    fun toggleDay(day: Int) = edit { it.copy(daysOut = if (day in it.daysOut) it.daysOut - day else it.daysOut + day) }
    fun setTakesOrders(on: Boolean) = edit { it.copy(takesOrders = on) }
    fun togglePayment(way: String) = edit { it.copy(payments = if (way in it.payments) it.payments - way else it.payments + way) }
    fun onSpotHintChange(text: String) = edit { it.copy(spotHint = text.take(100)) }
    fun setSeasonal(on: Boolean) = edit { it.copy(seasonal = on) }

    /** The detailed menu, from the menu editor (in draft mode) */
    fun setDetailedMenu(items: List<MenuItemRequest>) = edit { it.copy(detailedMenu = items) }

    fun onDescriptionChange(text: String) = edit { it.copy(description = text.take(MAX_SHOP_DESCRIPTION)) }
    fun onPhoneChange(text: String) = edit { it.copy(phone = text.take(20)) }
    fun onEmailChange(text: String) = edit { it.copy(email = text.take(100).trim()) }
    fun onAddressChange(text: String) = edit { it.copy(address = text.take(MAX_SHOP_ADDRESS)) }
    fun onLandmarkChange(text: String) = edit { it.copy(landmark = text.take(MAX_SHOP_LANDMARK)) }
    fun onCuisineChange(text: String) = edit { it.copy(cuisine = text.take(100)) }
    fun onCostForTwoChange(text: String) = edit { it.copy(costForTwo = text.filter(Char::isDigit).take(6)) }

    /** The spot picked on the full-screen map (Adjust pin); the shop goes there instead of where the phone is */
    fun setPin(spot: GeoLocation) = edit { it.copy(pin = spot) }
    fun setHomeDelivery(on: Boolean) = edit { it.copy(homeDelivery = on) }
    fun setTakeaway(on: Boolean) = edit { it.copy(takeaway = on) }
    fun setSeating(on: Boolean) = edit { it.copy(hasSeating = on) }
    fun setWifi(on: Boolean) = edit { it.copy(wifi = on) }

    /** Starts over with an empty form, forgetting the kept draft */
    fun clearAll() {
        if (_state.value.submitting) return
        settings.addShopDraft = null
        settings.draftMenuItems = emptyList()
        _state.value = AddShopState()
    }

    // ==================== Moving between steps ====================

    fun canGoOn(): Boolean = with(_state.value) { form.problem(listOf(step)) == null }

    fun next() {
        val state = _state.value
        state.form.problem(listOf(state.step))?.let { problem ->
            _state.update { it.copy(error = problem.message) }
            return
        }
        val next = state.steps.getOrNull(state.steps.indexOf(state.step) + 1) ?: return
        _state.update { it.copy(step = next, error = null, form = it.form.withTypedNameAdded()) }
    }

    /** Back to an earlier step, e.g. Route from the bread cart's morning round */
    fun goBackTo(step: ShopStep) {
        val state = _state.value
        if (state.submitting || step !in state.steps || state.steps.indexOf(step) >= state.steps.indexOf(state.step)) return
        _state.update { it.copy(step = step, error = null) }
    }

    /** Back one step; false on the first step, so the screen closes */
    fun back(): Boolean {
        val state = _state.value
        if (state.submitting) return true
        val previous = state.steps.getOrNull(state.steps.indexOf(state.step) - 1) ?: return false
        _state.update { it.copy(step = previous, error = null) }
        return true
    }

    // ==================== Adding the shop ====================

    /**
     * Checks everything, finds where the user is (before uploading anything), uploads the photos, adds the shop,
     * then saves its detailed menu. A moving cart is remembered on this phone for vendor mode.
     */
    fun submit() {
        val state = _state.value
        if (state.submitting || state.added != null) return
        val form = state.form.withTypedNameAdded()
        form.problem()?.let { problem ->
            _state.update { it.copy(form = form, step = problem.step, error = problem.message) }
            return
        }
        _state.update { it.copy(form = form, submitting = true, error = null, needsLocation = false) }
        viewModelScope.launch {
            // A pin put by hand wins over where the phone is
            val here = form.pin ?: (locationService.locate() as? LocationResult.Found)?.location ?: return@launch fail(
                "Your location is needed to put the shop on the map. Allow location and make sure it's on.",
                needsLocation = true,
            )
            // A cart's own photo goes first, so it's what people see to spot it
            val photos = form.cartPhotos + form.photos
            val photoUrls = if (photos.isEmpty()) emptyList() else {
                _state.update { it.copy(uploadProgress = 0) }
                try {
                    uploadPhotos(photos) { progress -> _state.update { it.copy(uploadProgress = progress) } }
                } catch (e: CancellationException) {
                    throw e
                } catch (e: PhotoUploadException) {
                    return@launch fail("Couldn't upload the photos. Check your connection and try again.")
                }
            }
            val uid = currentUid().orEmpty()
            val request = LocalShopRequest(
                ownerUid = uid,
                ownerPhone = normalizePhone(form.phone),
                ownerEmail = form.email.trim().ifEmpty { null },
                shopName = form.shopName.trim(),
                // A cart has no kind of shop to pick
                shopType = form.shopType.ifBlank { if (form.isCart) "Street Food" else form.shopType },
                cuisineType = form.cuisine.trim().ifEmpty { null },
                description = form.description.trim().ifEmpty { null },
                menuItems = form.allMenuNames,
                averageCostForTwo = form.costForTwo.trim().toIntOrNull(),
                imageUrls = photoUrls.ifEmpty { null },
                latitude = here.latitude,
                longitude = here.longitude,
                fullAddress = form.address.trim().ifEmpty { null },
                landmark = form.landmark.trim().ifEmpty { null },
                homeDelivery = form.homeDelivery,
                takeaway = form.takeaway,
                hasSeating = form.hasSeating,
                wifiAvailable = form.wifi,
                isMovingCart = form.isMovingCart == true,
                mapIcon = "default",
                cartDetails = form.cartDetails,
            )
            val shop = when (val result = shopApi.createShop(request)) {
                is ApiResult.Success -> result.value
                is ApiResult.Failure -> return@launch fail(result.error.message)
            }
            // The shop is added: the drafts are done with, whatever happens to the menu
            settings.addShopDraft = null
            settings.draftMenuItems = emptyList()
            if (form.isMovingCart == true) shop.id.toLongOrNull()?.let { settings.rememberVendorCart(it, uid) }
            // A cart's items are its menu
            val menu = if (form.isCart) form.cartMenu else form.detailedMenu
            val menuSaved = menu.isEmpty() || menuApi.addItems(shop.id, menu) is ApiResult.Success
            _state.update { it.copy(submitting = false, uploadProgress = null, added = AddedShop(shop.id, menuSaved)) }
        }
    }

    private fun fail(message: String, needsLocation: Boolean = false) =
        _state.update { it.copy(submitting = false, uploadProgress = null, error = message, needsLocation = needsLocation) }

    /** Changes the form and keeps it on the phone, so nothing typed is lost */
    private fun edit(change: (ShopForm) -> ShopForm) {
        if (_state.value.submitting || _state.value.added != null) return
        _state.update { it.copy(form = change(it.form), error = null) }
        val form = _state.value.form
        settings.addShopDraft = form.toDraft().takeUnless { it.isEmpty }
        settings.draftMenuItems = form.detailedMenu
    }

    private fun ShopForm.withTypedNameAdded() = copy(menuNames = withNames(menuNames, listOf(menuInput)), menuInput = "")

    private fun withNames(names: List<String>, new: List<String>): List<String> =
        (names + new.map { it.trim() }).filter { it.isNotEmpty() }.distinctBy { it.lowercase() }.take(MAX_SHOP_MENU_NAMES)
}
