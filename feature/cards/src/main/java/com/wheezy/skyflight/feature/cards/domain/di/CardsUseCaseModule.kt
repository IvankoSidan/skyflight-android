package com.wheezy.skyflight.feature.cards.domain.di

import com.wheezy.skyflight.feature.cards.data.di.CardsRepositoryModule
import com.wheezy.skyflight.feature.cards.domain.repository.CardsRepository
import com.wheezy.skyflight.feature.cards.domain.usecase.DeleteCardUseCase
import com.wheezy.skyflight.feature.cards.domain.usecase.GetSavedCardsUseCase
import com.wheezy.skyflight.feature.cards.domain.usecase.SetDefaultCardUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module(
    includes = [CardsRepositoryModule::class]
)
@InstallIn(SingletonComponent::class)
object CardsUseCaseModule {

    @Provides
    @Singleton
    fun provideGetSavedCardsUseCase(repository: CardsRepository): GetSavedCardsUseCase =
        GetSavedCardsUseCase(repository)

    @Provides
    @Singleton
    fun provideDeleteCardUseCase(repository: CardsRepository): DeleteCardUseCase =
        DeleteCardUseCase(repository)

    @Provides
    @Singleton
    fun provideSetDefaultCardUseCase(repository: CardsRepository): SetDefaultCardUseCase =
        SetDefaultCardUseCase(repository)
}