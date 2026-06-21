package com.wheezy.skyflight.core.common.network

import kotlinx.coroutines.delay
import retrofit2.Response
import java.io.IOException
import java.net.SocketTimeoutException
import kotlin.time.Duration.Companion.milliseconds

class SmartRetryPolicy {

    companion object {

        suspend fun <T> executeWithRetry(
            maxRetries: Int = 3,
            initialDelayMs: Long = 500,
            maxDelayMs: Long = 5000,
            shouldRetry: (Throwable) -> Boolean = {
                it is SocketTimeoutException || it is IOException
            },
            block: suspend () -> Response<T>
        ): Response<T> {

            var lastException: Throwable? = null
            var delayMs = initialDelayMs

            repeat(maxRetries) { attempt ->

                try {

                    val response = block()

                    if (response.isSuccessful || attempt == maxRetries - 1) {
                        return response
                    }

                    if (attempt < maxRetries - 1) {
                        delay(delayMs.milliseconds)
                        delayMs = (delayMs * 2).coerceAtMost(maxDelayMs)
                    }

                } catch (e: Exception) {

                    lastException = e

                    if (shouldRetry(e) && attempt < maxRetries - 1) {

                        delay(delayMs.milliseconds)
                        delayMs = (delayMs * 2).coerceAtMost(maxDelayMs)

                    } else {

                        throw e
                    }
                }
            }

            throw lastException ?: IOException("Max retries exceeded")
        }
    }
}