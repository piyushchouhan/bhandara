package com.example.bhandara.data.models.api

import com.google.gson.annotations.SerializedName

/**
 * Request model for creating a user in the backend
 */
data class CreateUserRequest(
    @SerializedName("firebaseUid")
    val firebaseUid: String,
    
    @SerializedName("fcmToken")
    val fcmToken: String,
    
    @SerializedName("latitude")
    val latitude: Double,
    
    @SerializedName("longitude")
    val longitude: Double
)
