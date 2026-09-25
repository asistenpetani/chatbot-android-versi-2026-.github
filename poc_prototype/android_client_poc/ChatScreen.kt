package com.asistenpetani.poc

import androidx.compose.runtime.*
import kotlinx.coroutines.launch

// --- Data Model ---
data class PermintaanKonsultasi(val pesan_petani: String)
data class ResponKonsultasi(val jawaban_ai: String)

// --- Pseudo-API Client ---
interface BackendApi {
    // Fungsi POST ke FastAPI Cloud Run (Hanya menerima Token, tidak pakai Service Account)
    suspend fun kirimPesan(token: String, request: PermintaanKonsultasi): ResponKonsultasi
}

// --- Komponen UI (Jetpack Compose) ---
@Composable
fun ChatScreenPOC(
    backendApi: BackendApi,
    getFirebaseIdToken: suspend () -> String // Token sesi user dari Firebase Auth
) {
    var input by remember { mutableStateOf("") }
    var jawaban by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()

    // UI Sederhana
    Column {
        TextField(
            value = input,
            onValueChange = { input = it },
            label = { Text("Tanya AI Asisten Petani") }
        )
        
        Button(onClick = {
            scope.launch {
                try {
                    // 1. Ambil Token Sesi yang valid dari Firebase Auth
                    val token = getFirebaseIdToken()
                    
                    // 2. Kirim permintaan ke server FastAPI membawa Token
                    val respon = backendApi.kirimPesan(
                        token = "Bearer $token",
                        request = PermintaanKonsultasi(pesan_petani = input)
                    )
                    
                    jawaban = respon.jawaban_ai
                } catch (e: Exception) {
                    jawaban = "Gagal menghubungi server."
                }
            }
        }) {
            Text("Kirim Pertanyaan")
        }

        Text("Jawaban AI: $jawaban")
    }
}
