package com.wheezy.skyflight.core.database.dao

import androidx.room.*
import com.wheezy.skyflight.core.database.entity.FlightEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FlightDao {

    @Query("SELECT * FROM cached_flights WHERE searchQueryKey = :queryKey ORDER BY cachedAt DESC")
    fun getFlightsByQuery(queryKey: String): Flow<List<FlightEntity>>

    @Query("SELECT * FROM cached_flights WHERE searchQueryKey = :queryKey ORDER BY cachedAt DESC")
    suspend fun getFlightsByQuerySuspend(queryKey: String): List<FlightEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFlights(flights: List<FlightEntity>)

    @Query("DELETE FROM cached_flights WHERE cachedAt < :olderThan")
    suspend fun deleteOldFlights(olderThan: Long)

    @Query("SELECT COUNT(*) FROM cached_flights WHERE cachedAt < :olderThan")
    suspend fun getOldFlightsCount(olderThan: Long): Int

    @Query("DELETE FROM cached_flights WHERE searchQueryKey = :queryKey")
    suspend fun deleteFlightsByQuery(queryKey: String)

    @Query("DELETE FROM cached_flights")
    suspend fun clearAll()
}