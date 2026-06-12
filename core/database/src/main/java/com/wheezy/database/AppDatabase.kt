package com.wheezy.skyflight.core.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.wheezy.skyflight.core.database.converter.Converters
import com.wheezy.skyflight.core.database.dao.FlightDao
import com.wheezy.skyflight.core.database.dao.OfflineBookingDao
import com.wheezy.skyflight.core.database.entity.FlightEntity
import com.wheezy.skyflight.core.database.entity.OfflineBookingEntity

@Database(
    entities = [FlightEntity::class, OfflineBookingEntity::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun flightDao(): FlightDao
    abstract fun offlineBookingDao(): OfflineBookingDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "skyflight_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}