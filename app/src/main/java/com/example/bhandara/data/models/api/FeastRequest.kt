package com.example.bhandara.data.models.api

import com.google.gson.annotations.SerializedName

data class FeastRequest(
    @SerializedName("firebaseUid")
    val firebaseUid: String,
    
    @SerializedName("organizerName")
    val organizerName: String?,
    
    @SerializedName("contactPhone")
    val contactPhone: String?,
    
    @SerializedName("menuItems")
    val menuItems: List<String>,
    
    @SerializedName("foodType")
    val foodType: String?,
    
    @SerializedName("description")
    val description: String?,
    
    @SerializedName("imageUrls")
    val imageUrls: List<String>,
    
    @SerializedName("feastDate")
    val feastDate: String,  // Format: "2026-01-10"
    
    @SerializedName("startTime")
    val startTime: String,  // Format: "12:00:00"
    
    @SerializedName("endTime")
    val endTime: String,    // Format: "15:00:00"
    
    @SerializedName("latitude")
    val latitude: Double,
    
    @SerializedName("longitude")
    val longitude: Double,
    
    @SerializedName("address")
    val address: String?,
    
    @SerializedName("landmark")
    val landmark: String?,
    
    @SerializedName("estimatedCapacity")
    val estimatedCapacity: Int?
)
