package com.example.bhandara.data.api

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import okhttp3.Interceptor
import okhttp3.Response

/**
 * Interceptor to add Firebase ID token to API requests
 */
class AuthInterceptor : Interceptor {
    
    companion object {
        private const val TAG = "AuthInterceptor"
    }
    
    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser == null) {
            return chain.proceed(originalRequest)
        }
        
        val idToken = try {
            runBlocking {
                currentUser.getIdToken(false).await().token
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get Firebase ID token", e)
            null
        }
        
        if (idToken == null) {
            return chain.proceed(originalRequest)
        }
        
        val authenticatedRequest = originalRequest.newBuilder()
            .header("Authorization", "Bearer $idToken")
            .build()
        
        return chain.proceed(authenticatedRequest)
    }
}
