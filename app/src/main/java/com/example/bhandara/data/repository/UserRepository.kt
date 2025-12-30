package com.example.bhandara.data.repository

import android.util.Log
import com.example.bhandara.data.models.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.GeoPoint
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.tasks.await

class UserRepository {
    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()
    private val messaging = FirebaseMessaging.getInstance()
    
    companion object {
        private const val TAG = "UserRepository"
        private const val USERS_COLLECTION = "users"
    }
    
    /**
     * Sign in anonymously and return the user UID
     */
    suspend fun signInAnonymously(): String? {
        return try {
            val currentUser = auth.currentUser
            if (currentUser != null) {
                Log.d(TAG, "User already signed in: ${currentUser.uid}")
                return currentUser.uid
            }
            
            val result = auth.signInAnonymously().await()
            val uid = result.user?.uid
            Log.d(TAG, "Anonymous sign in successful: $uid")
            uid
        } catch (e: Exception) {
            Log.e(TAG, "Anonymous sign in failed", e)
            null
        }
    }
    
    /**
     * Get FCM token for push notifications
     */
    suspend fun getFcmToken(): String? {
        return try {
            val token = messaging.token.await()
            Log.d(TAG, "FCM token retrieved: $token")
            token
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get FCM token", e)
            null
        }
    }
    
    /**
     * Save or update user data in Firestore
     */
    suspend fun saveUser(uid: String, fcmToken: String, location: GeoPoint? = null): Boolean {
        return try {
            val user = User(
                uid = uid,
                fcmToken = fcmToken,
                location = location,
                lastActive = System.currentTimeMillis()
            )
            
            firestore.collection(USERS_COLLECTION)
                .document(uid)
                .set(user)
                .await()
            
            Log.d(TAG, "User data saved successfully")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to save user data", e)
            false
        }
    }
    
    /**
     * Update user location
     */
    suspend fun updateUserLocation(uid: String, location: GeoPoint): Boolean {
        return try {
            firestore.collection(USERS_COLLECTION)
                .document(uid)
                .update(
                    mapOf(
                        "location" to location,
                        "lastActive" to System.currentTimeMillis()
                    )
                )
                .await()
            
            Log.d(TAG, "User location updated")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to update location", e)
            false
        }
    }
    
    /**
     * Get current user UID
     */
    fun getCurrentUserId(): String? {
        return auth.currentUser?.uid
    }
}
