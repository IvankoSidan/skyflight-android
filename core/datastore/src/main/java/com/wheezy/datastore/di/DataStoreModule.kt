package com.wheezy.skyflight.core.datastore.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStoreFile
import com.wheezy.skyflight.core.datastore.preferences.AuthPreferences
import com.wheezy.skyflight.core.datastore.preferences.FCMTokenPreferences
import com.wheezy.skyflight.core.datastore.preferences.ThemePreferences
import com.wheezy.skyflight.core.datastore.preferences.WebSocketPreferences
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Qualifier
import javax.inject.Singleton

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class AuthDataStore

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class ThemeDataStore

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class FCMDataStore

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class WebSocketDataStore

@Module
@InstallIn(SingletonComponent::class)
object DataStoreModule {

    @Provides
    @Singleton
    @AuthDataStore
    fun provideAuthDataStore(@ApplicationContext appContext: Context): DataStore<Preferences> {
        return PreferenceDataStoreFactory.create(
            produceFile = { appContext.preferencesDataStoreFile("auth_prefs") }
        )
    }

    @Provides
    @Singleton
    @FCMDataStore
    fun provideFCMDataStore(@ApplicationContext appContext: Context): DataStore<Preferences> {
        return PreferenceDataStoreFactory.create(
            produceFile = { appContext.preferencesDataStoreFile("fcm_prefs") }
        )
    }

    @Provides
    @Singleton
    @WebSocketDataStore
    fun provideWebSocketDataStore(@ApplicationContext appContext: Context): DataStore<Preferences> {
        return PreferenceDataStoreFactory.create(
            produceFile = { appContext.preferencesDataStoreFile("websocket_prefs") }
        )
    }

    @Provides
    @Singleton
    fun provideWebSocketPreferences(@WebSocketDataStore dataStore: DataStore<Preferences>): WebSocketPreferences {
        return WebSocketPreferences(dataStore)
    }

    @Provides
    @Singleton
    fun provideFCMTokenPreferences(@FCMDataStore dataStore: DataStore<Preferences>): FCMTokenPreferences {
        return FCMTokenPreferences(dataStore)
    }

    @Provides
    @Singleton
    @ThemeDataStore
    fun provideThemeDataStore(@ApplicationContext appContext: Context): DataStore<Preferences> {
        return PreferenceDataStoreFactory.create(
            produceFile = { appContext.preferencesDataStoreFile("theme_prefs") }
        )
    }

    @Provides
    @Singleton
    fun provideAuthPreferences(@AuthDataStore dataStore: DataStore<Preferences>): AuthPreferences {
        return AuthPreferences(dataStore)
    }

    @Provides
    @Singleton
    fun provideThemePreferences(@ThemeDataStore dataStore: DataStore<Preferences>): ThemePreferences {
        return ThemePreferences(dataStore)
    }
}