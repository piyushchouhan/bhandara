package com.example.bhandara.data.models.api

import kotlinx.serialization.Serializable

@Serializable
data class LocalShopRequest(
    val ownerUid: String,
    val ownerPhone: String? = null,
    val ownerEmail: String? = null,
    
    val shopName: String,
    val shopType: String,
    val cuisineType: String? = null,
    val description: String? = null,
    
    val menuItems: List<String>,
    val priceRange: String? = null,
    val averageCostForTwo: Int? = null,
    
    val imageUrls: List<String>? = null,
    
    val latitude: Double,
    val longitude: Double,
    val fullAddress: String? = null,
    val landmark: String? = null,
    val area: String? = null,
    val city: String? = null,
    val state: String? = null,
    val pincode: String? = null,
    
    val openingTime: String? = null, // "HH:mm:ss"
    val closingTime: String? = null, // "HH:mm:ss"
    val operatingDays: String? = null,
    
    val homeDelivery: Boolean? = false,
    val takeaway: Boolean? = true,
    val hasSeating: Boolean? = true,
    val onlinePayment: Boolean? = false,
    val parking: String? = null,
    val wifiAvailable: Boolean? = false,
    val isMovingCart: Boolean? = false,
    val mapIcon: String? = null,
    val cartDetails: CartDetails? = null,
)

/**
 * What only a moving cart has: what it's on (HAND_CART, CYCLE, SCOOTER, TEMPO, ON_FOOT), what it sells, its usual
 * round and days (MON … SUN), how to pay (CASH, UPI, CARD) and how to spot it
 *
 * @param options extra settings of what it sells, by "category/option" (e.g. "Milk & dairy/monthly_bill")
 */
@Serializable
data class CartDetails(
    val cartType: String? = null,
    val sells: List<String> = emptyList(),
    val stops: List<CartStopDetails> = emptyList(),
    val noFixedRoute: Boolean = false,
    val daysOut: List<String> = emptyList(),
    val takesOrders: Boolean = false,
    val payments: List<String> = emptyList(),
    val spotHint: String? = null,
    val seasonal: Boolean = false,
    val options: Map<String, Boolean> = emptyMap(),
)

/** A usual stop on a cart's round; [from] / [to] as "HH:mm:ss", both absent when [timesVary] */
@Serializable
data class CartStopDetails(
    val name: String,
    val from: String? = null,
    val to: String? = null,
    val timesVary: Boolean = false,
)

@Serializable
data class LocalShopResponse(
    val id: String,
    val ownerUid: String? = null,
    val ownerPhone: String? = null,
    val ownerEmail: String? = null,
    val ownerClaimed: Boolean? = null, // false = listed by the platform, the real owner can still claim it
    val shopName: String,
    val shopType: String? = null,
    val cuisineType: String? = null,
    val description: String? = null,
    val menuItems: List<String>? = null,
    val priceRange: String? = null,
    val averageCostForTwo: Int? = null,
    val imageUrls: List<String>? = null,
    val latitude: Double,
    val longitude: Double,
    val fullAddress: String? = null,
    val landmark: String? = null,
    val area: String? = null,
    val city: String? = null,
    val state: String? = null,
    val pincode: String? = null,
    val openingTime: String? = null,
    val closingTime: String? = null,
    val operatingDays: String? = null,
    val isCurrentlyOpen: Boolean? = null,
    val crowdLevel: String? = null, // QUIET, MODERATE or BUSY right now; null for moving carts
    val homeDelivery: Boolean? = null,
    val takeaway: Boolean? = null,
    val hasSeating: Boolean? = null,
    val onlinePayment: Boolean? = null,
    val parking: String? = null,
    val wifiAvailable: Boolean? = null,
    val averageRating: Double? = null,
    val totalRatings: Int? = null,
    val totalReviews: Int? = null,
    val distance: Double? = null,
    val isActive: Boolean? = null,
    val isVerified: Boolean? = null,
    val verificationStatus: String? = null,
    val reportCount: Int? = null,
    val createdAt: String? = null,
    val updatedAt: String? = null,
    val createdBy: String? = null,
    val updatedBy: String? = null,
    val isMovingCart: Boolean? = null,
    val mapIcon: String? = null,
    val score: Double? = null,
    val cartDetails: CartDetails? = null,
)

