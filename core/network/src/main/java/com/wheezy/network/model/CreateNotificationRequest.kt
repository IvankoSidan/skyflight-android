package com.wheezy.skyflight.core.network.model

data class CreateNotificationRequest(
    val message: String,
    val isRead: Boolean? = false
)