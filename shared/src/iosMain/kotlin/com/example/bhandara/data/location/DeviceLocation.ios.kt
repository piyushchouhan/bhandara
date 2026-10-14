package com.example.bhandara.data.location

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.useContents
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import platform.CoreLocation.CLLocation
import platform.CoreLocation.CLLocationManager
import platform.CoreLocation.CLLocationManagerDelegateProtocol
import platform.CoreLocation.kCLAuthorizationStatusAuthorizedAlways
import platform.CoreLocation.kCLAuthorizationStatusAuthorizedWhenInUse
import platform.CoreLocation.kCLAuthorizationStatusNotDetermined
import platform.Foundation.NSError
import platform.Foundation.timeIntervalSince1970
import platform.darwin.NSObject
import kotlin.coroutines.resume

/** The phone's location on iOS, from Core Location ("While Using the App" permission) */
class IosDeviceLocation : DeviceLocation {

    private val delegate = Delegate()
    private val manager = CLLocationManager().apply { delegate = this@IosDeviceLocation.delegate }
    private val oneRequestAtATime = Mutex()

    override fun hasPermission(): Boolean = manager.authorizationStatus.let {
        it == kCLAuthorizationStatusAuthorizedWhenInUse || it == kCLAuthorizationStatusAuthorizedAlways
    }

    override suspend fun current(): GeoLocation? = oneRequestAtATime.withLock {
        suspendCancellableCoroutine { continuation ->
            delegate.onLocation = { location ->
                delegate.onLocation = null
                if (continuation.isActive) continuation.resume(location?.toGeoLocation())
            }
            manager.requestLocation()
        }
    }

    override suspend fun lastKnown(): TimedLocation? = manager.location?.let {
        TimedLocation(it.toGeoLocation(), (it.timestamp.timeIntervalSince1970 * 1000).toLong())
    }

    /** Asks for "While Using the App" access unless the user already decided, then reports the answer */
    fun requestPermission(onResult: (granted: Boolean) -> Unit) {
        if (manager.authorizationStatus != kCLAuthorizationStatusNotDetermined) {
            onResult(hasPermission())
            return
        }
        delegate.onAuthorization = {
            if (manager.authorizationStatus != kCLAuthorizationStatusNotDetermined) {
                delegate.onAuthorization = null
                onResult(hasPermission())
            }
        }
        manager.requestWhenInUseAuthorization()
    }

    private class Delegate : NSObject(), CLLocationManagerDelegateProtocol {
        var onLocation: ((CLLocation?) -> Unit)? = null
        var onAuthorization: (() -> Unit)? = null

        override fun locationManager(manager: CLLocationManager, didUpdateLocations: List<*>) {
            onLocation?.invoke(didUpdateLocations.lastOrNull() as? CLLocation)
        }

        override fun locationManager(manager: CLLocationManager, didFailWithError: NSError) {
            onLocation?.invoke(null)
        }

        override fun locationManagerDidChangeAuthorization(manager: CLLocationManager) {
            onAuthorization?.invoke()
        }
    }
}

@OptIn(ExperimentalForeignApi::class)
private fun CLLocation.toGeoLocation(): GeoLocation =
    coordinate.useContents { GeoLocation(latitude, longitude) }

@Composable
actual fun rememberDeviceLocation(): DeviceLocation = remember { IosDeviceLocation() }

@Composable
actual fun rememberLocationPermissionRequest(onResult: (granted: Boolean) -> Unit): () -> Unit {
    val location = remember { IosDeviceLocation() }
    return { location.requestPermission(onResult) }
}
