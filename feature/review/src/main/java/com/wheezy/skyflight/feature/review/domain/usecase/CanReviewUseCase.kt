package com.wheezy.skyflight.feature.review.domain.usecase

import com.wheezy.skyflight.feature.review.domain.repository.ReviewRepository
import javax.inject.Inject

class CanReviewUseCase @Inject constructor(
    private val repository: ReviewRepository
) {
    suspend operator fun invoke(bookingId: Long): Result<Boolean> {
        return repository.canReview(bookingId)
    }
}