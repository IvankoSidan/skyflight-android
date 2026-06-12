package com.wheezy.skyflight.core.network.di

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.annotation.SuppressLint
import coil.ImageLoader
import com.wheezy.skyflight.core.network.api.ApiService
import com.wheezy.skyflight.core.network.api.WeatherApiService
import com.wheezy.skyflight.core.network.config.NetworkConfig
import com.wheezy.skyflight.core.network.interceptor.WeatherApiKeyInterceptor
import com.wheezy.skyflight.core.network.manager.TokenManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.Cache
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

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    private const val WEATHER_BASE_URL = "https://api.openweathermap.org/data/2.5/"

    @Provides
    @Singleton
    fun provideCache(@ApplicationContext context: Context): Cache {
        val cacheSize = 100 * 1024 * 1024L
        return Cache(File(context.cacheDir, "okhttp_cache"), cacheSize)
    }

    @Provides
    @Singleton
    fun provideConnectivityManager(@ApplicationContext context: Context): ConnectivityManager {
        return context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    }

    @Provides
    @Singleton
    fun provideAuthInterceptor(tokenManager: TokenManager): Interceptor {
        return Interceptor { chain ->
            val originalRequest = chain.request()
            val token = tokenManager.getToken()

            val request = if (!token.isNullOrEmpty()) {
                originalRequest.newBuilder()
                    .header("Authorization", "Bearer $token")
                    .build()
            } else {
                originalRequest
            }

            chain.proceed(request)
        }
    }

    @Provides
    @Singleton
    @Named("api")
    fun provideApiOkHttpClient(
        cache: Cache,
        authInterceptor: Interceptor,
        connectivityManager: ConnectivityManager,
        isDebug: Boolean
    ): OkHttpClient {

        val logging = HttpLoggingInterceptor().apply {
            level = if (isDebug) HttpLoggingInterceptor.Level.BODY
            else HttpLoggingInterceptor.Level.NONE
        }

        return OkHttpClient.Builder()
            .addInterceptor(logging)
            .addInterceptor(authInterceptor)
            .addInterceptor(NetworkCheckInterceptor(connectivityManager))
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
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

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
    fun provideApiRetrofit(@Named("api") okHttpClient: OkHttpClient): Retrofit =
        Retrofit.Builder()
            .baseUrl(NetworkConfig.API_BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

    @Provides
    @Singleton
    @Named("weather")
    fun provideWeatherRetrofit(@Named("weather") okHttpClient: OkHttpClient): Retrofit =
        Retrofit.Builder()
            .baseUrl(WEATHER_BASE_URL)
            .client(okHttpClient)
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
        @Named("api") okHttpClient: OkHttpClient
    ): ImageLoader =
        ImageLoader.Builder(context)
            .okHttpClient(okHttpClient)
            .crossfade(true)
            .build()
}

class NetworkCheckInterceptor(
    private val connectivityManager: ConnectivityManager
) : Interceptor {

    @SuppressLint("MissingPermission")
    override fun intercept(chain: Interceptor.Chain): okhttp3.Response {
        if (!isNetworkConnected()) {
            throw IOException("No internet connection")
        }
        return chain.proceed(chain.request())
    }

    @SuppressLint("MissingPermission")
    private fun isNetworkConnected(): Boolean {
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
                capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
    }
}