package com.wheezy.skyflight.core.network.interceptor

import okhttp3.Interceptor
import okhttp3.Response
import java.util.concurrent.ConcurrentHashMap

class ThrottleInterceptor : Interceptor {
    private val lastCallTime = ConcurrentHashMap<String, Long>()
    private val minIntervalMs = 1000L

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val key = "${request.method}:${request.url}"
        val now = System.currentTimeMillis()

        lastCallTime[key]?.let { lastTime ->
            val elapsed = now - lastTime
            if (elapsed < minIntervalMs) {
                Thread.sleep(minIntervalMs - elapsed)
            }
        }

        lastCallTime[key] = now
        return chain.proceed(request)
    }
}