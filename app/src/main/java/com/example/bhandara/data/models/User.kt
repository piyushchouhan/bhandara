package com.example.bhandara.data.models

import com.google.firebase.firestore.GeoPoint

data class User(
    val uid: String = "",
    val fcmToken: String = "",
    val location: GeoPoint? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val lastActive: Long = System.currentTimeMillis()
)
