package com.asistenpetani.domain.model

data class LahanStatus(
    val isVerified: Boolean,
    val landId: String,
    val coordinates: String,
    val ownerName: String,
    val areaHa: Double,
    val cropType: String,
    val nearestBuyerName: String,
    val nearestBuyerPrice: String,
    val nearestBuyerContact: String,
    val nearestBuyerPhone: String,
    val nearestBuyerAddress: String,
    val message: String
)

data class LatestPrices(
    val harvestPrice: String,
    val fertilizerPrice: String,
    val seedPrice: String
)

data class StoreInfo(
    val name: String,
    val address: String,
    val distance: String,
    val phone: String = "-"
)

data class BuyerInfo(
    val name: String,
    val address: String,
    val contact: String,
    val priceRange: String
)

data class ExpertKnowledge(
    val cropType: String,
    val standardGuide: String,
    val commonPests: String,
    val solution: String
)

data class FarmingRecommendation(
    val landSuitability: String,
    val standardDosage: String,
    val projectedYield: String
)

data class UserMember(
    val nama: String,
    val tanaman: String,
    val luasLahan: Double,
    val village: String,
    val subDistrict: String,
    val commodity: String
)

data class WeatherAlert(
    val title: String,
    val description: String,
    val isWarning: Boolean,
    val iconName: String,
    val temp: String,
    val humidity: String,
    val advice: String
)

