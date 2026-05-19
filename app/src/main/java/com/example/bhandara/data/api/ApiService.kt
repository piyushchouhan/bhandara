package com.example.bhandara.data.api

import com.example.bhandara.data.models.api.CreateUserRequest
import com.example.bhandara.data.models.api.CreateUserResponse
import com.example.bhandara.data.models.api.CrowdPingRequest
import com.example.bhandara.data.models.api.CrowdPingResponse
import com.example.bhandara.data.models.api.HeatmapPoint
import com.example.bhandara.data.models.api.FeastRequest
import com.example.bhandara.data.models.api.FeastResponse
import com.example.bhandara.data.models.api.UpdateLocationRequest
import com.example.bhandara.data.models.api.UpdateLocationResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Query

/**
 * API service interface for backend communication
 */
interface ApiService {
    
    /**
     * Create a new user in the backend
     * @param request User data including Firebase UID, FCM token, and location
     * @return Response containing created user data
     */
    @POST("api/users")
    suspend fun createUser(
        @Body request: CreateUserRequest
    ): Response<CreateUserResponse>
    
    /**
     * Update user location in the backend
     * @param request Location data with Firebase UID and coordinates
     * @return Response containing updated location data
     */
    @PUT("api/users/location")
    suspend fun updateUserLocation(
        @Body request: UpdateLocationRequest
    ): Response<UpdateLocationResponse>
    
    /**
     * Create a new feast/bhandara
     * @param request Feast data including menu, location, time, etc.
     * @return Response containing created feast data
     */
    @POST("api/feasts")
    suspend fun createFeast(
        @Body request: FeastRequest
    ): Response<FeastResponse>
    
    /**
     * Get nearby feasts based on user location
     * @param lat Latitude of the user's location
     * @param lon Longitude of the user's location
     * @param radius Search radius in meters (default 500m)
     * @return List of nearby feasts with distance information
     */
    @GET("api/feasts/nearby")
    suspend fun getFeastsNearby(
        @Query("lat") lat: Double,
        @Query("lon") lon: Double,
        @Query("radius") radius: Double = 500.0
    ): Response<List<FeastResponse>>
    
    /**
     * Report a feast as fake or inappropriate
     * @param id Feast ID to report
     * @return Response containing updated feast data with updated report count
     */
    @PUT("api/feasts/{id}/report")
    suspend fun reportFeast(
        @retrofit2.http.Path("id") id: String
    ): Response<FeastResponse>

    /**
     * Get details of a specific feast by its ID
     * @param id The ID of the feast to fetch
     * @return Response containing the feast details
     */
    @GET("api/feasts/{id}")
    suspend fun getFeastById(
        @retrofit2.http.Path("id") id: String
    ): Response<FeastResponse>

    /**
     * Create a new local food shop
     * @param request Shop data including name, type, location, etc.
     * @return Response containing created shop data
     */
    @POST("api/localshops")
    suspend fun createLocalShop(
        @Body request: com.example.bhandara.data.models.api.LocalShopRequest
    ): Response<com.example.bhandara.data.models.api.LocalShopResponse>

    /**
     * Get nearby local shops based on user location
     * @param lat Latitude of the user's location
     * @param lon Longitude of the user's location
     * @param radius Search radius in meters (default 5000m)
     * @return List of nearby local shops with distance information
     */
    @GET("api/localshops/nearby")
    suspend fun getLocalShopsNearby(
        @Query("latitude") lat: Double,
        @Query("longitude") lon: Double,
        @Query("radius") radius: Double = 500.0
    ): Response<List<com.example.bhandara.data.models.api.LocalShopResponse>>

    /**
     * Get details of a specific local shop by its ID
     * @param id The ID of the shop to fetch
     * @return Response containing the shop details
     */
    @GET("api/localshops/{id}")
    suspend fun getLocalShopById(
        @retrofit2.http.Path("id") id: String
    ): Response<com.example.bhandara.data.models.api.LocalShopResponse>

