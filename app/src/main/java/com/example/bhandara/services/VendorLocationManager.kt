package com.example.bhandara.services

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.os.Looper
import android.os.SystemClock
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
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

/**
 * Shares the vendor's cart location while vendor mode is on, using as little battery as possible.
 *
 * - MOVING: high-accuracy GPS every few seconds, so customers see the cart move smoothly.
 * - PARKED: after staying within a small radius for a couple of minutes, GPS is switched off and only
 *   low-power (Wi-Fi / cell) location is used to notice when the cart starts moving again.
 * - In both modes a presence update (the last known position, no new GPS fix) is sent every 45s when
 *   nothing else was sent, so a parked cart stays on the map and a dead connection is replaced quickly.
 *
 * App-wide singleton: there must only ever be one active location stream per vendor. Screens come and
 * go, so callers use [getInstance] rather than creating their own instance. Normally it is driven by
 * [VendorTrackingService], which keeps it running while the screen is off.
 */
class VendorLocationManager private constructor(context: Context) {

    companion object {
        private const val TAG = "VendorLocationManager"

        // MOVING: frequent, accurate fixes for smooth movement on customers' maps
        private const val MOVING_INTERVAL_MS = 4_000L
        private const val MOVING_MIN_DISTANCE_M = 5f

        // PARKED: cheap fixes, only used to notice the cart moving again
        private const val PARKED_INTERVAL_MS = 30_000L
        private const val PARKED_MIN_INTERVAL_MS = 15_000L

        // Considered parked after staying within PARK_RADIUS_M for PARK_AFTER_MS
        private const val PARK_RADIUS_M = 20f
        private const val PARK_AFTER_MS = 120_000L

        // Low-power fixes can be off by tens of meters, so only clear movement wakes GPS back up
        private const val WAKE_DISTANCE_M = 40f

        // Must stay well under the backend's 120s heartbeat timeout
        private const val PRESENCE_INTERVAL_MS = 45_000L

        @Volatile
        private var instance: VendorLocationManager? = null

        fun getInstance(context: Context): VendorLocationManager =
            instance ?: synchronized(this) {
                instance ?: VendorLocationManager(context.applicationContext).also { instance = it }
            }
    }

    private enum class Mode { MOVING, PARKED }

    private val fusedClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(context)

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private var shopId: Long = -1
    private var ownerUid: String = ""

    @Volatile
    private var isRunning = false

    // Mode state is only touched on the main thread (location callbacks and the presence loop run there)
    private var mode = Mode.MOVING
    private var anchor: Location? = null
    private var anchorSinceMs = 0L

    @Volatile
    private var lastUpdate: CartLocationUpdate? = null

    @Volatile
    private var lastSentAtMs = 0L

    private var presenceJob: Job? = null

    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(result: LocationResult) {
            result.lastLocation?.let { onLocation(it) }
        }
    }

    @SuppressLint("MissingPermission")
    @Synchronized
    fun start(shopId: Long, ownerUid: String) {
        if (isRunning) return
        this.shopId = shopId
        this.ownerUid = ownerUid
        isRunning = true
        mode = Mode.MOVING
        anchor = null
        lastUpdate = null
        lastSentAtMs = 0L

        requestUpdates(Mode.MOVING)

        // Show the cart straight away instead of waiting for the first movement
        fusedClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
            .addOnSuccessListener { location -> location?.let { onLocation(it) } }

        presenceJob = scope.launch(Dispatchers.Main) {
            while (isActive) {
                delay(PRESENCE_INTERVAL_MS)
                checkParked()
                sendPresenceIfQuiet()
            }
        }

        Log.d(TAG, "Vendor location sharing started for shop $shopId")
    }

    @Synchronized
    fun stop() {
        if (!isRunning) return
        isRunning = false
        fusedClient.removeLocationUpdates(locationCallback)
        presenceJob?.cancel()
        presenceJob = null

        val offline = CartLocationUpdate(
            shopId = shopId,
            ownerUid = ownerUid,
            lat = lastUpdate?.lat ?: 0.0,
            lng = lastUpdate?.lng ?: 0.0
        )
        scope.launch {
            // Take the cart off customers' maps now rather than after the 2-minute heartbeat timeout
            try {
                CartStompClient.vendor.sendCartOffline(offline)
            } catch (e: Exception) {
                Log.w(TAG, "Could not send offline update; cart will drop off after the heartbeat timeout", e)
            }
            CartStompClient.vendor.disconnect()
        }
        Log.d(TAG, "Vendor location sharing stopped")
    }

    fun isActive(): Boolean = isRunning

    private fun onLocation(location: Location) {
        if (!isRunning) return
        val now = SystemClock.elapsedRealtime()

        when (mode) {
            Mode.MOVING -> {
                send(location)
                val current = anchor
                if (current == null || current.distanceTo(location) > PARK_RADIUS_M) {
                    anchor = location
                    anchorSinceMs = now
                }
                checkParked()
            }
            Mode.PARKED -> {
                val parkedAt = anchor ?: location
                if (parkedAt.distanceTo(location) - location.accuracy > WAKE_DISTANCE_M) {
                    Log.d(TAG, "Cart is moving again, switching GPS back on")
                    anchor = location
                    anchorSinceMs = now
                    switchMode(Mode.MOVING)
                    send(location)
                }
            }
        }
    }

    /** Moving carts that have stayed put long enough switch to low-power location */
    private fun checkParked() {
        if (!isRunning || mode != Mode.MOVING || anchor == null) return
        if (SystemClock.elapsedRealtime() - anchorSinceMs >= PARK_AFTER_MS) {
            Log.d(TAG, "Cart parked, switching to low-power location")
            switchMode(Mode.PARKED)
        }
    }

    private fun sendPresenceIfQuiet() {
        val last = lastUpdate ?: return
        if (SystemClock.elapsedRealtime() - lastSentAtMs < PRESENCE_INTERVAL_MS) return
        send(last.copy(speed = 0.0))
    }

    private fun send(location: Location) {
        send(
            CartLocationUpdate(
                shopId = shopId,
                ownerUid = ownerUid,
                lat = location.latitude,
                lng = location.longitude,
                speed = if (location.hasSpeed()) location.speed.toDouble() else 0.0
            )
        )
    }

    private fun send(update: CartLocationUpdate) {
        lastUpdate = update
        lastSentAtMs = SystemClock.elapsedRealtime()
        scope.launch {
            try {
                CartStompClient.vendor.sendCartLocation(update)
            } catch (e: Exception) {
                // The client reconnects with backoff; the next update or presence tick retries
                Log.e(TAG, "Failed to send location", e)
            }
        }
    }

    private fun switchMode(newMode: Mode) {
        mode = newMode
        fusedClient.removeLocationUpdates(locationCallback)
        requestUpdates(newMode)
    }

    @SuppressLint("MissingPermission")
    private fun requestUpdates(forMode: Mode) {
        val request = when (forMode) {
            Mode.MOVING -> LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, MOVING_INTERVAL_MS)
                .setMinUpdateDistanceMeters(MOVING_MIN_DISTANCE_M)
                .setWaitForAccurateLocation(false)
                .build()
            Mode.PARKED -> LocationRequest.Builder(Priority.PRIORITY_BALANCED_POWER_ACCURACY, PARKED_INTERVAL_MS)
                .setMinUpdateIntervalMillis(PARKED_MIN_INTERVAL_MS)
                .build()
        }
        fusedClient.requestLocationUpdates(request, locationCallback, Looper.getMainLooper())
    }
}
