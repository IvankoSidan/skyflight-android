package com.wheezy.skyflight.feature.booking.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wheezy.skyflight.core.model.WeatherForecast
import com.wheezy.skyflight.core.model.WeatherModel
import com.wheezy.skyflight.feature.booking.domain.usecase.GetCurrentWeatherUseCase
import com.wheezy.skyflight.feature.booking.domain.usecase.GetWeatherForecastUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WeatherViewModel @Inject constructor(
    private val getCurrentWeatherUseCase: GetCurrentWeatherUseCase,
    private val getWeatherForecastUseCase: GetWeatherForecastUseCase
) : ViewModel() {

    sealed class WeatherUiState {
        object Loading : WeatherUiState()
        data class Success(
            val weather: WeatherModel,
            val forecast: List<WeatherForecast>? = null,
            val isCached: Boolean = false
        ) : WeatherUiState()
        data class Error(val message: String, val cachedData: WeatherModel? = null) : WeatherUiState()
    }

    private val _weatherState = MutableStateFlow<WeatherUiState>(WeatherUiState.Loading)
    val weatherState: StateFlow<WeatherUiState> = _weatherState.asStateFlow()

    private var lastLoadedCity: String? = null

    fun loadWeather(cityName: String, loadForecast: Boolean = true) {
        if (lastLoadedCity == cityName && _weatherState.value !is WeatherUiState.Loading) {
            return
        }
        lastLoadedCity = cityName

        viewModelScope.launch {
            _weatherState.value = WeatherUiState.Loading

            val currentResult = getCurrentWeatherUseCase(cityName)

            if (loadForecast) {
                val forecastResult = getWeatherForecastUseCase(cityName, 3)

                when {
                    currentResult.isSuccess -> {
                        val weather = currentResult.getOrThrow()
                        _weatherState.value = WeatherUiState.Success(
                            weather = weather,
                            forecast = forecastResult.getOrNull(),
                            isCached = weather.isStale
                        )
                    }
                    else -> {
                        _weatherState.value = WeatherUiState.Error(
                            message = currentResult.exceptionOrNull()?.message ?: "Failed to load weather",
                            cachedData = null
                        )
                    }
                }
            } else {
                when {
                    currentResult.isSuccess -> {
                        val weather = currentResult.getOrThrow()
                        _weatherState.value = WeatherUiState.Success(
                            weather = weather,
                            forecast = null,
                            isCached = weather.isStale
                        )
                    }
                    else -> {
                        _weatherState.value = WeatherUiState.Error(
                            message = currentResult.exceptionOrNull()?.message ?: "Failed to load weather"
                        )
                    }
                }
            }
        }
    }

    fun refresh(cityName: String) {
        lastLoadedCity = null
        loadWeather(cityName)
    }
}