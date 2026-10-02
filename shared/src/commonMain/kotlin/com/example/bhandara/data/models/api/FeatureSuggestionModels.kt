package com.example.bhandara.data.models.api

import kotlinx.serialization.Serializable

@Serializable
data class FeatureSuggestionRequest(
    val feature: String
)

@Serializable
data class FeatureSuggestionResponse(
    val id: Int,
    val userId: String,
    val feature: String,
    val createdAt: String,
    val createdBy: String
)
