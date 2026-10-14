package com.example.bhandara.ui.addshop

import com.example.bhandara.data.models.api.CartStopDetails
import com.example.bhandara.data.models.api.CartDetails
import com.example.bhandara.data.models.api.MenuItemRequest
import com.example.bhandara.data.settings.AddShopDraft
import com.example.bhandara.photos.PickedPhoto
import com.example.bhandara.ui.reportfeast.normalizePhone
import kotlinx.datetime.LocalTime

// The backend's limits for a shop (LocalShopRequestDTO)
const val MAX_SHOP_PHOTOS = 20
const val MAX_SHOP_MENU_NAMES = 100
const val MAX_SHOP_DESCRIPTION = 1000
const val MAX_SHOP_ADDRESS = 500
const val MAX_SHOP_LANDMARK = 255
const val MAX_COST_FOR_TWO = 50_000

/** Kinds of shop people pick from; sent as these names, shown in the user's language */
val SHOP_TYPES = listOf("Restaurant", "Food Truck", "Street Food", "Cafe", "Fast Food", "Bakery", "Sweet Shop")

/** The steps of adding a shop: what it is, the basics, a cart's route, the detailed menu (a cart's items), and the rest */
enum class ShopStep { KIND, BASICS, ROUTE, MENU, DETAILS }

/** A shop goes Kind · Basics · Menu · Details; a moving cart also has its Route */
fun stepsFor(isMovingCart: Boolean?): List<ShopStep> =
    if (isMovingCart == true) ShopStep.entries else ShopStep.entries - ShopStep.ROUTE

/** What a moving cart is pushed or ridden on */
val CART_TYPES = listOf("HAND_CART", "CYCLE", "SCOOTER", "TEMPO", "ON_FOOT")

/** What a moving cart sells; sent as these names among the shop's dishes */
val CART_SELLS = listOf("Sabzi", "Fruits", "Milk & dairy", "Bread & pav", "Eggs", "Chaat & snacks", "Chai & drinks", "Ice cream", "Other")

/** How customers can pay a cart */
val CART_PAYMENTS = listOf("CASH", "UPI", "CARD")

/**
 * A usual stop on a cart's round
 *
 * @param from / [to] the time window; both null when [timesVary]
 */
data class CartStop(
    val id: Long,
    val name: String,
    val from: LocalTime? = null,
    val to: LocalTime? = null,
    val timesVary: Boolean = false,
)

/**
 * What the user has filled in so far.
 *
 * @param isMovingCart null until the user says whether it's a cart on wheels or a fixed shop
 * @param detailedMenu items with prices and food types (optional), saved after the shop
 */
