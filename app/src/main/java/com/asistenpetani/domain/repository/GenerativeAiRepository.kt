package com.asistenpetani.domain.repository

import com.asistenpetani.data.remote.ThinkingLevel

interface GenerativeAiRepository {
    /**
     * Mengirimkan prompt sistem dan input pengguna ke Gemini Model
     * @param systemPrompt Aturan baku dan konteks data terstruktur
     * @param userInput Pertanyaan atau teks dari petani
     * @param imageBase64 Gambar dalam format base64 (opsional)
     * @param mimeType MIME tipe gambar (opsional)
     * @param thinkingLevel Level berpikir tinggi (opsional)
     * @return Jawaban teks dari Gemini atau null jika terjadi error
     */
    suspend fun generateResponse(
        systemPrompt: String,
        userInput: String,
        imageBase64: String? = null,
        mimeType: String? = null,
        thinkingLevel: ThinkingLevel? = null,
        historyList: List<com.asistenpetani.data.local.room.ChatMessage> = emptyList()
    ): String
}
