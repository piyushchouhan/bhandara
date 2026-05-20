package com.example.bhandara.data.api

import android.util.Log
import com.example.bhandara.BuildConfig
import com.example.bhandara.data.models.api.CartLocationUpdate
import com.google.gson.Gson
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.hildan.krossbow.stomp.StompClient
import org.hildan.krossbow.stomp.StompSession
import org.hildan.krossbow.stomp.sendText
import org.hildan.krossbow.stomp.subscribeText
import org.hildan.krossbow.websocket.okhttp.OkHttpWebSocketClient
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit

object CartStompClient {

    private const val TAG = "CartStompClient"
    private val gson = Gson()

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(0, TimeUnit.MINUTES)
        .build()

    private val stompClient = StompClient(OkHttpWebSocketClient(okHttpClient))

    private var session: StompSession? = null

    private val wsUrl: String
        get() {
            val base = BuildConfig.API_BASE_URL
                .replace("http://", "ws://")
                .replace("https://", "wss://")
                .trimEnd('/')
            return "$base/ws/websocket"
        }

    suspend fun connect() {
        if (session != null) return
        try {
            session = stompClient.connect(wsUrl)
            Log.d(TAG, "STOMP connected to $wsUrl")
        } catch (e: Exception) {
            Log.e(TAG, "STOMP connection failed", e)
            session = null
            throw e
        }
    }

    suspend fun sendCartLocation(update: CartLocationUpdate) {
        val s = session ?: throw IllegalStateException("Not connected")
        val json = gson.toJson(update)
        s.sendText("/app/cart/location", json)
    }

    suspend fun subscribeToCart(shopId: Long): Flow<CartLocationUpdate> {
        val s = session ?: throw IllegalStateException("Not connected")
        return s.subscribeText("/topic/cart/$shopId").map { frame ->
            gson.fromJson(frame, CartLocationUpdate::class.java)
        }
    }

    suspend fun disconnect() {
        try {
            session?.disconnect()
        } catch (e: Exception) {
            Log.e(TAG, "STOMP disconnect error", e)
        } finally {
            session = null
        }
    }
}
