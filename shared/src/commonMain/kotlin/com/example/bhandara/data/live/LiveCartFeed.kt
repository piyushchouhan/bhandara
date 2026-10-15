package com.example.bhandara.data.live

import com.example.bhandara.data.models.api.CartLocationUpdate
import com.example.bhandara.data.network.platformHttpEngine
import io.ktor.client.HttpClient
import io.ktor.client.plugins.websocket.WebSockets
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import kotlinx.serialization.json.Json
import org.hildan.krossbow.stomp.StompClient
import org.hildan.krossbow.stomp.subscribeText
import org.hildan.krossbow.websocket.ktor.KtorWebSocketClient

/**
 * Where a moving cart is, live, the same on Android and iOS. Keeps listening (reconnecting with a growing pause
 * when the connection drops) until the collector stops.
 *
 * @param subscribe opens one subscription to a topic: each element is one message's text; it ends or throws when
 * the connection drops
 */
class LiveCartFeed(
    private val subscribe: suspend (topic: String) -> Flow<String>,
    private val pauseMillis: (failures: Int) -> Long = ::reconnectPauseMillis,
) {
    private val json = Json { ignoreUnknownKeys = true }

    /** The cart's positions as they come; [onConnected] says whether it's listening right now */
    fun positions(shopId: Long, onConnected: (Boolean) -> Unit = {}): Flow<CartLocationUpdate> = flow {
        var failures = 0
        while (currentCoroutineContext().isActive) {
            try {
                val messages = subscribe("/topic/cart/$shopId")
                onConnected(true)
                failures = 0
                messages.collect { text ->
                    // A message the app can't read is skipped, not a reason to drop the connection
                    runCatching { json.decodeFromString(CartLocationUpdate.serializer(), text) }.getOrNull()?.let { emit(it) }
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                // Dropped; try again after a pause
            }
            onConnected(false)
            failures++
            delay(pauseMillis(failures))
        }
    }

    companion object {
        /**
         * The real connection: STOMP over a WebSocket to the backend at [apiBaseUrl] (http becomes ws), signed in
         * with [idToken] when there is one. Each subscription has its own connection, closed when it ends.
         */
        fun connectingTo(apiBaseUrl: String, idToken: suspend () -> String?): LiveCartFeed {
            val url = cartSocketUrl(apiBaseUrl)
            // Frequent pings notice a connection that died quietly (e.g. switching mobile networks)
            val stomp = cartStompClient(pingIntervalMillis = 20_000)
            return LiveCartFeed(subscribe = { topic ->
                val session = stomp.connect(url, customStompConnectHeaders = signedInHeaders(idToken()))
                session.subscribeText(topic).onCompletion {
                    withContext(NonCancellable) { runCatching { withTimeoutOrNull(2_000) { session.disconnect() } } }
                }
            })
        }
    }
}

/** 1 s, 2 s, 4 s … up to 30 s */
fun reconnectPauseMillis(failures: Int): Long = (1_000L shl (failures - 1).coerceIn(0, 5)).coerceAtMost(30_000L)

/** The backend's STOMP endpoint for live carts, from its address (http becomes ws) */
internal fun cartSocketUrl(apiBaseUrl: String): String =
    apiBaseUrl.replaceFirst("https://", "wss://").replaceFirst("http://", "ws://").trimEnd('/') + "/ws/websocket"

/** A STOMP client over a Ktor WebSocket, pinging this often to notice dead connections */
internal fun cartStompClient(pingIntervalMillis: Long): StompClient =
    StompClient(KtorWebSocketClient(HttpClient(platformHttpEngine()) { install(WebSockets) { this.pingIntervalMillis = pingIntervalMillis } }))

/** The backend only takes a cart's location from a connection that carries its owner's login */
internal fun signedInHeaders(idToken: String?): Map<String, String> =
    idToken?.let { mapOf("Authorization" to "Bearer $it") } ?: emptyMap()

