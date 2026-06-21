package com.wheezy.skyflight.feature.loyalty.domain.di

import com.wheezy.skyflight.feature.loyalty.data.di.LoyaltyRepositoryModule
import com.wheezy.skyflight.feature.loyalty.domain.repository.LoyaltyRepository
import com.wheezy.skyflight.feature.loyalty.domain.usecase.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module(
    includes = [LoyaltyRepositoryModule::class]
)
@InstallIn(SingletonComponent::class)
object LoyaltyUseCaseModule {

    @Provides
    @Singleton
    fun provideGetPointsBalanceUseCase(repository: LoyaltyRepository): GetPointsBalanceUseCase =
        GetPointsBalanceUseCase(repository)

    @Provides
    @Singleton
    fun provideGetPointsTransactionsUseCase(repository: LoyaltyRepository): GetPointsTransactionsUseCase =
        GetPointsTransactionsUseCase(repository)

    @Provides
    @Singleton
    fun provideGetTiersUseCase(repository: LoyaltyRepository): GetTiersUseCase =
        GetTiersUseCase(repository)

    @Provides
    @Singleton
    fun provideCalculateDiscountUseCase(repository: LoyaltyRepository): CalculateDiscountUseCase =
        CalculateDiscountUseCase(repository)

    @Provides
    @Singleton
    fun provideRedeemPointsUseCase(repository: LoyaltyRepository): RedeemPointsUseCase =
        RedeemPointsUseCase(repository)
}