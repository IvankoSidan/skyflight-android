package com.wheezy.skyflight.core.common.di

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.wheezy.skyflight.core.common.manager.WebSocketManager
import com.wheezy.skyflight.core.common.manager.WebSocketManagerImpl
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class WebSocketModule {

    @Suppress("unused")
    @Binds
    @Singleton
    abstract fun bindWebSocketManager(
        impl: WebSocketManagerImpl
    ): WebSocketManager

    companion object {
        @Provides
        @Singleton
        fun provideGson(): Gson = GsonBuilder().create()
    }
}