package com.example.bhandara.data.models.api

import kotlinx.serialization.Serializable

/**
 * Request model for creating a user in the backend
 */
@Serializable
data class CreateUserRequest(
    val firebaseUid: String,
    
    val fcmToken: String,
    
    val latitude: Double,
    
    val longitude: Double
)
