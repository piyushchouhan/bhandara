package com.example.bhandara.data.models.api

import kotlinx.serialization.Serializable

/** One report the user sent (api/reports/mine), about a shop, a review or a feast, and where it stands */
@Serializable
data class MyReport(
    /** SHOP, REVIEW or FEAST */
    val kind: String,
    val reportId: String,
    /** A shop report's reason (WRONG_LOCATION, ...), SUGGEST_DELETE, or null for reviews and feasts */
    val reason: String? = null,
    /** IN_REVIEW, FIXED or NO_ACTION */
    val status: String,
    val targetName: String? = null,
    val targetArea: String? = null,
    /** The shop to open (for a review, the shop it's on) */
    val shopId: String? = null,
    val feastId: String? = null,
    /** How many people reported the same thing */
    val reporterCount: Long = 0,
    val createdAt: String? = null,
    val resolvedAt: String? = null,
)
