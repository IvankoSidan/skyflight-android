package com.wheezy.skyflight.core.network.di

import com.wheezy.skyflight.core.network.api.LoyaltyApiService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object LoyaltyNetworkModule {

    @Provides
    @Singleton
    fun provideLoyaltyApiService(@Named("api") retrofit: Retrofit): LoyaltyApiService {
        return retrofit.create(LoyaltyApiService::class.java)
    }
}