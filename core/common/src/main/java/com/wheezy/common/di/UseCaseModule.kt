package com.wheezy.skyflight.core.common.di

import com.wheezy.skyflight.core.common.usecase.GetFlightByIdUseCase
import com.wheezy.skyflight.core.network.api.ApiService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object UseCaseModule {

    @Provides
    @Singleton
    fun provideGetFlightByIdUseCase(apiService: ApiService): GetFlightByIdUseCase {
        return GetFlightByIdUseCase(apiService)
    }
}