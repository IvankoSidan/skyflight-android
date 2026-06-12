package com.wheezy.skyflight.feature.search.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wheezy.skyflight.core.model.FlightModel
import com.wheezy.skyflight.core.model.Seat
import com.wheezy.skyflight.core.model.SeatStatus
import com.wheezy.skyflight.core.ui.snackbar.SnackbarHelper
import com.wheezy.skyflight.feature.search.domain.usecase.*
import com.wheezy.skyflight.feature.search.presentation.states.ClassSeatsUiState
import com.wheezy.skyflight.feature.search.presentation.states.FlightsUiState
import com.wheezy.skyflight.feature.search.presentation.states.SearchUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.math.BigDecimal
import javax.inject.Inject

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val getLocationsUseCase: GetLocationsUseCase,
    private val getClassSeatsUseCase: GetClassSeatsUseCase,
    private val searchFlightsUseCase: SearchFlightsUseCase,
    private val selectFlightUseCase: SelectFlightUseCase,
    private val getReservedSeatsUseCase: GetReservedSeatsUseCase
) : ViewModel() {

    private val _locationsState = MutableStateFlow<SearchUiState>(SearchUiState.Loading)
    val locationsState: StateFlow<SearchUiState> = _locationsState.asStateFlow()

    private val _classSeatsState = MutableStateFlow<ClassSeatsUiState>(ClassSeatsUiState.Loading)
    val classSeatsState: StateFlow<ClassSeatsUiState> = _classSeatsState.asStateFlow()

    private val _flightsState = MutableStateFlow<FlightsUiState>(FlightsUiState.Loading)
    val flightsState: StateFlow<FlightsUiState> = _flightsState.asStateFlow()

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

    fun fetchLocations() {
        viewModelScope.launch {
            _locationsState.value = SearchUiState.Loading
            val result = getLocationsUseCase()
            result.onSuccess { locations ->
                _locationsState.value = SearchUiState.Success(locations)
            }.onFailure { error ->
                _locationsState.value = SearchUiState.Error(error.message ?: "Failed to load locations")
                SnackbarHelper.showError(error.message ?: "Failed to load locations")
            }
        }
    }

    fun fetchClassSeats() {
        viewModelScope.launch {
            _classSeatsState.value = ClassSeatsUiState.Loading
            val result = getClassSeatsUseCase()
            result.onSuccess { seats ->
                _classSeatsState.value = ClassSeatsUiState.Success(seats)
            }.onFailure { error ->
                _classSeatsState.value = ClassSeatsUiState.Error(error.message ?: "Failed to load class seats")
                SnackbarHelper.showError(error.message ?: "Failed to load class seats")
            }
        }
    }

    fun searchFlights(from: String, to: String, classType: String?) {
        viewModelScope.launch {
            _flightsState.value = FlightsUiState.Loading
            val result = searchFlightsUseCase(from, to, classType)
            result.onSuccess { flights ->
                _flightsState.value = FlightsUiState.Success(flights)
            }.onFailure { error ->
                _flightsState.value = FlightsUiState.Error(error.message ?: "Failed to search flights")
                SnackbarHelper.showError(error.message ?: "Failed to search flights")
            }
        }
    }

    fun selectFlight(flight: FlightModel) {
        viewModelScope.launch {
            val result = selectFlightUseCase(flight)
            _selectedFlight.value = result.flight
            _seatList.value = result.seatList
            _reservedSeats.value = result.reservedSeats
            clearSeatSelection()
        }
    }

    fun selectSeat(seat: Seat, flight: FlightModel) {
        val currentSelected = _selectedSeats.value.toMutableList()
        val isSelected = currentSelected.any { it.name == seat.name }

        if (isSelected) {
            currentSelected.removeAll { it.name == seat.name }
            _seatList.update { seats ->
                seats.map { if (it.name == seat.name) it.copy(status = SeatStatus.AVAILABLE) else it }
            }
        } else {
            currentSelected.add(seat)
            _seatList.update { seats ->
                seats.map { if (it.name == seat.name) it.copy(status = SeatStatus.SELECTED) else it }
            }
        }
        _selectedSeats.value = currentSelected
        _totalPrice.value = flight.price.multiply(BigDecimal(currentSelected.size))
    }

    fun fetchReservedSeats(flightId: Long) {
        viewModelScope.launch {
            val result = getReservedSeatsUseCase(flightId)
            result.onSuccess { seats ->
                _reservedSeats.value = seats
            }.onFailure {
                _reservedSeats.value = emptyList()
            }
        }
    }

    private fun clearSeatSelection() {
        val updatedSeats = _seatList.value.map { seat ->
            if (seat.status == SeatStatus.SELECTED) seat.copy(status = SeatStatus.AVAILABLE)
            else seat
        }
        _seatList.value = updatedSeats
        _selectedSeats.value = emptyList()
        _totalPrice.value = BigDecimal.ZERO
    }

    fun clearErrors() {
        if (_locationsState.value is SearchUiState.Error) {
            _locationsState.value = SearchUiState.Loading
        }
        if (_classSeatsState.value is ClassSeatsUiState.Error) {
            _classSeatsState.value = ClassSeatsUiState.Loading
        }
        if (_flightsState.value is FlightsUiState.Error) {
            _flightsState.value = FlightsUiState.Loading
        }
    }
}