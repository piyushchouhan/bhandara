package com.example.bhandara.data.network

import com.example.bhandara.data.models.api.ShopClaimRequest
import com.example.bhandara.data.models.api.ShopClaimResponse
import io.ktor.client.request.setBody
import io.ktor.http.HttpMethod

/** Real owners taking over shops the platform listed */
class ClaimApi(private val client: ApiClient) {

    /**
     * Claims a shop with photos of its front taken there and where the user is. The backend approves it straight
     * away, rejects it, or sends it to an admin. A shop that already has an owner, a claim already waiting, or too
     * many claims in a day is a 409.
     */
    suspend fun claimShop(claim: ShopClaimRequest): ApiResult<ShopClaimResponse> =
        client.call("api/shop-claims") {
            method = HttpMethod.Post
            setBody(claim)
        }
}
