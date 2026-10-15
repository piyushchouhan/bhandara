package com.example.bhandara.data.models.api

import kotlinx.serialization.Serializable

/**
 * Request model for creating a user in the backend
 */
@Serializable
data class CreateUserRequest(
    val firebaseUid: String,
    
    /** Null for a phone that can't get push notifications yet (e.g. an iPhone until push is set up) */
    val fcmToken: String? = null,
    
    val latitude: Double,
    
    val longitude: Double
)
