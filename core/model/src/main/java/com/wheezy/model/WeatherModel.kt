package com.wheezy.skyflight.core.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class WeatherModel(
    val temperature: Double,
    val feelsLike: Double,
    val humidity: Int,
    val windSpeed: Double,
    val condition: String,
    val iconCode: String,
    val cityName: String,
    val timestamp: Long = System.currentTimeMillis()
) : Parcelable {
    val iconUrl: String get() = "https://openweathermap.org/img/wn/${iconCode}@2x.png"
    val temperatureFormatted: String get() = "${temperature.toInt()}°C"
    val feelsLikeFormatted: String get() = "Feels like ${feelsLike.toInt()}°C"
    val isStale: Boolean get() = System.currentTimeMillis() - timestamp > 3600000
}

@Parcelize
data class WeatherForecast(
    val date: String,
    val maxTemp: Double,
    val minTemp: Double,
    val condition: String,
    val iconCode: String
) : Parcelable {
    val maxTempFormatted: String get() = "${maxTemp.toInt()}°C"
    val minTempFormatted: String get() = "${minTemp.toInt()}°C"
    val formattedDate: String get() = date.substring(5)
}