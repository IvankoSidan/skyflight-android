package com.wheezy.skyflight.feature.booking.data.repository

import com.wheezy.skyflight.core.model.BookingStatus
import com.wheezy.skyflight.core.network.api.ApiService
import com.wheezy.skyflight.core.network.model.BookingDetailsDTO
import com.wheezy.skyflight.core.network.model.BookingStatusUpdateRequest
import com.wheezy.skyflight.feature.booking.domain.repository.BookingRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BookingRepositoryImpl @Inject constructor(
    private val apiService: ApiService
) : BookingRepository {

    override suspend fun getMyBookings(): Result<List<BookingDetailsDTO>> {
        return try {
            val response = apiService.getMyBookings()
            if (response.isSuccessful) {
                Result.success(response.body().orEmpty())
            } else {
                Result.failure(Exception(response.message()))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateBookingStatus(bookingId: Long, status: BookingStatus): Result<Unit> {
        return try {
            val response = apiService.updateBookingStatus(bookingId, BookingStatusUpdateRequest(status))
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(Exception(response.message()))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun cancelBooking(bookingId: Long): Result<Unit> {
        return try {
            val response = apiService.cancelBooking(bookingId)
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(Exception(response.message()))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteBooking(bookingId: Long): Result<Unit> {
        return try {
            val response = apiService.deleteBooking(bookingId)
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(Exception(response.message()))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}