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
    
    val createdAt: String? = null,
    
    val updatedAt: String? = null,
    
    val createdBy: String? = null,
    
    val updatedBy: String? = null,
    
    // For backward compatibility with create feast response
    val message: String? = null
)
