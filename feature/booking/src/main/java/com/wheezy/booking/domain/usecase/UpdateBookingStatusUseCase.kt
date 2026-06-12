package com.wheezy.skyflight.feature.booking.domain.usecase

import com.wheezy.skyflight.core.model.BookingStatus
import com.wheezy.skyflight.feature.booking.domain.repository.BookingRepository
import javax.inject.Inject

class UpdateBookingStatusUseCase @Inject constructor(
    private val repository: BookingRepository
) {
    suspend operator fun invoke(bookingId: Long, status: BookingStatus): Result<Unit> {
        return repository.updateBookingStatus(bookingId, status)
    }
}