package com.example.bhandara.utils

import com.example.bhandara.data.location.AndroidDeviceLocation
import com.example.bhandara.data.location.LocationResult
import com.example.bhandara.data.location.LocationService
import android.Manifest
import android.content.Context
import android.os.Build
import android.util.Log
import com.example.bhandara.data.location.GeoLocation

class LocationHelper(private val context: Context) {

    // The shared logic (also used on iOS): accepts precise or approximate permission, waits up to 10 s for a
    // fresh fix and otherwise falls back to the phone's last fix if it's recent
    private val device = AndroidDeviceLocation(context)
    private val locationService = LocationService(device)

    companion object {
        private const val TAG = "LocationHelper"

        val REQUIRED_PERMISSIONS = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            AndroidDeviceLocation.PERMISSIONS + Manifest.permission.POST_NOTIFICATIONS
        } else {
            AndroidDeviceLocation.PERMISSIONS
        }
    }

    /** Whether location access is allowed, precise or approximate */
    fun hasLocationPermissions(): Boolean = device.hasPermission()

    /** Where the user is, or null without permission or when the phone can't tell */
    suspend fun getCurrentLocation(): GeoLocation? = when (val result = locationService.locate()) {
        is LocationResult.Found -> result.location
        LocationResult.PermissionDenied -> null.also { Log.w(TAG, "Location permission not granted") }
        LocationResult.Unavailable -> null.also { Log.w(TAG, "Location unavailable") }
    }

    /** The phone's last known location (fast, but may be old) */
    suspend fun getLastKnownLocation(): GeoLocation? =
        if (device.hasPermission()) runCatching { device.lastKnown()?.location }.getOrNull() else null
}
