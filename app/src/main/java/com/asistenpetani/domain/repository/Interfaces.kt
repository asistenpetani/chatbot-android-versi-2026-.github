package com.asistenpetani.domain.repository

import com.asistenpetani.data.local.room.FarmerProfile
import com.asistenpetani.domain.model.*

interface AuthRepository {
    suspend fun getCurrentUser(): FarmerProfile?
    suspend fun getFarmerProfile(uid: String): FarmerProfile?
    suspend fun saveProfile(name: String, cropType: String, landArea: String, lahanId: String, location: String)
    fun getDeviceRecognitionInfo(): com.asistenpetani.domain.model.DeviceInfo
}

interface LahanRepository {
    suspend fun checkLahanStatus(lahanId: String): LahanStatus
}

interface MarketRepository {
    suspend fun getLatestPrices(): LatestPrices
}

interface MapsRepository {
    suspend fun getStores(location: String): List<StoreInfo>
    suspend fun getNearbyBuyers(location: String): List<BuyerInfo>
}

interface ExpertRepository {
    suspend fun getExpertData(cropType: String): ExpertKnowledge
    suspend fun getWikipediaData(query: String): String
    suspend fun askGemini(systemPrompt: String, userInput: String): String
    suspend fun askGemini(systemPrompt: String, userInput: String, imageBase64: String?, mimeType: String?): String
}

interface FarmingRepository {
    suspend fun getLandRecommendation(cropType: String): FarmingRecommendation
    suspend fun calculateHarvest(landArea: Double, cropType: String): String
}
