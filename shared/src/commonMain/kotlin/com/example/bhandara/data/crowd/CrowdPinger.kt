package com.example.bhandara.data.crowd

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import com.example.bhandara.data.location.GeoLocation
import com.example.bhandara.data.location.LocationResult
import com.example.bhandara.data.location.LocationService
import com.example.bhandara.data.location.currentTimeMillis
import com.example.bhandara.data.location.distanceMeters
import com.example.bhandara.data.location.rememberDeviceLocation
import com.example.bhandara.data.network.ApiResult
import com.example.bhandara.data.network.CrowdApi
import com.example.bhandara.data.network.ShopApi
import com.example.bhandara.platform.LocalAppDependencies
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive

/**
 * Feeds the "Busy now / Quiet" badges, the same on Android and iOS: while the app is open, now and then tells the
 * backend that someone is at a shop.
 *
 * A ping is only sent when the user is within [NEAR_SHOP_RADIUS_M] of a shop, so the app never reports where people
 * are unless they're at a food spot. It never asks for location access: without it, nothing happens. The location
 * is fresh for every check (people move); nearby shops are kept for a while and fetched again when old or after
 * moving a fair distance.
 */
class CrowdPinger(
    private val location: LocationService,
    private val shopApi: ShopApi,
    private val crowdApi: CrowdApi,
    private val isSignedIn: () -> Boolean,
    private val nowMillis: () -> Long = ::currentTimeMillis,
) {
    private var shops: List<GeoLocation> = emptyList()
    private var shopsAround: GeoLocation? = null
    private var shopsFetchedAt = 0L

    /** Checks every [PING_INTERVAL_MS] until cancelled (when the app goes to the background) */
    suspend fun runWhileForeground() {
        while (currentCoroutineContext().isActive) {
            try {
                pingIfAtAShop()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                // Best effort: the next check tries again
            }
            delay(PING_INTERVAL_MS)
        }
    }

    /** @return whether a ping went out */
    suspend fun pingIfAtAShop(): Boolean {
        // The backend only takes pings from signed-in users (guests count)
        if (!isSignedIn()) return false
        val here = (location.locate() as? LocationResult.Found)?.location ?: return false
        val atAShop = shopsNear(here).any { distanceMeters(it, here) <= NEAR_SHOP_RADIUS_M }
        return atAShop && crowdApi.ping(here) is ApiResult.Success
    }

    private suspend fun shopsNear(here: GeoLocation): List<GeoLocation> {
        val center = shopsAround
        val stale = center == null || nowMillis() - shopsFetchedAt > SHOP_CACHE_MAX_AGE_MS || distanceMeters(center, here) > SHOP_CACHE_MAX_DRIFT_M
        if (stale) {
            val result = shopApi.getNearbyShops(here, SHOP_SEARCH_RADIUS_M)
            if (result is ApiResult.Success) {
                shops = result.value.map { GeoLocation(it.latitude, it.longitude) }
                shopsAround = here
                shopsFetchedAt = nowMillis()
            }
        }
        return shops
    }

    companion object {
        /** The backend keeps a ping for 5 minutes and takes about one every 30 seconds */
        const val PING_INTERVAL_MS = 45_000L

        /** Phone GPS is often 10–30 m off, so 20 m missed people standing at the shop */
        const val NEAR_SHOP_RADIUS_M = 50.0

        const val SHOP_SEARCH_RADIUS_M = 1_000.0
        const val SHOP_CACHE_MAX_AGE_MS = 10 * 60_000L
        const val SHOP_CACHE_MAX_DRIFT_M = 500.0
    }
}

/** Runs [CrowdPinger] while the app is in the foreground, on whichever screen */
@Composable
fun KeepCountingCrowds() {
    val dependencies = LocalAppDependencies.current
    val device = rememberDeviceLocation()
    val pinger = remember(device) {
        CrowdPinger(
            LocationService(device), ShopApi(dependencies.apiClient), CrowdApi(dependencies.apiClient),
            isSignedIn = { dependencies.auth.currentUid != null },
        )
    }
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    LaunchedEffect(pinger, lifecycle) {
        lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) { pinger.runWhileForeground() }
    }
}
