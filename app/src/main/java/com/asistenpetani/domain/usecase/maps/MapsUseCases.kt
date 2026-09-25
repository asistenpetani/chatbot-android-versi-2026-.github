package com.asistenpetani.domain.usecase.maps

import com.asistenpetani.domain.model.BuyerInfo
import com.asistenpetani.domain.model.StoreInfo
import com.asistenpetani.domain.repository.MapsRepository

class GetNearbyStoresUseCase(private val mapsRepo: MapsRepository) {
    suspend operator fun invoke(location: String): List<StoreInfo> {
        return mapsRepo.getStores(location)
    }
}

class GetBuyerLocationUseCase(private val mapsRepo: MapsRepository) {
    suspend operator fun invoke(location: String): List<BuyerInfo> {
        return mapsRepo.getNearbyBuyers(location)
    }
}
