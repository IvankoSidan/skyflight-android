package com.wheezy.skyflight.feature.booking.domain.usecase

import com.wheezy.skyflight.core.model.WeatherForecast
import com.wheezy.skyflight.feature.booking.domain.repository.WeatherRepository
import javax.inject.Inject

class GetWeatherForecastUseCase @Inject constructor(
    private val repository: WeatherRepository
) {
    suspend operator fun invoke(cityName: String, days: Int = 3): Result<List<WeatherForecast>> {
        return repository.getForecast(cityName, days)
    }
}