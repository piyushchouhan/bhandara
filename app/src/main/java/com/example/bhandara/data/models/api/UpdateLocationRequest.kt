package com.example.bhandara.data.models.api

import com.google.gson.annotations.SerializedName

/**
 * Request model for updating user location
 */
data class UpdateLocationRequest(
    @SerializedName("firebaseUid")
    val firebaseUid: String,
    
    @SerializedName("latitude")
    val latitude: Double,
    
    @SerializedName("longitude")
    val longitude: Double,
    
    @SerializedName("fcmToken")
    val fcmToken: String
)
