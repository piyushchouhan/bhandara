package com.example.bhandara.data.models.api

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
    val mapIcon: String? = null
)

data class LocalShopResponse(
    val id: String,
    val ownerUid: String? = null,
    val ownerPhone: String? = null,
    val ownerEmail: String? = null,
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
    val mapIcon: String? = null
)

data class ReviewRequest(
    val shopId: String? = null, // Optional for PUT
    val rating: Int,
    val comment: String? = null,
    val imageUrls: List<String>? = null
)

data class ReviewResponse(
    val id: String,
    val shopId: String,
    val reviewerUid: String,
    val rating: Int,
    val comment: String? = null,
    val imageUrls: List<String>? = null,
    val isActive: Boolean? = null,
    val createdAt: String? = null,
    val updatedAt: String? = null,
    val createdBy: String? = null,
    val updatedBy: String? = null
)

data class GenericResponse(
    val message: String
)

// --- Menu Items ---

data class MenuItemRequest(
    val name: String,
    val foodType: String,
    val price: Double? = null,
    val isAvailable: Boolean = true
)

data class ManualMenuItemsRequest(
    val items: List<MenuItemRequest>
)

data class MenuItemResponse(
    val id: String,
    val shopId: String,
    val name: String,
    val foodType: String,
    val price: Double? = null,
    val isAvailable: Boolean,
    val isActive: Boolean? = null,
    val source: String? = null,
    val confidence: Double? = null,
    val createdAt: String? = null,
    val updatedAt: String? = null,
    val createdBy: String? = null,
    val updatedBy: String? = null
)

data class MenuItemExtractResponse(
    val items: List<MenuItemRequest>
)
