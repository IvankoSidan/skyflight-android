package com.wheezy.skyflight.feature.search.domain.usecase

import com.wheezy.skyflight.feature.search.domain.repository.SearchRepository
import javax.inject.Inject

class GetReservedSeatsUseCase @Inject constructor(
    private val repository: SearchRepository
) {
    suspend operator fun invoke(flightId: Long): Result<List<String>> {
        return repository.getReservedSeats(flightId)
    }
}