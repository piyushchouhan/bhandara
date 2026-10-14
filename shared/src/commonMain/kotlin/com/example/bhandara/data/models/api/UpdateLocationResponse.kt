package com.example.bhandara.data.models.api

import kotlinx.serialization.Serializable

/**
 * Response model for location update
 */
@Serializable
data class UpdateLocationResponse(
    val firebaseUid: String?,
    
    val latitude: Double?,
    
    val longitude: Double?,
    
    val updatedAt: String?,
    
    val message: String?
)
