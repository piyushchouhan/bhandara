package com.example.bhandara.data.models.api

import com.google.gson.annotations.SerializedName

/**
 * Response model for location update
 */
data class UpdateLocationResponse(
    @SerializedName("firebaseUid")
    val firebaseUid: String?,
    
    @SerializedName("latitude")
    val latitude: Double?,
    
    @SerializedName("longitude")
    val longitude: Double?,
    
    @SerializedName("updatedAt")
    val updatedAt: String?,
    
    @SerializedName("message")
    val message: String?
)
