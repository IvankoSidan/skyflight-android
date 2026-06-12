package com.wheezy.skyflight.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "offline_bookings")
data class OfflineBookingEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val flightId: Long,
    val seatNumbers: String,
    val passengerName: String?,
    val passengerEmail: String?,
    val status: String,
    val createdAt: Long = System.currentTimeMillis(),
    var retryCount: Int = 0,
    var lastError: String? = null
)