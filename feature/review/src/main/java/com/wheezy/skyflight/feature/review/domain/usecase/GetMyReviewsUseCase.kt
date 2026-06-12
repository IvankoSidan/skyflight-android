package com.wheezy.skyflight.feature.review.domain.usecase

import com.wheezy.skyflight.core.model.Review
import com.wheezy.skyflight.feature.review.domain.repository.ReviewRepository
import javax.inject.Inject

class GetMyReviewsUseCase @Inject constructor(
    private val repository: ReviewRepository
) {
    suspend operator fun invoke(): Result<List<Review>> {
        return repository.getMyReviews()
    }
}