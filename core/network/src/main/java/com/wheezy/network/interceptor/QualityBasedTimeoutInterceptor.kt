package com.wheezy.skyflight.core.network.interceptor

import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import java.util.concurrent.TimeUnit

class QualityBasedTimeoutInterceptor(
    private val networkQualityMonitor: NetworkQualityMonitor
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val timeout = networkQualityMonitor.getTimeoutByQuality()

        val client = OkHttpClient.Builder()
            .connectTimeout(timeout, TimeUnit.MILLISECONDS)
            .readTimeout(timeout, TimeUnit.MILLISECONDS)
            .writeTimeout(timeout, TimeUnit.MILLISECONDS)
            .build()

        return client.newCall(chain.request()).execute()
    }
}