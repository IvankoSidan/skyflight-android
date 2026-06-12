package com.wheezy.skyflight.feature.cards.data.di

import com.wheezy.skyflight.feature.cards.data.repository.CardsRepositoryImpl
import com.wheezy.skyflight.feature.cards.domain.repository.CardsRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class CardsRepositoryModule {

    @Binds
    @Singleton
    abstract fun bindCardsRepository(
        impl: CardsRepositoryImpl
    ): CardsRepository
}