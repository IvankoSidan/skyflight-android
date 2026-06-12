package com.wheezy.skyflight.feature.invoice.domain.usecase

import com.wheezy.skyflight.core.model.TaxRate
import com.wheezy.skyflight.feature.invoice.domain.repository.InvoiceRepository
import javax.inject.Inject

class GetTaxRatesUseCase @Inject constructor(
    private val repository: InvoiceRepository
) {
    suspend operator fun invoke(): Result<List<TaxRate>> {
        return repository.getTaxRates()
    }
}