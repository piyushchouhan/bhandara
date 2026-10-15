package com.example.bhandara.vendor

import com.example.bhandara.data.location.GeoLocation
import com.example.bhandara.data.location.distanceMeters
import com.example.bhandara.data.models.api.CartLocationUpdate
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

/** A location fix as the phone gives it; [atMillis] from a clock that never jumps (e.g. time since boot) */
data class VendorFix(
    val location: GeoLocation,
    val atMillis: Long,
    val accuracyMeters: Double = 0.0,
    val speedMetersPerSecond: Double? = null,
)

/** How hard the phone looks for the cart: accurate GPS while it moves, cheap location once it's parked */
enum class VendorPower { MOVING, PARKED }

/** Sends a cart's location to the backend (STOMP), for customers following it */
interface CartLocationSender {
    suspend fun send(update: CartLocationUpdate)

    /** The cart stopped sharing: it leaves customers' maps at once */
    suspend fun sendOffline(update: CartLocationUpdate)

    suspend fun close()
}

/**
 * Shares a vendor's cart location while Vendor Mode is on, the same way on Android and iOS, using as little battery
 * as it can:
 * - MOVING: every fix is sent, so customers see the cart move smoothly
 * - PARKED: after staying within [PARK_RADIUS_M] for [PARK_AFTER_MILLIS], the phone switches to cheap location and
 *   only clear movement ([WAKE_DISTANCE_M] beyond the fix's accuracy) switches accurate location back on
 * - either way, the last position is sent again every [PRESENCE_EVERY_MILLIS] when nothing else was, so a parked
 *   cart stays on the map (the backend drops carts silent for 2 minutes) and a dead connection is noticed
 *
 * The platform feeds it fixes ([onFix]) and switches the location hardware when [onPowerChange] says so.
 */
class VendorSharing(
    private val shopId: Long,
    private val ownerUid: String,
    private val sender: CartLocationSender,
    private val scope: CoroutineScope,
    private val nowMillis: () -> Long,
    private val onPowerChange: (VendorPower) -> Unit = {},
) {
    var power: VendorPower = VendorPower.MOVING
        private set

    /** Sharing until [stop] */
    var isRunning: Boolean = false
        private set

    private var anchor: GeoLocation? = null
    private var anchorSince = 0L
    private var lastUpdate: CartLocationUpdate? = null
    private var lastSentAt = 0L
    private var presence: Job? = null

    fun start() {
        if (isRunning) return
        isRunning = true
        power = VendorPower.MOVING
        anchor = null
        lastUpdate = null
        presence = scope.launch {
            while (isActive) {
                delay(PRESENCE_EVERY_MILLIS)
                checkParked()
                sendPresenceIfQuiet()
            }
        }
    }

    /** A new location from the phone */
    fun onFix(fix: VendorFix) {
        if (!isRunning) return
        when (power) {
            VendorPower.MOVING -> {
                send(fix)
                val parkedAt = anchor
                if (parkedAt == null || distanceMeters(parkedAt, fix.location) > PARK_RADIUS_M) {
                    anchor = fix.location
                    anchorSince = fix.atMillis
                }
                checkParked()
            }
            VendorPower.PARKED -> {
                val parkedAt = anchor ?: fix.location
                // Cheap fixes wander by tens of meters, so only clear movement counts
                if (distanceMeters(parkedAt, fix.location) - fix.accuracyMeters > WAKE_DISTANCE_M) {
                    anchor = fix.location
                    anchorSince = fix.atMillis
                    switch(VendorPower.MOVING)
                    send(fix)
                }
            }
        }
    }

    /** Stops sharing and takes the cart off customers' maps now, rather than after the backend's 2-minute timeout */
    suspend fun stop() {
        if (!isRunning) return
        isRunning = false
        presence?.cancel()
        presence = null
        val last = lastUpdate
        try {
            sender.sendOffline(CartLocationUpdate(shopId, ownerUid, last?.lat ?: 0.0, last?.lng ?: 0.0))
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            // It drops off the map after the timeout instead
        }
        sender.close()
    }

    private fun checkParked() {
        if (!isRunning || power != VendorPower.MOVING || anchor == null) return
        if (nowMillis() - anchorSince >= PARK_AFTER_MILLIS) switch(VendorPower.PARKED)
    }

    private fun sendPresenceIfQuiet() {
        val last = lastUpdate ?: return
        if (nowMillis() - lastSentAt < PRESENCE_EVERY_MILLIS) return
        send(last.copy(speed = 0.0))
    }

    private fun send(fix: VendorFix) = send(
        CartLocationUpdate(shopId, ownerUid, fix.location.latitude, fix.location.longitude, fix.speedMetersPerSecond ?: 0.0),
    )

    private fun send(update: CartLocationUpdate) {
        lastUpdate = update
        lastSentAt = nowMillis()
        scope.launch {
            try {
                sender.send(update)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                // The sender reconnects; the next fix or presence tick tries again
            }
        }
    }

    private fun switch(to: VendorPower) {
        power = to
        onPowerChange(to)
    }

    companion object {
        /** Accurate fixes while moving: how often, and how far apart at least */
        const val MOVING_EVERY_MILLIS = 4_000L
        const val MOVING_MIN_DISTANCE_M = 5.0

        /** Cheap fixes while parked */
        const val PARKED_EVERY_MILLIS = 30_000L

        const val PARK_RADIUS_M = 20.0
        const val PARK_AFTER_MILLIS = 120_000L
        const val WAKE_DISTANCE_M = 40.0

        /** Well under the backend's 2-minute timeout */
        const val PRESENCE_EVERY_MILLIS = 45_000L
    }
}
