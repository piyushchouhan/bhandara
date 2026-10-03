package com.example.bhandara.data.auth

import com.example.bhandara.auth.AuthService
import com.example.bhandara.auth.PlatformAuth
import com.google.firebase.auth.FirebaseAuth

/** Firebase Authentication on Android, for the shared [AuthService] */
class AndroidPlatformAuth(private val auth: FirebaseAuth = FirebaseAuth.getInstance()) : PlatformAuth {

    override fun currentUid(): String? = auth.currentUser?.uid

    override fun signInAnonymously(onResult: (uid: String?, error: String?) -> Unit) {
        auth.signInAnonymously()
            .addOnSuccessListener { result -> onResult(result.user?.uid, null) }
            .addOnFailureListener { error -> onResult(null, error.message) }
    }

    override fun idToken(forceRefresh: Boolean, onResult: (token: String?, error: String?) -> Unit) {
        val user = auth.currentUser ?: return onResult(null, "Not signed in")
        user.getIdToken(forceRefresh)
            .addOnSuccessListener { result -> onResult(result.token, null) }
            .addOnFailureListener { error -> onResult(null, error.message) }
    }
}

/** The app's one sign-in service (one per process, so concurrent sign-ins are shared) */
object AppAuth {
    val service: AuthService by lazy { AuthService(AndroidPlatformAuth()) }
}
