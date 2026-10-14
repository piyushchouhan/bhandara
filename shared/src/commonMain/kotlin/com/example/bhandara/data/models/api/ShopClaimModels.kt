package com.example.bhandara.data.models.api

import kotlinx.serialization.Serializable

@Serializable
data class ShopClaimRequest(
    val shopId: Long,
    val imageUrls: List<String>,
    val latitude: Double,
    val longitude: Double,
    val ownerPhone: String? = null
)

@Serializable
data class ShopClaimResponse(
    val id: Long,
    val shopId: Long,
    val shopName: String? = null,
    val status: String, // PENDING_REVIEW, APPROVED, REJECTED
    val decisionReason: String? = null,
    val distanceMeters: Double? = null,
    val createdAt: String? = null
)

/** Either the claim, or a message explaining why it couldn't be submitted */
@Serializable
data class ShopClaimResult(
    val claim: ShopClaimResponse? = null,
    val errorMessage: String? = null
)
