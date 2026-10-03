package com.example.bhandara.data.network

import com.example.bhandara.data.models.api.ReviewRequest
import com.example.bhandara.data.models.api.ReviewResponse
import io.ktor.client.request.setBody
import io.ktor.http.HttpMethod

/** Shop reviews: one per customer per shop, editable and deletable by its author */
class ReviewApi(private val client: ApiClient) {

    /** Reviews shown on the shop's page, newest first */
    suspend fun getReviews(shopId: String): ApiResult<List<ReviewResponse>> =
        client.call("api/reviews/shop/$shopId") { method = HttpMethod.Get }

    suspend fun createReview(review: ReviewRequest): ApiResult<ReviewResponse> =
        client.call("api/reviews") {
            method = HttpMethod.Post
            setBody(review)
        }

    suspend fun updateReview(reviewId: String, review: ReviewRequest): ApiResult<ReviewResponse> =
        client.call("api/reviews/$reviewId") {
            method = HttpMethod.Put
            setBody(review)
        }

    suspend fun deleteReview(reviewId: String): ApiResult<Unit> =
        client.callForNothing("api/reviews/$reviewId") { method = HttpMethod.Delete }

    /** Flag a review; a moderator decides whether to hide it. Each user counts once. */
    suspend fun reportReview(reviewId: String): ApiResult<ReviewResponse> =
        client.call("api/reviews/$reviewId/report") { method = HttpMethod.Put }
}
