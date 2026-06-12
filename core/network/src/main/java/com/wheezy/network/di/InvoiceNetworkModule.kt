package com.wheezy.skyflight.core.network.di

import com.wheezy.skyflight.core.network.api.InvoiceApiService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object InvoiceNetworkModule {

    @Provides
    @Singleton
    fun provideInvoiceApiService(@Named("api") retrofit: Retrofit): InvoiceApiService {
        return retrofit.create(InvoiceApiService::class.java)
    }
}