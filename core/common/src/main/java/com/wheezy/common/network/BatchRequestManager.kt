package com.wheezy.skyflight.core.common.network

import android.util.Log
import com.wheezy.skyflight.core.common.utils.DelayConstants
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import java.util.concurrent.ConcurrentLinkedQueue
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BatchRequestManager @Inject constructor() {

    companion object {
        private const val TAG = "BatchRequestManager"
        private const val MAX_BATCH_SIZE = 10
    }

    private data class BatchRequest<T>(
        val id: String,
        val execute: suspend () -> T,
        val onResult: (T) -> Unit,
        val onError: (Throwable) -> Unit
    )

    private val pendingRequests = ConcurrentLinkedQueue<BatchRequest<*>>()
    private val _batchEvents = MutableSharedFlow<Unit>()
    val batchEvents = _batchEvents.asSharedFlow()

    private var isProcessing = false
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    suspend fun <T> addRequest(
        id: String,
        execute: suspend () -> T,
        onResult: (T) -> Unit,
        onError: (Throwable) -> Unit
    ) {
        try {
            pendingRequests.add(BatchRequest(id, execute, onResult, onError))
            Log.d(TAG, "Request added: $id, queue size: ${pendingRequests.size}")
            if (!isProcessing) {
                processBatch()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error adding request: $id", e)
            onError(e)
        }
    }

    private suspend fun processBatch() {
        if (isProcessing) {
            Log.d(TAG, "Already processing, skipping")
            return
        }

        try {
            isProcessing = true
            Log.d(TAG, "Starting batch processing")

            delay(DelayConstants.SHORT_DELAY.toMillis())

            val batch = mutableListOf<BatchRequest<*>>()
            while (pendingRequests.isNotEmpty() && batch.size < MAX_BATCH_SIZE) {
                pendingRequests.poll()?.let { batch.add(it) }
            }

            Log.d(TAG, "Processing ${batch.size} requests")

            if (batch.isNotEmpty()) {
                batch.forEach { request ->
                    try {
                        executeRequest(request)
                    } catch (e: Exception) {
                        Log.e(TAG, "Error executing request: ${request.id}", e)
                        request.onError(e)
                    }
                }
            }

        } catch (e: Exception) {
            Log.e(TAG, "Error processing batch", e)
        } finally {
            isProcessing = false
            if (pendingRequests.isNotEmpty()) {
                Log.d(TAG, "More requests pending, processing next batch")
                processBatch()
            }
        }
    }

    private suspend fun <T> executeRequest(request: BatchRequest<T>) {
        try {
            Log.d(TAG, "Executing request: ${request.id}")
            val result = request.execute()
            request.onResult(result)
            Log.d(TAG, "Request executed successfully: ${request.id}")
        } catch (e: Exception) {
            Log.e(TAG, "Error executing request: ${request.id}", e)
            request.onError(e)
        }
    }

    fun cancelAll() {
        try {
            Log.d(TAG, "Cancelling all requests, queue size: ${pendingRequests.size}")
            pendingRequests.clear()
            scope.cancel()
            Log.d(TAG, "All requests cancelled")
        } catch (e: Exception) {
            Log.e(TAG, "Error cancelling all requests", e)
        }
    }
}