package com.wheezy.skyflight.feature.notifications.domain.di

import com.wheezy.skyflight.feature.notifications.domain.repository.NotificationSettingsRepository
import com.wheezy.skyflight.feature.notifications.domain.usecase.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NotificationSettingsUseCaseModule {

    @Provides
    @Singleton
    fun provideGetNotificationSettingsUseCase(
        repository: NotificationSettingsRepository
    ): GetNotificationSettingsUseCase = GetNotificationSettingsUseCase(repository)

    @Provides
    @Singleton
    fun provideUpdateNotificationSettingsUseCase(
        repository: NotificationSettingsRepository
    ): UpdateNotificationSettingsUseCase = UpdateNotificationSettingsUseCase(repository)

    @Provides
    @Singleton
    fun provideShouldSendNotificationUseCase(
        repository: NotificationSettingsRepository
    ): ShouldSendNotificationUseCase = ShouldSendNotificationUseCase(repository)

    @Provides
    @Singleton
    fun provideGetQuietHoursStatusUseCase(
        repository: NotificationSettingsRepository
    ): GetQuietHoursStatusUseCase = GetQuietHoursStatusUseCase(repository)
}