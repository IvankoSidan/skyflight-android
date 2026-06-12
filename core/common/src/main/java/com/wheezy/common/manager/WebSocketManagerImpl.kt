package com.wheezy.skyflight.core.common.manager

import android.util.Log
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.wheezy.skyflight.core.common.event.BookingUpdateEventBus
import com.wheezy.skyflight.core.common.event.PaymentUpdateEventBus
import com.wheezy.skyflight.core.common.network.BatchRequestManager
import com.wheezy.skyflight.core.common.utils.NotificationEventBus
import com.wheezy.skyflight.core.network.config.NetworkConfig
import com.wheezy.skyflight.core.network.manager.TokenManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import okhttp3.logging.HttpLoggingInterceptor
import okio.ByteString
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WebSocketManagerImpl @Inject constructor(
    private val gson: Gson,
    private val networkMonitor: NetworkMonitor,
    private val batchRequestManager: BatchRequestManager,
    private val tokenManager: TokenManager
) : WebSocketManager {

    companion object {
        private const val TAG = "WebSocketManager"
        private const val RECONNECT_DELAY = 5000L
        private const val HEARTBEAT_INTERVAL = 15000L
    }

    private var webSocket: WebSocket? = null
    private var isManualDisconnect = false
    private var heartbeatJob: Job? = null
    private var reconnectJob: Job? = null
    private val isConnecting = AtomicBoolean(false)
    private var subscriptionCount = 0
    private var reconnectAttempts = 0
    private val maxReconnectAttempts = 10

    private val client = OkHttpClient.Builder()
        .pingInterval(30, TimeUnit.SECONDS)
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .addInterceptor(HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.NONE
        })
        .build()

    private val _connectionState = MutableStateFlow(WebSocketManager.ConnectionState.DISCONNECTED)
    override val connectionState: StateFlow<WebSocketManager.ConnectionState> = _connectionState

    private val _lastMessage = MutableStateFlow<String?>(null)
    override val lastMessage: StateFlow<String?> = _lastMessage

    private val wsUrl = NetworkConfig.WS_BASE_URL

    init {
        CoroutineScope(Dispatchers.IO).launch {
            networkMonitor.isConnected.collect { isConnected ->
                if (isConnected && !isManualDisconnect && _connectionState.value != WebSocketManager.ConnectionState.CONNECTED) {
                    delay(1000)
                    connectWithBackoff()
                }
            }
        }
    }

    private suspend fun connectWithBackoff() {
        val delay = when {
            reconnectAttempts < 3 -> 1000L
            reconnectAttempts < 6 -> 5000L
            else -> 30000L
        }

        if (reconnectAttempts > 0) {
            delay(delay)
        }

        if (reconnectAttempts < maxReconnectAttempts) {
            reconnectAttempts++
            connect()
        } else {
            reconnectAttempts = 0
        }
    }

    @Synchronized
    override fun connect() {
        if (isConnecting.get()) {
            Log.d(TAG, "Already connecting, skipping")
            return
        }

        if (webSocket != null && _connectionState.value == WebSocketManager.ConnectionState.CONNECTED) {
            Log.d(TAG, "Already connected, skipping")
            return
        }

        val token = tokenManager.getToken()
        if (token.isNullOrEmpty()) {
            Log.e(TAG, "No auth token available, cannot connect WebSocket")
            _connectionState.value = WebSocketManager.ConnectionState.DISCONNECTED
            return
        }

        Log.d(TAG, "Starting connection... (attempt $reconnectAttempts)")
        isConnecting.set(true)
        isManualDisconnect = false
        _connectionState.value = WebSocketManager.ConnectionState.CONNECTING

        CoroutineScope(Dispatchers.IO).launch {
            val request = Request.Builder()
                .url(wsUrl)
                .addHeader("Authorization", "Bearer $token")
                .build()

            webSocket = client.newWebSocket(request, createWebSocketListener())
        }
    }

    override fun subscribeToNotifications() {
        subscriptionCount++
        Log.d(TAG, "subscribeToNotifications, total: $subscriptionCount")
        if (subscriptionCount == 1) {
            sendMessage("/app/notifications/subscribe", emptyMap<String, String>())
        }
    }

    override fun subscribeToBookingUpdates() {
        subscriptionCount++
        Log.d(TAG, "subscribeToBookingUpdates, total: $subscriptionCount")
        if (subscriptionCount == 1) {
            sendMessage("/app/bookings/subscribe", emptyMap<String, String>())
        }
    }

    @Synchronized
    override fun disconnect() {
        Log.d(TAG, "Manual disconnect")
        isManualDisconnect = true
        isConnecting.set(false)
        reconnectAttempts = 0
        stopHeartbeat()
        reconnectJob?.cancel()
        webSocket?.close(1000, "Manual disconnect")
        webSocket = null
        _connectionState.value = WebSocketManager.ConnectionState.DISCONNECTED
        subscriptionCount = 0
    }

    override fun sendMessage(destination: String, payload: Any) {
        if (_connectionState.value != WebSocketManager.ConnectionState.CONNECTED) {
            CoroutineScope(Dispatchers.IO).launch {
                batchRequestManager.addRequest(
                    id = destination,
                    execute = {
                        webSocket?.send(gson.toJson(mapOf("destination" to destination, "payload" to payload)))
                        true
                    },
                    onResult = { },
                    onError = { Log.e(TAG, "Failed to send message to $destination", it) }
                )
            }
            return
        }
        val message = mapOf("destination" to destination, "payload" to payload)
        webSocket?.send(gson.toJson(message))
    }

    private fun startHeartbeat() {
        stopHeartbeat()
        heartbeatJob = CoroutineScope(Dispatchers.IO).launch {
            while (_connectionState.value == WebSocketManager.ConnectionState.CONNECTED && !isManualDisconnect) {
                delay(HEARTBEAT_INTERVAL)
                try {
                    webSocket?.send("\n")
                } catch (e: Exception) {
                    Log.e(TAG, "Heartbeat failed", e)
                }
            }
        }
    }

    private fun stopHeartbeat() {
        heartbeatJob?.cancel()
        heartbeatJob = null
    }

    private fun createWebSocketListener() = object : WebSocketListener() {
        override fun onOpen(webSocket: WebSocket, response: Response) {
            Log.d(TAG, "WebSocket opened")
            isConnecting.set(false)
            reconnectAttempts = 0

            val connectFrame = "CONNECT\naccept-version:1.2\nheart-beat:10000,10000\n\n\u0000\n"
            webSocket.send(connectFrame)

            _connectionState.value = WebSocketManager.ConnectionState.CONNECTED
            startHeartbeat()
        }

        override fun onMessage(webSocket: WebSocket, text: String) {
            _lastMessage.value = text
            handleMessage(text)
        }

        override fun onMessage(webSocket: WebSocket, bytes: ByteString) {
            Log.d(TAG, "Received binary: ${bytes.hex()}")
        }

        override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
            Log.d(TAG, "Closing: $reason")
            _connectionState.value = WebSocketManager.ConnectionState.DISCONNECTED
            stopHeartbeat()
            isConnecting.set(false)
        }

        override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
            Log.d(TAG, "Closed: $reason")
            _connectionState.value = WebSocketManager.ConnectionState.DISCONNECTED
            stopHeartbeat()
            isConnecting.set(false)
            attemptReconnect()
        }

        override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
            Log.e(TAG, "WebSocket error: ${t.message}")
            _connectionState.value = WebSocketManager.ConnectionState.ERROR
            stopHeartbeat()
            isConnecting.set(false)
            attemptReconnect()
        }

        private fun attemptReconnect() {
            if (!isManualDisconnect && !isConnecting.get()) {
                reconnectJob?.cancel()
                reconnectJob = CoroutineScope(Dispatchers.IO).launch {
                    delay(RECONNECT_DELAY)
                    Log.d(TAG, "Attempting reconnect...")
                    connectWithBackoff()
                }
            }
        }
    }

    private fun handleMessage(text: String) {
        try {
            val json = gson.fromJson(text, JsonObject::class.java)
            val type = json.get("type")?.asString

            when (type) {
                "notification" -> {
                    val data = json.getAsJsonObject("data")
                    val message = data.get("message")?.asString ?: return
                    val isRead = data.get("isRead")?.asBoolean ?: false
                    CoroutineScope(Dispatchers.Main).launch {
                        NotificationEventBus.sendNotificationEvent(message, isRead)
                    }
                }
                "booking_update" -> {
                    val data = json.getAsJsonObject("data")
                    val bookingId = data.get("bookingId")?.asLong ?: return
                    val status = data.get("status")?.asString ?: return
                    CoroutineScope(Dispatchers.Main).launch {
                        BookingUpdateEventBus.sendEvent(bookingId, status)
                    }
                }
                "payment_update" -> {
                    val data = json.getAsJsonObject("data")
                    val bookingId = data.get("bookingId")?.asLong ?: return
                    val paymentStatus = data.get("paymentStatus")?.asString ?: return
                    CoroutineScope(Dispatchers.Main).launch {
                        PaymentUpdateEventBus.sendEvent(bookingId, paymentStatus)
                    }
                }
                "connected", "pong" -> { }
                else -> { }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing message", e)
        }
    }
}