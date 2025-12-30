package com.example.bhandara.managers

import android.content.Context
import android.util.Log
import com.example.bhandara.data.repository.UserRepository
import com.example.bhandara.utils.LocationHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

/**
 * Manages user initialization and tracking
 */
class UserManager(
    private val context: Context,
    private val coroutineScope: CoroutineScope
) {
    private val userRepository = UserRepository()
    private val locationHelper = LocationHelper(context)
    
    companion object {
        private const val TAG = "UserManager"
    }
    
    /**
     * Initialize anonymous user silently on app start
     */
    fun initializeUser() {
        coroutineScope.launch {
            try {
                // Step 1: Sign in anonymously
                val uid = userRepository.signInAnonymously()
                if (uid == null) {
                    Log.e(TAG, "Failed to sign in anonymously")
                    return@launch
                }
                
                Log.d(TAG, "User signed in with UID: $uid")
                
                // Step 2: Get FCM token
                val fcmToken = userRepository.getFcmToken()
                if (fcmToken == null) {
                    Log.e(TAG, "Failed to get FCM token")
                    return@launch
                }
                
                Log.d(TAG, "FCM token obtained")
                
                // Step 3: Save user to Firestore (without location initially)
                val saved = userRepository.saveUser(uid, fcmToken)
                if (saved) {
                    Log.d(TAG, "User saved to Firestore")
                }
                
                // TODO: Send user data to backend API
                // sendUserDataToBackend(uid, fcmToken, null)
                
            } catch (e: Exception) {
                Log.e(TAG, "Error initializing user", e)
            }
        }
    }
    
    /**
     * Update user location in Firestore and backend
     */
    fun updateUserLocation() {
        coroutineScope.launch {
            val uid = userRepository.getCurrentUserId() ?: return@launch
            
            val location = locationHelper.getCurrentLocation()
            if (location != null) {
                // Update in Firestore
                userRepository.updateUserLocation(uid, location)
                Log.d(TAG, "User location updated in Firestore")
                
                // TODO: Update location in backend
                // sendUserDataToBackend(uid, null, location)
            }
        }
    }
    
    fun hasLocationPermissions(): Boolean {
        return locationHelper.hasLocationPermissions()
    }
    
    // TODO: Implement backend API call
    /**
     * Send user data to backend
     * @param uid User ID from Firebase
     * @param fcmToken FCM token for notifications
     * @param location User's current location
     */
    private suspend fun sendUserDataToBackend(
        uid: String,
        fcmToken: String?,
        location: com.google.firebase.firestore.GeoPoint?
    ) {
        // TODO: Implement Retrofit/OkHttp API call
        /*
        val request = UserUpdateRequest(
            uid = uid,
            fcmToken = fcmToken,
            latitude = location?.latitude,
            longitude = location?.longitude
        )
        
        try {
            val response = apiService.updateUser(request)
            if (response.isSuccessful) {
                Log.d(TAG, "User data sent to backend successfully")
            } else {
                Log.e(TAG, "Failed to send user data to backend: ${response.code()}")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error sending user data to backend", e)
        }
        */
    }
}
