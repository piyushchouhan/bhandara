package com.example.bhandara.data.models

import com.google.firebase.firestore.GeoPoint

data class Feast(
    val id: String = "",
    val photoUrls: List<String> = emptyList(),
    val location: GeoPoint? = null,
    val address: String = "",
    val timing: String = "",
    val isActive: Boolean = true,
    val menu: String = "",
    val reportedBy: String = "", // User UID
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
