package com.wheezy.skyflight.core.datastore.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStoreFile
import androidx.security.crypto.EncryptedFile
import androidx.security.crypto.MasterKey
import com.wheezy.skyflight.core.datastore.preferences.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import java.io.File
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

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class SearchDataStore

@Module
@InstallIn(SingletonComponent::class)
object DataStoreModule {

    private fun getEncryptedFile(context: Context, fileName: String): File {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()

        EncryptedFile.Builder(
            context,
            File(context.filesDir, fileName),
            masterKey,
            EncryptedFile.FileEncryptionScheme.AES256_GCM_HKDF_4KB
        ).build()

        return File(context.filesDir, fileName)
    }

    @Provides
    @Singleton
    @AuthDataStore
    fun provideAuthDataStore(@ApplicationContext appContext: Context): DataStore<Preferences> {
        val encryptedFile = getEncryptedFile(appContext, "auth_prefs")
        return PreferenceDataStoreFactory.create(
            corruptionHandler = null,
            migrations = listOf(),
            scope = CoroutineScope(Dispatchers.IO + SupervisorJob()),
            produceFile = { encryptedFile }
        )
    }

    @Provides
    @Singleton
    @FCMDataStore
    fun provideFCMDataStore(@ApplicationContext appContext: Context): DataStore<Preferences> {
        val encryptedFile = getEncryptedFile(appContext, "fcm_prefs")
        return PreferenceDataStoreFactory.create(
            corruptionHandler = null,
            migrations = listOf(),
            scope = CoroutineScope(Dispatchers.IO + SupervisorJob()),
            produceFile = { encryptedFile }
        )
    }

    @Provides
    @Singleton
    @WebSocketDataStore
    fun provideWebSocketDataStore(@ApplicationContext appContext: Context): DataStore<Preferences> {
        val encryptedFile = getEncryptedFile(appContext, "websocket_prefs")
        return PreferenceDataStoreFactory.create(
            corruptionHandler = null,
            migrations = listOf(),
            scope = CoroutineScope(Dispatchers.IO + SupervisorJob()),
            produceFile = { encryptedFile }
        )
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
    @SearchDataStore
    fun provideSearchDataStore(@ApplicationContext appContext: Context): DataStore<Preferences> {
        return PreferenceDataStoreFactory.create(
            produceFile = { appContext.preferencesDataStoreFile("search_prefs") }
        )
    }

    // ========== PROVIDE PREFERENCES ==========

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
    fun provideAuthPreferences(@AuthDataStore dataStore: DataStore<Preferences>): AuthPreferences {
        return AuthPreferences(dataStore)
    }

    @Provides
    @Singleton
    fun provideThemePreferences(@ThemeDataStore dataStore: DataStore<Preferences>): ThemePreferences {
        return ThemePreferences(dataStore)
    }

    @Provides
    @Singleton
    fun provideSearchPreferences(@SearchDataStore dataStore: DataStore<Preferences>): SearchPreferences {
        return SearchPreferences(dataStore)
    }
}