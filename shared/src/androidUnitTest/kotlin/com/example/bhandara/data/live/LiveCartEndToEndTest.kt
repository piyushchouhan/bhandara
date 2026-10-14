package com.example.bhandara.data.live

import com.example.bhandara.data.models.api.CartLocationUpdate
import com.example.bhandara.data.models.api.LocalShopRequest
import com.example.bhandara.data.network.ApiClient
import com.example.bhandara.data.network.ApiResult
import com.example.bhandara.data.network.ShopApi
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.websocket.WebSockets
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import kotlinx.serialization.json.Json
import org.hildan.krossbow.stomp.StompClient
import org.hildan.krossbow.stomp.sendText
import org.hildan.krossbow.websocket.ktor.KtorWebSocketClient
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Live cart tracking end to end, against a real running backend: a vendor sends the cart's location the way Vendor
 * Mode does, and a customer follows it with the app's own [LiveCartFeed].
 *
 * Skipped unless these are set (it needs a backend and a signed-in vendor):
 * - LIVE_CART_E2E_URL: the backend, e.g. http://localhost:8080/
 * - LIVE_CART_E2E_TOKEN / LIVE_CART_E2E_UID: a Firebase ID token and its uid (the vendor)
 */
class LiveCartEndToEndTest {

    @Test
    fun aCustomerFollowingACartSeesWhereTheVendorIs() = runBlocking {
        val url = System.getenv("LIVE_CART_E2E_URL") ?: return@runBlocking
        val token = System.getenv("LIVE_CART_E2E_TOKEN")!!
        val uid = System.getenv("LIVE_CART_E2E_UID")!!

        // Given: the vendor's cart (Vishrantwadi, Pune)
        val created = ShopApi(ApiClient(url, { token })).createShop(
            LocalShopRequest(
                ownerUid = uid, shopName = "E2E live cart", shopType = "Street Food", menuItems = listOf("Chai"),
                latitude = 18.5823, longitude = 73.8845, isMovingCart = true,
            )
        )
        assertTrue(created is ApiResult.Success, "Couldn't add the cart: $created")
        val shopId = created.value.id.toLong()

        // When: a customer follows it, and the vendor sends where the cart is (again each second, since the
        // customer's subscription takes a moment to open)
        val following = async { withTimeout(30_000) { LiveCartFeed.connectingTo(url) { null }.positions(shopId).first() } }
        val wsUrl = url.replaceFirst("http://", "ws://").replaceFirst("https://", "wss://").trimEnd('/') + "/ws/websocket"
        val vendor = StompClient(KtorWebSocketClient(HttpClient(OkHttp) { install(WebSockets) }))
            .connect(wsUrl, customStompConnectHeaders = mapOf("Authorization" to "Bearer $token"))
        val sending = launch {
            while (isActive) {
                vendor.sendText("/app/cart/location", Json.encodeToString(CartLocationUpdate.serializer(), CartLocationUpdate(shopId, uid, 18.5830, 73.8851, 2.5)))
                delay(1_000)
            }
        }

        // Then: the customer sees the cart where the vendor is
        val seen = following.await()
        sending.cancel()
        vendor.sendText("/app/cart/offline", Json.encodeToString(CartLocationUpdate.serializer(), CartLocationUpdate(shopId, uid, 18.5830, 73.8851)))
        vendor.disconnect()
        assertEquals(shopId, seen.shopId)
        assertEquals(18.5830, seen.lat)
        assertEquals(73.8851, seen.lng)
        assertEquals(2.5, seen.speed)
        println("LIVE CART E2E: shop $shopId seen at ${seen.lat}, ${seen.lng} (speed ${seen.speed}, owner ${seen.ownerUid})")
    }
}
