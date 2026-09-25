package com.asistenpetani.domain.usecase.farming

import com.asistenpetani.domain.model.FarmingRecommendation
import com.asistenpetani.domain.repository.FarmingRepository

class GetLandRecommendationUseCase(private val farmingRepo: FarmingRepository) {
    suspend operator fun invoke(cropType: String): FarmingRecommendation {
        return farmingRepo.getLandRecommendation(cropType)
    }
}

class CalculateHarvestUseCase(private val farmingRepo: FarmingRepository) {
    suspend operator fun invoke(landArea: Double, cropType: String): String {
        return farmingRepo.calculateHarvest(landArea, cropType)
    }
}
