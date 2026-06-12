package com.wheezy.skyflight.feature.search.domain.usecase

import com.wheezy.skyflight.feature.search.data.local.SearchLocalDataSource
import com.wheezy.skyflight.feature.search.domain.model.SearchParams
import javax.inject.Inject

class SaveSearchParamsUseCase @Inject constructor(
    private val localDataSource: SearchLocalDataSource
) {
    suspend operator fun invoke(params: SearchParams) {
        localDataSource.saveSearchParams(params)
    }
}