package com.example.bhandara.data.models.api

import kotlinx.serialization.Serializable

/** FSSAI numbers: registrations (small food businesses) start with 2, licences with 1 */
object FssaiKind {
    const val LICENCE = "LICENCE"
    const val REGISTRATION = "REGISTRATION"
}

/** What AI read from a certificate, for the owner to check; either may be missing */
@Serializable
data class FssaiRead(
    val licenceNumber: String? = null,
    val validTill: String? = null, // yyyy-MM-dd
    val kind: String? = null,
)

/** A licence the owner sent, and whether it was checked */
@Serializable
data class FssaiStatus(
    val id: Long,
    val shopId: Long,
    val licenceNumber: String,
    val kind: String = FssaiKind.LICENCE,
    val validTill: String,
    val status: String, // PENDING_REVIEW, VERIFIED, REJECTED or REPLACED
    val decisionReason: String? = null,
    val decidedAt: String? = null,
    val createdAt: String? = null,
) {
    companion object {
        const val PENDING_REVIEW = "PENDING_REVIEW"
        const val VERIFIED = "VERIFIED"
        const val REJECTED = "REJECTED"
    }
}

/** A shop's checked licence as anyone sees it on the shop page */
@Serializable
data class FssaiBadge(
    val maskedNumber: String, // "1152 3026 •••• 23"
    val kind: String = FssaiKind.LICENCE,
    val validTill: String, // yyyy-MM-dd
    val checkedOn: String, // yyyy-MM-dd
)
