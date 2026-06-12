package com.wheezy.skyflight.core.common.network

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import java.util.concurrent.ConcurrentLinkedQueue
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BatchRequestManager @Inject constructor() {

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
        pendingRequests.add(BatchRequest(id, execute, onResult, onError))
        if (!isProcessing) {
            processBatch()
        }
    }

    private suspend fun processBatch() {
        if (isProcessing) return
        isProcessing = true

        delay(100)

        val batch = mutableListOf<BatchRequest<*>>()
        while (pendingRequests.isNotEmpty() && batch.size < 10) {
            pendingRequests.poll()?.let { batch.add(it) }
        }

        if (batch.isNotEmpty()) {
            batch.forEach { request ->
                try {
                    executeRequest(request)
                } catch (e: Exception) {
                    request.onError(e)
                }
            }
        }

        isProcessing = false
        if (pendingRequests.isNotEmpty()) {
            processBatch()
        }
    }

    private suspend fun <T> executeRequest(request: BatchRequest<T>) {
        try {
            val result = request.execute()
            request.onResult(result)
        } catch (e: Exception) {
            request.onError(e)
        }
    }

    fun cancelAll() {
        pendingRequests.clear()
        scope.cancel()
    }
}