// core/common/src/main/java/.../worker/SyncBookingWorker.kt
package com.wheezy.skyflight.core.common.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.wheezy.skyflight.core.common.usecase.SyncOfflineBookingsUseCase
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@HiltWorker
class SyncBookingWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val syncOfflineBookingsUseCase: SyncOfflineBookingsUseCase
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        val result = syncOfflineBookingsUseCase()

        if (result.isSuccess) {
            val syncedCount = result.getOrNull() ?: 0
            setProgress(workDataOf("synced_count" to syncedCount))
            Result.success()
        } else {
            Result.retry()
        }
    }
}