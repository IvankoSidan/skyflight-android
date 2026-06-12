package com.wheezy.skyflight.core.common.manager

import kotlinx.coroutines.flow.StateFlow

interface WebSocketManager {
    val connectionState: StateFlow<ConnectionState>
    val lastMessage: StateFlow<String?>

    fun connect()
    fun disconnect()
    fun sendMessage(destination: String, payload: Any)
    fun subscribeToNotifications()
    fun subscribeToBookingUpdates()

    enum class ConnectionState {
        CONNECTING, CONNECTED, DISCONNECTED, ERROR
    }
}