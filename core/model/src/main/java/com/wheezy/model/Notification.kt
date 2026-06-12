package com.wheezy.skyflight.core.model

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

data class Notification(
    val id: Long = System.currentTimeMillis(),
    val message: String,
    val timestamp: LocalDateTime = LocalDateTime.now(),
    var isRead: Boolean = false
)