data class ShopForm(
    val isMovingCart: Boolean? = null,
    val shopName: String = "",
    val shopType: String = "",
    val menuNames: List<String> = emptyList(),
    val menuInput: String = "",
    val photos: List<PickedPhoto> = emptyList(),
    val detailedMenu: List<MenuItemRequest> = emptyList(),
    val description: String = "",
    val phone: String = "",
    val email: String = "",
    val address: String = "",
    val landmark: String = "",
    val cuisine: String = "",
    val costForTwo: String = "",
    val homeDelivery: Boolean = false,
    val takeaway: Boolean = true,
    val hasSeating: Boolean = true,
    val wifi: Boolean = false,
    // A moving cart's own parts (design "Moving cart branch"), sent as its cartDetails
    val cartType: String = "",
    val cartSells: List<String> = emptyList(),
    val cartPhotos: List<PickedPhoto> = emptyList(),
    val stops: List<CartStop> = emptyList(),
    val noFixedRoute: Boolean = false,
    /** Days out, 0 = Monday … 6 = Sunday */
    val daysOut: Set<Int> = setOf(0, 1, 2, 3, 4, 5),
    val takesOrders: Boolean = false,
    val payments: Set<String> = setOf("CASH", "UPI"),
    val spotHint: String = "",
    val seasonal: Boolean = false,
    // The cart's Items step (design 3i–3r): what's on the cart, by category, and each category's extra settings
    val cartItems: List<CartItem> = emptyList(),
    /** Extra settings by "category/option" (e.g. "Milk & dairy/monthly_bill") */
    val cartOptions: Map<String, Boolean> = emptyMap(),
    /** The months a category is out, by category (0 = January) */
    val seasons: Map<String, Pair<Int, Int>> = emptyMap(),
    /** Items only sold in their category's season */
    val seasonalItems: Set<Long> = emptySet(),
) {
    val isCart: Boolean get() = isMovingCart == true

    fun cartOption(category: String, option: String): Boolean =
        cartOptions["$category/$option"] ?: presetFor(category).options[option] ?: false

    /**
     * A cart's items as menu items to save after the cart, with the unit the rate is for, the category, and the
     * category's season for items only sold then; each name-and-unit once, nameless rows left out
     */
    val cartMenu: List<MenuItemRequest>
        get() = cartItems
            .filter { it.name.trim().length >= 2 }
            .map { item ->
                // Seasons are kept 0 = January on the phone; the backend counts from 1
                val season = seasons[item.category]?.takeIf { item.id in seasonalItems }
                MenuItemRequest(
                    name = item.name.trim(),
                    foodType = if (item.category == "Eggs") "EGG" else "VEG",
                    price = item.price.toDoubleOrNull()?.takeIf { it > 0 },
                    unit = item.unit.trim().ifEmpty { null },
                    category = item.category.trim().ifEmpty { null },
                    seasonFromMonth = season?.first?.plus(1),
                    seasonToMonth = season?.second?.plus(1),
                )
            }
            .distinctBy { "${it.name.lowercase()}|${it.unit?.lowercase()}" }

    /** A cart's own details as the backend keeps them; null for a fixed shop */
    val cartDetails: CartDetails?
        get() = if (!isCart) null else CartDetails(
            cartType = cartType.ifBlank { null },
            sells = cartSells,
            stops = if (noFixedRoute) emptyList() else stops.filter { it.name.isNotBlank() }.map { stop ->
                CartStopDetails(
                    name = stop.name.trim(),
                    from = stop.from?.takeUnless { stop.timesVary }?.let(::timeOfDay),
                    to = stop.to?.takeUnless { stop.timesVary }?.let(::timeOfDay),
                    timesVary = stop.timesVary,
                )
            },
            noFixedRoute = noFixedRoute,
            // Kept 0 = Monday on the phone
            daysOut = daysOut.sorted().mapNotNull { WEEKDAYS.getOrNull(it) },
            takesOrders = takesOrders,
            payments = CART_PAYMENTS.filter { it in payments },
            spotHint = spotHint.trim().ifEmpty { null },
            seasonal = seasonal,
            // Each setting of what it sells, as picked or as its preset has it
            options = cartSells.flatMap { category ->
                presetFor(category).options.keys.map { option -> "$category/$option" to cartOption(category, option) }
            }.toMap(),
        )

    /** Every dish named, from the quick list and the detailed menu, each once */
    val allMenuNames: List<String>
        get() = (menuNames + menuInput.trim() + (if (isCart) cartSells else emptyList()) + detailedMenu.map { it.name })
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .distinctBy { it.lowercase() }

    /** The form as it's kept on the phone between visits (photos aren't kept: they may not be readable later) */
    fun toDraft() = AddShopDraft(
        isMovingCart = isMovingCart,
        shopName = shopName,
        shopType = shopType,
        menuItemNames = (menuNames + menuInput.trim()).filter { it.isNotEmpty() },
        ownerPhone = phone,
        ownerEmail = email,
        cuisineType = cuisine,
        description = description,
        averageCostForTwo = costForTwo,
        fullAddress = address,
        landmark = landmark,
        homeDelivery = homeDelivery,
        takeaway = takeaway,
        hasSeating = hasSeating,
        wifiAvailable = wifi,
    )

    companion object {
        fun fromDraft(draft: AddShopDraft?, detailedMenu: List<MenuItemRequest>) = ShopForm(
            isMovingCart = draft?.isMovingCart,
            shopName = draft?.shopName.orEmpty(),
            shopType = draft?.shopType.orEmpty(),
            menuNames = draft?.menuItemNames.orEmpty(),
            detailedMenu = detailedMenu,
            description = draft?.description.orEmpty(),
            phone = draft?.ownerPhone.orEmpty(),
            email = draft?.ownerEmail.orEmpty(),
            address = draft?.fullAddress.orEmpty(),
            landmark = draft?.landmark.orEmpty(),
            cuisine = draft?.cuisineType.orEmpty(),
            costForTwo = draft?.averageCostForTwo.orEmpty(),
            homeDelivery = draft?.homeDelivery ?: false,
            takeaway = draft?.takeaway ?: true,
            hasSeating = draft?.hasSeating ?: true,
            wifi = draft?.wifiAvailable ?: false,
        )
    }
}

