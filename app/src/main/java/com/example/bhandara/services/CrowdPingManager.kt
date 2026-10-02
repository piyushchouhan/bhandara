package com.example.bhandara.services

import android.content.Context
import android.location.Location
import android.os.SystemClock
import android.util.Log
import com.example.bhandara.data.api.NetworkModule
import com.example.bhandara.data.models.api.CrowdPingRequest
import com.example.bhandara.utils.LocationHelper
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive

/**
 * Feeds the "Busy now / Quiet" badges: while the app is open, periodically tells the backend that someone
 * is at a shop.
 *
 * A ping is only sent when the user is within [NEAR_SHOP_RADIUS_M] of a shop, so the app never reports
 * where people are unless they're actually at a food spot. The location is fetched fresh for every check
 * (people move), and nearby shops are cached and only refetched when stale or after moving a fair distance.
 *
 * Run [runWhileForeground] from a coroutine tied to the app being visible (see MainActivity).
 */
class CrowdPingManager(context: Context) {

    private companion object {
        const val TAG = "CrowdPingManager"

        // Backend keeps a ping for 5 minutes; rate limit allows one per ~30s
        const val PING_INTERVAL_MS = 45_000L

        // Phone GPS is often 10-30 m off, so 20 m missed people standing at the shop
        const val NEAR_SHOP_RADIUS_M = 50f

        // Nearby-shop cache: refetch after 10 minutes or after moving 500 m
        const val SHOP_SEARCH_RADIUS_M = 1_000.0
        const val SHOP_CACHE_MAX_AGE_MS = 10 * 60_000L
        const val SHOP_CACHE_MAX_DRIFT_M = 500f
    }

    private val locationHelper = LocationHelper(context.applicationContext)
    private val apiService = NetworkModule.apiService

    private var cachedShops: List<Location> = emptyList()
    private var cacheCenter: Location? = null
    private var cacheTimeMs = 0L

    suspend fun runWhileForeground() {
        while (currentCoroutineContext().isActive) {
            try {
                pingIfAtAShop()
            } catch (e: Exception) {
                Log.w(TAG, "Crowd ping check failed", e)
            }
            delay(PING_INTERVAL_MS)
        }
    }

    private suspend fun pingIfAtAShop() {
        // The backend only accepts pings from signed-in users (anonymous Firebase accounts count)
        if (FirebaseAuth.getInstance().currentUser == null) return

        val point = locationHelper.getCurrentLocation() ?: return
        val here = Location("crowd").apply {
            latitude = point.latitude
            longitude = point.longitude
        }

        val atAShop = shopsAround(here).any { it.distanceTo(here) <= NEAR_SHOP_RADIUS_M }
        if (!atAShop) return

        val response = apiService.crowdPing(CrowdPingRequest(latitude = here.latitude, longitude = here.longitude))
        if (!response.isSuccessful) {
            Log.w(TAG, "Crowd ping rejected: ${response.code()}")
        }
    }

    private suspend fun shopsAround(here: Location): List<Location> {
        val center = cacheCenter
        val stale = center == null
                || SystemClock.elapsedRealtime() - cacheTimeMs > SHOP_CACHE_MAX_AGE_MS
                || center.distanceTo(here) > SHOP_CACHE_MAX_DRIFT_M
        if (stale) {
            val response = apiService.getNearbyShops(here.latitude, here.longitude, SHOP_SEARCH_RADIUS_M)
            if (response.isSuccessful) {
                cachedShops = response.body().orEmpty().map { shop ->
                    Location("shop").apply {
                        latitude = shop.latitude
                        longitude = shop.longitude
                    }
                }
                cacheCenter = here
                cacheTimeMs = SystemClock.elapsedRealtime()
            }
        }
        return cachedShops
    }
}
