package com.example.bhandara.push

import com.example.bhandara.data.network.ApiResult
import com.example.bhandara.data.network.UserApi
import com.example.bhandara.data.settings.AppSettings

/**
 * Keeps the backend's copy of this phone's push-notification token current. Firebase replaces tokens from time to
 * time; if the backend kept the old one, notifications would silently stop reaching the phone.
 *
 * Only a token the backend doesn't have yet is sent, and one that couldn't be sent (e.g. offline) is tried again
 * at the next [sync], so calling it on every app start and every new token is cheap and safe.
 */
class PushTokenSync(private val userApi: UserApi, private val settings: AppSettings) {

    /** @return true if the backend has [fcmToken] for [uid] afterwards */
    suspend fun sync(uid: String, fcmToken: String): Boolean {
        if (fcmToken.isBlank()) return false
        if (settings.lastPushTokenSent(uid) == fcmToken) return true

        return when (userApi.updatePushToken(fcmToken)) {
            is ApiResult.Success -> {
                settings.markPushTokenSent(uid, fcmToken)
                true
            }
            // Not registered yet (registration sends the token anyway), offline, ...: try again next time
            is ApiResult.Failure -> false
        }
    }
}
