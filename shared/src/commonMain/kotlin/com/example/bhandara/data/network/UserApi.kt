package com.example.bhandara.data.network

import com.example.bhandara.data.location.GeoLocation
import com.example.bhandara.data.models.api.CreateUserRequest
import com.example.bhandara.data.models.api.CreateUserResponse
import com.example.bhandara.data.models.api.UpdateLocationRequest
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

    /**
     * Registers the signed-in user with their push token, so they can be told about feasts nearby. Without a
     * location yet, the backend gets an "unknown" one (-90, -180) that is never near anything.
     */
    suspend fun register(uid: String, fcmToken: String, location: GeoLocation?): ApiResult<CreateUserResponse> =
        client.call("api/users") {
            method = HttpMethod.Post
            setBody(CreateUserRequest(uid, fcmToken, location?.latitude ?: UNKNOWN_LATITUDE, location?.longitude ?: UNKNOWN_LONGITUDE))
        }

    /** Where the user is now, for notifications about feasts near them */
    suspend fun updateLocation(uid: String, fcmToken: String, location: GeoLocation): ApiResult<Unit> =
        client.callForNothing("api/users/location") {
            method = HttpMethod.Put
            setBody(UpdateLocationRequest(uid, location.latitude, location.longitude, fcmToken))
        }

    private companion object {
        // A point no feast is near, until the phone knows where the user is
        const val UNKNOWN_LATITUDE = -90.0
        const val UNKNOWN_LONGITUDE = -180.0
    }
}
