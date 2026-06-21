package com.wheezy.skyflight.feature.booking.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wheezy.skyflight.core.common.event.PaymentUpdateEventBus
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
import java.math.BigDecimal
import javax.inject.Inject

@HiltViewModel
class PaymentViewModel @Inject constructor(
    private val processPaymentUseCase: ProcessPaymentUseCase
) : ViewModel() {

    private val _paymentState = MutableStateFlow<PaymentState>(PaymentState.Idle)
    val paymentState: StateFlow<PaymentState> = _paymentState.asStateFlow()

    init {
        viewModelScope.launch {
            PaymentUpdateEventBus.events.collect { event ->
                event?.let {
                    when (it.paymentStatus.uppercase()) {
                        "SUCCESS", "COMPLETED" -> {
                            handleSuccess()
                            SnackbarHelper.showSuccess("Payment confirmed by server")
                        }
                        "FAILED", "ERROR" -> {
                            handleFailure("Payment failed on server side")
                        }
                        "PENDING" -> {
                            SnackbarHelper.showInfo("Payment is being processed...")
                        }
                    }
                }
            }
        }
    }

    fun processPayment(
        flight: FlightModel,
        selectedSeats: List<Seat>,
        bookingId: Long
    ) {
        viewModelScope.launch {
            _paymentState.value = PaymentState.Loading
            val amount = flight.price
                .multiply(BigDecimal(selectedSeats.size))
                .multiply(BigDecimal(100))
                .toLong()
            when (val result = processPaymentUseCase(bookingId, amount)) {
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

    fun processPaymentWithAmount(bookingId: Long, amount: Long) {
        viewModelScope.launch {
            _paymentState.value = PaymentState.Loading
            when (val result = processPaymentUseCase(bookingId, amount)) {
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