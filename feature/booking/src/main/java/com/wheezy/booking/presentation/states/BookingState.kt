package com.wheezy.skyflight.feature.booking.presentation.states

import com.wheezy.skyflight.core.network.model.BookingDetailsDTO

sealed class BookingListState {
    object Loading : BookingListState()
    data class Success(val bookings: List<BookingDetailsDTO>) : BookingListState()
    data class Error(val message: String) : BookingListState()
}

sealed class PaymentState {
    object Idle : PaymentState()
    object Loading : PaymentState()
    data class Prepared(val clientSecret: String, val customerId: String, val ephemeralKey: String) : PaymentState()
    data class Error(val message: String) : PaymentState()
}