package com.wheezy.skyflight.core.network.interceptor

import com.wheezy.skyflight.core.network.BuildConfig
import okhttp3.Interceptor
import okhttp3.Response

class WeatherApiKeyInterceptor : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val original = chain.request()
        val url = original.url.newBuilder()
            .addQueryParameter("appid", BuildConfig.OPENWEATHER_API_KEY)
            .build()

        val request = original.newBuilder()
            .url(url)
            .build()

        return chain.proceed(request)
    }
}