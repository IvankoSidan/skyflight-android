package com.wheezy.skyflight.feature.booking.domain.usecase

import com.wheezy.skyflight.core.model.WeatherModel
import com.wheezy.skyflight.feature.booking.domain.repository.WeatherRepository
import javax.inject.Inject

class GetCurrentWeatherUseCase @Inject constructor(
    private val repository: WeatherRepository
) {
    suspend operator fun invoke(cityName: String): Result<WeatherModel> {
        return repository.getCurrentWeather(cityName)
    }
}