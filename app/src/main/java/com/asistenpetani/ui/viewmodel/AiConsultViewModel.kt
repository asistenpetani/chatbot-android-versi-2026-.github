package com.asistenpetani.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.asistenpetani.data.remote.ChatBackendRequest
import com.asistenpetani.data.remote.RetrofitClient
import com.asistenpetani.domain.usecase.ai.EnrichAiKnowledgeUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class AiConsultState {
    object Idle : AiConsultState()
    object Loading : AiConsultState()
    data class Success(val reply: String) : AiConsultState()
    data class Error(val message: String) : AiConsultState()
}

@HiltViewModel
class AiConsultViewModel @Inject constructor(
    private val enrichAiKnowledgeUseCase: EnrichAiKnowledgeUseCase
) : ViewModel() {

    private val _aiState = MutableStateFlow<AiConsultState>(AiConsultState.Idle)
    val aiState: StateFlow<AiConsultState> = _aiState.asStateFlow()

    private val history = mutableListOf<Map<String, Any>>()

    fun consultAi(message: String) {
        viewModelScope.launch {
            _aiState.value = AiConsultState.Loading
            try {
                // Ekstraksi seluruh pengetahuan dari Use Case (Profil, Lahan, Pasar, SOP)
                val enrichedData = enrichAiKnowledgeUseCase()

                val richMessage = if (history.isEmpty()) {
                    "$message\n\n${enrichedData.rawFormattedContext}"
                } else {
                    message
                }

                val request = ChatBackendRequest(message = richMessage, history = history)
                val response = RetrofitClient.service.chatWithBackend("Bearer DUMMY_TOKEN", request)
                
                if (response.reply != null) {
                    _aiState.value = AiConsultState.Success(response.reply)
                    history.add(mapOf("role" to "user", "parts" to listOf(mapOf("text" to message))))
                    history.add(mapOf("role" to "model", "parts" to listOf(mapOf("text" to response.reply))))
                } else {
                    _aiState.value = AiConsultState.Error("Respon tidak valid.")
                }
            } catch (e: Exception) {
                _aiState.value = AiConsultState.Error(e.message ?: "Terjadi kesalahan")
            }
        }
    }
}
