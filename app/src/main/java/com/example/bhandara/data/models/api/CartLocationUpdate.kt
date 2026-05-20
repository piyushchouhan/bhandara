package com.example.bhandara.data.models.api

import com.google.gson.annotations.SerializedName

data class CartLocationUpdate(
    @SerializedName("shopId")
    val shopId: Long,

    @SerializedName("ownerUid")
    val ownerUid: String,

    @SerializedName("lat")
    val lat: Double,

    @SerializedName("lng")
    val lng: Double,

    @SerializedName("speed")
    val speed: Double = 0.0
)
