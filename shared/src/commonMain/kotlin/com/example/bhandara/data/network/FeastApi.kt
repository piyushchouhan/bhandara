package com.example.bhandara.data.network

import com.example.bhandara.data.location.GeoLocation
import com.example.bhandara.data.models.api.FeastRequest
import com.example.bhandara.data.models.api.FeastResponse
import io.ktor.client.request.parameter
import io.ktor.client.request.setBody
import io.ktor.http.HttpMethod

/** Community feasts (bhandaras): posting, finding and flagging fake ones, and the organizer's own feasts */
class FeastApi(private val client: ApiClient) {

    /** Feasts on now or coming up near the user, with their distance; ended ones are left out */
    suspend fun getNearby(near: GeoLocation, radiusMeters: Double = 5_000.0): ApiResult<List<FeastResponse>> =
        client.call("api/feasts/nearby") {
            method = HttpMethod.Get
            parameter("lat", near.latitude)
            parameter("lon", near.longitude)
            parameter("radius", radiusMeters)
        }

    /** Posts a new feast; everyone nearby is told about it. Limited to a few per hour per user (429 after that). */
    suspend fun createFeast(feast: FeastRequest): ApiResult<FeastResponse> =
        client.call("api/feasts") {
            method = HttpMethod.Post
            setBody(feast)
        }

    suspend fun getFeast(feastId: String): ApiResult<FeastResponse> =
        client.call("api/feasts/$feastId") { method = HttpMethod.Get }

    /** The signed-in user's feasts with their status, latest date first */
    suspend fun getMyFeasts(): ApiResult<List<FeastResponse>> =
        client.call("api/feasts/mine") { method = HttpMethod.Get }

    /** The organizer changes a feast that hasn't ended; nobody is notified again */
    suspend fun updateFeast(feastId: String, feast: FeastRequest): ApiResult<FeastResponse> =
        client.call("api/feasts/$feastId") {
            method = HttpMethod.Put
            setBody(feast)
        }

    /** The organizer ends a feast now (the food ran out): it leaves the map */
    suspend fun endFeastEarly(feastId: String): ApiResult<FeastResponse> =
        client.call("api/feasts/$feastId/end") { method = HttpMethod.Post }

    /** The organizer takes a feast down: it leaves the map and their list */
    suspend fun takeDownFeast(feastId: String): ApiResult<Unit> =
        client.callForNothing("api/feasts/$feastId") { method = HttpMethod.Delete }

    /** Flags a fake or inappropriate feast; each user counts once, and enough reports take it down */
    suspend fun reportFeast(feastId: String): ApiResult<FeastResponse> =
        client.call("api/feasts/$feastId/report") { method = HttpMethod.Put }
}
