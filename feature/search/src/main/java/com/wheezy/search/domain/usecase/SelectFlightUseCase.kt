package com.wheezy.skyflight.feature.search.domain.usecase

import com.wheezy.skyflight.core.model.FlightModel
import com.wheezy.skyflight.core.model.Seat
import com.wheezy.skyflight.core.model.SeatStatus
import com.wheezy.skyflight.feature.search.domain.repository.SearchRepository
import javax.inject.Inject

class SelectFlightUseCase @Inject constructor(
    private val repository: SearchRepository
) {

    data class SelectedFlightResult(
        val flight: FlightModel,
        val seatList: List<Seat>,
        val reservedSeats: List<String>
    )

    suspend operator fun invoke(flight: FlightModel): SelectedFlightResult {
        val reservedSeats = repository.getReservedSeats(flight.flightId ?: 0)
            .getOrElse { emptyList() }

        val seatList = generateSeatList(flight, reservedSeats)

        return SelectedFlightResult(
            flight = flight,
            seatList = seatList,
            reservedSeats = reservedSeats
        )
    }

    private fun generateSeatList(
        flightModel: FlightModel,
        reservedSeats: List<String>
    ): List<Seat> {
        val seatList = mutableListOf<Seat>()
        val numberSeat = flightModel.totalSeats + (flightModel.totalSeats / 7) + 1
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
                    flightModel.reservedSeats.contains(seatName) -> SeatStatus.UNAVAILABLE
                    else -> SeatStatus.AVAILABLE
                }
                seatList.add(Seat(seatStatus, seatName))
            }
        }
        return seatList
    }
}