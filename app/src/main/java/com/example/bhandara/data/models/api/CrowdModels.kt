package com.example.bhandara.data.models.api

import com.google.gson.annotations.SerializedName

/** Body for POST /api/crowd/ping */
data class CrowdPingRequest(
    val latitude: Double,
    val longitude: Double
)

/** Response from POST /api/crowd/ping */
data class CrowdPingResponse(
    val token: String
)

