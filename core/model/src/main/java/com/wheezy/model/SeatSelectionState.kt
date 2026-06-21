package com.wheezy.skyflight.core.model

sealed class SeatSelectionState {
    object Loading : SeatSelectionState()
    data class Success(val flightId: Long) : SeatSelectionState()
    data class Error(val message: String) : SeatSelectionState()
}