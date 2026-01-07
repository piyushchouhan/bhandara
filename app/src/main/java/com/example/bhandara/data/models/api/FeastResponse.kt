package com.example.bhandara.data.models.api

import com.google.gson.annotations.SerializedName

data class FeastResponse(
    @SerializedName("id")
    val id: Long,
    
    @SerializedName("message")
    val message: String
)
