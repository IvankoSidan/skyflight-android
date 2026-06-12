package com.wheezy.skyflight.feature.loyalty.data.di

import com.wheezy.skyflight.feature.loyalty.data.repository.LoyaltyRepositoryImpl
import com.wheezy.skyflight.feature.loyalty.domain.repository.LoyaltyRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class LoyaltyRepositoryModule {

    @Binds
    @Singleton
    abstract fun bindLoyaltyRepository(
        impl: LoyaltyRepositoryImpl
    ): LoyaltyRepository
}