package com.wheezy.skyflight.feature.booking.domain.usecase

import com.wheezy.skyflight.core.network.api.ReviewApiService
import javax.inject.Inject

class CheckCanReviewUseCase @Inject constructor(
    private val reviewApiService: ReviewApiService
) {
    suspend operator fun invoke(bookingId: Long): Boolean {
        return try {
            val response = reviewApiService.canReview(bookingId)
            response.isSuccessful && response.body()?.canReview == true
        } catch (e: Exception) {
            false
        }
    }
}