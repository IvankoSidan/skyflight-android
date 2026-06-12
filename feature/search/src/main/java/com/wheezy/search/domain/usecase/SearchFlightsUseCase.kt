package com.wheezy.skyflight.feature.search.domain.usecase

import com.wheezy.skyflight.core.model.FlightModel
import com.wheezy.skyflight.feature.search.domain.repository.SearchRepository
import javax.inject.Inject

class SearchFlightsUseCase @Inject constructor(
    private val repository: SearchRepository
) {
    suspend operator fun invoke(
        from: String,
        to: String,
        classType: String?
    ): Result<List<FlightModel>> {
        return repository.searchFlights(from, to, classType)
    }
}