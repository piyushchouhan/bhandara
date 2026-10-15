package com.example.bhandara.services

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.os.Looper
import android.os.SystemClock
import com.example.bhandara.BuildConfig
import com.example.bhandara.data.auth.AppAuth
import com.example.bhandara.data.location.GeoLocation
import com.example.bhandara.vendor.StompCartSender
import com.example.bhandara.vendor.VendorFix
import com.example.bhandara.vendor.VendorPower
import com.example.bhandara.vendor.VendorSharing
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

/**
 * Android's location hardware for Vendor Mode: Google Play location fixes go to the shared [VendorSharing], which
 * decides what to send and when to save battery, and switches between accurate GPS (moving) and cheap Wi-Fi / cell
 * location (parked).
 *
 * App-wide singleton: there must only ever be one location stream per vendor. Normally it is driven by
 * [VendorTrackingService], which keeps it running while the screen is off.
 */
class VendorLocationManager private constructor(context: Context) {

    companion object {
        @Volatile
        private var instance: VendorLocationManager? = null

        fun getInstance(context: Context): VendorLocationManager =
            instance ?: synchronized(this) {
                instance ?: VendorLocationManager(context.applicationContext).also { instance = it }
            }
    }

    private val fusedClient: FusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context)

    // Location callbacks come on the main thread, so sharing runs there too
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    private val sender = StompCartSender.connectingTo(BuildConfig.API_BASE_URL, AppAuth.service::idToken, SystemClock::elapsedRealtime)

    @Volatile
    private var sharing: VendorSharing? = null

    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(result: LocationResult) {
            result.lastLocation?.let { sharing?.onFix(it.toFix()) }
        }
    }

    @SuppressLint("MissingPermission")
    @Synchronized
    fun start(shopId: Long, ownerUid: String) {
        if (sharing?.isRunning == true) return
        val started = VendorSharing(shopId, ownerUid, sender, scope, SystemClock::elapsedRealtime, onPowerChange = ::requestUpdates)
        sharing = started
        started.start()
        requestUpdates(VendorPower.MOVING)
        // Show the cart straight away instead of waiting for the first movement
        fusedClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
            .addOnSuccessListener { location -> location?.let { started.onFix(it.toFix()) } }
    }

    @Synchronized
    fun stop() {
        val stopping = sharing ?: return
        sharing = null
        fusedClient.removeLocationUpdates(locationCallback)
        scope.launch { stopping.stop() }
    }

    fun isActive(): Boolean = sharing?.isRunning == true

    @SuppressLint("MissingPermission")
    private fun requestUpdates(power: VendorPower) {
        fusedClient.removeLocationUpdates(locationCallback)
        val request = when (power) {
            VendorPower.MOVING -> LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, VendorSharing.MOVING_EVERY_MILLIS)
                .setMinUpdateDistanceMeters(VendorSharing.MOVING_MIN_DISTANCE_M.toFloat())
                .setWaitForAccurateLocation(false)
                .build()
            VendorPower.PARKED -> LocationRequest.Builder(Priority.PRIORITY_BALANCED_POWER_ACCURACY, VendorSharing.PARKED_EVERY_MILLIS)
                .setMinUpdateIntervalMillis(VendorSharing.PARKED_EVERY_MILLIS / 2)
                .build()
        }
        fusedClient.requestLocationUpdates(request, locationCallback, Looper.getMainLooper())
    }

    private fun Location.toFix() = VendorFix(
        location = GeoLocation(latitude, longitude),
        atMillis = SystemClock.elapsedRealtime(),
        accuracyMeters = if (hasAccuracy()) accuracy.toDouble() else 0.0,
        speedMetersPerSecond = if (hasSpeed()) speed.toDouble() else null,
    )
}
