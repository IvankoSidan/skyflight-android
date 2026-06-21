package com.wheezy.skyflight.core.common.usecase

import android.util.Log
import com.wheezy.skyflight.core.common.manager.NetworkMonitor
import com.wheezy.skyflight.core.database.dao.OfflineBookingDao
import com.wheezy.skyflight.core.network.api.ApiService
import com.wheezy.skyflight.core.network.manager.TokenManager
import com.wheezy.skyflight.core.network.model.BookingRequestDto
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import kotlinx.coroutines.Dispatchers
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SyncOfflineBookingsUseCase @Inject constructor(
    private val offlineBookingDao: OfflineBookingDao,
    private val apiService: ApiService,
    private val networkMonitor: NetworkMonitor,
    private val tokenManager: TokenManager
) {

    companion object {
        private const val TAG = "SyncOfflineBookingsUseCase"
    }

    sealed class SyncStatus {
        object Idle : SyncStatus()
        object InProgress : SyncStatus()
        data class Success(val syncedCount: Int) : SyncStatus()
        data class Error(val message: String) : SyncStatus()
    }

    private val _syncStatus: MutableStateFlow<SyncStatus> = MutableStateFlow<SyncStatus>(SyncStatus.Idle)
    val syncStatus = _syncStatus.asStateFlow()

    suspend operator fun invoke(): Result<Int> = withContext(Dispatchers.IO) {
        if (!networkMonitor.isConnected.value) {
            _syncStatus.value = SyncStatus.Error("No internet connection")
            return@withContext Result.failure(Exception("No internet connection"))
        }

        val token = tokenManager.getToken()
        if (token.isNullOrEmpty()) {
            _syncStatus.value = SyncStatus.Error("Not authenticated")
            return@withContext Result.failure(Exception("Not authenticated"))
        }

        _syncStatus.value = SyncStatus.InProgress

        try {
            val pendingBookings = offlineBookingDao.getPendingSyncBookingsSuspend()
            if (pendingBookings.isEmpty()) {
                _syncStatus.value = SyncStatus.Success(0)
                return@withContext Result.success(0)
            }

            var syncedCount = 0
            val maxRetries = 5

            for (booking in pendingBookings) {
                if (booking.retryCount >= maxRetries) {
                    offlineBookingDao.updateBooking(
                        booking.copy(
                            lastError = "Max retries exceeded",
                            status = "FAILED"
                        )
                    )
                    continue
                }

                try {
                    val response = apiService.createBooking(
                        BookingRequestDto(
                            flightId = booking.flightId,
                            seatNumber = booking.seatNumbers
                        )
                    )

                    if (response.isSuccessful && response.body() != null) {
                        offlineBookingDao.deleteBookingById(booking.id)
                        syncedCount++
                    } else {
                        offlineBookingDao.updateBooking(
                            booking.copy(
                                retryCount = booking.retryCount + 1,
                                lastError = response.message()
                            )
                        )
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error syncing booking", e)
                    offlineBookingDao.updateBooking(
                        booking.copy(
                            retryCount = booking.retryCount + 1,
                            lastError = e.message?.take(500)
                        )
                    )
                }
            }

            val thirtyDaysAgo = System.currentTimeMillis() - (30L * 24 * 60 * 60 * 1000)
            offlineBookingDao.deleteSyncedBookings(thirtyDaysAgo)

            _syncStatus.value = SyncStatus.Success(syncedCount)
            Result.success(syncedCount)

        } catch (e: Exception) {
            Log.e(TAG, "Error during sync", e)
            _syncStatus.value = SyncStatus.Error(e.message ?: "Unknown error")
            Result.failure(e)
        }
    }
}