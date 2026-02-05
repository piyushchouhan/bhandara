package com.example.bhandara.data.repository

import android.util.Log
import com.example.bhandara.data.api.NetworkModule
import com.example.bhandara.data.models.api.CreateUserRequest
import com.example.bhandara.data.models.api.CreateUserResponse
import com.example.bhandara.data.models.api.FeastRequest
import com.example.bhandara.data.models.api.FeastResponse
import com.google.firebase.firestore.GeoPoint

/**
 * Repository for backend API calls
 */
class BackendRepository {
    
    private val apiService = NetworkModule.apiService
    
    companion object {
        private const val TAG = "BackendRepository"
    }
    
    /**
     * Create a user in the backend database
     * @param firebaseUid Firebase user ID
     * @param fcmToken FCM token for notifications
     * @param location User's current location
     * @return CreateUserResponse if successful, null otherwise
     */
    suspend fun createUser(
        firebaseUid: String,
        fcmToken: String,
        location: GeoPoint?
    ): CreateUserResponse? {
        return try {
            val request = CreateUserRequest(
                firebaseUid = firebaseUid,
                fcmToken = fcmToken,
                latitude = location?.latitude ?: -90.0,
                longitude = location?.longitude ?: -180.0
            )
            
            val response = apiService.createUser(request)
            
            if (response.isSuccessful) {
                response.body()
            } else {
                Log.e(TAG, "Failed to create user: ${response.code()}")
                null
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error creating user in backend", e)
            null
        }
    }
    
    /**
     * Update user location in the backend
     * @param firebaseUid Firebase user ID
     * @param fcmToken FCM token for notifications
     * @param location User's current location
     * @return true if successful, false otherwise
     */
    suspend fun updateUserLocation(
        firebaseUid: String,
        fcmToken: String,
        location: GeoPoint
    ): Boolean {
        return try {
            val request = com.example.bhandara.data.models.api.UpdateLocationRequest(
                firebaseUid = firebaseUid,
                latitude = location.latitude,
                longitude = location.longitude,
                fcmToken = fcmToken
            )
            
            val response = apiService.updateUserLocation(request)
            response.isSuccessful
        } catch (e: Exception) {
            Log.e(TAG, "Error updating location in backend", e)
            false
        }
    }
    
    /**
     * Create a new feast/bhandara
     * @param request FeastRequest with all feast details
     * @return FeastResponse if successful, null otherwise
     */
    suspend fun createFeast(
        request: com.example.bhandara.data.models.api.FeastRequest
    ): com.example.bhandara.data.models.api.FeastResponse? {
        return try {
            val response = apiService.createFeast(request)
            
            if (response.isSuccessful) {
                response.body()
            } else {
                Log.e(TAG, "Failed to create feast: ${response.code()}")
                null
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error creating feast in backend", e)
            null
        }
    }
    
    /**
     * Report a feast as fake or inappropriate
     * @param feastId ID of the feast to report
     * @return FeastResponse if successful, null otherwise
     */
    suspend fun reportFeast(feastId: String): FeastResponse? {
        return try {
            Log.d(TAG, "Reporting feast ID: $feastId")
            
            val response = apiService.reportFeast(feastId)
            
            if (response.isSuccessful) {
                val body = response.body()
                Log.d(TAG, "✅ Feast reported successfully")
                Log.d(TAG, "Feast ID: ${body?.id}, IsActive: ${body?.isActive}")
                body
            } else {
                Log.e(TAG, "❌ Failed to report feast: ${response.code()}")
                null
            }
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error reporting feast", e)
            null
        }
    }
}
