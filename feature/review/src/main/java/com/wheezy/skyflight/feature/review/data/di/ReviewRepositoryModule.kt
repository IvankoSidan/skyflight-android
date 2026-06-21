package com.wheezy.skyflight.feature.review.data.di

import com.wheezy.skyflight.feature.review.data.repository.ReviewRepositoryImpl
import com.wheezy.skyflight.feature.review.domain.repository.ReviewRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class ReviewRepositoryModule {

    @Binds
    @Singleton
    abstract fun bindReviewRepository(impl: ReviewRepositoryImpl): ReviewRepository
}