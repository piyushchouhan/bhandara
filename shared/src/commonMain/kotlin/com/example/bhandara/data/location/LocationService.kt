package com.example.bhandara.data.location

import androidx.compose.runtime.Composable
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.withTimeoutOrNull

/** A location and when the phone determined it */
data class TimedLocation(val location: GeoLocation, val epochMillis: Long)

/**
 * The phone's location as each platform provides it: Google Play services on Android, Core Location on iOS.
 * [LocationService] builds the app's location logic on top of it.
 */
interface DeviceLocation {

    /** Whether the user allowed location access (precise or approximate) */
    fun hasPermission(): Boolean

    /** A fresh fix; may take a few seconds, may fail (null or an exception) */
    suspend fun current(): GeoLocation?

    /** The last fix the phone already knows, possibly old; null if none */
    suspend fun lastKnown(): TimedLocation?
}

sealed interface LocationResult {

    data class Found(val location: GeoLocation) : LocationResult

    /** The user hasn't allowed location access */
    data object PermissionDenied : LocationResult

    /** Allowed, but the phone couldn't tell where it is (e.g. location services are off) */
    data object Unavailable : LocationResult
}

/**
 * Where the user is, the same way on Android and iOS: a fresh fix if one comes within [timeoutMillis], otherwise
 * the phone's last fix if it's recent enough to trust (e.g. indoors, where a fresh fix often fails).
 */
class LocationService(
    private val device: DeviceLocation,
    private val timeoutMillis: Long = 10_000,
    private val maxFallbackAgeMillis: Long = 10 * 60_000,
    private val nowMillis: () -> Long = ::currentTimeMillis,
) {
    suspend fun locate(): LocationResult {
        if (!device.hasPermission()) return LocationResult.PermissionDenied

        val fresh = withTimeoutOrNull(timeoutMillis) {
            try {
                device.current()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                null
            }
        }
        if (fresh != null) return LocationResult.Found(fresh)

        val recent = try {
            device.lastKnown()?.takeIf { nowMillis() - it.epochMillis <= maxFallbackAgeMillis }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            null
        }
        return if (recent != null) LocationResult.Found(recent.location) else LocationResult.Unavailable
    }
}

/** Milliseconds since 1970, from the platform's clock */
expect fun currentTimeMillis(): Long

/** This platform's [DeviceLocation] */
@Composable
expect fun rememberDeviceLocation(): DeviceLocation

/**
 * Returns a function that asks the user for location access (if they haven't decided yet) and then calls
 * [onResult] with whether it's allowed, precise or approximate.
 */
@Composable
expect fun rememberLocationPermissionRequest(onResult: (granted: Boolean) -> Unit): () -> Unit
