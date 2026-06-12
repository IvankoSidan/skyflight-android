package com.wheezy.skyflight.feature.booking.data.di

import com.wheezy.skyflight.feature.booking.data.repository.BookingRepositoryImpl
import com.wheezy.skyflight.feature.booking.data.repository.FlightRepositoryImpl
import com.wheezy.skyflight.feature.booking.data.repository.PaymentRepositoryImpl
import com.wheezy.skyflight.feature.booking.domain.repository.BookingRepository
import com.wheezy.skyflight.feature.booking.domain.repository.FlightRepository
import com.wheezy.skyflight.feature.booking.domain.repository.PaymentRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class BookingRepositoryModule {

    @Binds
    @Singleton
    abstract fun bindBookingRepository(
        impl: BookingRepositoryImpl
    ): BookingRepository

    @Binds
    @Singleton
    abstract fun bindFlightRepository(
        impl: FlightRepositoryImpl
    ): FlightRepository

    @Binds
    @Singleton
    abstract fun bindPaymentRepository(
        impl: PaymentRepositoryImpl
    ): PaymentRepository
}