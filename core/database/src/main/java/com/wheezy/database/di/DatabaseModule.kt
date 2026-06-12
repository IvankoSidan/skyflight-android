package com.wheezy.skyflight.core.database.di

import android.content.Context
import com.wheezy.skyflight.core.database.AppDatabase
import com.wheezy.skyflight.core.database.dao.FlightDao
import com.wheezy.skyflight.core.database.dao.OfflineBookingDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return AppDatabase.getInstance(context)
    }

    @Provides
    @Singleton
    fun provideFlightDao(database: AppDatabase): FlightDao {
        return database.flightDao()
    }

    @Provides
    @Singleton
    fun provideOfflineBookingDao(database: AppDatabase): OfflineBookingDao {
        return database.offlineBookingDao()
    }
}