package com.wheezy.skyflight.core.common.utils

import kotlinx.coroutines.*
import kotlin.coroutines.CoroutineContext

class DebounceHelper(
    private val delayMs: Long = 500L,
    private val coroutineContext: CoroutineContext = Dispatchers.Main
) {
    private var job: Job? = null

    fun debounce(action: suspend () -> Unit) {
        job?.cancel()
        job = CoroutineScope(coroutineContext).launch {
            delay(delayMs)
            action()
        }
    }

    fun cancel() {
        job?.cancel()
        job = null
    }
}