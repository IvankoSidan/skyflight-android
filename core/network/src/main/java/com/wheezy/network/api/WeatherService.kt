package com.wheezy.skyflight.core.network.api

import com.google.gson.annotations.SerializedName
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface WeatherApiService {
    @GET("weather")
    suspend fun getCurrentWeather(
        @Query("q") cityName: String,
        @Query("units") units: String = "metric"
    ): Response<WeatherResponse>

    @GET("forecast")
    suspend fun getForecast(
        @Query("q") cityName: String,
        @Query("units") units: String = "metric",
        @Query("cnt") count: Int = 40
    ): Response<ForecastResponse>
}

data class WeatherResponse(
    val main: MainData,
    val weather: List<WeatherData>,
    val wind: WindData,
    val name: String
)

data class ForecastResponse(
    val list: List<ForecastItem>,
    val city: ForecastCity
)

data class ForecastItem(
    @SerializedName("dt_txt")
    val dtTxt: String,
    val main: MainData,
    val weather: List<WeatherData>
)

data class ForecastCity(val name: String)

data class MainData(
    val temp: Double,
    @SerializedName("feels_like")
    val feelsLike: Double,
    val humidity: Int
)

data class WeatherData(
    val main: String,
    val icon: String
)

data class WindData(
    val speed: Double
)