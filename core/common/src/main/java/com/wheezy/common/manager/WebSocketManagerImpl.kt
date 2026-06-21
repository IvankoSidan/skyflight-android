package com.wheezy.skyflight.core.common.manager

import android.util.Log
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.wheezy.skyflight.core.common.event.BookingUpdateEventBus
import com.wheezy.skyflight.core.common.event.PaymentUpdateEventBus
import com.wheezy.skyflight.core.common.network.BatchRequestManager
import com.wheezy.skyflight.core.common.utils.DelayConstants
import com.wheezy.skyflight.core.common.utils.NotificationEventBus
import com.wheezy.skyflight.core.datastore.preferences.WebSocketPreferences
import com.wheezy.skyflight.core.network.config.NetworkConfig
import com.wheezy.skyflight.core.network.manager.TokenManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
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
    private val tokenManager: TokenManager,
    private val fcmTokenManager: FCMTokenManagerImpl,
    private val webSocketPreferences: WebSocketPreferences
) : WebSocketManager {

    companion object {
        private const val TAG = "WebSocketManager"
    }

    private var webSocket: WebSocket? = null
    private var isManualDisconnect: Boolean = false
    private var heartbeatJob: Job? = null
    private var reconnectJob: Job? = null
    private val isConnecting: AtomicBoolean = AtomicBoolean(false)
    private var subscriptionCount: Int = 0
    private var reconnectAttempts: Int = 0
    private val maxReconnectAttempts: Int = 10

    private val client: OkHttpClient = OkHttpClient.Builder()
        .pingInterval(30, TimeUnit.SECONDS)
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .addInterceptor(HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.NONE
        })
        .build()

    private val _connectionState: MutableStateFlow<WebSocketManager.ConnectionState> = MutableStateFlow(WebSocketManager.ConnectionState.DISCONNECTED)
    override val connectionState: StateFlow<WebSocketManager.ConnectionState> = _connectionState

    private val _lastMessage: MutableStateFlow<String?> = MutableStateFlow<String?>(null)
    override val lastMessage: StateFlow<String?> = _lastMessage

    private val wsUrl: String = NetworkConfig.WS_BASE_URL

    init {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                fcmTokenManager.observeToken()

                batchRequestManager.batchEvents.collect {
                    Log.d(TAG, "Batch event received")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error in init", e)
            }
        }

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val autoConnect: Boolean = webSocketPreferences.autoConnect.first()
                if (autoConnect) {
                    connect()
                }

                val reconnectEnabled: Boolean = webSocketPreferences.reconnectEnabled.first()
                if (!reconnectEnabled) {
                    Log.d(TAG, "Reconnect disabled by user preference")
                }

                networkMonitor.isConnected.collect { isConnected ->
                    try {
                        if (isConnected && !isManualDisconnect && _connectionState.value != WebSocketManager.ConnectionState.CONNECTED) {
                            delay(DelayConstants.LONG_DELAY.toMillis())
                            if (webSocketPreferences.reconnectEnabled.first()) {
                                connectWithBackoff()
                            }
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Error in network monitor", e)
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error initializing WebSocketManager", e)
            }
        }
    }

    private suspend fun connectWithBackoff() {
        try {
            val delayDuration: java.time.Duration = when {
                reconnectAttempts < 3 -> DelayConstants.LONG_DELAY
                reconnectAttempts < 6 -> DelayConstants.VERY_LONG_DELAY
                else -> DelayConstants.EXTRA_LONG_DELAY
            }

            if (reconnectAttempts > 0) {
                delay(delayDuration.toMillis())
            }

            if (reconnectAttempts < maxReconnectAttempts) {
                reconnectAttempts++
                connect()
            } else {
                reconnectAttempts = 0
                Log.e(TAG, "Max reconnect attempts reached, giving up")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error in connectWithBackoff", e)
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

        val token: String? = tokenManager.getToken()
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
            try {
                val request: Request = Request.Builder()
                    .url(wsUrl)
                    .addHeader("Authorization", "Bearer $token")
                    .build()

                webSocket = client.newWebSocket(request, createWebSocketListener())
            } catch (e: Exception) {
                Log.e(TAG, "Error creating WebSocket", e)
                isConnecting.set(false)
                _connectionState.value = WebSocketManager.ConnectionState.DISCONNECTED
            }
        }
    }

    override fun subscribeToNotifications() {
        try {
            subscriptionCount++
            CoroutineScope(Dispatchers.IO).launch {
                webSocketPreferences.setSubscribed(true)
            }
            Log.d(TAG, "subscribeToNotifications, total: $subscriptionCount")
            if (subscriptionCount == 1) {
                sendMessage("/app/notifications/subscribe", emptyMap<String, String>())
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error subscribing to notifications", e)
        }
    }

    override fun subscribeToBookingUpdates() {
        try {
            subscriptionCount++
            CoroutineScope(Dispatchers.IO).launch {
                webSocketPreferences.setSubscribed(true)
            }
            Log.d(TAG, "subscribeToBookingUpdates, total: $subscriptionCount")
            if (subscriptionCount == 1) {
                sendMessage("/app/bookings/subscribe", emptyMap<String, String>())
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error subscribing to booking updates", e)
        }
    }

    @Synchronized
    override fun disconnect() {
        try {
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

            batchRequestManager.cancelAll()

            CoroutineScope(Dispatchers.IO).launch {
                webSocketPreferences.setSubscribed(false)
                webSocketPreferences.setReconnectEnabled(false)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error disconnecting", e)
        }
    }

    override fun sendMessage(destination: String, payload: Any) {
        try {
            if (_connectionState.value != WebSocketManager.ConnectionState.CONNECTED) {
                CoroutineScope(Dispatchers.IO).launch {
                    batchRequestManager.addRequest(
                        id = destination,
                        execute = {
                            webSocket?.send(gson.toJson(mapOf("destination" to destination, "payload" to payload)))
                            true
                        },
                        onResult = { },
                        onError = { error: Throwable -> Log.e(TAG, "Failed to send message to $destination", error) }
                    )
                }
                return
            }
            val message: Map<String, Any> = mapOf("destination" to destination, "payload" to payload)
            webSocket?.send(gson.toJson(message))
        } catch (e: Exception) {
            Log.e(TAG, "Error sending message to $destination", e)
        }
    }

    private fun startHeartbeat() {
        stopHeartbeat()
        heartbeatJob = CoroutineScope(Dispatchers.IO).launch {
            while (_connectionState.value == WebSocketManager.ConnectionState.CONNECTED && !isManualDisconnect) {
                try {
                    delay(DelayConstants.HEARTBEAT_INTERVAL.toMillis())
                    webSocket?.send("\n")
                } catch (e: Exception) {
                    Log.e(TAG, "Heartbeat failed", e)
                }
            }
        }
    }

    private fun stopHeartbeat() {
        try {
            heartbeatJob?.cancel()
            heartbeatJob = null
        } catch (e: Exception) {
            Log.e(TAG, "Error stopping heartbeat", e)
        }
    }

    private fun createWebSocketListener(): WebSocketListener = object : WebSocketListener() {
        override fun onOpen(webSocket: WebSocket, response: Response) {
            try {
                Log.d(TAG, "WebSocket opened")
                isConnecting.set(false)
                reconnectAttempts = 0

                val connectFrame: String = "CONNECT\naccept-version:1.2\nheart-beat:10000,10000\n\n\u0000\n"
                webSocket.send(connectFrame)

                _connectionState.value = WebSocketManager.ConnectionState.CONNECTED
                startHeartbeat()

                CoroutineScope(Dispatchers.IO).launch {
                    webSocketPreferences.setReconnectEnabled(true)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error in onOpen", e)
            }
        }

        override fun onMessage(webSocket: WebSocket, text: String) {
            try {
                _lastMessage.value = text
                handleMessage(text)
            } catch (e: Exception) {
                Log.e(TAG, "Error handling message", e)
            }
        }

        override fun onMessage(webSocket: WebSocket, bytes: ByteString) {
            Log.d(TAG, "Received binary: ${bytes.hex()}")
        }

        override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
            try {
                Log.d(TAG, "Closing: $reason")
                _connectionState.value = WebSocketManager.ConnectionState.DISCONNECTED
                stopHeartbeat()
                isConnecting.set(false)
            } catch (e: Exception) {
                Log.e(TAG, "Error in onClosing", e)
            }
        }

        override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
            try {
                Log.d(TAG, "Closed: $reason")
                _connectionState.value = WebSocketManager.ConnectionState.DISCONNECTED
                stopHeartbeat()
                isConnecting.set(false)
                attemptReconnectWithDelay(DelayConstants.RECONNECT_DELAY)
            } catch (e: Exception) {
                Log.e(TAG, "Error in onClosed", e)
            }
        }

        override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
            try {
                Log.e(TAG, "WebSocket error: ${t.message}", t)
                _connectionState.value = WebSocketManager.ConnectionState.ERROR
                stopHeartbeat()
                isConnecting.set(false)
                attemptReconnectWithDelay(DelayConstants.RECONNECT_DELAY)
            } catch (e: Exception) {
                Log.e(TAG, "Error in onFailure", e)
            }
        }

        private fun attemptReconnectWithDelay(delay: java.time.Duration) {
            if (!isManualDisconnect && !isConnecting.get()) {
                try {
                    reconnectJob?.cancel()
                    reconnectJob = CoroutineScope(Dispatchers.IO).launch {
                        delay(delay.toMillis())
                        Log.d(TAG, "Attempting reconnect...")
                        connectWithBackoff()
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error attempting reconnect", e)
                }
            }
        }
    }

    private fun handleMessage(text: String) {
        try {
            val json: JsonObject = gson.fromJson(text, JsonObject::class.java)
            val type: String? = json.get("type")?.asString

            when (type) {
                "notification" -> {
                    val data: JsonObject = json.getAsJsonObject("data")
                    val message: String? = data.get("message")?.asString
                    if (message == null) return
                    val isRead: Boolean = data.get("isRead")?.asBoolean ?: false
                    CoroutineScope(Dispatchers.Main).launch {
                        NotificationEventBus.sendNotificationEvent(message, isRead)
                    }
                }
                "booking_update" -> {
                    val data: JsonObject = json.getAsJsonObject("data")
                    val bookingId: Long? = data.get("bookingId")?.asLong
                    if (bookingId == null) return
                    val status: String? = data.get("status")?.asString
                    if (status == null) return
                    CoroutineScope(Dispatchers.Main).launch {
                        BookingUpdateEventBus.sendEvent(bookingId, status)
                    }
                }
                "payment_update" -> {
                    val data: JsonObject = json.getAsJsonObject("data")
                    val bookingId: Long? = data.get("bookingId")?.asLong
                    if (bookingId == null) return
                    val paymentStatus: String? = data.get("paymentStatus")?.asString
                    if (paymentStatus == null) return
                    CoroutineScope(Dispatchers.Main).launch {
                        PaymentUpdateEventBus.sendEvent(bookingId, paymentStatus)
                    }
                }
                "connected", "pong" -> { }
                else -> {
                    Log.d(TAG, "Unknown message type: $type")
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing message", e)
        }
    }
}