package com.wheezy.skyflight.core.ui.viewmodel

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wheezy.skyflight.core.model.Notification
import com.wheezy.skyflight.core.common.utils.NotificationEventBus
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import javax.inject.Inject

@HiltViewModel
class TopBarViewModel @Inject constructor() : ViewModel() {

    private val _notifications = MutableStateFlow<List<Notification>>(emptyList())
    val notifications: StateFlow<List<Notification>> = _notifications.asStateFlow()

    private val _unreadCount = MutableStateFlow(0)
    val unreadCount: StateFlow<Int> = _unreadCount.asStateFlow()

    private var broadcastReceiver: BroadcastReceiver? = null

    init {
        observeEventBus()
    }

    private fun observeEventBus() {
        viewModelScope.launch {
            NotificationEventBus.notificationEvents.collect { event ->
                addNotification(event.message, event.isRead)
            }
        }
    }

    fun registerReceiver(context: Context) {
        if (broadcastReceiver != null) return

        broadcastReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                updateUnreadCount()
            }
        }

        val intentFilter = IntentFilter("UPDATE_NOTIFICATIONS_COUNT")

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            context.registerReceiver(
                broadcastReceiver,
                intentFilter,
                Context.RECEIVER_NOT_EXPORTED
            )
        } else {
            context.registerReceiver(broadcastReceiver, intentFilter)
        }
    }

    fun unregisterReceiver(context: Context) {
        broadcastReceiver?.let {
            try {
                context.unregisterReceiver(it)
            } catch (e: Exception) {
                // Receiver already unregistered
            }
            broadcastReceiver = null
        }
    }

    private fun addNotification(message: String, isRead: Boolean = false) {
        val newNotification = Notification(
            message = message,
            timestamp = LocalDateTime.now(),
            isRead = isRead
        )
        _notifications.value = listOf(newNotification) + _notifications.value
        updateUnreadCount()
    }

    private fun updateUnreadCount() {
        _unreadCount.value = _notifications.value.count { !it.isRead }
    }

    fun clearAll() {
        _notifications.value = emptyList()
        updateUnreadCount()
    }
}