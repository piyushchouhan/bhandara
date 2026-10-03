package com.example.bhandara.data.network

import io.ktor.client.request.setBody
import io.ktor.http.HttpMethod
import kotlinx.serialization.Serializable

/** The signed-in user's own record on the backend */
class UserApi(private val client: ApiClient) {

    @Serializable
    private data class PushTokenRequest(val fcmToken: String)

    /** Notifications for the signed-in user go to this token from now on */
    suspend fun updatePushToken(fcmToken: String): ApiResult<Unit> =
        client.callForNothing("api/users/push-token") {
            method = HttpMethod.Put
            setBody(PushTokenRequest(fcmToken))
        }
}
