package com.wheezy.skyflight.core.network.interceptor

import android.util.Log
import okhttp3.Interceptor
import okhttp3.Response
import java.util.concurrent.TimeUnit

class QualityBasedTimeoutInterceptor(
    private val networkQualityMonitor: NetworkQualityMonitor
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val quality = networkQualityMonitor.networkQuality.value
        val timeout = networkQualityMonitor.getTimeoutByQuality().toInt()

        Log.d(
            "NetworkQuality",
            "Current quality: $quality, timeout: $timeout ms"
        )

        return chain
            .withConnectTimeout(timeout, TimeUnit.MILLISECONDS)
            .withReadTimeout(timeout, TimeUnit.MILLISECONDS)
            .withWriteTimeout(timeout, TimeUnit.MILLISECONDS)
            .proceed(chain.request())
    }
}