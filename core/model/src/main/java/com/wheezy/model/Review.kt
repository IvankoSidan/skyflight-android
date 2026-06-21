package com.wheezy.skyflight.core.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.parcelize.IgnoredOnParcel
import java.time.LocalDateTime
import java.util.Locale

@Parcelize
data class Review(
    val id: Long,
    val userId: Long,
    val userName: String?,
    val bookingId: Long,
    val flightId: Long,
    val airlineName: String,
    val rating: Int,
    val comment: String?,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime,
    val canEdit: Boolean = false
) : Parcelable {
    @IgnoredOnParcel
    val formattedRating: String
        get() = "$rating/5"

    @IgnoredOnParcel
    val isNew: Boolean
        get() = createdAt.isAfter(LocalDateTime.now().minusHours(24))
}

@Parcelize
data class AirlineRating(
    val airlineName: String,
    val averageRating: Double,
    val totalReviews: Int,
    val ratingDistribution: Map<Int, Int>
) : Parcelable {
    @IgnoredOnParcel
    val averageRatingFormatted: String
        get() = String.format(Locale.US, "%.1f", averageRating)

    @IgnoredOnParcel
    val starsCount: Int
        get() = averageRating.toInt()

    @IgnoredOnParcel
    val hasHalfStar: Boolean
        get() = averageRating - starsCount >= 0.5
}

data class CreateReviewRequest(
    val bookingId: Long,
    val rating: Int,
    val comment: String?
)

data class UpdateReviewRequest(
    val rating: Int,
    val comment: String?
)

data class CanReviewResponse(
    val canReview: Boolean
)