package com.example.bhandara.data.settings

import kotlinx.serialization.Serializable

/**
 * The "add a shop" form as the user left it, so nothing typed is lost if they leave or the app is closed before
 * the shop is submitted. The detailed menu (with prices) is kept separately in [AppSettings.draftMenuItems].
 *
 * @param photoUris where the chosen photos are on the phone; some may no longer be readable later
 */
@Serializable
data class AddShopDraft(
    val isMovingCart: Boolean? = null,
    val shopName: String = "",
    val shopType: String = "",
    val menuItemNames: List<String> = emptyList(),
    val photoUris: List<String> = emptyList(),
    val ownerPhone: String = "",
    val ownerEmail: String = "",
    val cuisineType: String = "",
    val description: String = "",
    val averageCostForTwo: String = "",
    val priceRange: String = "",
    val fullAddress: String = "",
    val landmark: String = "",
    val homeDelivery: Boolean = false,
    val takeaway: Boolean = true,
    val hasSeating: Boolean = true,
    val wifiAvailable: Boolean = false,
) {
    /** Nothing entered yet: an untouched form */
    val isEmpty: Boolean get() = this == AddShopDraft()
}
