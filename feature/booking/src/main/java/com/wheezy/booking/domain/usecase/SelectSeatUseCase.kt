package com.wheezy.skyflight.feature.booking.domain.usecase

import com.wheezy.skyflight.core.model.FlightModel
import com.wheezy.skyflight.core.model.Seat
import com.wheezy.skyflight.core.model.SeatStatus
import java.math.BigDecimal
import javax.inject.Inject

class SelectSeatUseCase @Inject constructor() {

    data class Result(
        val selectedSeats: List<Seat>,
        val seatList: List<Seat>,
        val totalPrice: BigDecimal
    )

    operator fun invoke(
        seat: Seat,
        flight: FlightModel,
        currentSelectedSeats: List<Seat>,
        currentSeatList: List<Seat>
    ): Result {
        val newSelectedSeats = currentSelectedSeats.toMutableList()
        val isSelected = newSelectedSeats.any { it.name == seat.name }

        val newSeatList = if (isSelected) {
            newSelectedSeats.removeAll { it.name == seat.name }
            currentSeatList.map {
                if (it.name == seat.name) it.copy(status = SeatStatus.AVAILABLE) else it
            }
        } else {
            newSelectedSeats.add(seat)
            currentSeatList.map {
                if (it.name == seat.name) it.copy(status = SeatStatus.SELECTED) else it
            }
        }

        val totalPrice = flight.price.multiply(BigDecimal(newSelectedSeats.size))

        return Result(newSelectedSeats, newSeatList, totalPrice)
    }
}