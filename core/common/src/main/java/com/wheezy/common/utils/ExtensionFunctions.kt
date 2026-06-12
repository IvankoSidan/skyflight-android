package com.wheezy.skyflight.core.common.utils

import androidx.compose.ui.graphics.Color
import com.wheezy.skyflight.core.model.BookingStatus

fun BookingStatus.statusColor(): Color = when (this) {
    BookingStatus.PENDING_PAYMENT -> Color(0xFFFFC107)
    BookingStatus.CONFIRMED -> Color(0xFF4CAF50)
    BookingStatus.FAILED -> Color(0xFFF44336)
    BookingStatus.PAID -> Color(0xFF2196F3)
    BookingStatus.CANCELED -> Color(0xFF9E9E9E)
    BookingStatus.UNPAID -> Color(0xFFFFC107)
}