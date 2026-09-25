package com.asistenpetani.domain.usecase.ai

import com.asistenpetani.domain.repository.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Data Model konteks pengetahuan terintegrasi dari seluruh Use Case aplikasi.
 */
data class AggregatedFarmingKnowledge(
    val farmerName: String,
    val cropType: String,
    val landArea: String,
    val location: String,
    val landId: String,
    val isLandVerified: Boolean,
    val landRecommendation: String,
    val harvestProjection: String,
    val latestHarvestPrice: String,
    val latestFertilizerPrice: String,
    val nearestBuyerInfo: String,
    val expertGuide: String,
    val rawFormattedContext: String
)

@Singleton
class EnrichAiKnowledgeUseCase @Inject constructor(
    private val authRepo: AuthRepository,
    private val farmingRepo: FarmingRepository,
    private val marketRepo: MarketRepository,
    private val lahanRepo: LahanRepository,
    private val expertRepo: ExpertRepository
) {
    /**
     * Mengambil dan menggabungkan seluruh data Use Case aplikasi secara asynchronous
     * untuk memperkaya pengetahuan AI sebelum eksekusi fungsi suspending.
     */
    suspend operator fun invoke(customCropType: String? = null): AggregatedFarmingKnowledge = withContext(Dispatchers.IO) {
        // 1. Data Use Case Autentikasi & Profil Petani
        val profile = try {
            authRepo.getCurrentUser()
        } catch (e: Exception) {
            null
        }

        val activeCrop = customCropType ?: profile?.cropType ?: "Kakao Sultra"
        val activeLandArea = profile?.landArea?.toDoubleOrNull() ?: 1.5
        val activeLahanId = profile?.lahanId ?: "LHN-KOLAKA-001"

        // 2. Data Use Case Lahan & Lokasi
        val lahanStatus = try {
            lahanRepo.checkLahanStatus(activeLahanId)
        } catch (e: Exception) {
            null
        }

        // 3. Data Use Case Rekomendasi Budidaya & Estimasi Panen
        val farmingRec = try {
            farmingRepo.getLandRecommendation(activeCrop)
        } catch (e: Exception) {
            null
        }

        val harvestCalc = try {
            farmingRepo.calculateHarvest(activeLandArea, activeCrop)
        } catch (e: Exception) {
            "Estimasi panen: ${activeLandArea * 1.5} Ton per musim."
        }

        // 4. Data Use Case Pasar & Harga Komoditas
        val marketPrices = try {
            marketRepo.getLatestPrices()
        } catch (e: Exception) {
            null
        }

        // 5. Data Use Case Pengetahuan Pakar Agronomi
        val expertKnowledge = try {
            expertRepo.getExpertData(activeCrop)
        } catch (e: Exception) {
            null
        }

        val formattedContext = buildString {
            appendLine("=== KONTEKS LENGKAP PENGGUNA & LAHAN (ASPEK USE-CASE AGGREGATION) ===")
            appendLine("• Nama Petani: ${profile?.name ?: "Petani Binaan"}")
            appendLine("• Komoditas Utama: $activeCrop")
            appendLine("• Luas Lahan: ${profile?.landArea ?: "1.5"} Ha")
            appendLine("• Lokasi / Wilayah: ${profile?.location ?: "Kabupaten Kolaka, Sulawesi Tenggara"}")
            appendLine("• ID Lahan: $activeLahanId (Status Terverifikasi: ${lahanStatus?.isVerified ?: true})")
            if (lahanStatus != null) {
                appendLine("• Pembeli/Gudang Terdekat: ${lahanStatus.nearestBuyerName} (${lahanStatus.nearestBuyerPrice})")
            }
            if (marketPrices != null) {
                appendLine("• Harga Pasar Terkini: ${marketPrices.harvestPrice} | Pupuk: ${marketPrices.fertilizerPrice}")
            }
            if (farmingRec != null) {
                appendLine("• Rekomendasi Lahan: ${farmingRec.landSuitability}")
                appendLine("• Dosis Baku Pupuk: ${farmingRec.standardDosage}")
            }
            appendLine("• Kalkulasi Panen: $harvestCalc")
            if (expertKnowledge != null) {
                appendLine("• Panduan Standar SOP: ${expertKnowledge.standardGuide}")
                appendLine("• Hama Rentan & Solusi: ${expertKnowledge.commonPests} -> ${expertKnowledge.solution}")
            }
            appendLine("====================================================================")
        }

        AggregatedFarmingKnowledge(
            farmerName = profile?.name ?: "Petani Binaan",
            cropType = activeCrop,
            landArea = profile?.landArea ?: "1.5",
            location = profile?.location ?: "Kabupaten Kolaka, Sulawesi Tenggara",
            landId = activeLahanId,
            isLandVerified = lahanStatus?.isVerified ?: true,
            landRecommendation = farmingRec?.landSuitability ?: "Lahan subur tropis.",
            harvestProjection = harvestCalc,
            latestHarvestPrice = marketPrices?.harvestPrice ?: "Rp 122.000 / Kg",
            latestFertilizerPrice = marketPrices?.fertilizerPrice ?: "Rp 2.250 / Kg (HET)",
            nearestBuyerInfo = lahanStatus?.nearestBuyerName ?: "Gudang Komoditas Terdekat",
            expertGuide = expertKnowledge?.standardGuide ?: "SOP Tanaman Tropis",
            rawFormattedContext = formattedContext
        )
    }
}
