package com.example.bhandara.data.repository

import com.example.bhandara.data.auth.AppAuth
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.tasks.await

class UserRepository {
    private val auth = FirebaseAuth.getInstance()
    private val messaging = FirebaseMessaging.getInstance()
    
    companion object {
        private const val TAG = "UserRepository"
    }
    
    /**
     * Sign in anonymously and return the user UID
     */
    suspend fun signInAnonymously(): String? {
        // Shared with iOS: signs in only if needed, and never twice at the same time
        return AppAuth.service.ensureSignedIn()
            .onFailure { Log.e(TAG, "Anonymous sign in failed", it) }
            .getOrNull()
    }
    
    /**
     * Get FCM token for push notifications
     */
    suspend fun getFcmToken(): String? {
        return try {
            val token = messaging.token.await()
            // Never log the token itself: it lets anyone send notifications to this device
            Log.d(TAG, "FCM token retrieved")
            token
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get FCM token", e)
            null
        }
    }
    
    /**
     * Get current user UID
     */
    fun getCurrentUserId(): String? {
        return auth.currentUser?.uid
    }
}
