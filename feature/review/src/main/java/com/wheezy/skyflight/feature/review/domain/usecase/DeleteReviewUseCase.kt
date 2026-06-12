package com.wheezy.skyflight.feature.review.domain.usecase

import com.wheezy.skyflight.feature.review.domain.repository.ReviewRepository
import javax.inject.Inject

class DeleteReviewUseCase @Inject constructor(
    private val repository: ReviewRepository
) {
    suspend operator fun invoke(reviewId: Long): Result<Unit> {
        return repository.deleteReview(reviewId)
    }
}