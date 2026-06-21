package com.wheezy.skyflight.feature.booking.data.repository

import android.util.Log
import com.wheezy.skyflight.core.common.cache.DataCache
import com.wheezy.skyflight.core.model.WeatherForecast
import com.wheezy.skyflight.core.model.WeatherModel
import com.wheezy.skyflight.core.network.api.WeatherApiService
import com.wheezy.skyflight.feature.booking.domain.repository.WeatherRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WeatherRepositoryImpl @Inject constructor(
    private val weatherApiService: WeatherApiService
) : WeatherRepository {

    companion object {
        private const val TAG = "WeatherRepository"
    }

    private val weatherCache = DataCache<String, WeatherModel>()
    private val forecastCache = DataCache<String, List<WeatherForecast>>()

    override suspend fun getCurrentWeather(cityName: String): Result<WeatherModel> {
        val cacheKey = "weather_$cityName"
        weatherCache.get(cacheKey)?.let { return Result.success(it) }

        return try {
            val response = weatherApiService.getCurrentWeather(cityName)
            if (response.isSuccessful && response.body() != null) {
                val data = response.body()!!
                val weather = WeatherModel(
                    temperature = data.main.temp,
                    feelsLike = data.main.feels_like,
                    humidity = data.main.humidity,
                    windSpeed = data.wind.speed,
                    condition = data.weather.firstOrNull()?.main ?: "Unknown",
                    iconCode = data.weather.firstOrNull()?.icon ?: "01d",
                    cityName = data.name
                )
                weatherCache.put(cacheKey, weather, 3600)
                Result.success(weather)
            } else {
                Result.failure(Exception(response.message()))
            }
        } catch (e: Exception) {
            Log.e(TAG, "getCurrentWeather error", e)
            Result.failure(e)
        }
    }

    override suspend fun getForecast(cityName: String, days: Int): Result<List<WeatherForecast>> {
        val cacheKey = "forecast_$cityName"
        forecastCache.get(cacheKey)?.let { return Result.success(it.take(days)) }

        return try {
            val response = weatherApiService.getForecast(cityName)
            if (response.isSuccessful && response.body() != null) {
                val data = response.body()!!
                val forecasts = data.list.groupBy { it.dt_txt.substring(0, 10) }
                    .mapNotNull { (date, items) ->
                        val maxTemp = items.maxOfOrNull { it.main.temp } ?: return@mapNotNull null
                        val minTemp = items.minOfOrNull { it.main.temp } ?: return@mapNotNull null
                        val weather = items.firstOrNull()?.weather?.firstOrNull()
                        WeatherForecast(
                            date = date,
                            maxTemp = maxTemp,
                            minTemp = minTemp,
                            condition = weather?.main ?: "Unknown",
                            iconCode = weather?.icon ?: "01d"
                        )
                    }
                forecastCache.put(cacheKey, forecasts, 7200)
                Result.success(forecasts.take(days))
            } else {
                Result.failure(Exception(response.message()))
            }
        } catch (e: Exception) {
            Log.e(TAG, "getForecast error", e)
            Result.failure(e)
        }
    }
}