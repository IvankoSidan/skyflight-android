package com.wheezy.skyflight.feature.booking.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wheezy.skyflight.core.model.Booking
import com.wheezy.skyflight.core.model.FlightModel
import com.wheezy.skyflight.core.model.Seat
import com.wheezy.skyflight.core.ui.snackbar.SnackbarHelper
import com.wheezy.skyflight.feature.booking.domain.usecase.ProcessPaymentUseCase
import com.wheezy.skyflight.feature.booking.presentation.states.PaymentState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PaymentViewModel @Inject constructor(
    private val processPaymentUseCase: ProcessPaymentUseCase
) : ViewModel() {

    private val _paymentState = MutableStateFlow<PaymentState>(PaymentState.Idle)
    val paymentState: StateFlow<PaymentState> = _paymentState.asStateFlow()

    fun processPayment(
        flight: FlightModel,
        selectedSeats: List<Seat>,
        bookingId: Long
    ) {
        viewModelScope.launch {
            _paymentState.value = PaymentState.Loading

            val result = processPaymentUseCase(bookingId, flight, selectedSeats)
            when (result) {
                is ProcessPaymentUseCase.Result.Success -> {
                    _paymentState.value = PaymentState.Prepared(
                        clientSecret = result.clientSecret,
                        customerId = result.customerId ?: "",
                        ephemeralKey = result.ephemeralKey ?: ""
                    )
                    SnackbarHelper.showInfo("Payment prepared")
                }
                is ProcessPaymentUseCase.Result.Error -> {
                    _paymentState.value = PaymentState.Error(result.message)
                    SnackbarHelper.showError(result.message)
                }
            }
        }
    }

    fun handleSuccess() {
        _paymentState.value = PaymentState.Idle
        SnackbarHelper.showSuccess("Payment completed successfully!")
    }

    fun handleCancel() {
        _paymentState.value = PaymentState.Idle
        SnackbarHelper.showInfo("Payment was cancelled")
    }

    fun handleFailure(message: String) {
        _paymentState.value = PaymentState.Error(message)
        SnackbarHelper.showError("Payment failed: $message")
    }

    fun clearError() {
        if (_paymentState.value is PaymentState.Error) {
            _paymentState.value = PaymentState.Idle
        }
    }
}