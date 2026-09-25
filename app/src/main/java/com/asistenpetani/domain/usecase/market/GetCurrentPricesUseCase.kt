package com.asistenpetani.domain.usecase.market

import com.asistenpetani.domain.model.LatestPrices
import com.asistenpetani.domain.repository.MarketRepository

class GetCurrentPricesUseCase(private val marketRepo: MarketRepository) {
    suspend operator fun invoke(): LatestPrices {
        return marketRepo.getLatestPrices()
    }
}
