package com.example.bhandara.utils

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.os.Build
import android.util.Log
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import com.google.firebase.firestore.GeoPoint
import kotlinx.coroutines.tasks.await

class LocationHelper(private val context: Context) {
    
    private val fusedLocationClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(context)
    
    companion object {
        private const val TAG = "LocationHelper"
        
        val REQUIRED_PERMISSIONS = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.POST_NOTIFICATIONS
            )
        } else {
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        }
    }
    
    /**
     * Check if location permissions are granted
     */
    fun hasLocationPermissions(): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }
    
    /**
     * Get current location
     */
    suspend fun getCurrentLocation(): GeoPoint? {
        if (!hasLocationPermissions()) {
            Log.w(TAG, "Location permissions not granted")
            return null
        }
        
        return try {
            val cancellationTokenSource = CancellationTokenSource()
            val location: Location = fusedLocationClient.getCurrentLocation(
                Priority.PRIORITY_HIGH_ACCURACY,
                cancellationTokenSource.token
            ).await()
            
            GeoPoint(location.latitude, location.longitude).also {
                Log.d(TAG, "Location obtained: ${it.latitude}, ${it.longitude}")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get location", e)
            null
        }
    }
    
    /**
     * Get last known location (faster but may be outdated)
     */
    suspend fun getLastKnownLocation(): GeoPoint? {
        if (!hasLocationPermissions()) {
            Log.w(TAG, "Location permissions not granted")
            return null
        }
        
        return try {
            val location: Location? = fusedLocationClient.lastLocation.await()
            location?.let {
                GeoPoint(it.latitude, it.longitude).also { geoPoint ->
                    Log.d(TAG, "Last known location: ${geoPoint.latitude}, ${geoPoint.longitude}")
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get last known location", e)
            null
        }
    }
}
