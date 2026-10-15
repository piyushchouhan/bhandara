package com.example.bhandara.vendor

import com.example.bhandara.data.location.GeoLocation
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.useContents
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import platform.CoreLocation.CLLocation
import platform.CoreLocation.CLLocationManager
import platform.CoreLocation.CLLocationManagerDelegateProtocol
import platform.CoreLocation.kCLAuthorizationStatusAuthorizedWhenInUse
import platform.CoreLocation.kCLAuthorizationStatusNotDetermined
import platform.CoreLocation.kCLLocationAccuracyBest
import platform.CoreLocation.kCLLocationAccuracyHundredMeters
import platform.Foundation.NSError
import platform.Foundation.NSProcessInfo
import platform.darwin.NSObject

/**
 * iOS's Vendor Mode: Core Location keeps delivering the cart's position while the screen is off (the app's
 * "location" background mode, and the blue indicator so the vendor sees it's on), feeding the shared
 * [VendorSharing]. Asks for "Always" access, which sharing from the background needs.
 */
class IosVendorTracker(
    private val apiBaseUrl: String,
    private val idToken: suspend () -> String?,
) : VendorTracker {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private val delegate = Delegate()
    private val manager = CLLocationManager().apply {
        delegate = this@IosVendorTracker.delegate
        pausesLocationUpdatesAutomatically = false
    }
    private var sharing: VendorSharing? = null

    override val isActive: Boolean get() = sharing?.isRunning == true

    override fun start(shopId: Long, ownerUid: String) {
        if (isActive) return
        val sender = StompCartSender.connectingTo(apiBaseUrl, idToken, ::uptimeMillis)
        val started = VendorSharing(shopId, ownerUid, sender, scope, ::uptimeMillis, onPowerChange = ::use)
        sharing = started
        delegate.onFix = { started.onFix(it) }
        started.start()
        when (manager.authorizationStatus) {
            kCLAuthorizationStatusNotDetermined, kCLAuthorizationStatusAuthorizedWhenInUse -> manager.requestAlwaysAuthorization()
            else -> {}
        }
        manager.allowsBackgroundLocationUpdates = true
        manager.showsBackgroundLocationIndicator = true
        use(VendorPower.MOVING)
        manager.startUpdatingLocation()
    }

    override fun stop() {
        val stopping = sharing ?: return
        sharing = null
        delegate.onFix = null
        manager.stopUpdatingLocation()
        manager.allowsBackgroundLocationUpdates = false
        scope.launch { stopping.stop() }
    }

    /** Accurate GPS while the cart moves; cheap location once it's parked, enough to notice it moving again */
    private fun use(power: VendorPower) {
        when (power) {
            VendorPower.MOVING -> {
                manager.desiredAccuracy = kCLLocationAccuracyBest
                manager.distanceFilter = VendorSharing.MOVING_MIN_DISTANCE_M
            }
            VendorPower.PARKED -> {
                manager.desiredAccuracy = kCLLocationAccuracyHundredMeters
                manager.distanceFilter = VendorSharing.WAKE_DISTANCE_M / 2
            }
        }
    }

    private class Delegate : NSObject(), CLLocationManagerDelegateProtocol {
        var onFix: ((VendorFix) -> Unit)? = null

        @OptIn(ExperimentalForeignApi::class)
        override fun locationManager(manager: CLLocationManager, didUpdateLocations: List<*>) {
            val location = didUpdateLocations.lastOrNull() as? CLLocation ?: return
            val position = location.coordinate.useContents { GeoLocation(latitude, longitude) }
            onFix?.invoke(
                VendorFix(
                    location = position,
                    atMillis = uptimeMillis(),
                    accuracyMeters = location.horizontalAccuracy.coerceAtLeast(0.0),
                    speedMetersPerSecond = location.speed.takeIf { it >= 0 },
                ),
            )
        }

        override fun locationManager(manager: CLLocationManager, didFailWithError: NSError) {
            // A fix failed (e.g. indoors); Core Location keeps trying
        }
    }
}

/** Milliseconds since boot: never jumps when the clock is changed */
private fun uptimeMillis(): Long = (NSProcessInfo.processInfo.systemUptime * 1000).toLong()
