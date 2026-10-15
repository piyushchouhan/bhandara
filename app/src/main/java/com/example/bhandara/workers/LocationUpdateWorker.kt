package com.example.bhandara.workers

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.bhandara.data.auth.AppBackend
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
    private val locationHelper = LocationHelper(context)
    
    companion object {
        private const val TAG = "LocationUpdateWorker"
        const val WORK_NAME = "periodic_location_update"
    }
    
    override suspend fun doWork(): Result {
        return try {
            if (userRepository.getCurrentUserId() == null) return Result.failure()
            val location = locationHelper.getCurrentLocation() ?: return Result.retry()
            // Registers the phone first if it isn't yet; a failed update (e.g. no connection) is tried again later
            if (AppBackend.deviceRegistration(applicationContext).shareLocation(location)) Result.success() else Result.retry()
        } catch (e: Exception) {
            Log.e(TAG, "Error during periodic location update", e)
            Result.retry()
        }
    }
}

