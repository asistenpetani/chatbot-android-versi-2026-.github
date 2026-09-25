package com.asistenpetani.domain.model

/**
 * Representasi hasil data pertanian dari Gemini AI / database lokal
 * untuk memberikan data terstruktur pada UI.
 */
data class DataResult(
    val originalQuery: String,
    val aiResponse: String,
    val timestamp: Long = System.currentTimeMillis(),
    val status: String = "SUCCESS"
)
