package com.wheezy.skyflight.feature.booking.domain.usecase

import com.wheezy.skyflight.core.model.BookingStatus
import com.wheezy.skyflight.core.model.canBeCancelled
import com.wheezy.skyflight.core.model.canBeDeleted
import com.wheezy.skyflight.feature.booking.domain.repository.BookingRepository
import javax.inject.Inject

class CancelOrDeleteBookingUseCase @Inject constructor(
    private val repository: BookingRepository
) {
    sealed class Result {
        object Success : Result()
        data class Error(val message: String) : Result()
    }

    suspend operator fun invoke(bookingId: Long, status: BookingStatus): Result {
        return when {
            status.canBeDeleted() -> {
                try {
                    val response = repository.deleteBooking(bookingId)
                    if (response.isSuccess) Result.Success
                    else Result.Error(response.exceptionOrNull()?.message ?: "Delete failed")
                } catch (e: Exception) {
                    Result.Error(e.message ?: "Unknown error")
                }
            }
            status.canBeCancelled() -> {
                try {
                    val response = repository.cancelBooking(bookingId)
                    if (response.isSuccess) Result.Success
                    else Result.Error(response.exceptionOrNull()?.message ?: "Cancel failed")
                } catch (e: Exception) {
                    Result.Error(e.message ?: "Unknown error")
                }
            }
            else -> Result.Error("Action not allowed for status: $status")
        }
    }
}