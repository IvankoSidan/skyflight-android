package com.wheezy.skyflight.core.common.manager

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationSubscriptionManager @Inject constructor(
    private val webSocketManager: WebSocketManager
) {
    private var isSubscribedToNotifications = false
    private var isSubscribedToBookings = false

    fun subscribeToNotifications() {
        if (!isSubscribedToNotifications) {
            isSubscribedToNotifications = true
            CoroutineScope(Dispatchers.IO).launch {
                webSocketManager.subscribeToNotifications()
            }
        }
    }

    fun subscribeToBookingUpdates() {
        if (!isSubscribedToBookings) {
            isSubscribedToBookings = true
            CoroutineScope(Dispatchers.IO).launch {
                webSocketManager.subscribeToBookingUpdates()
            }
        }
    }

    fun unsubscribeFromAll() {
        isSubscribedToNotifications = false
        isSubscribedToBookings = false
    }
}