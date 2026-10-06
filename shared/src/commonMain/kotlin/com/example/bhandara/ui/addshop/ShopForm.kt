package com.example.bhandara.ui.addshop

import com.example.bhandara.data.models.api.MenuItemRequest
import com.example.bhandara.data.settings.AddShopDraft
import com.example.bhandara.photos.PickedPhoto
import com.example.bhandara.ui.reportfeast.normalizePhone

// The backend's limits for a shop (LocalShopRequestDTO)
const val MAX_SHOP_PHOTOS = 20
const val MAX_SHOP_MENU_NAMES = 100
const val MAX_SHOP_DESCRIPTION = 1000
const val MAX_SHOP_ADDRESS = 500
const val MAX_SHOP_LANDMARK = 255
const val MAX_COST_FOR_TWO = 50_000

/** Kinds of shop people pick from; sent as these names, shown in the user's language */
val SHOP_TYPES = listOf("Restaurant", "Food Truck", "Street Food", "Cafe", "Fast Food", "Bakery", "Sweet Shop")

/** The four steps of adding a shop: what it is, the basics, the detailed menu, and the rest */
enum class ShopStep { KIND, BASICS, MENU, DETAILS }

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
) {
    /** Every dish named, from the quick list and the detailed menu, each once */
    val allMenuNames: List<String>
        get() = (menuNames + menuInput.trim() + detailedMenu.map { it.name })
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

data class ShopProblem(val step: ShopStep, val message: String)

private val EMAIL = Regex("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")

/** The first problem with the given steps of the form, in order; null when they're fine */
fun ShopForm.problem(steps: Collection<ShopStep> = ShopStep.entries): ShopProblem? {
    if (ShopStep.KIND in steps && isMovingCart == null) {
        return ShopProblem(ShopStep.KIND, "Say whether it's a moving cart or a shop")
    }
    if (ShopStep.BASICS in steps) {
        val name = shopName.trim()
        if (name.isEmpty()) return ShopProblem(ShopStep.BASICS, "Enter the shop's name")
        if (name.length < 2) return ShopProblem(ShopStep.BASICS, "The shop's name is too short")
        if (shopType.isBlank()) return ShopProblem(ShopStep.BASICS, "Choose the kind of shop")
        if (allMenuNames.isEmpty()) return ShopProblem(ShopStep.BASICS, "Add at least one dish it serves")
        if (allMenuNames.size > MAX_SHOP_MENU_NAMES) return ShopProblem(ShopStep.BASICS, "A shop can list up to $MAX_SHOP_MENU_NAMES dishes")
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
