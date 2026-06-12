package com.wheezy.skyflight.feature.review.domain.usecase

import com.wheezy.skyflight.core.model.AirlineRating
import com.wheezy.skyflight.feature.review.domain.repository.ReviewRepository
import javax.inject.Inject

class GetAirlineRatingUseCase @Inject constructor(
    private val repository: ReviewRepository
) {
    suspend operator fun invoke(airlineName: String): Result<AirlineRating> {
        return repository.getAirlineRating(airlineName)
    }
}