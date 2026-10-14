package com.example.bhandara.data.models.api

import kotlinx.serialization.Serializable

/**
 * Response model for user creation
 */
@Serializable
data class CreateUserResponse(
    val id: String?,
    
    val firebaseUid: String?,
    
    val fcmToken: String?,
    
    val latitude: Double?,
    
    val longitude: Double?,
    
    val createdAt: String?,
    
    val message: String?
)
