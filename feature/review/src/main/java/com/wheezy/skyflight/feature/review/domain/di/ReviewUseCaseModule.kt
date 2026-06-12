package com.wheezy.skyflight.feature.review.domain.di

import com.wheezy.skyflight.core.network.api.ApiService
import com.wheezy.skyflight.feature.review.domain.repository.ReviewRepository
import com.wheezy.skyflight.feature.review.domain.usecase.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ReviewUseCaseModule {

    @Provides
    @Singleton
    fun provideCreateReviewUseCase(repository: ReviewRepository): CreateReviewUseCase =
        CreateReviewUseCase(repository)

    @Provides
    @Singleton
    fun provideUpdateReviewUseCase(repository: ReviewRepository): UpdateReviewUseCase =
        UpdateReviewUseCase(repository)

    @Provides
    @Singleton
    fun provideDeleteReviewUseCase(repository: ReviewRepository): DeleteReviewUseCase =
        DeleteReviewUseCase(repository)

    @Provides
    @Singleton
    fun provideGetFlightReviewsUseCase(repository: ReviewRepository): GetFlightReviewsUseCase =
        GetFlightReviewsUseCase(repository)

    @Provides
    @Singleton
    fun provideGetAirlineRatingUseCase(repository: ReviewRepository): GetAirlineRatingUseCase =
        GetAirlineRatingUseCase(repository)

    @Provides
    @Singleton
    fun provideGetMyReviewsUseCase(repository: ReviewRepository): GetMyReviewsUseCase =
        GetMyReviewsUseCase(repository)

    @Provides
    @Singleton
    fun provideCanReviewUseCase(repository: ReviewRepository): CanReviewUseCase =
        CanReviewUseCase(repository)

    @Provides
    @Singleton
    fun provideGetFlightByIdUseCase(apiService: ApiService): GetFlightByIdUseCase =
        GetFlightByIdUseCase(apiService)

    @Provides
    @Singleton
    fun provideGetFlightReviewsPaginatedUseCase(repository: ReviewRepository): GetFlightReviewsPaginatedUseCase =
        GetFlightReviewsPaginatedUseCase(repository)
}