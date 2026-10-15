package com.example.bhandara.managers

import com.example.bhandara.data.auth.AppBackend
import com.example.bhandara.data.settings.AppSettings
import android.content.Context
import android.util.Log
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
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
    private val locationHelper = LocationHelper(context)
    
    private val settings = AppSettings(context)
    private val registration = AppBackend.deviceRegistration(context)
    
    /**
     * Deferred that completes when initializeUser() finishes.
     * true  = user is registered in the backend (safe to call location update)
     * false = registration failed (skip backend location calls)
     */
    private val initCompleted = CompletableDeferred<Boolean>()
    
    companion object {
        private const val TAG = "UserManager"
    }
    
    /**
     * Check if user has been synced to backend
     */
    fun isUserSyncedToBackend(uid: String): Boolean {
        return settings.isRegisteredWithBackend(uid)
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
                // Registers once per account (with the push token when Firebase gives one), else keeps the backend's
                // push token current: it may have changed while the app was closed, or an earlier send failed
                val registered = registration.ensureRegistered()
                if (!registered) Log.e(TAG, "Failed to register with the backend, will retry on next start")
                initCompleted.complete(registered)
            } catch (e: Exception) {
                Log.e(TAG, "Error initializing user", e)
                initCompleted.complete(false)
            }
        }
    }
    
    /**
     * Send the user's location to the backend, after [initializeUser] has had its go at registering (sharing the
     * location registers the phone too if that failed).
     */
    fun updateUserLocation() {
        coroutineScope.launch {
            val location = locationHelper.getCurrentLocation() ?: return@launch
            initCompleted.await()
            if (!registration.shareLocation(location)) Log.w(TAG, "Location update failed, will try again later")
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
