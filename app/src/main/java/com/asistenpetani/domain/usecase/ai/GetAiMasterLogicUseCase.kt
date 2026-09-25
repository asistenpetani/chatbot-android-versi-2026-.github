package com.asistenpetani.domain.usecase.ai

import android.graphics.Bitmap
import com.asistenpetani.data.remote.AnalisaGambarResponse
import com.asistenpetani.data.remote.CloudRunRetrofitClient
import com.asistenpetani.util.ImageCompressor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Response
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GetAiMasterLogicUseCase @Inject constructor(
    private val enrichAiKnowledgeUseCase: EnrichAiKnowledgeUseCase
) {

    /**
     * Menjalankan analisis multimodal (gambar + teks) yang diperkaya dengan
     * seluruh pengetahuan dari Use Case aplikasi (Profil, Lahan, Pasar, SOP Pakar)
     * serta Cloud RAG di sisi server.
     */
    suspend fun analisaGambarMultimodal(
        pesan: String,
        bitmap: Bitmap,
        baseUrl: String
    ): Result<String> = withContext(Dispatchers.IO) {
        var compressedFile: File? = null
        try {
            // 1. Ekstraksi seluruh pengetahuan Use Case secara menyeluruh
            val aggregatedContext = enrichAiKnowledgeUseCase()

            // 2. Gabungkan catatan pengguna dengan konteks Use Case terpadu
            val pesanLengkap = buildString {
                appendLine(pesan)
                appendLine()
                appendLine(aggregatedContext.rawFormattedContext)
            }

            // 3. Kompres gambar < 200KB (Hemat Bandwidth & RAM)
            compressedFile = ImageCompressor.kompresGambarKeBawah200KB(bitmap)

            // 4. Siapkan Request Body (Multipart)
            val requestFile = compressedFile.asRequestBody("image/jpeg".toMediaTypeOrNull())
            val body = MultipartBody.Part.createFormData("file", compressedFile.name, requestFile)
            val pesanBody = pesanLengkap.toRequestBody("text/plain".toMediaTypeOrNull())

            // 5. Panggil API Cloud Run
            val apiResponse: Response<AnalisaGambarResponse> = CloudRunRetrofitClient
                .getService(baseUrl)
                .analisaGambarAman(pesanBody, body)

            if (apiResponse.isSuccessful) {
                val jawaban = apiResponse.body()?.jawaban ?: "Tidak ada balasan dari AI."
                Result.success(jawaban)
            } else {
                Result.failure(Exception("Kesalahan server (Kode: ${apiResponse.code()})"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        } finally {
            // 6. Hapus file cache sementara
            compressedFile?.let {
                if (it.exists()) it.delete()
            }
        }
    }

    /**
     * Memperoleh seluruh data pengetahuan use case terkini untuk keperluan prompt lokal / Gemini SDK.
     */
    suspend fun getEnrichedKnowledge(cropType: String? = null): AggregatedFarmingKnowledge {
        return enrichAiKnowledgeUseCase(cropType)
    }
}
