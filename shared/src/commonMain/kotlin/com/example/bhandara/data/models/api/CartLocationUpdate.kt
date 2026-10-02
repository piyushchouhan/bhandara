package com.example.bhandara.data.models.api

import kotlinx.serialization.Serializable

@Serializable
data class CartLocationUpdate(
    val shopId: Long,

    val ownerUid: String,

    val lat: Double,

    val lng: Double,

    val speed: Double = 0.0
)
