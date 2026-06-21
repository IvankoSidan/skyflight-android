package com.wheezy.skyflight.feature.search.presentation.states

import com.wheezy.skyflight.core.model.FlightModel
import com.wheezy.skyflight.core.model.LocationModel
import com.wheezy.skyflight.core.model.Seat
import com.wheezy.skyflight.core.model.SeatSelectionState
import com.wheezy.skyflight.feature.search.domain.model.SearchParams
import java.math.BigDecimal

sealed class SearchUiState {
    object Loading : SearchUiState()
    data class Success(val locations: List<LocationModel>) : SearchUiState()
    data class Error(val message: String) : SearchUiState()
}

sealed class ClassSeatsUiState {
    object Loading : ClassSeatsUiState()
    data class Success(val classSeats: List<String>) : ClassSeatsUiState()
    data class Error(val message: String) : ClassSeatsUiState()
}

sealed class FlightsUiState {
    object Loading : FlightsUiState()
    data class Success(val flights: List<FlightModel>) : FlightsUiState()
    data class Error(val message: String) : FlightsUiState()
}

data class SearchScreenState(
    val searchParams: SearchParams = SearchParams(),
    val locationsState: SearchUiState = SearchUiState.Loading,
    val classSeatsState: ClassSeatsUiState = ClassSeatsUiState.Loading,
    val flightsState: FlightsUiState = FlightsUiState.Loading,
    val seatSelectionState: SeatSelectionState = SeatSelectionState.Loading,
    val selectedFlight: FlightModel? = null,
    val seatList: List<Seat> = emptyList(),
    val selectedSeats: List<Seat> = emptyList(),
    val totalPrice: BigDecimal = BigDecimal.ZERO,
    val reservedSeats: List<String> = emptyList()
)