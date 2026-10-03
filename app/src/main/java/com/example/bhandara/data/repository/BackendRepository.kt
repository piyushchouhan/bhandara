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

    /**
     * Report a local shop as fake or inappropriate
     * @param shopId ID of the shop to report
     * @return LocalShopResponse if successful, null otherwise
     */
    suspend fun reportLocalShop(shopId: String): com.example.bhandara.data.models.api.LocalShopResponse? {
        return try {
            Log.d(TAG, "Reporting local shop ID: $shopId")
            
            val response = apiService.reportLocalShop(shopId)
            
            if (response.isSuccessful) {
                val body = response.body()
                Log.d(TAG, "✅ Local shop reported successfully")
                body
            } else {
                Log.e(TAG, "❌ Failed to report local shop: ${response.code()}")
                null
            }
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error reporting local shop", e)
            null
        }
    }

    /**
     * Deactivate a local shop (only allowed for owner)
     * @param shopId ID of the shop to deactivate
     * @return LocalShopResponse if successful, null otherwise
     */
    suspend fun deactivateLocalShop(shopId: String): com.example.bhandara.data.models.api.LocalShopResponse? {
        return try {
            Log.d(TAG, "Deactivating local shop ID: $shopId")
            
            val response = apiService.deactivateLocalShop(shopId)
            
            if (response.isSuccessful) {
                val body = response.body()
                Log.d(TAG, "✅ Local shop deactivated successfully")
                body
            } else {
                Log.e(TAG, "❌ Failed to deactivate local shop: ${response.code()}")
                null
            }
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error deactivating local shop", e)
            null
        }
    }

    /**
     * Suggest deleting a local shop (for non-owners)
     * @param shopId ID of the shop to suggest deleting
     * @return LocalShopResponse if successful, null otherwise
     */
    suspend fun suggestDeleteLocalShop(shopId: String): com.example.bhandara.data.models.api.LocalShopResponse? {
        return try {
            Log.d(TAG, "Suggesting delete for local shop ID: $shopId")
            
            val response = apiService.suggestDeleteLocalShop(shopId)
            
            if (response.isSuccessful) {
                val body = response.body()
                Log.d(TAG, "✅ Local shop delete suggested successfully")
                body
            } else {
                Log.e(TAG, "❌ Failed to suggest delete local shop: ${response.code()}")
                null
            }
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error suggesting delete local shop", e)
            null
        }
    }

    // --- Reviews ---
    
    suspend fun getReviewsForShop(shopId: String): List<com.example.bhandara.data.models.api.ReviewResponse> {
        return try {
            val response = apiService.getReviewsForShop(shopId)
            if (response.isSuccessful) {
                response.body() ?: emptyList()
            } else {
                Log.e(TAG, "❌ Failed to fetch reviews: ${response.code()}")
                emptyList()
            }
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error fetching reviews", e)
            emptyList()
        }
    }
    
    suspend fun createReview(request: com.example.bhandara.data.models.api.ReviewRequest): com.example.bhandara.data.models.api.ReviewResponse? {
        return try {
            val response = apiService.createReview(request)
            if (response.isSuccessful) {
                response.body()
            } else {
                Log.e(TAG, "❌ Failed to create review: ${response.code()}")
                null
            }
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error creating review", e)
            null
        }
    }
    
    suspend fun updateReview(id: String, request: com.example.bhandara.data.models.api.ReviewRequest): com.example.bhandara.data.models.api.ReviewResponse? {
        return try {
            val response = apiService.updateReview(id, request)
            if (response.isSuccessful) {
                response.body()
            } else {
                Log.e(TAG, "❌ Failed to update review: ${response.code()}")
                null
            }
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error updating review", e)
            null
        }
    }
    
    suspend fun deleteReview(id: String): Boolean {
        return try {
            val response = apiService.deleteReview(id)
            if (response.isSuccessful) {
                true
            } else {
                Log.e(TAG, "❌ Failed to delete review: ${response.code()}")
                false
            }
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error deleting review", e)
            false
        }
    }
    
    suspend fun reportReview(reviewId: String): com.example.bhandara.data.models.api.ReviewResponse? {
        return try {
            val response = apiService.reportReview(reviewId)
            if (response.isSuccessful) {
                response.body()
            } else {
                Log.e(TAG, "❌ Failed to report review: ${response.code()}")
                null
            }
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error reporting review", e)
            null
        }
    }

    // --- Menu Items ---

    /** The shop's menu with prices (empty for older shops that only have a list of item names) */
    suspend fun getShopMenu(shopId: String): List<com.example.bhandara.data.models.api.MenuItemResponse>? {
        return try {
            val response = apiService.getShopMenu(shopId)
            if (response.isSuccessful) {
                response.body()
            } else {
                Log.e(TAG, "❌ Failed to fetch shop menu: ${response.code()}")
                null
            }
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error fetching shop menu", e)
            null
        }
    }

    suspend fun addMenuItemsManual(shopId: String, request: com.example.bhandara.data.models.api.ManualMenuItemsRequest): List<com.example.bhandara.data.models.api.MenuItemResponse>? {
        return try {
            val response = apiService.addMenuItemsManual(shopId, request)
            if (response.isSuccessful) {
                response.body()
            } else {
                Log.e(TAG, "❌ Failed to add manual menu items: ${response.code()}")
                null
            }
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error adding manual menu items", e)
            null
        }
    }

    // --- Feed ---

    suspend fun getFeed(lat: Double, lon: Double, radius: Double = 5000.0): com.example.bhandara.data.models.api.FeedResponse? {
        return try {
            val response = apiService.getFeed(lat, lon, radius)
            if (response.isSuccessful) {
                response.body()
            } else {
                Log.e(TAG, "Failed to get feed: ${response.code()}")
                null
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting feed", e)
            null
        }
    }

    suspend fun verifyShop(
        shopId: String,
        vote: String,
        latitude: Double?,
        longitude: Double?
    ): com.example.bhandara.data.models.api.VerifyShopResult {
        return try {
            val request = com.example.bhandara.data.models.api.VerifyShopRequest(vote, latitude, longitude)
            val response = apiService.verifyShop(shopId, request)
            if (response.isSuccessful) {
                com.example.bhandara.data.models.api.VerifyShopResult(success = true)
            } else {
                // Backend explains refusals (too far away, cart not live, already voted) in {"message": "..."}
                val message = response.errorBody()?.string()?.let {
                    runCatching { org.json.JSONObject(it).optString("message") }.getOrNull()
                }?.takeIf { it.isNotBlank() }
                Log.e(TAG, "Failed to verify shop: ${response.code()} $message")
                com.example.bhandara.data.models.api.VerifyShopResult(
                    success = false,
                    alreadyVoted = message?.contains("already voted", ignoreCase = true) == true,
                    message = message ?: "Could not submit your answer (error ${response.code()})"
                )
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error verifying shop", e)
            com.example.bhandara.data.models.api.VerifyShopResult(success = false, message = "Network error. Please try again.")
        }
    }

    suspend fun submitShopClaim(request: com.example.bhandara.data.models.api.ShopClaimRequest): com.example.bhandara.data.models.api.ShopClaimResult {
        return try {
            val response = apiService.submitShopClaim(request)
            if (response.isSuccessful) {
                com.example.bhandara.data.models.api.ShopClaimResult(claim = response.body())
            } else {
                // Backend explains 400/409 refusals in {"message": "..."}
                val message = response.errorBody()?.string()?.let {
                    runCatching { org.json.JSONObject(it).optString("message") }.getOrNull()
                }
                Log.e(TAG, "Failed to submit shop claim: ${response.code()} $message")
                com.example.bhandara.data.models.api.ShopClaimResult(
                    errorMessage = message?.takeIf { it.isNotBlank() } ?: "Could not submit your claim (error ${response.code()})"
                )
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error submitting shop claim", e)
            com.example.bhandara.data.models.api.ShopClaimResult(errorMessage = "Network error. Please try again.")
        }
    }

    suspend fun extractMenuItemsFromImage(images: List<okhttp3.MultipartBody.Part>): com.example.bhandara.data.models.api.MenuItemExtractResponse? {
        return try {
            val response = apiService.extractMenuItemsFromImage(images)
            if (response.isSuccessful) {
                response.body()
            } else {
                Log.e(TAG, "❌ Failed to extract menu items from image: ${response.code()}")
                null
            }
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error extracting menu items from image", e)
            null
        }
    }

    suspend fun suggestFeature(feature: String): com.example.bhandara.data.models.api.FeatureSuggestionResponse? {
        return try {
            val request = com.example.bhandara.data.models.api.FeatureSuggestionRequest(feature = feature)
            val response = apiService.suggestFeature(request)
            if (response.isSuccessful) {
                response.body()
            } else {
                Log.e(TAG, "❌ Failed to suggest feature: ${response.code()}")
                null
            }
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error suggesting feature", e)
            null
        }
    }

    suspend fun reportBug(bug: String): com.example.bhandara.data.models.api.BugReportResponse? {
        return try {
            val request = com.example.bhandara.data.models.api.BugReportRequest(bug = bug)
            val response = apiService.reportBug(request)
            if (response.isSuccessful) {
                response.body()
            } else {
                Log.e(TAG, "❌ Failed to report bug: ${response.code()}")
                null
            }
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error reporting bug", e)
            null
        }
    }
}

