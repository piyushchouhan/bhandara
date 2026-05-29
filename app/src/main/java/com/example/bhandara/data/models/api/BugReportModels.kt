package com.example.bhandara.data.models.api

import com.google.gson.annotations.SerializedName

data class BugReportRequest(
    @SerializedName("bug")
    val bug: String
)

data class BugReportResponse(
    @SerializedName("id")
    val id: Int,
    @SerializedName("userId")
    val userId: String,
    @SerializedName("bug")
    val bug: String,
    @SerializedName("createdAt")
    val createdAt: String,
    @SerializedName("createdBy")
    val createdBy: String
)
