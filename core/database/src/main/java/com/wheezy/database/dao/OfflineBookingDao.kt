package com.wheezy.skyflight.core.database.dao

import androidx.room.*
import com.wheezy.skyflight.core.database.entity.OfflineBookingEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface OfflineBookingDao {

    @Query("SELECT * FROM offline_bookings WHERE status = 'PENDING_SYNC'")
    fun getPendingSyncBookings(): Flow<List<OfflineBookingEntity>>

    @Query("SELECT * FROM offline_bookings WHERE status = 'PENDING_SYNC'")
    suspend fun getPendingSyncBookingsSuspend(): List<OfflineBookingEntity>

    @Query("SELECT * FROM offline_bookings WHERE status = 'SYNCED' AND createdAt < :olderThan")
    suspend fun getOldSyncedBookings(olderThan: Long): List<OfflineBookingEntity>

    @Insert
    suspend fun insertBooking(booking: OfflineBookingEntity): Long

    @Update
    suspend fun updateBooking(booking: OfflineBookingEntity)

    @Delete
    suspend fun deleteBooking(booking: OfflineBookingEntity)

    @Query("DELETE FROM offline_bookings WHERE id = :bookingId")
    suspend fun deleteBookingById(bookingId: Long)

    @Query("DELETE FROM offline_bookings WHERE status = 'SYNCED' AND createdAt < :olderThan")
    suspend fun deleteSyncedBookings(olderThan: Long)

    @Query("SELECT COUNT(*) FROM offline_bookings WHERE status = 'PENDING_SYNC'")
    suspend fun getPendingCount(): Int
}