package com.asistenpetani.ui.viewmodel

import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.asistenpetani.domain.usecase.ai.GetAiMasterLogicUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * STATE (KONDISI UI): Mewakili status dari proses AI untuk interaksi pengguna akhir yang nyaman.
 */
sealed class AiRecommendationState {
    object Idle : AiRecommendationState() // Belum ada interaksi
    object Loading : AiRecommendationState() // Sedang memproses (tampilkan animasi loading, -_-)
    data class Success(val jawaban: String) : AiRecommendationState() // Berhasil (*0*)
    data class Error(val message: String, val canRetry: Boolean = true) : AiRecommendationState() // Gagal, dengan opsi repair/reload
}

@HiltViewModel
class AiRecommendationViewModel @Inject constructor(
    private val getAiMasterLogicUseCase: GetAiMasterLogicUseCase
) : ViewModel() {

    private val _aiState = MutableStateFlow<AiRecommendationState>(AiRecommendationState.Idle)
    val aiState: StateFlow<AiRecommendationState> = _aiState.asStateFlow()

    // Variabel untuk menyimpan konteks request terakhir (untuk fitur Retry/Reload)
    private var lastPesan: String? = null
    private var lastBitmap: Bitmap? = null
    private var lastBaseUrl: String? = null

    // Fungsi untuk mengirim pesan Multimodal (Gambar + Teks)
    fun analisaGambar(pesan: String, bitmap: Bitmap, baseUrl: String) {
        // Simpan state terakhir untuk fitur reload
        lastPesan = pesan
        lastBitmap = bitmap
        lastBaseUrl = baseUrl

        eksekusiAnalisaGambar(pesan, bitmap, baseUrl)
    }

    // Fungsi internal untuk menjalankan proses
    private fun eksekusiAnalisaGambar(pesan: String, bitmap: Bitmap, baseUrl: String) {
        viewModelScope.launch {
            _aiState.value = AiRecommendationState.Loading
            
            // Pendelegasian ke UseCase agar ViewModel tetap bersih (Clean Architecture)
            val result = getAiMasterLogicUseCase.analisaGambarMultimodal(pesan, bitmap, baseUrl)
            
            result.onSuccess { jawaban ->
                _aiState.value = AiRecommendationState.Success(jawaban)
            }.onFailure { exception ->
                _aiState.value = AiRecommendationState.Error(
                    message = exception.message ?: "Gagal mengirim data. Periksa koneksi internet Anda.",
                    canRetry = true
                )
            }
        }
    }

    // Fitur Retry / Reload jika terjadi kesalahan (Error State)
    fun retryLastAction() {
        val pesan = lastPesan
        val bitmap = lastBitmap
        val baseUrl = lastBaseUrl

        if (pesan != null && bitmap != null && baseUrl != null) {
            eksekusiAnalisaGambar(pesan, bitmap, baseUrl)
        } else {
            _aiState.value = AiRecommendationState.Error("Tidak ada data untuk diulang.", false)
        }
    }
    
    // Reset state ke Idle saat pengguna menutup percakapan atau membuat percakapan baru
    fun resetState() {
        _aiState.value = AiRecommendationState.Idle
        lastPesan = null
        lastBitmap = null
        lastBaseUrl = null
    }
}
