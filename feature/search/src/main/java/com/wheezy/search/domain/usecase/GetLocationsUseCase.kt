package com.wheezy.skyflight.feature.search.domain.usecase

import com.wheezy.skyflight.core.model.LocationModel
import com.wheezy.skyflight.feature.search.domain.repository.SearchRepository
import javax.inject.Inject

class GetLocationsUseCase @Inject constructor(
    private val repository: SearchRepository
) {
    suspend operator fun invoke(): Result<List<LocationModel>> {
        return repository.getLocations()
    }
}