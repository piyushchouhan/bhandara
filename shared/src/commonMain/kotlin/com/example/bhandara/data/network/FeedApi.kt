package com.example.bhandara.data.network

import com.example.bhandara.data.location.GeoLocation
import com.example.bhandara.data.models.api.FeedResponse
import io.ktor.client.request.parameter
import io.ktor.http.HttpMethod

/** What's around the user: open shops and live carts (best first), and maybe one "Is this place real?" question */
class FeedApi(private val client: ApiClient) {

    suspend fun getFeed(near: GeoLocation, radiusMeters: Double = 5_000.0): ApiResult<FeedResponse> =
        client.call("api/feed") {
            method = HttpMethod.Get
            parameter("lat", near.latitude)
            parameter("lon", near.longitude)
            parameter("radius", radiusMeters)
        }
}
