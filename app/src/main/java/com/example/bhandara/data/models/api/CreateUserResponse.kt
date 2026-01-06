package com.example.bhandara.data.models.api

import com.google.gson.annotations.SerializedName

/**
 * Response model for user creation
 */
data class CreateUserResponse(
    @SerializedName("id")
    val id: String?,
    
    @SerializedName("firebaseUid")
    val firebaseUid: String?,
    
    @SerializedName("fcmToken")
    val fcmToken: String?,
    
    @SerializedName("latitude")
    val latitude: Double?,
    
    @SerializedName("longitude")
    val longitude: Double?,
    
    @SerializedName("createdAt")
    val createdAt: String?,
    
    @SerializedName("message")
    val message: String?
)
