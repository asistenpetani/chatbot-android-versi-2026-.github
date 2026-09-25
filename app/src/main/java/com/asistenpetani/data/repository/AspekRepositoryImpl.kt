package com.asistenpetani.data.repository

import com.asistenpetani.domain.model.DataResult
import com.asistenpetani.domain.repository.AspekRepository
import com.asistenpetani.di.WorkloadCredentials

/**
 * Berisi implementasi aman dari AspekRepository interface.
 * Menggunakan WorkloadCredentials hasil DI Hilt/Koin dan AiAnalysisRepositoryImpl untuk komunikasi aman.
 */
class AspekRepositoryImpl(
    private val appRepository: AiAnalysisRepositoryImpl,
    private val workloadCredentials: WorkloadCredentials?
) : AspekRepository {

    override suspend fun getAgriculturalData(query: String): DataResult {
        // Log status kebenaran credentials OIDC dari Single Source of Truth
        if (workloadCredentials != null) {
            android.util.Log.d("AspekRepo", "Autentikasi OIDC Workload Identity terdeteksi aktif untuk: ${workloadCredentials.audience}")
        } else {
            android.util.Log.w("AspekRepo", "Peringatan: Kredensial OIDC Workload Identity tidak terkonfigurasi di res/raw")
        }

        val profile = appRepository.getProfileSync()
        
        return try {
            // Panggil asisten cerdas Gemini via AppRepository
            val responseText = appRepository.askGemini(query, profile)
            DataResult(
                originalQuery = query,
                aiResponse = responseText,
                status = "SUCCESS"
            )
        } catch (e: Exception) {
            android.util.Log.e("AspekRepo", "Kegagalan pemicu data GCP/Gemini: ${e.message}")
            // Fallback anggun untuk menghindari crash
            DataResult(
                originalQuery = query,
                aiResponse = "Tabe' pak, koneksi asisten cloud sedang bermasalah. Anda tetap dapat menggunakan fitur offline dan e-RDKK standar.",
                status = "OFFLINE_FALLBACK"
            )
        }
    }
}
