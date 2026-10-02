package com.example.bhandara.data.models.api

import kotlinx.serialization.Serializable

/**
 * Request model for updating user location
 */
@Serializable
data class UpdateLocationRequest(
    val firebaseUid: String,
    
    val latitude: Double,
    
    val longitude: Double,
    
    val fcmToken: String
)
