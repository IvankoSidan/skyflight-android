package com.wheezy.skyflight.feature.booking.domain.usecase

import com.wheezy.skyflight.core.network.model.BookingDetailsDTO
import com.wheezy.skyflight.feature.booking.domain.repository.BookingRepository
import javax.inject.Inject

class GetMyBookingsUseCase @Inject constructor(
    private val repository: BookingRepository
) {
    suspend operator fun invoke(): Result<List<BookingDetailsDTO>> {
        return repository.getMyBookings()
    }
}