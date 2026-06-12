package com.wheezy.skyflight.feature.booking.data.di

import com.wheezy.skyflight.feature.booking.data.repository.WeatherRepositoryImpl
import com.wheezy.skyflight.feature.booking.domain.repository.WeatherRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class WeatherRepositoryModule {

    @Binds
    @Singleton
    abstract fun bindWeatherRepository(
        impl: WeatherRepositoryImpl
    ): WeatherRepository
}