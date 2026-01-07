package com.example.bhandara.data.api

import com.example.bhandara.data.models.api.CreateUserRequest
import com.example.bhandara.data.models.api.CreateUserResponse
import com.example.bhandara.data.models.api.FeastRequest
import com.example.bhandara.data.models.api.FeastResponse
import com.example.bhandara.data.models.api.UpdateLocationRequest
import com.example.bhandara.data.models.api.UpdateLocationResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.PUT

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
}
