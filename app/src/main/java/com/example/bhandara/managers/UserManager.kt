package com.example.bhandara.managers

import android.content.Context
import android.util.Log
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.example.bhandara.data.repository.BackendRepository
import com.example.bhandara.data.repository.UserRepository
import com.example.bhandara.utils.LocationHelper
import com.example.bhandara.workers.LocationUpdateWorker
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

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
    
    // SharedPreferences to track backend sync status
    private val prefs = context.getSharedPreferences("user_sync_prefs", Context.MODE_PRIVATE)
    
    companion object {
        private const val TAG = "UserManager"
        private const val KEY_BACKEND_SYNCED = "backend_synced_"
    }
    
    /**
     * Check if user has been synced to backend
     */
    private fun isUserSyncedToBackend(uid: String): Boolean {
        return prefs.getBoolean(KEY_BACKEND_SYNCED + uid, false)
    }
    
    /**
     * Mark user as synced to backend
     */
    private fun markUserSyncedToBackend(uid: String) {
        prefs.edit().putBoolean(KEY_BACKEND_SYNCED + uid, true).apply()
        Log.d(TAG, "Marked user $uid as synced to backend")
    }
    
    /**
     * Initialize anonymous user silently on app start
     */
    fun initializeUser() {
        coroutineScope.launch {
            try {
                val uid = userRepository.signInAnonymously()
                if (uid == null) {
                    Log.e(TAG, "Failed to sign in anonymously")
                    return@launch
                }
                
                val fcmToken = userRepository.getFcmToken()
                if (fcmToken == null) {
                    Log.e(TAG, "Failed to get FCM token")
                    return@launch
                }
                
                userRepository.saveUser(uid, fcmToken)
                
                // Only create user in backend if not already synced
                if (!isUserSyncedToBackend(uid)) {
                    val backendResponse = backendRepository.createUser(uid, fcmToken, null)
                    if (backendResponse != null) {
                        markUserSyncedToBackend(uid)
                    } else {
                        Log.e(TAG, "Failed to create user in backend, will retry on next start")
                    }
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
                userRepository.updateUserLocation(uid, location)
                
                val fcmToken = userRepository.getFcmToken()
                if (fcmToken != null) {
                    backendRepository.updateUserLocation(uid, fcmToken, location)
                }
            }
        }
    }
    
    fun hasLocationPermissions(): Boolean {
        return locationHelper.hasLocationPermissions()
    }
    
    /**
     * Start periodic location updates every 15 minutes
     */
    fun startPeriodicLocationUpdates() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()
        
        val locationWorkRequest = PeriodicWorkRequestBuilder<LocationUpdateWorker>(
            15,
            TimeUnit.MINUTES
        )
            .setConstraints(constraints)
            .build()
        
        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            LocationUpdateWorker.WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            locationWorkRequest
        )
    }
    
    /**
     * Stop periodic location updates
     */
    fun stopPeriodicLocationUpdates() {
        WorkManager.getInstance(context).cancelUniqueWork(LocationUpdateWorker.WORK_NAME)
    }
}