@Serializable
data class ReviewRequest(
    val shopId: String? = null, // Optional for PUT
    val rating: Int,
    val comment: String? = null,
    val imageUrls: List<String>? = null
)

@Serializable
data class ReviewResponse(
    val id: String,
    val shopId: String,
    val reviewerUid: String,
    val rating: Int,
    val comment: String? = null,
    val imageUrls: List<String>? = null,
    val isActive: Boolean? = null,
    /** A moderator removed it after reports; only its author still sees it (in My reviews) */
    val removedByModerator: Boolean? = null,
    /** The reviewed shop, for lists of one person's reviews */
    val shopName: String? = null,
    val shopArea: String? = null,
    val shopImageUrl: String? = null,
    val shopIsMovingCart: Boolean? = null,
    val createdAt: String? = null,
    val updatedAt: String? = null,
    val createdBy: String? = null,
    val updatedBy: String? = null
)

@Serializable
data class GenericResponse(
    val message: String
)

// --- Menu Items ---

@Serializable
data class MenuItemRequest(
    val name: String,
    val foodType: String,
    val price: Double? = null,
    val isAvailable: Boolean = true,
    /** What the price is for, e.g. "kg", "dozen" (carts) */
    val unit: String? = null,
    /** The owner's grouping, e.g. "Sabzi" */
    val category: String? = null,
    /** Only sold from this month to that one (1 = January, may wrap over the new year); null all year */
    val seasonFromMonth: Int? = null,
    val seasonToMonth: Int? = null,
)

@Serializable
data class ManualMenuItemsRequest(
    val items: List<MenuItemRequest>
)

@Serializable
data class MenuItemResponse(
    val id: String,
    val shopId: String,
    val name: String,
    val foodType: String,
    val price: Double? = null,
    val isAvailable: Boolean,
    val unit: String? = null,
    val category: String? = null,
    val seasonFromMonth: Int? = null,
    val seasonToMonth: Int? = null,
    /** When the price was last set or changed */
    val priceUpdatedAt: String? = null,
    val isActive: Boolean? = null,
    val source: String? = null,
    val confidence: Double? = null,
    val createdAt: String? = null,
    val updatedAt: String? = null,
    val createdBy: String? = null,
    val updatedBy: String? = null
)

@Serializable
data class MenuItemExtractResponse(
    val items: List<MenuItemRequest>
)

@Serializable
data class FeedResponse(
    val activeShops: List<LocalShopResponse>,
    val verificationPrompt: LocalShopResponse?,
    /** Fixed shops standing close together (a "khau galli"), drawn as one shape; older backends send none */
    val zones: List<ShopZone> = emptyList(),
)

/** A group of 5-10 nearby fixed shops, drawn on the map as one padded outline with an "N shops" label */
@Serializable
data class ShopZone(
    /** Stable while the same shops are in the zone */
    val id: String,
    val shopIds: List<String>,
    val shopCount: Int,
    /** Where the label goes */
    val centerLatitude: Double,
    val centerLongitude: Double,
    /** The outline's corners as [latitude, longitude] pairs */
    val outline: List<List<Double>> = emptyList(),
    /** A name such as "Chaat Gali", once one has been made for the zone */
    val name: String? = null,
    val nameHi: String? = null,
    val nameMr: String? = null,
    /** STREET_FOOD, FISH_MARKET, VEG_MARKET, SWEETS, TEA_SNACKS or MIXED */
    val type: String? = null,
) {
    /** The name in the app's language ("en", "hi", "mr"), else in English */
    fun nameIn(language: String): String? = when (language) {
        "hi" -> nameHi ?: name
        "mr" -> nameMr ?: name
        else -> name
    }?.takeIf { it.isNotBlank() }
}

@Serializable
data class VerifyShopRequest(
    val vote: String,
    // Where the voter is right now; YES/NO votes only count from people near the shop or live cart
    val latitude: Double? = null,
    val longitude: Double? = null
)

/** Why a user reports a shop; sent as the code (e.g. WRONG_LOCATION) so moderators see it in any language */
enum class ShopReportReason { NOT_FOOD_SHOP, WRONG_LOCATION, INAPPROPRIATE_CONTENT, PERMANENTLY_CLOSED }

@Serializable
data class ReportShopRequest(val reason: String)

/** Outcome of a verification vote */
@Serializable
data class VerifyShopResult(
    val success: Boolean,
    val alreadyVoted: Boolean = false,
    val message: String? = null
)
