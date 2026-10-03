package com.example.bhandara.auth

/**
 * Firebase Authentication as each platform provides it: implemented with the official Firebase SDK on Android
 * (in the app module) and on iOS (in Swift). Callbacks rather than suspend functions keep it easy to implement
 * from Swift. [AuthService] builds the app's sign-in logic on top of it.
 */
interface PlatformAuth {

    /** The signed-in user's ID, or null if nobody is signed in */
    fun currentUid(): String?

    /** Signs in without an account; calls back once with the user's ID, or with an error message */
    fun signInAnonymously(onResult: (uid: String?, error: String?) -> Unit)

    /** The signed-in user's Firebase ID token for the backend; calls back once with it, or with an error message */
    fun idToken(forceRefresh: Boolean, onResult: (token: String?, error: String?) -> Unit)
}
