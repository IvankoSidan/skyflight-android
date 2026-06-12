package com.wheezy.skyflight.feature.search.domain.usecase

import com.wheezy.skyflight.feature.search.domain.repository.SearchRepository
import javax.inject.Inject

class GetClassSeatsUseCase @Inject constructor(
    private val repository: SearchRepository
) {
    suspend operator fun invoke(): Result<List<String>> {
        return repository.getClassSeats()
    }
}