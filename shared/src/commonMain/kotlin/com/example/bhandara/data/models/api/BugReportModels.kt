package com.example.bhandara.data.models.api

import kotlinx.serialization.Serializable

@Serializable
data class BugReportRequest(
    val bug: String
)

@Serializable
data class BugReportResponse(
    val id: Int,
    val userId: String,
    val bug: String,
    val createdAt: String,
    val createdBy: String
)
