package com.wheezy.skyflight.core.common.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.wheezy.skyflight.core.database.dao.FlightDao
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class CacheCleanupWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val flightDao: FlightDao
) : CoroutineWorker(context, params) {

    companion object {
        private const val CACHE_TTL_MS = 7 * 24 * 60 * 60 * 1000L // 7 days
    }

    override suspend fun doWork(): Result {
        return try {
            val cutoffTime = System.currentTimeMillis() - CACHE_TTL_MS
            flightDao.deleteOldFlights(cutoffTime)
            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }
}