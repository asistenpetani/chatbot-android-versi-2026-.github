package com.asistenpetani.ui.features.maps

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.asistenpetani.domain.repository.AspekRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import dagger.hilt.android.lifecycle.HiltViewModel

data class LokasiGcp(val nama: String, val lat: Double, val lon: Double, val tipe: String, val jarakKm: Double? = null)

@HiltViewModel
class MapViewModel @Inject constructor(
    private val repository: AspekRepository
) : ViewModel() {

    private val _aiAnalysisResult = MutableStateFlow("Menunggu instruksi...")
    val aiAnalysisResult: StateFlow<String> = _aiAnalysisResult.asStateFlow()

    private val _lokasiSekunder = MutableStateFlow<List<LokasiGcp>>(emptyList())
    val lokasiSekunder: StateFlow<List<LokasiGcp>> = _lokasiSekunder.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    fun analisaKondisiLahan(luasLahan: String, jenisTanaman: String, lat: Double, lon: Double) {
        viewModelScope.launch {
            _isLoading.value = true
            _aiAnalysisResult.value = "Sedang menganalisa geografi dan cuaca di koordinat ($lat, $lon)..."
            
            try {
                // Simulasi memanggil AI dengan prompt Geografi (dapat diganti dengan panggil GeminiRepository)
                val cuaca = "Hujan Ringan"
                
                // Nanti bisa dipanggil ke Gemini API melalui Repository yang sudah ada
                val prompt = "Data Lahan: $luasLahan, Cuaca: $cuaca, Tanaman: $jenisTanaman di lokasi ($lat, $lon). Berikan rekomendasi pemupukan dan jadwal panen."
                
                // Simulasi delay AI
                kotlinx.coroutines.delay(2000)
                
                _aiAnalysisResult.value = "Analisis Selesai: Berdasarkan cuaca $cuaca di koordinat tersebut, tunda pemupukan $jenisTanaman hari ini. Pastikan saluran air (drainase) lancar agar akar tidak busuk. Lahan seluas $luasLahan terpantau cukup rawan genangan."
            } catch (e: Exception) {
                _aiAnalysisResult.value = "Gagal menganalisa: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun muatLokasiDariGcp(tipe: String, latUser: Double, lonUser: Double) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                // Simulasi fetching JSON dari GCP / Cloud Run FastAPI
                kotlinx.coroutines.delay(1000)
                
                val dummyData = listOf(
                    LokasiGcp("Kios Tani Subur", latUser + 0.01, lonUser + 0.01, "Kios", 1.2),
                    LokasiGcp("Toko Pertanian Makmur", latUser - 0.02, lonUser + 0.015, "Kios", 2.5),
                    LokasiGcp("Pengepul Bawang Pak Budi", latUser + 0.03, lonUser - 0.01, "Pembeli", 3.8)
                )
                
                _lokasiSekunder.value = dummyData.filter { it.tipe.equals(tipe, ignoreCase = true) }
            } catch (e: Exception) {
                // Tangani error
            } finally {
                _isLoading.value = false
            }
        }
    }
}
