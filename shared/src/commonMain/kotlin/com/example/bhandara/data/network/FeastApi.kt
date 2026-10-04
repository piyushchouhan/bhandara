package com.example.bhandara.data.network

import com.example.bhandara.data.location.GeoLocation
import com.example.bhandara.data.models.api.FeastResponse
import io.ktor.client.request.parameter
import io.ktor.http.HttpMethod

/** Community feasts (bhandaras): finding them and flagging fake ones */
class FeastApi(private val client: ApiClient) {

    /** Feasts on now or coming up near the user, with their distance; ended ones are left out */
    suspend fun getNearby(near: GeoLocation, radiusMeters: Double = 5_000.0): ApiResult<List<FeastResponse>> =
        client.call("api/feasts/nearby") {
            method = HttpMethod.Get
            parameter("lat", near.latitude)
            parameter("lon", near.longitude)
            parameter("radius", radiusMeters)
        }

    suspend fun getFeast(feastId: String): ApiResult<FeastResponse> =
        client.call("api/feasts/$feastId") { method = HttpMethod.Get }

    /** Flags a fake or inappropriate feast; each user counts once, and enough reports take it down */
    suspend fun reportFeast(feastId: String): ApiResult<FeastResponse> =
        client.call("api/feasts/$feastId/report") { method = HttpMethod.Put }
}
