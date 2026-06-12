package com.wheezy.skyflight.feature.invoice.domain.di

import com.wheezy.skyflight.feature.invoice.domain.repository.InvoiceRepository
import com.wheezy.skyflight.feature.invoice.domain.usecase.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object InvoiceUseCaseModule {

    @Provides
    @Singleton
    fun provideGetInvoiceByBookingIdUseCase(repository: InvoiceRepository): GetInvoiceByBookingIdUseCase =
        GetInvoiceByBookingIdUseCase(repository)

    @Provides
    @Singleton
    fun provideGetMyInvoicesUseCase(repository: InvoiceRepository): GetMyInvoicesUseCase =
        GetMyInvoicesUseCase(repository)

    @Provides
    @Singleton
    fun provideDownloadInvoiceUseCase(repository: InvoiceRepository): DownloadInvoiceUseCase =
        DownloadInvoiceUseCase(repository)

    @Provides
    @Singleton
    fun provideResendInvoiceEmailUseCase(repository: InvoiceRepository): ResendInvoiceEmailUseCase =
        ResendInvoiceEmailUseCase(repository)

    @Provides
    @Singleton
    fun provideGetTaxRatesUseCase(repository: InvoiceRepository): GetTaxRatesUseCase =
        GetTaxRatesUseCase(repository)
}