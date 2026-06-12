package com.wheezy.skyflight.feature.booking.domain.di

import com.wheezy.skyflight.feature.booking.domain.repository.WeatherRepository
import com.wheezy.skyflight.feature.booking.domain.usecase.GetCurrentWeatherUseCase
import com.wheezy.skyflight.feature.booking.domain.usecase.GetWeatherForecastUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object WeatherUseCaseModule {

    @Provides
    @Singleton
    fun provideGetCurrentWeatherUseCase(
        repository: WeatherRepository
    ): GetCurrentWeatherUseCase = GetCurrentWeatherUseCase(repository)

    @Provides
    @Singleton
    fun provideGetWeatherForecastUseCase(
        repository: WeatherRepository
    ): GetWeatherForecastUseCase = GetWeatherForecastUseCase(repository)
}