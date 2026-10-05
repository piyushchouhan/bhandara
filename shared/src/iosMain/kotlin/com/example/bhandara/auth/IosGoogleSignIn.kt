package com.example.bhandara.auth

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import com.example.bhandara.ui.profile.AccountSignIn
import com.example.bhandara.ui.profile.SignInOutcome
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/** How Google sign-in ended, as the Swift bridge reports it */
enum class GoogleSignInResult { SIGNED_IN, CANCELLED, FAILED }

/**
 * Google sign-in on iOS, implemented in Swift (IosGoogleSignIn.swift) with Google's and Firebase's SDKs. Like on
 * Android, the guest account is upgraded to the Google one; if that Google account already exists, the user switches
 * to it.
 */
interface IosGoogleSignInBridge {
    fun signIn(onResult: (GoogleSignInResult, String?) -> Unit)
    fun signOut(onResult: (String?) -> Unit)
}

/** The shared [AccountSignIn] on top of the Swift bridge */
class IosAccountSignIn(private val bridge: IosGoogleSignInBridge) : AccountSignIn {

    @Composable
    override fun rememberSignIn(onResult: (SignInOutcome) -> Unit): () -> Unit {
        val latestOnResult = rememberUpdatedState(onResult)
        return remember(bridge) {
            {
                bridge.signIn { result, error ->
                    latestOnResult.value(
                        when (result) {
                            GoogleSignInResult.SIGNED_IN -> SignInOutcome.SignedIn
                            GoogleSignInResult.CANCELLED -> SignInOutcome.Cancelled
                            GoogleSignInResult.FAILED -> SignInOutcome.Failed(error)
                        }
                    )
                }
            }
        }
    }

    override suspend fun signOut() = suspendCancellableCoroutine { continuation ->
        bridge.signOut { error ->
            if (!continuation.isActive) return@signOut
            if (error == null) continuation.resume(Unit) else continuation.resumeWithException(IllegalStateException(error))
        }
    }
}
