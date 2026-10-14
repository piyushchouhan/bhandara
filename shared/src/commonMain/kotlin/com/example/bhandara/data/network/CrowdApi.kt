package com.example.bhandara.data.network

import com.example.bhandara.data.location.GeoLocation
import com.example.bhandara.data.models.api.CrowdPingRequest
import io.ktor.client.request.setBody
import io.ktor.http.HttpMethod

/** "How busy is it": phones at a shop say so, anonymously, and the shop's crowd level comes from that */
class CrowdApi(private val client: ApiClient) {

    /** This phone is at a shop right now (signed-in users only, limited by the backend) */
    suspend fun ping(here: GeoLocation): ApiResult<Unit> =
        client.callForNothing("api/crowd/ping") {
            method = HttpMethod.Post
            setBody(CrowdPingRequest(here.latitude, here.longitude))
        }
}
