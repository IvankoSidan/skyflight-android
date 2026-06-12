package com.wheezy.skyflight.core.network.di

import com.wheezy.skyflight.core.network.api.ReviewApiService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ReviewNetworkModule {

    @Provides
    @Singleton
    fun provideReviewApiService(@Named("api") retrofit: Retrofit): ReviewApiService {
        return retrofit.create(ReviewApiService::class.java)
    }
}