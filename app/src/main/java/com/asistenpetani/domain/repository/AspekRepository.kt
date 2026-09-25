package com.asistenpetani.domain.repository

import com.asistenpetani.domain.model.DataResult

/**
 * Interface Repository utama untuk mengabstraksi komunikasi dengan AI
 * dan basis data eksternal/GCP.
 */
interface AspekRepository {
    suspend fun getAgriculturalData(query: String): DataResult
}
