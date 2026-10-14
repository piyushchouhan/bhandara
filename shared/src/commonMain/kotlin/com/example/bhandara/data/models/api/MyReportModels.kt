package com.example.bhandara.data.models.api

import kotlinx.serialization.Serializable

/** One report the user sent (api/reports/mine), about a shop, a review or a feast, and where it stands */
@Serializable
data class MyReport(
    /** SHOP, REVIEW or FEAST */
    val kind: String,
    val reportId: String,
    /** A shop report's reason (WRONG_LOCATION, ...), SUGGEST_DELETE, a feast report's reason, or null (reviews, older reports) */
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
    /** What the user said ("What you said"): the note sent with the report first, then anything added later */
    val notes: List<ReportNote> = emptyList(),
)

/** One thing the reporter said about their report: a note, photos or both */
@Serializable
data class ReportNote(
    val text: String? = null,
    val photoUrls: List<String> = emptyList(),
    val createdAt: String? = null,
)

/** A note and photos sent with a review report, or added to any report later ("Add more info") */
@Serializable
data class ReportDetails(
    val note: String? = null,
    val photoUrls: List<String>? = null,
)

/** Why the user reports a feast, with an optional note and photos */
@Serializable
data class FeastReportRequest(
    val reason: String,
    val note: String? = null,
    val photoUrls: List<String>? = null,
)

/** Why a feast is reported; the most common reason is shown to its organizer if it's taken down */
enum class FeastReportReason { WRONG_LOCATION, WRONG_TIME, NOT_HAPPENING, ASKING_FOR_MONEY, INAPPROPRIATE }
