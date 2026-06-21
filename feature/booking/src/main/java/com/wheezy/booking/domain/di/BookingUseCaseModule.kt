package com.wheezy.skyflight.feature.booking.domain.di

import com.wheezy.skyflight.core.network.api.ReviewApiService
import com.wheezy.skyflight.feature.booking.data.di.BookingRepositoryModule
import com.wheezy.skyflight.feature.booking.data.di.WeatherRepositoryModule
import com.wheezy.skyflight.feature.booking.domain.repository.BookingRepository
import com.wheezy.skyflight.feature.booking.domain.repository.FlightRepository
import com.wheezy.skyflight.feature.booking.domain.repository.PaymentRepository
import com.wheezy.skyflight.feature.booking.domain.usecase.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module(
    includes = [BookingRepositoryModule::class, WeatherRepositoryModule::class]
)
@InstallIn(SingletonComponent::class)
object BookingUseCaseModule {

    @Provides
    @Singleton
    fun provideGetMyBookingsUseCase(repository: BookingRepository): GetMyBookingsUseCase =
        GetMyBookingsUseCase(repository)

    @Provides
    @Singleton
    fun provideGetBookingByIdUseCase(repository: BookingRepository): GetBookingByIdUseCase =
        GetBookingByIdUseCase(repository)

    @Provides
    @Singleton
    fun provideCreateBookingUseCase(flightRepository: FlightRepository): CreateBookingUseCase =
        CreateBookingUseCase(flightRepository)

    @Provides
    @Singleton
    fun provideCancelOrDeleteBookingUseCase(repository: BookingRepository): CancelOrDeleteBookingUseCase =
        CancelOrDeleteBookingUseCase(repository)

    @Provides
    @Singleton
    fun provideUpdateBookingStatusUseCase(repository: BookingRepository): UpdateBookingStatusUseCase =
        UpdateBookingStatusUseCase(repository)

    @Provides
    @Singleton
    fun provideProcessPaymentUseCase(paymentRepository: PaymentRepository): ProcessPaymentUseCase =
        ProcessPaymentUseCase(paymentRepository)

    @Provides
    @Singleton
    fun provideGetReservedSeatsUseCase(flightRepository: FlightRepository): GetReservedSeatsUseCase =
        GetReservedSeatsUseCase(flightRepository)

    @Provides
    @Singleton
    fun provideGenerateSeatMapUseCase(): GenerateSeatMapUseCase =
        GenerateSeatMapUseCase()

    @Provides
    @Singleton
    fun provideSelectSeatUseCase(): SelectSeatUseCase =
        SelectSeatUseCase()

    @Provides
    @Singleton
    fun provideCheckCanReviewUseCase(reviewApiService: ReviewApiService): CheckCanReviewUseCase =
        CheckCanReviewUseCase(reviewApiService)
}