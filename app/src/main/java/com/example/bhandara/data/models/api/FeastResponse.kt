package com.example.bhandara.data.models.api

import com.google.gson.annotations.SerializedName

/**
 * Response model for Feast data from API
 * Used for both single feast and nearby feasts list
 */
data class FeastResponse(
    @SerializedName("id")
    val id: String,
    
    @SerializedName("firebaseUid")
    val firebaseUid: String? = null,
    
    @SerializedName("organizerName")
    val organizerName: String,
    
    @SerializedName("contactPhone")
    val contactPhone: String,
    
    @SerializedName("menuItems")
    val menuItems: List<String>,
    
    @SerializedName("foodType")
    val foodType: String,
    
    @SerializedName("description")
    val description: String,
    
    @SerializedName("imageUrls")
    val imageUrls: List<String> = emptyList(),
    
    @SerializedName("feastDate")
    val feastDate: String,
    
    @SerializedName("startTime")
    val startTime: String,
    
    @SerializedName("endTime")
    val endTime: String,
    
    @SerializedName("latitude")
    val latitude: Double,
    
    @SerializedName("longitude")
    val longitude: Double,
    
    @SerializedName("address")
    val address: String,
    
    @SerializedName("landmark")
    val landmark: String? = null,
    
    @SerializedName("distance")
    val distance: Double? = null, // Distance in meters (only in nearby endpoint)
    
    @SerializedName("estimatedCapacity")
    val estimatedCapacity: Int,
    
    @SerializedName("isActive")
    val isActive: Boolean = true,
    
    @SerializedName("isVerified")
    val isVerified: Boolean = false,
    
    @SerializedName("createdAt")
    val createdAt: String? = null,
    
    @SerializedName("updatedAt")
    val updatedAt: String? = null,
    
    @SerializedName("createdBy")
    val createdBy: String? = null,
    
    @SerializedName("updatedBy")
    val updatedBy: String? = null,
    
    // For backward compatibility with create feast response
    @SerializedName("message")
    val message: String? = null
)
