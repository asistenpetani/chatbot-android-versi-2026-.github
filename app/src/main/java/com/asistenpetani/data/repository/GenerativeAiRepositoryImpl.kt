package com.asistenpetani.data.repository

import com.asistenpetani.BuildConfig
import com.asistenpetani.data.remote.Content
import com.asistenpetani.data.remote.GenerateContentRequest
import com.asistenpetani.data.remote.ChatBackendRequest
import com.asistenpetani.data.remote.AnalyzeBackendRequest
import com.asistenpetani.data.remote.Part
import com.asistenpetani.data.remote.RetrofitClient
import com.asistenpetani.data.remote.InlineData
import com.asistenpetani.data.remote.GenerationConfig
import com.asistenpetani.data.remote.ThinkingConfig
import com.asistenpetani.data.remote.ThinkingLevel
import com.asistenpetani.domain.repository.GenerativeAiRepository
import java.io.IOException
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.tasks.await

class GenerativeAiRepositoryImpl(private val chatDao: com.asistenpetani.data.local.room.DiagnosaDao? = null) : GenerativeAiRepository {

    override suspend fun generateResponse(
        systemPrompt: String,
        userInput: String,
        imageBase64: String?,
        mimeType: String?,
        thinkingLevel: ThinkingLevel?,
        historyList: List<com.asistenpetani.data.local.room.ChatMessage>
    ): String {
        val user = FirebaseAuth.getInstance().currentUser
        val token = user?.getIdToken(false)?.await()?.token
        
        if (token == null) {
            return "Anda harus login untuk menggunakan fitur AI."
        }
        
        val authHeader = "Bearer $token"
        
        return try {
            val imageBytes = imageBase64?.let { 
                android.util.Base64.decode(it, android.util.Base64.DEFAULT) 
            }
            val firebaseResponse = FirebaseAiHelper.generateWithFirebaseAi(
                prompt = userInput,
                systemInstruction = systemPrompt,
                modelName = if (thinkingLevel == ThinkingLevel.HIGH) "gemini-3.1-pro-preview" else "gemini-3.5-flash",
                historyList = historyList,
                imageBytes = imageBytes,
                mimeType = mimeType
            )
            firebaseResponse ?: "Gagal mendapatkan respon dari Firebase AI SDK."
        } catch (e: Exception) {
            val allMessages = chatDao?.getChatMessagesSync() ?: emptyList()
            var foundOfflineAnswer: String? = null
            for (i in 0 until allMessages.size - 1) {
                if (allMessages[i].sender == "user" && allMessages[i+1].sender == "ai") {
                    val userWords = userInput.lowercase().split(" ")
                    val storedWords = allMessages[i].text.lowercase().split(" ")
                    
                    val commonWords = userWords.intersect(storedWords.toSet())
                    if (commonWords.size > 2) {
                        foundOfflineAnswer = allMessages[i+1].text
                        break
                    }
                }
            }
            
            if (foundOfflineAnswer != null) {
                "[OFFLINE MODE] " + foundOfflineAnswer
            } else {
                "Koneksi gagal atau kesalahan server: ${e.localizedMessage ?: "Tidak diketahui"}. Dan Anda sedang offline."
            }
        }
    }
}
