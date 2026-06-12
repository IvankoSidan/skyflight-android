package com.wheezy.skyflight.core.common.utils

import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

data class NotificationEvent(val message: String, val isRead: Boolean)

object NotificationEventBus {
    private val _notificationEvents = MutableSharedFlow<NotificationEvent>(
        replay = 0,
        extraBufferCapacity = 64,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    val notificationEvents: SharedFlow<NotificationEvent> = _notificationEvents.asSharedFlow()

    suspend fun sendNotificationEvent(message: String, isRead: Boolean = false) {
        _notificationEvents.emit(NotificationEvent(message, isRead))
    }
}