package com.wheezy.skyflight.feature.booking.domain.repository

import com.wheezy.skyflight.core.model.WeatherForecast
import com.wheezy.skyflight.core.model.WeatherModel

interface WeatherRepository {
    suspend fun getCurrentWeather(cityName: String): Result<WeatherModel>
    suspend fun getForecast(cityName: String, days: Int): Result<List<WeatherForecast>>
}