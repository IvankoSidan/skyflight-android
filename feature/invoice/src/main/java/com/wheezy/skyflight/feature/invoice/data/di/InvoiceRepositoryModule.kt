package com.wheezy.skyflight.feature.invoice.data.di

import com.wheezy.skyflight.feature.invoice.data.repository.InvoiceRepositoryImpl
import com.wheezy.skyflight.feature.invoice.domain.repository.InvoiceRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class InvoiceRepositoryModule {

    @Binds
    @Singleton
    abstract fun bindInvoiceRepository(
        impl: InvoiceRepositoryImpl
    ): InvoiceRepository
}