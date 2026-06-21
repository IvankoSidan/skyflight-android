package com.wheezy.skyflight.core.common.contract

import com.wheezy.skyflight.core.model.FlightModel
import com.wheezy.skyflight.core.model.Seat
import kotlinx.coroutines.flow.StateFlow
import java.math.BigDecimal

interface SeatSelectionContract {
    val seatList: StateFlow<List<Seat>>
    val selectedSeats: StateFlow<List<Seat>>
    val totalPrice: StateFlow<BigDecimal>
    val reservedSeats: StateFlow<List<String>>
    val selectedFlight: StateFlow<FlightModel?>

    fun selectFlight(flight: FlightModel)
    fun selectSeat(seat: Seat)
}