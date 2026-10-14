package com.example.bhandara.data.network

import io.ktor.client.request.setBody
import io.ktor.http.HttpMethod
import kotlinx.serialization.Serializable

/** What a customer does with a shop, feast or review photo, counted for its owner (each person once a day) */
enum class ActivityAction { VIEW, DIRECTIONS, CALL }

/** A shop's views, directions and calls this week, for its owner */
@Serializable
data class ShopWeekStats(val views: Long = 0, val directions: Long = 0, val calls: Long = 0)

/** The numbers on the user's profile */
@Serializable
data class UserStats(val reviews: Long = 0, val photos: Long = 0, val feasts: Long = 0, val verified: Long = 0)

/**
 * Tells the backend that the user looked at something, asked for directions or called, so owners, organizers and
 * reviewers see their reach. Best effort: a failure changes nothing for the user. The backend leaves out an owner's
 * own activity and counts each person once a day.
 */
class ActivityApi(private val client: ApiClient) {

    @Serializable
    private data class ActivityRequest(val targetKind: String, val targetId: Long, val action: String, val photoUrl: String? = null)

    suspend fun shop(shopId: String, action: ActivityAction): ApiResult<Unit> = record("SHOP", shopId, action)

    suspend fun feast(feastId: String, action: ActivityAction): ApiResult<Unit> = record("FEAST", feastId, action)

    /** Someone opened a photo of a review */
    suspend fun reviewPhoto(reviewId: String, photoUrl: String): ApiResult<Unit> =
        record("REVIEW_PHOTO", reviewId, ActivityAction.VIEW, photoUrl)

    private suspend fun record(kind: String, id: String, action: ActivityAction, photoUrl: String? = null): ApiResult<Unit> {
        // Places from older data or a bad link aren't worth a request
        val targetId = id.toLongOrNull() ?: return ApiResult.Success(Unit)
        return client.callForNothing("api/activity") {
            method = HttpMethod.Post
            setBody(ActivityRequest(kind, targetId, action.name, photoUrl))
        }
    }

    /** The owner's shop this week: views, directions and calls */
    suspend fun shopWeek(shopId: String): ApiResult<ShopWeekStats> =
        client.call("api/localshops/$shopId/stats") { method = HttpMethod.Get }

    /** Reviews, photos, feasts and shops verified, for the profile */
    suspend fun myStats(): ApiResult<UserStats> = client.call("api/users/me/stats") { method = HttpMethod.Get }
}
