package com.wheezy.skyflight.feature.notifications.data.di

import com.wheezy.skyflight.feature.notifications.data.repository.NotificationSettingsRepositoryImpl
import com.wheezy.skyflight.feature.notifications.domain.repository.NotificationSettingsRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class NotificationSettingsRepositoryModule {

    @Binds
    @Singleton
    abstract fun bindNotificationSettingsRepository(
        impl: NotificationSettingsRepositoryImpl
    ): NotificationSettingsRepository
}