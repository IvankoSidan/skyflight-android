package com.wheezy.skyflight.core.network.model

data class NotificationDTO(
    val message: String,
    val timestamp: String,
    val isRead: Boolean
)