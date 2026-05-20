package com.example.bhandara.services

import android.annotation.SuppressLint
import android.content.Context
import android.os.Looper
import android.util.Log
import com.example.bhandara.data.api.CartStompClient
import com.example.bhandara.data.models.api.CartLocationUpdate
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class VendorLocationManager(context: Context) {

    private companion object {
        const val TAG = "VendorLocationManager"
        const val INTERVAL_MS = 15_000L
        const val MIN_DISPLACEMENT_M = 10f
    }

    private val fusedClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(context)

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private var shopId: Long = -1
    private var ownerUid: String = ""
    private var isRunning = false

    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(result: LocationResult) {
            val location = result.lastLocation ?: return
            val update = CartLocationUpdate(
                shopId = shopId,
                ownerUid = ownerUid,
                lat = location.latitude,
                lng = location.longitude,
                speed = if (location.hasSpeed()) location.speed.toDouble() else 0.0
            )
            scope.launch {
                try {
                    CartStompClient.sendCartLocation(update)
                    Log.d(TAG, "Sent location: ${location.latitude}, ${location.longitude}")
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to send location", e)
                }
            }
        }
    }

    @SuppressLint("MissingPermission")
    fun start(shopId: Long, ownerUid: String) {
        if (isRunning) return
        this.shopId = shopId
        this.ownerUid = ownerUid
        isRunning = true

        scope.launch {
            try {
                CartStompClient.connect()
            } catch (e: Exception) {
                Log.e(TAG, "WebSocket connect failed on start", e)
            }
        }

        val request = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, INTERVAL_MS)
            .setMinUpdateDistanceMeters(MIN_DISPLACEMENT_M)
            .setWaitForAccurateLocation(false)
            .build()

        fusedClient.requestLocationUpdates(request, locationCallback, Looper.getMainLooper())
        Log.d(TAG, "Vendor location tracking started for shop $shopId")
    }

    fun stop() {
        if (!isRunning) return
        isRunning = false
        fusedClient.removeLocationUpdates(locationCallback)
        scope.launch {
            CartStompClient.disconnect()
        }
        Log.d(TAG, "Vendor location tracking stopped")
    }

    fun isActive(): Boolean = isRunning
}
