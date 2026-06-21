package com.wheezy.skyflight.core.network.di

import android.content.Context
import android.net.ConnectivityManager
import android.annotation.SuppressLint
import android.net.NetworkCapabilities
import coil.ImageLoader
import com.wheezy.skyflight.core.network.BuildConfig
import com.wheezy.skyflight.core.network.api.ApiService
import com.wheezy.skyflight.core.network.api.NotificationSettingsApiService
import com.wheezy.skyflight.core.network.api.WeatherApiService
import com.wheezy.skyflight.core.network.config.NetworkConfig
import com.wheezy.skyflight.core.network.interceptor.NetworkQualityMonitor
import com.wheezy.skyflight.core.network.interceptor.QualityBasedTimeoutInterceptor
import com.wheezy.skyflight.core.network.interceptor.SSLErrorHandler
import com.wheezy.skyflight.core.network.interceptor.ThrottleInterceptor
import com.wheezy.skyflight.core.network.interceptor.WeatherApiKeyInterceptor
import com.wheezy.skyflight.core.network.manager.TokenManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.Cache
import okhttp3.CertificatePinner
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.File
import java.io.IOException
import java.util.concurrent.TimeUnit
import javax.inject.Named
import javax.inject.Singleton
import javax.net.ssl.SSLException
import javax.net.ssl.SSLPeerUnverifiedException

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    private const val WEATHER_BASE_URL = "https://api.openweathermap.org/data/2.5/"

    @Provides
    @Singleton
    fun provideNotificationSettingsApiService(@Named("api") retrofit: Retrofit): NotificationSettingsApiService =
        retrofit.create(NotificationSettingsApiService::class.java)

    @Provides
    @Singleton
    fun provideCache(@ApplicationContext context: Context): Cache =
        Cache(File(context.cacheDir, "okhttp_cache"), 100 * 1024 * 1024L)

    @Provides
    @Singleton
    fun provideConnectivityManager(@ApplicationContext context: Context): ConnectivityManager =
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    @Provides
    @Singleton
    fun provideSSLErrorHandler(): SSLErrorHandler = SSLErrorHandler()

    @Provides
    @Singleton
    fun provideNetworkQualityMonitor(@ApplicationContext context: Context): NetworkQualityMonitor =
        NetworkQualityMonitor(context)

    @Provides
    @Singleton
    fun provideQualityBasedTimeoutInterceptor(
        monitor: NetworkQualityMonitor
    ): QualityBasedTimeoutInterceptor =
        QualityBasedTimeoutInterceptor(monitor)

    @Provides
    @Singleton
    fun provideThrottleInterceptor(): ThrottleInterceptor = ThrottleInterceptor()

    @Provides
    @Singleton
    fun provideAuthInterceptor(tokenManager: TokenManager): Interceptor =
        Interceptor { chain ->
            val request = chain.request()
            val token = tokenManager.getToken()
            val newRequest = if (!token.isNullOrEmpty()) {
                request.newBuilder().header("Authorization", "Bearer $token").build()
            } else request
            chain.proceed(newRequest)
        }

    @Provides
    @Singleton
    @Named("api")
    fun provideApiOkHttpClient(
        cache: Cache,
        authInterceptor: Interceptor,
        connectivityManager: ConnectivityManager,
        qualityBasedTimeoutInterceptor: QualityBasedTimeoutInterceptor,
        sslErrorHandler: SSLErrorHandler,
        throttleInterceptor: ThrottleInterceptor
    ): OkHttpClient {
        val logging = HttpLoggingInterceptor().apply {
            level = if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BODY else HttpLoggingInterceptor.Level.NONE
        }
        val certificatePinner = CertificatePinner.Builder()
            .add("skyflightbooking.ru", "sha256/6rBSrDuH7ckyF11m/2JVnchz5WkP27Pig37UIhs89YQ=")
            .add("skyflightbooking.ru", "sha256/47DEQpj8HBSa+/TImW+5JCeuQeRkm5NMpJWZG3hSuFU=")
            .build()

        return OkHttpClient.Builder()
            .addInterceptor(logging)
            .addInterceptor(authInterceptor)
            .addInterceptor(NetworkCheckInterceptor(connectivityManager, sslErrorHandler))
            .addInterceptor(qualityBasedTimeoutInterceptor)
            .addInterceptor(throttleInterceptor)
            .certificatePinner(certificatePinner)
            .cache(cache)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .retryOnConnectionFailure(true)
            .connectionPool(okhttp3.ConnectionPool(10, 10, TimeUnit.SECONDS))
            .protocols(listOf(okhttp3.Protocol.HTTP_2, okhttp3.Protocol.HTTP_1_1))
            .build()
    }

    @Provides
    @Singleton
    @Named("weather")
    fun provideWeatherOkHttpClient(): OkHttpClient {
        val logging = HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BODY }
        return OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .addInterceptor(WeatherApiKeyInterceptor())
            .addInterceptor(logging)
            .build()
    }

    @Provides
    @Singleton
    @Named("api")
    fun provideApiRetrofit(@Named("api") client: OkHttpClient): Retrofit =
        Retrofit.Builder()
            .baseUrl(NetworkConfig.API_BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

    @Provides
    @Singleton
    @Named("weather")
    fun provideWeatherRetrofit(@Named("weather") client: OkHttpClient): Retrofit =
        Retrofit.Builder()
            .baseUrl(WEATHER_BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

    @Provides
    @Singleton
    fun provideApiService(@Named("api") retrofit: Retrofit): ApiService =
        retrofit.create(ApiService::class.java)

    @Provides
    @Singleton
    fun provideWeatherApiService(@Named("weather") retrofit: Retrofit): WeatherApiService =
        retrofit.create(WeatherApiService::class.java)

    @Provides
    @Singleton
    fun provideImageLoader(
        @ApplicationContext context: Context,
        @Named("api") client: OkHttpClient
    ): ImageLoader =
        ImageLoader.Builder(context)
            .okHttpClient(client)
            .crossfade(true)
            .build()
}

class NetworkCheckInterceptor(
    private val connectivityManager: ConnectivityManager,
    private val sslErrorHandler: SSLErrorHandler
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): okhttp3.Response {
        if (!isNetworkConnected()) {
            throw IOException("No internet connection")
        }
        try {
            return chain.proceed(chain.request())
        } catch (e: SSLPeerUnverifiedException) {
            if (sslErrorHandler.isSSLError(e)) {
                throw IOException(sslErrorHandler.handleSSLException(e), e)
            } else {
                throw e
            }
        } catch (e: SSLException) {
            if (sslErrorHandler.isSSLError(e)) {
                throw IOException(sslErrorHandler.handleSSLException(e), e)
            } else {
                throw e
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun isNetworkConnected(): Boolean {
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
                capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
    }
}