package com.example.bhandara.workers

import com.example.bhandara.data.settings.AppSettings
import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.bhandara.data.repository.BackendRepository
import com.example.bhandara.data.repository.UserRepository
import com.example.bhandara.managers.UserManager
import com.example.bhandara.utils.LocationHelper

/**
 * Worker to periodically send the user's location to the backend
 */
class LocationUpdateWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {
    
    private val userRepository = UserRepository()
    private val backendRepository = BackendRepository()
    private val locationHelper = LocationHelper(context)
    private val settings = AppSettings(context)
    
    companion object {
        private const val TAG = "LocationUpdateWorker"
        const val WORK_NAME = "periodic_location_update"
    }
    
    override suspend fun doWork(): Result {
        return try {
            val uid = userRepository.getCurrentUserId() ?: return Result.failure()
            
            val location = locationHelper.getCurrentLocation()
            if (location == null) {
                return Result.retry()
            }
            
            // Only call backend if the user has been registered there
            val isSynced = settings.isRegisteredWithBackend(uid)
            if (!isSynced) {
                Log.w(TAG, "Skipping backend location update — user not yet registered in backend")
                return Result.success()
            }
            
            val fcmToken = userRepository.getFcmToken()
            if (fcmToken != null) {
                backendRepository.updateUserLocation(uid, fcmToken, location)
            }
            
            Result.success()
        } catch (e: Exception) {
            Log.e(TAG, "Error during periodic location update", e)
            Result.retry()
        }
    }
}

