package com.wheezy.skyflight.feature.search.domain.di

import com.wheezy.skyflight.feature.search.domain.repository.SearchRepository
import com.wheezy.skyflight.feature.search.domain.usecase.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object SearchUseCaseModule {

    @Provides
    @Singleton
    fun provideGetLocationsUseCase(
        repository: SearchRepository
    ): GetLocationsUseCase = GetLocationsUseCase(repository)

    @Provides
    @Singleton
    fun provideGetClassSeatsUseCase(
        repository: SearchRepository
    ): GetClassSeatsUseCase = GetClassSeatsUseCase(repository)

    @Provides
    @Singleton
    fun provideSearchFlightsUseCase(
        repository: SearchRepository
    ): SearchFlightsUseCase = SearchFlightsUseCase(repository)

    @Provides
    @Singleton
    fun provideSelectFlightUseCase(
        repository: SearchRepository
    ): SelectFlightUseCase = SelectFlightUseCase(repository)

    @Provides
    @Singleton
    fun provideGetReservedSeatsUseCase(
        repository: SearchRepository
    ): GetReservedSeatsUseCase = GetReservedSeatsUseCase(repository)
}