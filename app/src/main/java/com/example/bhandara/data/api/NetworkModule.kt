package com.example.bhandara.data.api

import com.example.bhandara.BuildConfig
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/**
 * Network module for Retrofit configuration
 */
object NetworkModule {
    
    private const val TIMEOUT_SECONDS = 30L
    
    /**
     * OkHttp client with logging and auth interceptors
     */
    private val okHttpClient: OkHttpClient by lazy {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = if (BuildConfig.DEBUG) {
                HttpLoggingInterceptor.Level.BODY
            } else {
                HttpLoggingInterceptor.Level.NONE
            }
        }
        
        val authInterceptor = AuthInterceptor()
        
        OkHttpClient.Builder()
            .addInterceptor(authInterceptor)  // Add auth token first
            .addInterceptor(loggingInterceptor)  // Then log the authenticated request
            .connectTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .readTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .writeTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .build()
    }
    
    /**
     * Retrofit instance
     */
    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BuildConfig.API_BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
    
    /**
     * API service instance
     */
    val apiService: ApiService by lazy {
        retrofit.create(ApiService::class.java)
    }
}