    /**
     * Report a local shop
     * @param id The ID of the shop to report
     * @return Response containing the updated shop details
     */
    @PUT("api/localshops/{id}/report")
    suspend fun reportLocalShop(
        @retrofit2.http.Path("id") id: String
    ): Response<com.example.bhandara.data.models.api.LocalShopResponse>

    /**
     * Deactivate a local shop (only allowed for owner)
     * @param id The ID of the shop to deactivate
     * @return Response containing the updated shop details
     */
    @PUT("api/localshops/{id}/deactivate")
    suspend fun deactivateLocalShop(
        @retrofit2.http.Path("id") id: String
    ): Response<com.example.bhandara.data.models.api.LocalShopResponse>

    /**
     * Suggest deleting a local shop (available for non-owners)
     * @param id The ID of the shop to suggest deleting
     * @return Response containing the updated shop details
     */
    @PUT("api/localshops/{id}/suggest-delete")
    suspend fun suggestDeleteLocalShop(
        @retrofit2.http.Path("id") id: String
    ): Response<com.example.bhandara.data.models.api.LocalShopResponse>

    /**
     * Report the current user's location for crowd tracking.
     * Requires Firebase auth — handled automatically by AuthInterceptor.
     * Call every 30–60 seconds while the map screen is visible.
     */
    @POST("api/crowd/ping")
    suspend fun crowdPing(
        @Body request: CrowdPingRequest
    ): Response<CrowdPingResponse>

    /**
     * Fetch heatmap density points around a coordinate.
     * No auth required. Feed directly into HeatmapTileProvider.
     * @param lat Center latitude
     * @param lng Center longitude
     * @param radius Radius in metres (default 500 m)
     */
    @GET("api/crowd/heatmap")
    suspend fun getCrowdHeatmap(
        @Query("lat") lat: Double,
        @Query("lng") lng: Double,
        @Query("radius") radius: Int = 500
    ): Response<List<HeatmapPoint>>

    // --- Reviews ---
    
    @POST("api/reviews")
    suspend fun createReview(
        @Body request: com.example.bhandara.data.models.api.ReviewRequest
    ): Response<com.example.bhandara.data.models.api.ReviewResponse>
    
    @PUT("api/reviews/{id}")
    suspend fun updateReview(
        @retrofit2.http.Path("id") id: String,
        @Body request: com.example.bhandara.data.models.api.ReviewRequest
    ): Response<com.example.bhandara.data.models.api.ReviewResponse>
    
    @DELETE("api/reviews/{id}")
    suspend fun deleteReview(
        @retrofit2.http.Path("id") id: String
    ): Response<com.example.bhandara.data.models.api.GenericResponse>
    
    @GET("api/reviews/shop/{shopId}")
    suspend fun getReviewsForShop(
        @retrofit2.http.Path("shopId") shopId: String
    ): Response<List<com.example.bhandara.data.models.api.ReviewResponse>>
    
    @PUT("api/reviews/{reviewId}/report")
    suspend fun reportReview(
        @retrofit2.http.Path("reviewId") reviewId: String
    ): Response<com.example.bhandara.data.models.api.ReviewResponse>

    // --- Menu Items ---

    @POST("api/menu-items/manual/shop/{shopId}")
    suspend fun addMenuItemsManual(
        @retrofit2.http.Path("shopId") shopId: String,
        @Body request: com.example.bhandara.data.models.api.ManualMenuItemsRequest
    ): Response<List<com.example.bhandara.data.models.api.MenuItemResponse>>

    @retrofit2.http.Multipart
    @POST("api/menu-items/image/extract")
    suspend fun extractMenuItemsFromImage(
        @retrofit2.http.Part images: List<okhttp3.MultipartBody.Part>
    ): Response<com.example.bhandara.data.models.api.MenuItemExtractResponse>
}
