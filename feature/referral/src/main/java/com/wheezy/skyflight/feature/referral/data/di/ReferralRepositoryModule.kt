package com.wheezy.skyflight.feature.referral.data.di

import com.wheezy.skyflight.feature.referral.data.repository.ReferralRepositoryImpl
import com.wheezy.skyflight.feature.referral.domain.repository.ReferralRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class ReferralRepositoryModule {

    @Binds
    @Singleton
    abstract fun bindReferralRepository(
        impl: ReferralRepositoryImpl
    ): ReferralRepository
}