package com.example.bhandara.data.models.api

import kotlinx.serialization.Serializable

/**
 * Response model for Feast data from API
 * Used for both single feast and nearby feasts list
 */
@Serializable
data class FeastResponse(
    val id: String,
    
    val firebaseUid: String? = null,
    
    val organizerName: String? = null,
    
    val contactPhone: String? = null,
    
    val menuItems: List<String>,
    
    val foodType: String? = null,
    
    val description: String? = null,
    
    val imageUrls: List<String> = emptyList(),
    
    val feastDate: String,
    
    val startTime: String,
    
    val endTime: String,
    
    val latitude: Double,
    
    val longitude: Double,
    
    val address: String? = null,
    
    val landmark: String? = null,
    
    val distance: Double? = null, // Distance in meters (only in nearby endpoint)
    
    val estimatedCapacity: Int,
    
    val isActive: Boolean = true,
    
    val isVerified: Boolean = false,

    /** In the organizer's own list (api/feasts/mine): UPCOMING, PAST or TAKEN_DOWN (hidden after reports) */
    val status: String? = null,

    /** When the organizer ended it early (e.g. the food ran out) */
    val endedEarlyAt: String? = null,

    /** TAKEN_DOWN only: the reason most reporters gave (a FeastReportReason name), or REPORTED when none said */
    val takedownReason: String? = null,

    /** TAKEN_DOWN only: PENDING (fixed and sent back, a moderator checks it) or REJECTED (it stays down) */
    val resubmission: String? = null,

    /** Whether the organizer can still fix it and send it back */
    val canResubmit: Boolean = false,
    
    val createdAt: String? = null,
    
    val updatedAt: String? = null,
    
    val createdBy: String? = null,
    
    val updatedBy: String? = null,
    
    // For backward compatibility with create feast response
    val message: String? = null
)
