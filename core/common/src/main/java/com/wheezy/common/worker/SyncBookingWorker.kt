package com.wheezy.skyflight.core.common.worker

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.wheezy.skyflight.core.common.usecase.SyncOfflineBookingsUseCase
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext

@HiltWorker
class SyncBookingWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val syncOfflineBookingsUseCase: SyncOfflineBookingsUseCase
) : CoroutineWorker(context, params) {

    companion object {
        private const val TAG = "SyncBookingWorker"
    }

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Starting sync bookings work")

            val result = syncOfflineBookingsUseCase()

            if (result.isSuccess) {
                val syncedCount = result.getOrNull() ?: 0
                Log.d(TAG, "Sync completed successfully, synced: $syncedCount bookings")
                setProgress(workDataOf("synced_count" to syncedCount))

                when (val status = syncOfflineBookingsUseCase.syncStatus.first()) {
                    is SyncOfflineBookingsUseCase.SyncStatus.Success -> {
                        Log.d(TAG, "Sync status: Success with ${status.syncedCount} items")
                    }
                    is SyncOfflineBookingsUseCase.SyncStatus.Error -> {
                        Log.w(TAG, "Sync status: Error - ${status.message}")
                    }
                    else -> {
                        Log.d(TAG, "Sync status: ${status::class.simpleName}")
                    }
                }

                Result.success()
            } else {
                val exception = result.exceptionOrNull()
                Log.e(TAG, "Sync failed", exception)

                when (val status = syncOfflineBookingsUseCase.syncStatus.first()) {
                    is SyncOfflineBookingsUseCase.SyncStatus.Error -> {
                        Log.e(TAG, "Sync error: ${status.message}")
                    }
                    else -> {
                        Log.e(TAG, "Sync error: ${exception?.message ?: "Unknown error"}")
                    }
                }

                Result.retry()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Unexpected error during sync", e)
            Result.retry()
        }
    }
}