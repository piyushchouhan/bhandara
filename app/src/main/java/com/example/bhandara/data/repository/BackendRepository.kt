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

    /**
     * Get details of a specific bhandara by its ID
     * @param id The ID of the bhandara to fetch
     * @return FeastResponse if successful, null otherwise
     */
    suspend fun getFeastById(id: String): FeastResponse? {
        return try {
            val response = apiService.getFeastById(id)
            if (response.isSuccessful) {
                response.body()
            } else {
                Log.e(TAG, "Failed to get feast by ID: ${response.code()}")
                null
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting feast by ID", e)
            null
        }
    }

    /**
     * Create a new local shop
     * @param request LocalShopRequest with all shop details
     * @return LocalShopResponse if successful, null otherwise
     */
    suspend fun createLocalShop(
        request: com.example.bhandara.data.models.api.LocalShopRequest
    ): com.example.bhandara.data.models.api.LocalShopResponse? {
        return try {
            val response = apiService.createLocalShop(request)
            
            if (response.isSuccessful) {
                response.body()
            } else {
                Log.e(TAG, "Failed to create local shop: ${response.code()}")
                null
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error creating local shop in backend", e)
            null
        }
    }

    /**
     * Get nearby local shops based on user location
     * @param lat Latitude of the user's location
     * @param lon Longitude of the user's location
     * @param radius Search radius in meters (default 5000m)
     * @return List of nearby local shops with distance information
     */
    suspend fun getLocalShopsNearby(lat: Double, lon: Double, radius: Double = 5000.0): List<com.example.bhandara.data.models.api.LocalShopResponse>? {
        return try {
            val response = apiService.getLocalShopsNearby(lat, lon, radius)
            if (response.isSuccessful) {
                response.body()
            } else {
                Log.e(TAG, "Failed to get local shops nearby: ${response.code()}")
                null
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting local shops nearby", e)
            null
        }
    }

    /**
     * Get details of a specific local shop by its ID
     * @param id The ID of the shop to fetch
     * @return LocalShopResponse if successful, null otherwise
     */
    suspend fun getLocalShopById(id: String): com.example.bhandara.data.models.api.LocalShopResponse? {
        return try {
            val response = apiService.getLocalShopById(id)
            if (response.isSuccessful) {
                response.body()
            } else {
                Log.e(TAG, "Failed to get local shop by ID: ${response.code()}")
                null
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting local shop by ID", e)
            null
        }
    }

}
