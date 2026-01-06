package com.example.bhandara.managers

import android.content.Context
import android.util.Log
import com.example.bhandara.data.repository.BackendRepository
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
    private val backendRepository = BackendRepository()
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
                
                // Step 4: Send user data to backend API
                val backendResponse = backendRepository.createUser(uid, fcmToken, null)
                if (backendResponse != null) {
                    Log.d(TAG, "User created in backend database: ${backendResponse.id}")
                } else {
                    Log.e(TAG, "Failed to create user in backend")
                }
                
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
                
                // Update location in backend (also updates fcmToken to keep it fresh)
                val fcmToken = userRepository.getFcmToken()
                if (fcmToken != null) {
                    val success = backendRepository.updateUserLocation(uid, fcmToken, location)
                    if (success) {
                        Log.d(TAG, "User location updated in backend")
                    } else {
                        Log.e(TAG, "Failed to update location in backend")
                    }
                } else {
                    Log.e(TAG, "FCM token not available for location update")
                }
            }
        }
    }
    
    fun hasLocationPermissions(): Boolean {
        return locationHelper.hasLocationPermissions()
    }
}
