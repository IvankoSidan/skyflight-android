package com.wheezy.skyflight.core.common.event

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object BookingUpdateEventBus {
    private val _events = MutableStateFlow<BookingUpdateEvent?>(null)
    val events: StateFlow<BookingUpdateEvent?> = _events.asStateFlow()

    suspend fun sendEvent(bookingId: Long, status: String) {
        _events.emit(BookingUpdateEvent(bookingId, status))
    }
}

data class BookingUpdateEvent(
    val bookingId: Long,
    val status: String
)

object PaymentUpdateEventBus {
    private val _events = MutableStateFlow<PaymentUpdateEvent?>(null)
    val events: StateFlow<PaymentUpdateEvent?> = _events.asStateFlow()

    suspend fun sendEvent(bookingId: Long, paymentStatus: String) {
        _events.emit(PaymentUpdateEvent(bookingId, paymentStatus))
    }
}

data class PaymentUpdateEvent(
    val bookingId: Long,
    val paymentStatus: String
)