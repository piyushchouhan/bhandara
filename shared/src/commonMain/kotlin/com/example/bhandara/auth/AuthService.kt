package com.example.bhandara.auth

import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlin.coroutines.resume

/** Signing in could not be completed (e.g. no connection) */
class SignInException(message: String) : Exception(message)

/**
 * Signing in, the same on Android and iOS. Everyone using the app is signed in, anonymously unless they sign in
 * with Google, so that the backend knows who did what.
 */
class AuthService(private val platform: PlatformAuth) {

    private val signInLock = Mutex()

    /** The signed-in user's ID, or null */
    val currentUid: String? get() = platform.currentUid()

    /**
     * The signed-in user's ID, signing in anonymously first if needed. Calls made at the same time (e.g. app start
     * and the first request) share one sign-in, so a user never ends up with two anonymous accounts.
     */
    suspend fun ensureSignedIn(): Result<String> = signInLock.withLock {
        platform.currentUid()?.let { return Result.success(it) }
        suspendCancellableCoroutine { continuation ->
            platform.signInAnonymously { uid, error ->
                if (!continuation.isActive) return@signInAnonymously
                continuation.resume(
                    if (uid != null) Result.success(uid)
                    else Result.failure(SignInException(error ?: "Couldn't sign in. Please try again."))
                )
            }
        }
    }

    /**
     * The ID token the backend expects ("Authorization: Bearer <token>"), or null when nobody is signed in or it
     * can't be fetched; the request then goes out without it and the backend answers 401 if it needs one.
     * Meant for [com.example.bhandara.data.network.ApiClient]'s idTokenProvider.
     */
    suspend fun idToken(): String? {
        if (platform.currentUid() == null) return null
        return suspendCancellableCoroutine { continuation ->
            platform.idToken(forceRefresh = false) { token, _ ->
                if (continuation.isActive) continuation.resume(token)
            }
        }
    }
}
