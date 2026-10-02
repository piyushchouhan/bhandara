package com.example.bhandara.data.api

import android.util.Log
import com.example.bhandara.BuildConfig
import com.example.bhandara.data.models.api.CartLocationUpdate
import com.google.firebase.auth.FirebaseAuth
import com.google.gson.Gson
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withTimeoutOrNull
import org.hildan.krossbow.stomp.StompClient
import org.hildan.krossbow.stomp.StompSession
import org.hildan.krossbow.stomp.sendText
import org.hildan.krossbow.stomp.subscribeText
import org.hildan.krossbow.websocket.okhttp.OkHttpWebSocketClient
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit

/**
 * One STOMP connection for live cart locations, with automatic reconnects.
 *
 * Each user of a connection gets its own instance so that closing one never breaks another:
 * - [vendor] is the single long-lived connection used by the vendor's location stream
 * - [forViewer] creates a fresh connection for one customer screen; [close] it when the screen goes away
 */
class CartStompClient private constructor(
    private val name: String,
    pingIntervalSeconds: Long
) {

    companion object {
        private const val TAG = "CartStompClient"
        private const val MAX_BACKOFF_MS = 30_000L

        private val gson = Gson()

        private val baseHttpClient = OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(0, TimeUnit.MINUTES)
            .build()

        private val wsUrl: String
            get() {
                val base = BuildConfig.API_BASE_URL
                    .replace("http://", "ws://")
                    .replace("https://", "wss://")
                    .trimEnd('/')
                return "$base/ws/websocket"
            }

        // Outlives any screen, so connections can still be closed after the screen's own scope is cancelled
        private val cleanupScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

        /**
         * Used only by the vendor's location stream. It pings rarely to save battery: the vendor also sends a
         * presence update every 45s, which fails (and reconnects) on its own if the connection has died.
         */
        val vendor = CartStompClient("vendor", pingIntervalSeconds = 60)

        /** A separate connection for one customer screen; pings often so a dead connection is noticed quickly */
        fun forViewer(): CartStompClient = CartStompClient("viewer", pingIntervalSeconds = 20)

        /** 1s, 2s, 4s ... capped at 30s */
        private fun backoffMs(failures: Int): Long =
            (1_000L shl (failures - 1).coerceIn(0, 5)).coerceAtMost(MAX_BACKOFF_MS)
    }

    // WebSocket pings detect connections that died silently (e.g. switching mobile networks)
    private val stompClient = StompClient(
        OkHttpWebSocketClient(
            baseHttpClient.newBuilder().pingInterval(pingIntervalSeconds, TimeUnit.SECONDS).build()
        )
    )
    private val mutex = Mutex()
    private var session: StompSession? = null
    private var failures = 0
    private var nextAttemptAtMs = 0L

    /**
     * Returns the open session, connecting if needed. After a failed attempt, further attempts are
     * refused until the backoff delay has passed, so a flaky network isn't hammered.
     */
    suspend fun connect(): StompSession = mutex.withLock {
        session?.let { return it }

        val waitMs = nextAttemptAtMs - System.currentTimeMillis()
        if (waitMs > 0) {
            throw IllegalStateException("[$name] reconnecting in ${waitMs}ms")
        }

        try {
            // The backend only accepts location updates from connections that carry the owner's login token
            val idToken = FirebaseAuth.getInstance().currentUser?.getIdToken(false)?.await()?.token
            val headers = if (idToken != null) mapOf("Authorization" to "Bearer $idToken") else emptyMap()
            stompClient.connect(wsUrl, customStompConnectHeaders = headers).also {
                session = it
                failures = 0
                Log.d(TAG, "[$name] STOMP connected to $wsUrl")
            }
        } catch (e: Exception) {
            failures++
            nextAttemptAtMs = System.currentTimeMillis() + backoffMs(failures)
            Log.e(TAG, "[$name] STOMP connection failed (attempt $failures)", e)
            throw e
        }
    }

    /**
     * Sends the vendor's location. If the connection turns out to be dead, it is replaced and the
     * update is sent once more on the new connection.
     */
    suspend fun sendCartLocation(update: CartLocationUpdate) {
        val json = gson.toJson(update)
        val first = connect()
        try {
            first.sendText("/app/cart/location", json)
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            Log.w(TAG, "[$name] send failed, reconnecting", e)
            dropSession(first)
            connect().sendText("/app/cart/location", json)
        }
    }

    /** Tells the backend the vendor stopped sharing, so the cart leaves the map immediately */
    suspend fun sendCartOffline(update: CartLocationUpdate) {
        connect().sendText("/app/cart/offline", gson.toJson(update))
    }

    /**
     * Live updates for one cart. Keeps resubscribing (with backoff) whenever the connection drops,
     * until the collector is cancelled.
     */
    fun subscribeToCart(
        shopId: Long,
        onConnectionChange: (connected: Boolean) -> Unit = {}
    ): Flow<CartLocationUpdate> = flow {
        var attempts = 0
        while (currentCoroutineContext().isActive) {
            var current: StompSession? = null
            try {
                current = connect()
                val updates = current.subscribeText("/topic/cart/$shopId")
                attempts = 0
                onConnectionChange(true)
                updates.collect { frame ->
                    emit(gson.fromJson(frame, CartLocationUpdate::class.java))
                }
                Log.w(TAG, "[$name] subscription to cart $shopId ended, resubscribing")
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.w(TAG, "[$name] subscription to cart $shopId failed: ${e.message}")
            }
            onConnectionChange(false)
            current?.let { dropSession(it) }
            attempts++
            delay(backoffMs(attempts))
        }
    }

    suspend fun disconnect() {
        val s = mutex.withLock {
            session.also { session = null }
        }
        s?.let { quietlyDisconnect(it) }
    }

    /** Non-suspending [disconnect] for use from dispose callbacks */
    fun close() {
        cleanupScope.launch { disconnect() }
    }

    /** Forget a session that stopped working, so the next call reconnects */
    private suspend fun dropSession(broken: StompSession) {
        mutex.withLock {
            if (session === broken) session = null
        }
        cleanupScope.launch { quietlyDisconnect(broken) }
    }

    private suspend fun quietlyDisconnect(s: StompSession) {
        try {
            withTimeoutOrNull(2_000) { s.disconnect() }
        } catch (e: Exception) {
            Log.d(TAG, "[$name] STOMP disconnect error: ${e.message}")
        }
    }
}
