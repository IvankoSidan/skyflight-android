package com.wheezy.skyflight.feature.search.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wheezy.skyflight.core.common.contract.SeatSelectionContract
import com.wheezy.skyflight.core.common.coroutines.CoroutineOptimizer
import com.wheezy.skyflight.core.common.coroutines.CoroutineOptimizer.debounceFirst
import com.wheezy.skyflight.core.common.usecase.GetFlightByIdUseCase
import com.wheezy.skyflight.core.model.FlightModel
import com.wheezy.skyflight.core.model.Seat
import com.wheezy.skyflight.core.model.SeatStatus
import com.wheezy.skyflight.core.model.SeatSelectionState
import com.wheezy.skyflight.core.ui.snackbar.SnackbarHelper
import com.wheezy.skyflight.feature.search.domain.usecase.*
import com.wheezy.skyflight.feature.search.presentation.states.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.math.BigDecimal
import javax.inject.Inject

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val getLocationsUseCase: GetLocationsUseCase,
    private val getClassSeatsUseCase: GetClassSeatsUseCase,
    private val searchFlightsUseCase: SearchFlightsUseCase,
    private val selectFlightUseCase: SelectFlightUseCase,
    private val getReservedSeatsUseCase: GetReservedSeatsUseCase,
    private val getFlightByIdUseCase: GetFlightByIdUseCase
) : ViewModel(), SeatSelectionContract {

    private val _state = MutableStateFlow(SearchScreenState())
    val state: StateFlow<SearchScreenState> = _state.asStateFlow()

    val locationsState: StateFlow<SearchUiState> = _state.map { it.locationsState }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = SearchUiState.Loading
    )

    val classSeatsState: StateFlow<ClassSeatsUiState> = _state.map { it.classSeatsState }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = ClassSeatsUiState.Loading
    )

    val flightsState: StateFlow<FlightsUiState> = _state.map { it.flightsState }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = FlightsUiState.Loading
    )

    val seatSelectionState: StateFlow<SeatSelectionState> = _state.map { it.seatSelectionState }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = SeatSelectionState.Loading
    )

    override val selectedFlight: StateFlow<FlightModel?> = _state.map { it.selectedFlight }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = null
    )

    override val seatList: StateFlow<List<Seat>> = _state.map { it.seatList }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = emptyList()
    )

    override val selectedSeats: StateFlow<List<Seat>> = _state.map { it.selectedSeats }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = emptyList()
    )

    override val totalPrice: StateFlow<BigDecimal> = _state.map { it.totalPrice }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = BigDecimal.ZERO
    )

    override val reservedSeats: StateFlow<List<String>> = _state.map { it.reservedSeats }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = emptyList()
    )

    fun fetchLocations() {
        viewModelScope.launch(CoroutineOptimizer.ioDispatcher) {
            _state.update { it.copy(locationsState = SearchUiState.Loading) }
            getLocationsUseCase().onSuccess { locations ->
                _state.update { it.copy(locationsState = SearchUiState.Success(locations)) }
            }.onFailure { error ->
                _state.update { it.copy(locationsState = SearchUiState.Error(error.message ?: "Failed to load locations")) }
                SnackbarHelper.showError(error.message ?: "Failed to load locations")
            }
        }
    }

    fun fetchClassSeats() {
        viewModelScope.launch(CoroutineOptimizer.ioDispatcher) {
            _state.update { it.copy(classSeatsState = ClassSeatsUiState.Loading) }
            getClassSeatsUseCase().onSuccess { seats ->
                _state.update { it.copy(classSeatsState = ClassSeatsUiState.Success(seats)) }
            }.onFailure { error ->
                _state.update { it.copy(classSeatsState = ClassSeatsUiState.Error(error.message ?: "Failed to load class seats")) }
                SnackbarHelper.showError(error.message ?: "Failed to load class seats")
            }
        }
    }

    fun searchFlights(from: String, to: String, classType: String?) {
        viewModelScope.launch(CoroutineOptimizer.ioDispatcher) {
            flow {
                emit(Triple(from, to, classType))
            }.debounceFirst().collect { (fromParam, toParam, classParam) ->
                _state.update { it.copy(flightsState = FlightsUiState.Loading) }

                val results = CoroutineOptimizer.parallelMap(
                    items = listOf(Triple(fromParam, toParam, classParam)),
                    parallelism = 1
                ) { (fromP, toP, classP) ->
                    searchFlightsUseCase(fromP, toP, classP)
                }

                results.firstOrNull()?.let { result ->
                    result.onSuccess { flights ->
                        withContext(CoroutineOptimizer.mainDispatcher) {
                            _state.update { it.copy(flightsState = FlightsUiState.Success(flights)) }
                        }
                    }.onFailure { error ->
                        withContext(CoroutineOptimizer.mainDispatcher) {
                            _state.update { it.copy(flightsState = FlightsUiState.Error(error.message ?: "Failed to search flights")) }
                            SnackbarHelper.showError(error.message ?: "Failed to search flights")
                        }
                    }
                } ?: run {
                    withContext(CoroutineOptimizer.mainDispatcher) {
                        _state.update { it.copy(flightsState = FlightsUiState.Error("No results")) }
                    }
                }
            }
        }
    }

    suspend fun loadFlight(flightId: Long) {
        _state.update { it.copy(seatSelectionState = SeatSelectionState.Loading) }
        try {
            val flight = getFlightByIdUseCase(flightId)
            if (flight != null) {
                selectFlight(flight)
            } else {
                _state.update { it.copy(seatSelectionState = SeatSelectionState.Error("Flight not found")) }
                SnackbarHelper.showError("Flight not found")
            }
        } catch (e: Exception) {
            _state.update { it.copy(seatSelectionState = SeatSelectionState.Error(e.message ?: "Failed to load flight")) }
            SnackbarHelper.showError(e.message ?: "Failed to load flight")
        }
    }

    override fun selectFlight(flight: FlightModel) {
        viewModelScope.launch(CoroutineOptimizer.ioDispatcher) {
            val reservedSeatsResult = getReservedSeatsUseCase(flight.flightId ?: 0)
            val result = selectFlightUseCase(flight)

            _state.update {
                it.copy(
                    selectedFlight = flight,
                    reservedSeats = reservedSeatsResult.getOrElse { result.reservedSeats },
                    seatList = result.seatList,
                    seatSelectionState = SeatSelectionState.Success(flight.flightId ?: 0),
                    selectedSeats = emptyList(),
                    totalPrice = BigDecimal.ZERO
                )
            }
        }
    }

    override fun selectSeat(seat: Seat) {
        val currentState = _state.value
        val flight = currentState.selectedFlight ?: return
        val currentSelected = currentState.selectedSeats.toMutableList()
        val isSelected = currentSelected.any { it.name == seat.name }

        val newSelectedSeats: List<Seat>
        val newSeatList: List<Seat>

        if (isSelected) {
            currentSelected.removeAll { it.name == seat.name }
            newSelectedSeats = currentSelected
            newSeatList = currentState.seatList.map {
                if (it.name == seat.name) it.copy(status = SeatStatus.AVAILABLE) else it
            }
        } else {
            if (currentState.reservedSeats.contains(seat.name)) {
                SnackbarHelper.showError("This seat is already taken")
                return
            }
            currentSelected.add(seat)
            newSelectedSeats = currentSelected
            newSeatList = currentState.seatList.map {
                if (it.name == seat.name) it.copy(status = SeatStatus.SELECTED) else it
            }
        }

        _state.update {
            it.copy(
                selectedSeats = newSelectedSeats,
                seatList = newSeatList,
                totalPrice = flight.price.multiply(BigDecimal(newSelectedSeats.size))
            )
        }
    }
}