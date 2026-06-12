package com.wheezy.skyflight.feature.booking.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wheezy.skyflight.core.model.FlightModel
import com.wheezy.skyflight.core.model.Seat
import com.wheezy.skyflight.core.ui.snackbar.SnackbarHelper
import com.wheezy.skyflight.feature.booking.domain.usecase.GenerateSeatMapUseCase
import com.wheezy.skyflight.feature.booking.domain.usecase.GetFlightByIdUseCase
import com.wheezy.skyflight.feature.booking.domain.usecase.GetReservedSeatsUseCase
import com.wheezy.skyflight.feature.booking.domain.usecase.SelectSeatUseCase
import com.wheezy.skyflight.feature.booking.presentation.states.SeatSelectionState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.math.BigDecimal
import javax.inject.Inject

@HiltViewModel
class SeatSelectionViewModel @Inject constructor(
    private val getFlightByIdUseCase: GetFlightByIdUseCase,
    private val getReservedSeatsUseCase: GetReservedSeatsUseCase,
    private val generateSeatMapUseCase: GenerateSeatMapUseCase,
    private val selectSeatUseCase: SelectSeatUseCase
) : ViewModel() {

    private val _seatSelectionState = MutableStateFlow<SeatSelectionState>(SeatSelectionState.Loading)
    val seatSelectionState: StateFlow<SeatSelectionState> = _seatSelectionState.asStateFlow()

    private val _selectedFlight = MutableStateFlow<FlightModel?>(null)
    val selectedFlight: StateFlow<FlightModel?> = _selectedFlight.asStateFlow()

    private val _selectedSeats = MutableStateFlow<List<Seat>>(emptyList())
    val selectedSeats: StateFlow<List<Seat>> = _selectedSeats.asStateFlow()

    private val _totalPrice = MutableStateFlow(BigDecimal.ZERO)
    val totalPrice: StateFlow<BigDecimal> = _totalPrice.asStateFlow()

    private val _seatList = MutableStateFlow<List<Seat>>(emptyList())
    val seatList: StateFlow<List<Seat>> = _seatList.asStateFlow()

    private val _reservedSeats = MutableStateFlow<List<String>>(emptyList())
    val reservedSeats: StateFlow<List<String>> = _reservedSeats.asStateFlow()

    fun loadFlight(flightId: Long) {
        if (flightId <= 0) {
            _seatSelectionState.value = SeatSelectionState.Error("Invalid flight ID")
            return
        }

        viewModelScope.launch {
            _seatSelectionState.value = SeatSelectionState.Loading

            try {
                val flight = getFlightByIdUseCase(flightId)
                if (flight != null) {
                    _selectedFlight.value = flight

                    val reserved = getReservedSeatsUseCase(flightId)
                    _reservedSeats.value = reserved

                    val result = generateSeatMapUseCase(flight, reserved)
                    _seatList.value = result.seatList
                    _seatSelectionState.value = SeatSelectionState.Success(flightId)
                } else {
                    _seatSelectionState.value = SeatSelectionState.Error("Flight not found")
                    SnackbarHelper.showError("Flight not found")
                }
            } catch (e: Exception) {
                _seatSelectionState.value = SeatSelectionState.Error(e.message ?: "Failed to load flight")
                SnackbarHelper.showError(e.message ?: "Failed to load flight data")
            }
        }
    }

    fun selectSeat(seat: Seat) {
        val flight = _selectedFlight.value ?: return

        val result = selectSeatUseCase(
            seat = seat,
            flight = flight,
            currentSelectedSeats = _selectedSeats.value,
            currentSeatList = _seatList.value
        )

        _selectedSeats.value = result.selectedSeats
        _seatList.value = result.seatList
        _totalPrice.value = result.totalPrice
    }
}