package com.wheezy.skyflight.feature.notifications.data.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import com.wheezy.skyflight.feature.notifications.data.local.NotificationSettingsLocalDataSource
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

private val Context.notificationSettingsDataStore by preferencesDataStore(
    name = "notification_settings"
)

@Module(
    includes = [NotificationSettingsRepositoryModule::class]
)
@InstallIn(SingletonComponent::class)
object NotificationSettingsModule {

    @Provides
    @Singleton
    fun provideNotificationSettingsDataStore(@ApplicationContext context: Context): DataStore<Preferences> {
        return context.notificationSettingsDataStore
    }

    @Provides
    @Singleton
    fun provideNotificationSettingsLocalDataSource(
        dataStore: DataStore<Preferences>
    ): NotificationSettingsLocalDataSource {
        return NotificationSettingsLocalDataSource(dataStore)
    }
}