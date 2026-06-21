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
import kotlinx.coroutines.CompletableDeferred
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
    
    /**
     * Deferred that completes when initializeUser() finishes.
     * true  = user is registered in the backend (safe to call location update)
     * false = registration failed (skip backend location calls)
     */
    private val initCompleted = CompletableDeferred<Boolean>()
    
    companion object {
        private const val TAG = "UserManager"
        const val KEY_BACKEND_SYNCED = "backend_synced_"
    }
    
    /**
     * Check if user has been synced to backend
     */
    fun isUserSyncedToBackend(uid: String): Boolean {
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
     * Initialize anonymous user silently on app start.
     * Completes [initCompleted] when finished so that dependent
     * calls (e.g. updateUserLocation) can safely proceed.
     */
    fun initializeUser() {
        coroutineScope.launch {
            try {
                val uid = userRepository.signInAnonymously()
                if (uid == null) {
                    Log.e(TAG, "Failed to sign in anonymously")
                    initCompleted.complete(false)
                    return@launch
                }
                
                val fcmToken = userRepository.getFcmToken()
                if (fcmToken == null) {
                    Log.e(TAG, "Failed to get FCM token")
                    initCompleted.complete(false)
                    return@launch
                }
                
                userRepository.saveUser(uid, fcmToken)
                
                // Only create user in backend if not already synced
                if (!isUserSyncedToBackend(uid)) {
                    val backendResponse = backendRepository.createUser(uid, fcmToken, null)
                    if (backendResponse != null) {
                        markUserSyncedToBackend(uid)
                        initCompleted.complete(true)
                    } else {
                        Log.e(TAG, "Failed to create user in backend, will retry on next start")
                        initCompleted.complete(false)
                    }
                } else {
                    // Already synced from a previous session
                    initCompleted.complete(true)
                }
                
            } catch (e: Exception) {
                Log.e(TAG, "Error initializing user", e)
                initCompleted.complete(false)
            }
        }
    }
    
    /**
     * Update user location in Firestore and backend.
     * Waits for [initializeUser] to complete before calling the backend
     * to avoid the "User not found" error.
     */
    fun updateUserLocation() {
        coroutineScope.launch {
            val uid = userRepository.getCurrentUserId() ?: return@launch
            
            val location = locationHelper.getCurrentLocation()
            if (location != null) {
                // Always update Firestore (no dependency on backend registration)
                userRepository.updateUserLocation(uid, location)
                
                // Wait for backend registration to finish before sending location
                val backendReady = initCompleted.await()
                if (!backendReady) {
                    Log.w(TAG, "Skipping backend location update — user not registered in backend")
                    return@launch
                }
                
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
