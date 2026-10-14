package com.example.bhandara.data.models.api

import kotlinx.serialization.Serializable

@Serializable
data class FeastRequest(
    val firebaseUid: String,
    
    val organizerName: String?,
    
    val contactPhone: String?,
    
    val menuItems: List<String>,
    
    val foodType: String?,
    
    val description: String?,
    
    val imageUrls: List<String>,
    
    val feastDate: String,  // Format: "2026-01-10"
    
    val startTime: String,  // Format: "12:00:00"
    
    val endTime: String,    // Format: "15:00:00"
    
    val latitude: Double,
    
    val longitude: Double,
    
    val address: String?,
    
    val landmark: String?,
    
    val estimatedCapacity: Int?
)
