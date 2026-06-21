package com.wheezy.skyflight.feature.review.domain.usecase

import com.wheezy.skyflight.feature.review.domain.model.ReviewsPageData
import com.wheezy.skyflight.feature.review.domain.repository.ReviewRepository
import javax.inject.Inject

class GetAirlineReviewsPaginatedUseCase @Inject constructor(
    private val repository: ReviewRepository
) {
    suspend operator fun invoke(airlineName: String, page: Int, size: Int): Result<ReviewsPageData> {
        return repository.getAirlineReviewsPaginated(airlineName, page, size)
    }
}