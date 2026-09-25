package com.asistenpetani.data.repository

import com.asistenpetani.data.remote.ChatRequest
import com.asistenpetani.data.remote.CloudRunApiService
import com.asistenpetani.domain.repository.BigDataRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class CloudRunBigDataRepositoryImpl @Inject constructor(
    private val apiService: CloudRunApiService
) : BigDataRepository {
    
    override suspend fun fetchData(query: String): String = withContext(Dispatchers.IO) {
        try {
            // Memanggil Endpoint Cloud Run yang berisi main.py Anda
            val request = ChatRequest(message = query)
            val response = apiService.sendChatMessage(request)
            
            // Mengembalikan jawaban dari AI
            response.response
        } catch (e: Exception) {
            "Maaf, Asisten Petani gagal terhubung ke satelit pusat: ${e.localizedMessage}"
        }
    }
}
