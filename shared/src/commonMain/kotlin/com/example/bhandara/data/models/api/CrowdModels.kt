package com.example.bhandara.data.models.api

import kotlinx.serialization.Serializable

/** Body for POST /api/crowd/ping */
@Serializable
data class CrowdPingRequest(
    val latitude: Double,
    val longitude: Double
)

/** Response from POST /api/crowd/ping */
@Serializable
data class CrowdPingResponse(
    val token: String
)

