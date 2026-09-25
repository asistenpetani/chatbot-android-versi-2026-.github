package com.asistenpetani.data.repository

import com.asistenpetani.domain.repository.BigDataRepository
import kotlinx.coroutines.delay

class MockBigDataRepositoryImpl : BigDataRepository {
    override suspend fun fetchData(query: String): String {
        // Simulasi loading dari server Cloud Run
        delay(1500)
        
        return """
            [DATA MOCKING - OFFLINE MODE]
            Analisis Komoditas untuk: $query
            
            Harga rata-rata saat ini stabil.
            Cuaca diprediksi cerah untuk 3 hari ke depan, optimal untuk penanaman.
            Kadar Nitrogen tanah di area Anda cenderung rendah, disarankan penambahan pupuk NPK.
            
            (Catatan: Ini adalah data simulasi karena REST API sedang dalam mode hemat biaya)
        """.trimIndent()
    }
}
