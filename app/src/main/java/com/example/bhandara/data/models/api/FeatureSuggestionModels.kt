package com.example.bhandara.data.models.api

import com.google.gson.annotations.SerializedName

data class FeatureSuggestionRequest(
    @SerializedName("feature")
    val feature: String
)

data class FeatureSuggestionResponse(
    @SerializedName("id")
    val id: Int,
    @SerializedName("userId")
    val userId: String,
    @SerializedName("feature")
    val feature: String,
    @SerializedName("createdAt")
    val createdAt: String,
    @SerializedName("createdBy")
    val createdBy: String
)