/** Days as the backend names them, Monday first */
val WEEKDAYS = listOf("MON", "TUE", "WED", "THU", "FRI", "SAT", "SUN")

/** "07:30" → "07:30:00" */
private fun timeOfDay(time: LocalTime): String =
    "${time.hour.toString().padStart(2, '0')}:${time.minute.toString().padStart(2, '0')}:00"

data class ShopProblem(val step: ShopStep, val message: String)

private val EMAIL = Regex("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")

/** The first problem with the given steps of the form, in order; null when they're fine */
fun ShopForm.problem(steps: Collection<ShopStep> = ShopStep.entries): ShopProblem? {
    if (ShopStep.KIND in steps && isMovingCart == null) {
        return ShopProblem(ShopStep.KIND, "Say whether it's a moving cart or a shop")
    }
    if (ShopStep.BASICS in steps && isCart) {
        val name = shopName.trim()
        if (name.isEmpty()) return ShopProblem(ShopStep.BASICS, "Enter the cart's name")
        if (name.length < 2) return ShopProblem(ShopStep.BASICS, "The cart's name is too short")
        if (cartSells.isEmpty()) return ShopProblem(ShopStep.BASICS, "Pick what it sells")
        if (allMenuNames.size > MAX_SHOP_MENU_NAMES) return ShopProblem(ShopStep.BASICS, "A shop can list up to $MAX_SHOP_MENU_NAMES dishes")
    }
    if (ShopStep.BASICS in steps && !isCart) {
        val name = shopName.trim()
        if (name.isEmpty()) return ShopProblem(ShopStep.BASICS, "Enter the shop's name")
        if (name.length < 2) return ShopProblem(ShopStep.BASICS, "The shop's name is too short")
        if (shopType.isBlank()) return ShopProblem(ShopStep.BASICS, "Choose the kind of shop")
        if (allMenuNames.isEmpty()) return ShopProblem(ShopStep.BASICS, "Add at least one dish it serves")
        if (allMenuNames.size > MAX_SHOP_MENU_NAMES) return ShopProblem(ShopStep.BASICS, "A shop can list up to $MAX_SHOP_MENU_NAMES dishes")
    }
    if (ShopStep.ROUTE in steps && isCart) {
        if (stops.isEmpty() && !noFixedRoute) return ShopProblem(ShopStep.ROUTE, "Add a stop, or say it has no fixed route")
        if (stops.isNotEmpty() && daysOut.isEmpty()) return ShopProblem(ShopStep.ROUTE, "Pick the days it's out")
    }
    if (ShopStep.DETAILS in steps) {
        if (phone.isNotBlank() && normalizePhone(phone) == null) return ShopProblem(ShopStep.DETAILS, "Enter a phone number with 10 to 15 digits")
        if (email.isNotBlank() && !EMAIL.matches(email.trim())) return ShopProblem(ShopStep.DETAILS, "Check the email address")
        if (costForTwo.isNotBlank()) {
            val cost = costForTwo.trim().toIntOrNull()
            if (cost == null || cost !in 1..MAX_COST_FOR_TWO) {
                return ShopProblem(ShopStep.DETAILS, "Enter the cost for two in rupees, from 1 to $MAX_COST_FOR_TWO")
            }
        }
    }
    return null
}

/** Whether a signed-in email is one of the app's admins, who list shops for their owners */
fun isAdminEmail(email: String?, adminEmails: List<String>): Boolean =
    email != null && adminEmails.any { it.trim().isNotEmpty() && it.trim().equals(email.trim(), ignoreCase = true) }
