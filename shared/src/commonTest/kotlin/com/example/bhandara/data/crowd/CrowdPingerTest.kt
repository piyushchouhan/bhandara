package com.example.bhandara.data.crowd

import com.example.bhandara.data.location.DeviceLocation
import com.example.bhandara.data.location.GeoLocation
import com.example.bhandara.data.location.LocationService
import com.example.bhandara.data.location.TimedLocation
import com.example.bhandara.data.network.ApiClient
import com.example.bhandara.data.network.CrowdApi
import com.example.bhandara.data.network.ShopApi
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.MockEngineConfig
import io.ktor.client.engine.mock.respond
import io.ktor.client.request.HttpRequestData
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.content.TextContent
import io.ktor.http.headersOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * "How busy is it" pings, the same on Android and iOS: only from someone standing at a shop, never without location
 * access or a sign-in, and without fetching the nearby shops every time. The backend and the phone's GPS are fakes.
 */
class CrowdPingerTest {

    private val dispatcher = StandardTestDispatcher()

    // ==================== Fakes ====================

    private val shop = GeoLocation(18.5823, 73.8845)
    // 0.0003° of latitude is about 33 m; 0.0011° about 120 m; 0.0055° about 610 m
    private val atTheShop = GeoLocation(18.5826, 73.8845)
    private val downTheRoad = GeoLocation(18.5834, 73.8845)
    private val acrossTown = GeoLocation(18.5878, 73.8845)

    private var here: GeoLocation? = atTheShop
    private var permitted = true
    private var signedIn = true
    private var now = 0L

    private val requests = mutableListOf<HttpRequestData>()

    private val client = ApiClient("https://api.test/", { "id-token" }, MockEngine(MockEngineConfig().apply {
        dispatcher = this@CrowdPingerTest.dispatcher
        addHandler { request ->
            requests += request
            val body = when (request.url.encodedPath) {
                "/api/localshops/nearby" -> """[{"id":"1","shopName":"Sharma Chaat","latitude":${shop.latitude},"longitude":${shop.longitude}}]"""
                else -> """{"level":"QUIET"}"""
            }
            respond(body, HttpStatusCode.OK, headersOf(HttpHeaders.ContentType, "application/json"))
        }
    }))

    private val phone = object : DeviceLocation {
        override fun hasPermission() = permitted
        override suspend fun current() = here
        override suspend fun lastKnown(): TimedLocation? = null
    }

    private val pinger = CrowdPinger(LocationService(phone), ShopApi(client), CrowdApi(client), isSignedIn = { signedIn }, nowMillis = { now })

    private fun pings() = requests.filter { it.url.encodedPath == "/api/crowd/ping" }
    private fun shopLookups() = requests.filter { it.url.encodedPath == "/api/localshops/nearby" }

    // ==================== Tests ====================

    @Test
    fun someoneStandingAtAShopIsCounted() = runTest(dispatcher) {
        assertTrue(pinger.pingIfAtAShop())

        val ping = pings().single()
        assertTrue((ping.body as TextContent).text.contains("18.5826"))
    }

    @Test
    fun someoneDownTheRoadIsNot() = runTest(dispatcher) {
        here = downTheRoad

        assertFalse(pinger.pingIfAtAShop())

        assertTrue(pings().isEmpty())
    }

    @Test
    fun withoutLocationAccessOrASignInNothingIsAskedOrSent() = runTest(dispatcher) {
        permitted = false
        assertFalse(pinger.pingIfAtAShop())

        permitted = true
        signedIn = false
        assertFalse(pinger.pingIfAtAShop())

        assertTrue(requests.isEmpty())
    }

    @Test
    fun theNearbyShopsAreFetchedAgainOnlyWhenOldOrFarAway() = runTest(dispatcher) {
        pinger.pingIfAtAShop()
        now += 5 * 60_000
        pinger.pingIfAtAShop()
        assertEquals(1, shopLookups().size, "same spot, 5 minutes later")

        here = acrossTown
        pinger.pingIfAtAShop()
        assertEquals(2, shopLookups().size, "moved about 600 m")

        now += 11 * 60_000
        pinger.pingIfAtAShop()
        assertEquals(3, shopLookups().size, "11 minutes later")
    }
}
