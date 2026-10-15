package com.example.bhandara.push

import com.example.bhandara.platform.LocalAppDependencies
import com.example.bhandara.data.location.rememberDeviceLocation
import com.example.bhandara.data.location.currentTimeMillis
import com.example.bhandara.data.location.LocationService
import com.example.bhandara.data.location.LocationResult
import androidx.lifecycle.repeatOnLifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.compose.runtime.setValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Composable
import com.example.bhandara.data.location.GeoLocation
import com.example.bhandara.data.network.ApiResult
import com.example.bhandara.data.network.UserApi
import com.example.bhandara.data.settings.AppSettings
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * Keeps this phone registered with the backend, the same way on Android and iOS: once per account, then its push
 * token kept current and its location shared for feasts nearby. A phone without push notifications yet (an iPhone
 * until push is set up) registers too, so its notification settings are kept and its token can follow later.
 *
 * @param currentUid who is signed in, or null
 * @param pushToken this phone's push token, or null when it has none (yet)
 */
class DeviceRegistration(
    private val userApi: UserApi,
    private val settings: AppSettings,
    private val currentUid: () -> String?,
    private val pushToken: suspend () -> String?,
) {
    private val lock = Mutex()
    private val tokenSync = PushTokenSync(userApi, settings)

    /** Registers the phone if this account hasn't yet, else sends a new push token; true when the backend has it */
    suspend fun ensureRegistered(location: GeoLocation? = null): Boolean = lock.withLock {
        val uid = currentUid() ?: return false
        val token = currentToken()
        if (!settings.isRegisteredWithBackend(uid)) {
            if (userApi.register(uid, token, location) !is ApiResult.Success) return false
            settings.markRegisteredWithBackend(uid)
            token?.let { settings.markPushTokenSent(uid, it) }
            return true
        }
        token?.let { tokenSync.sync(uid, it) }
        true
    }

    /** Shares where the phone is now, for feasts nearby, registering it first if needed */
    suspend fun shareLocation(location: GeoLocation): Boolean {
        if (!ensureRegistered(location)) return false
        val uid = currentUid() ?: return false
        return userApi.updateLocation(uid, settings.lastPushTokenSent(uid), location) is ApiResult.Success
    }

    /** The push token, or null when there is none or it can't be had right now */
    private suspend fun currentToken(): String? = try {
        pushToken()?.trim()?.takeIf { it.isNotEmpty() }
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        null
    }
}

/**
 * Keeps the phone registered while the app runs, for platforms without a background worker (iOS): on start and each
 * time the app comes back, it registers if needed and, with location access, shares where the phone is (at most
 * every [LOCATION_EVERY_MILLIS]). It never asks for location access itself.
 *
 * @param pushToken this phone's push token; null until the platform can get one
 */
@Composable
fun KeepDeviceRegistered(pushToken: suspend () -> String? = { null }) {
    val dependencies = LocalAppDependencies.current
    val device = rememberDeviceLocation()
    val registration = remember {
        DeviceRegistration(UserApi(dependencies.apiClient), dependencies.settings, { dependencies.auth.currentUid }, pushToken)
    }
    val location = remember(device) { LocationService(device) }
    var lastShared by remember { mutableStateOf(0L) }
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    LaunchedEffect(registration, lifecycle) {
        lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
            if (dependencies.auth.ensureSignedIn().isFailure) return@repeatOnLifecycle
            val here = (location.locate() as? LocationResult.Found)?.location
            if (here != null && currentTimeMillis() - lastShared >= LOCATION_EVERY_MILLIS) {
                if (registration.shareLocation(here)) lastShared = currentTimeMillis()
            } else {
                registration.ensureRegistered()
            }
        }
    }
}

/** How often a phone without a background worker shares its location while open */
private const val LOCATION_EVERY_MILLIS = 10 * 60_000L
