package com.wheezy.skyflight.core.common.coroutines

import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.buffer
import kotlinx.coroutines.flow.conflate

@OptIn(ExperimentalCoroutinesApi::class)
object CoroutineOptimizer {

    val ioDispatcher = Dispatchers.IO.limitedParallelism(4)
    val computationDispatcher = Dispatchers.Default.limitedParallelism(2)
    val mainDispatcher = Dispatchers.Main.immediate

    suspend fun <T, R> parallelMap(
        items: List<T>,
        parallelism: Int = 3,
        transform: suspend (T) -> R
    ): List<R> = coroutineScope {
        val channel = Channel<suspend () -> R>(parallelism)

        launch(ioDispatcher) {
            items.forEach { item ->
                channel.send { transform(item) }
            }
            channel.close()
        }

        (1..parallelism).map { _ ->
            async(computationDispatcher) {
                val list = mutableListOf<R>()
                for (task in channel) {
                    list.add(task())
                }
                list
            }
        }.flatMap { it.await() }
    }

    fun <T> Flow<T>.debounceFirst(): Flow<T> {
        return this.buffer(0).conflate()
    }
}