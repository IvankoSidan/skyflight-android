package com.wheezy.skyflight.core.network.di

import com.wheezy.skyflight.core.network.api.CardsApiService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object CardsNetworkModule {

    @Provides
    @Singleton
    fun provideCardsApiService(@Named("api") retrofit: Retrofit): CardsApiService {
        return retrofit.create(CardsApiService::class.java)
    }
}