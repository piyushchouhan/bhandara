package com.example.bhandara.vendor

import com.example.bhandara.data.live.cartSocketUrl
import com.example.bhandara.data.live.cartStompClient
import com.example.bhandara.data.live.signedInHeaders
import com.example.bhandara.data.models.api.CartLocationUpdate
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import kotlinx.serialization.json.Json
import org.hildan.krossbow.stomp.StompClient
import org.hildan.krossbow.stomp.StompSession
import org.hildan.krossbow.stomp.sendText

/**
 * Sends the vendor's cart location over one STOMP connection, signed in as the owner (the backend refuses other
 * connections). A connection that died is replaced and the update sent once more; after a failed connect, further
 * tries wait (1 s, 2 s, 4 s … up to 30 s) so a bad network isn't hammered.
 *
 * @param connect opens a session; the real one is [StompCartSender.connectingTo]
 */
class StompCartSender(
    private val connect: suspend () -> StompSession,
    private val nowMillis: () -> Long,
) : CartLocationSender {

    private val json = Json { encodeDefaults = true }
    private val lock = Mutex()
    private var session: StompSession? = null
    private var failures = 0
    private var nextTryAt = 0L

    override suspend fun send(update: CartLocationUpdate) = sendText(LOCATION, update)

    override suspend fun sendOffline(update: CartLocationUpdate) = sendText(OFFLINE, update)

    override suspend fun close() {
        val open = lock.withLock { session.also { session = null } }
        open?.let { quietlyDisconnect(it) }
    }

    private suspend fun sendText(destination: String, update: CartLocationUpdate) {
        val text = json.encodeToString(CartLocationUpdate.serializer(), update)
        val first = session()
        try {
            first.sendText(destination, text)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            // The connection died (e.g. the phone switched networks): replace it and send once more
            drop(first)
            session().sendText(destination, text)
        }
    }

    private suspend fun session(): StompSession = lock.withLock {
        session?.let { return it }
        val wait = nextTryAt - nowMillis()
        check(wait <= 0) { "Reconnecting in $wait ms" }
        try {
            connect().also {
                session = it
                failures = 0
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            failures++
            nextTryAt = nowMillis() + (1_000L shl (failures - 1).coerceIn(0, 5)).coerceAtMost(30_000L)
            throw e
        }
    }

    private suspend fun drop(broken: StompSession) {
        lock.withLock { if (session === broken) session = null }
        quietlyDisconnect(broken)
    }

    private suspend fun quietlyDisconnect(open: StompSession) = withContext(NonCancellable) {
        runCatching { withTimeoutOrNull(2_000) { open.disconnect() } }
    }

    companion object {
        const val LOCATION = "/app/cart/location"
        const val OFFLINE = "/app/cart/offline"

        /**
         * The real connection to the backend at [apiBaseUrl], signed in with [idToken]. Pings rarely to save battery:
         * presence updates every 45 s notice a dead connection anyway.
         */
        fun connectingTo(apiBaseUrl: String, idToken: suspend () -> String?, nowMillis: () -> Long): StompCartSender {
            val stomp: StompClient = cartStompClient(pingIntervalMillis = 60_000)
            val url = cartSocketUrl(apiBaseUrl)
            return StompCartSender({ stomp.connect(url, customStompConnectHeaders = signedInHeaders(idToken())) }, nowMillis)
        }
    }
}
