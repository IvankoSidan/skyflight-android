package com.wheezy.skyflight.core.common.di

import com.wheezy.skyflight.core.common.manager.FCMTokenManager
import com.wheezy.skyflight.core.common.manager.FCMTokenManagerImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class FCMTokenManagerModule {

    @Suppress("unused")
    @Binds
    @Singleton
    abstract fun bindFCMTokenManager(
        fcmTokenManagerImpl: FCMTokenManagerImpl
    ): FCMTokenManager
}