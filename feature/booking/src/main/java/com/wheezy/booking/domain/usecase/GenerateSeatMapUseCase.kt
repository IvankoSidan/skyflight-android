package com.wheezy.skyflight.feature.booking.domain.usecase

import com.wheezy.skyflight.core.model.FlightModel
import com.wheezy.skyflight.core.model.Seat
import com.wheezy.skyflight.core.model.SeatStatus
import javax.inject.Inject

class GenerateSeatMapUseCase @Inject constructor() {

    data class Result(
        val seatList: List<Seat>,
        val reservedSeats: List<String>
    )

    operator fun invoke(
        flight: FlightModel,
        reservedSeats: List<String>
    ): Result {
        val seatList = mutableListOf<Seat>()
        val numberSeat = flight.totalSeats + (flight.totalSeats / 7) + 1
        val seatAlphabetMap = mapOf(
            0 to "A", 1 to "B", 2 to "C", 4 to "D",
            5 to "E", 6 to "F"
        )

        var row = 0
        for (i in 0 until numberSeat) {
            if (i % 7 == 0) row++
            if (i % 7 == 3) {
                seatList.add(Seat(SeatStatus.EMPTY, row.toString()))
            } else {
                val seatName = seatAlphabetMap[i % 7]?.plus(row.toString()) ?: row.toString()
                val seatStatus = when {
                    reservedSeats.contains(seatName) -> SeatStatus.UNAVAILABLE
                    flight.reservedSeats.contains(seatName) -> SeatStatus.UNAVAILABLE
                    else -> SeatStatus.AVAILABLE
                }
                seatList.add(Seat(seatStatus, seatName))
            }
        }
        return Result(seatList, reservedSeats)
    }
}