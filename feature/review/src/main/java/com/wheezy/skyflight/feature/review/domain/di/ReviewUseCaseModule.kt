package com.wheezy.skyflight.feature.review.domain.di

import com.wheezy.skyflight.feature.review.data.di.ReviewRepositoryModule
import com.wheezy.skyflight.feature.review.domain.repository.ReviewRepository
import com.wheezy.skyflight.feature.review.domain.usecase.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module(
    includes = [ReviewRepositoryModule::class]
)
@InstallIn(SingletonComponent::class)
object ReviewUseCaseModule {

    @Provides
    @Singleton
    fun provideCreateReviewUseCase(repo: ReviewRepository): CreateReviewUseCase =
        CreateReviewUseCase(repo)

    @Provides
    @Singleton
    fun provideUpdateReviewUseCase(repo: ReviewRepository): UpdateReviewUseCase =
        UpdateReviewUseCase(repo)

    @Provides
    @Singleton
    fun provideDeleteReviewUseCase(repo: ReviewRepository): DeleteReviewUseCase =
        DeleteReviewUseCase(repo)

    @Provides
    @Singleton
    fun provideGetFlightReviewsUseCase(repo: ReviewRepository): GetFlightReviewsUseCase =
        GetFlightReviewsUseCase(repo)

    @Provides
    @Singleton
    fun provideGetAirlineRatingUseCase(repo: ReviewRepository): GetAirlineRatingUseCase =
        GetAirlineRatingUseCase(repo)

    @Provides
    @Singleton
    fun provideGetMyReviewsUseCase(repo: ReviewRepository): GetMyReviewsUseCase =
        GetMyReviewsUseCase(repo)

    @Provides
    @Singleton
    fun provideCanReviewUseCase(repo: ReviewRepository): CanReviewUseCase =
        CanReviewUseCase(repo)

    @Provides
    @Singleton
    fun provideGetFlightReviewsPaginatedUseCase(repo: ReviewRepository): GetFlightReviewsPaginatedUseCase =
        GetFlightReviewsPaginatedUseCase(repo)

    // Убираем getAirlineReviewsPaginatedUseCase, так как он не используется
}