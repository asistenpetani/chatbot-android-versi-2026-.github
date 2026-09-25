package com.asistenpetani.data.repository

import com.asistenpetani.BuildConfig
import com.asistenpetani.data.remote.*
import com.asistenpetani.data.local.room.DiagnosaDao
import com.asistenpetani.data.local.room.ChatMessage
import com.asistenpetani.data.local.room.FarmerProfile
import com.asistenpetani.data.local.room.PlantingSchedule
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class AiAnalysisRepositoryImpl(private val dao: DiagnosaDao) {

    val farmerProfile: Flow<FarmerProfile?> = dao.getProfile()
    val chatMessages: Flow<List<ChatMessage>> = dao.getChatMessages()
    suspend fun getChatMessagesSync(): List<ChatMessage> = dao.getChatMessagesSync()
    val plantingSchedules: Flow<List<PlantingSchedule>> = dao.getPlantingSchedules()

    /**
     * Memuat riwayat chat dari Cloud Firestore dengan LIMIT KETAT (Maksimal 10 pesan terbaru)
     * untuk mencegah lonjakan data transfer egress dan memastikan tetap berada di kuota gratis.
     */
    suspend fun syncChatHistoryFromCloud(limitCount: Long = 10) {
        try {
            val auth = FirebaseAuth.getInstance()
            val uid = auth.currentUser?.uid ?: return
            val db = FirebaseFirestore.getInstance()

            val snapshot = db.collection("chats")
                .whereEqualTo("userId", uid)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .limit(limitCount)
                .get()
                .awaitOrNull()

            if (snapshot != null && !snapshot.isEmpty) {
                val cloudMessages = snapshot.documents.mapNotNull { doc ->
                    val sender = doc.getString("sender") ?: return@mapNotNull null
                    val text = doc.getString("text") ?: return@mapNotNull null
                    val timestamp = doc.getLong("timestamp") ?: System.currentTimeMillis()
                    val sessionId = doc.getString("sessionId") ?: "default_session"
                    val category = doc.getString("category") ?: "Umum"
                    ChatMessage(
                        sender = sender,
                        text = text,
                        timestamp = timestamp,
                        sessionId = sessionId,
                        category = category
                    )
                }
                // Simpan ke Room DB lokal petani
                cloudMessages.reversed().forEach { msg ->
                    dao.insertChatMessage(msg)
                }
            }
        } catch (e: Exception) {
            android.util.Log.w("AiAnalysisRepository", "Sinkronisasi riwayat chat dibatasi / offline: ${e.message}")
        }
    }

    private suspend fun <T> com.google.android.gms.tasks.Task<T>.awaitOrNull(): T? {
        return kotlin.coroutines.suspendCoroutine { cont ->
            addOnSuccessListener { cont.resumeWith(Result.success(it)) }
            addOnFailureListener { cont.resumeWith(Result.success(null)) }
        }
    }

    suspend fun saveProfile(name: String, cropType: String, landArea: String) {
        val profile = FarmerProfile(
            name = name,
            cropType = cropType,
            landArea = landArea,
            isLoggedIn = true,
            location = "KOLAKA, SULTRA"
        )
        dao.insertProfile(profile)

        try {
            val auth = FirebaseAuth.getInstance()
            val db = FirebaseFirestore.getInstance()
            val uid = auth.currentUser?.uid
            if (uid != null) {
                val profileData = mapOf(
                    "name" to profile.name,
                    "cropType" to profile.cropType,
                    "landArea" to profile.landArea,
                    "location" to profile.location,
                    "userId" to uid
                )
                db.collection("users").document(uid).set(profileData)
            }
        } catch (e: Exception) {
            android.util.Log.e("AiAnalysisRepository", "Gagal sinkronisasi profil ke Firestore: ${e.message}")
        }
    }

    suspend fun getProfileSync(): FarmerProfile? {
        return dao.getProfileSync()
    }

    suspend fun clearProfile() {
        dao.deleteProfile()
        dao.clearChatMessages()
    }

    suspend fun saveMessage(sender: String, text: String, sessionId: String = "default_session") {
        val calculatedCategory = when {
            text.contains("pupuk", ignoreCase = true) || text.contains("urea", ignoreCase = true) || text.contains("za", ignoreCase = true) || text.contains("het", ignoreCase = true) || text.contains("subsidi", ignoreCase = true) -> "Pupuk"
            text.contains("cuaca", ignoreCase = true) || text.contains("hujan", ignoreCase = true) || text.contains("kemarau", ignoreCase = true) || text.contains("suhu", ignoreCase = true) || text.contains("angin", ignoreCase = true) -> "Cuaca"
            text.contains("hama", ignoreCase = true) || text.contains("penyakit", ignoreCase = true) || text.contains("daun", ignoreCase = true) || text.contains("ulat", ignoreCase = true) || text.contains("bakteri", ignoreCase = true) || text.contains("jamur", ignoreCase = true) || text.contains("klorosis", ignoreCase = true) || text.contains("bercak", ignoreCase = true) -> "Hama & Penyakit"
            text.contains("panen", ignoreCase = true) || text.contains("tanam", ignoreCase = true) || text.contains("bibit", ignoreCase = true) || text.contains("benih", ignoreCase = true) -> "Budidaya"
            else -> "Umum"
        }
        val chatMessage = ChatMessage(
            sender = sender,
            text = text,
            timestamp = System.currentTimeMillis(),
            sessionId = sessionId,
            category = calculatedCategory
        )
        dao.insertChatMessage(chatMessage)

        try {
            val auth = FirebaseAuth.getInstance()
            val db = FirebaseFirestore.getInstance()
            val uid = auth.currentUser?.uid
            if (uid != null) {
                val chatData = mapOf(
                    "sender" to chatMessage.sender,
                    "text" to chatMessage.text,
                    "timestamp" to chatMessage.timestamp,
                    "sessionId" to chatMessage.sessionId,
                    "category" to chatMessage.category,
                    "userId" to uid
                )
                val chatId = "${chatMessage.sessionId}_${chatMessage.timestamp}"
                db.collection("chats").document(chatId).set(chatData)
            }
        } catch (e: Exception) {
            android.util.Log.e("AiAnalysisRepository", "Gagal sinkronisasi chat ke Firestore: ${e.message}")
        }
    }

    suspend fun clearChat() {
        dao.clearChatMessages()
    }

    suspend fun deleteChatMessageByText(text: String) {
        dao.deleteChatMessageByText(text)
    }

    suspend fun deleteChatMessageBySessionId(sessionId: String) {
        dao.deleteChatMessageBySessionId(sessionId)
    }

    suspend fun savePlantingSchedule(schedule: PlantingSchedule) {
        dao.insertPlantingSchedule(schedule)
    }

    suspend fun deletePlantingSchedule(schedule: PlantingSchedule) {
        dao.deletePlantingSchedule(schedule)
    }

    suspend fun clearPlantingSchedules() {
        dao.clearPlantingSchedules()
    }

    suspend fun generateResponse(
        name: String,
        crop: String,
        area: String,
        location: String,
        userInput: String,
        imageBase64: String? = null,
        mimeType: String = "image/jpeg"
    ): String {
        return withContext(Dispatchers.IO) {
            try {
                // 1. PENGAMANAN KUNCI API
                val apiKey = listOf(
                    BuildConfig.GENERATIVE_LANGUAGE_API_KEY,
                    BuildConfig.Generative_Language_API_Key,
                    BuildConfig.GEMINI_API_KEY
                ).firstOrNull {
                    it.isNotEmpty() && 
                    it != "MY_GENERATIVE_LANGUAGE_API_KEY" && 
                    it != "MY_GENERATIVE_LANGUAGE_API_KEY_CASE" && 
                    it != "MY_GEMINI_API_KEY"
                } ?: throw Exception("API Key tidak ditemukan!")

                // 2. MENDORONG HIPER-LOKAL (SYSTEM INSTRUCTION)
                val systemPrompt = """
                    INSTRUKSI kecerdasan buatan dalam PERCAKAPAN:
                    Anda adalah ahli agronomi di bidang pertanian, perkebunan, dan perikanan tambak. 
                    Anda adalah Asisten Petani Kita (ASPEK), asisten digital cerdas, ramah, dan solutif untuk petani.
                    
                    Gunakan nada bahasa percakapan sehari-hari. Sesuai lokasi petani login ($location). 
                    Jangan pakai istilah teknis yang berat. Terdengarlah seperti teman yang sedang ngobrol.
                    
                    Sapaan khas bahasa daerah berdasarkan lokasi ($location):
                    - SUMATERA (Aceh): "Assalamu’alaikum, Pane neuwoe?". (Batak): "Horas!". (Minang): "Salamaik pagi".
                    - JAWA: "Sugeng enjing/enjang, piye kabare Panjenengan?".
                    - SUNDA: "Sampurasun! Punten. Kumaha damang/sae?".
                    - BALI: "Om Swastiastu. Rahajeng semeng. Matur suksma".
                    - KALIMANTAN: "Salamat baisukan. Narai kabar? Tabe".
                    - SULAWESI (Bugis/Makassar/Kolaka): "Tabe', Apa kareba? Baji-baji ji?". Hindari Iko/Iyyo ke orang tua, gunakan Iyye'.
                    
                    ATURAN PENYARINGAN TOPIK MUTLAK dengan empati :
                    Jika bertanya tentang topik di luar tAnaman atau perikanan, WAJIB berempati dan memberikan kembali motivasi atau percakapan secara sopan ke budi daya tanaman atau budiya perikanan atau sarankan beristirahat untuk makan atau ngopi atau tidur dan lanjutkan lagi nanti pekerjaannya setelahnya.    
                    Gunakan data Profil petani saat ini untuk memberikan jawaban SANGAT PRESISI:
                    - Nama: $name
                    - Tanaman Utama: $crop
                    - Luas Lahan: $area Hektar
                    - Lokasi saat ini: $location
                    
                    Jika petani menanyakan pupuk bersubsidi, ingatkan ai alur pengetahuan use case kareena didalamnya ada banyak pengetahuan termasuk  i-Pubers (KTP, e-RDKK, dll).
                    Referensi Harga HET per Zak 50kg: Urea (Rp90.000), NPK Phonska (Rp92.000).
                """.trimIndent()

                // 3. MENYUSUN REQUEST UNTUK RETROFIT
                val systemInstruction = SystemInstruction(
                    parts = listOf(Part(text = systemPrompt))
                )

                // 4. DUKUNGAN MULTIMODAL (TEKS + GAMBAR)
                val userParts = mutableListOf<Part>()
                userParts.add(Part(text = userInput))
                
                if (!imageBase64.isNullOrEmpty()) {
                    userParts.add(Part(inlineData = InlineData(mimeType = mimeType, data = imageBase64)))
                }

                val userContent = Content(parts = userParts)

                val requestBody = GeminiRequest(
                    systemInstruction = systemInstruction,
                    contents = listOf(userContent)
                )

                // 5. EKSEKUSI KE GEMINI MELALUI RETROFIT
                val response = RetrofitClient.service.generateContent(
                    model = "gemini-3.5-flash", 
                    apiKey = apiKey,
                    request = requestBody
                )

                // 6. MEMPROSES HASIL DENGAN LOGIKA YANG LEBIH AMAN
                if (response.isSuccessful) {
                    val body = response.body()
                    if (body != null) {
                        val aiText = body.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                        if (aiText != null) {
                            aiText
                        } else {
                            "Respon diterima, tapi isinya kosong. Mungkin pertanyaan diblokir oleh sistem keamanan Google."
                        }
                    } else {
                        "Sistem gagal membaca data JSON dari server."
                    }
                } else {
                    val errorDetail = response.errorBody()?.string() ?: "Kesalahan tidak diketahui"
                    "Maaf, gagal menghubungi kecerdasan buatan. Kode Error: ${response.code()} - $errorDetail"
                }
            } catch (e: Exception) {
                if (e is IOException) {
                    "Sinyal sedang kurang stabil di lahan, mari coba sekali lagi dalam beberapa detik, Akang/Teteh."
                } else {
                    "Pesan Gagal Terkirim, periksa koneksi internet Anda. Detail: ${e.localizedMessage}"
                }
            }
        }
    }

    suspend fun askGemini(prompt: String, farmerProfile: FarmerProfile?): String {
        return askGemini(prompt, null, null, farmerProfile)
    }

    suspend fun askGemini(prompt: String, imageBase64: String?, mimeType: String?, farmerProfile: FarmerProfile?): String {
        val name = farmerProfile?.name ?: "Petani"
        val crop = farmerProfile?.cropType ?: "Kakao/Padi"
        val area = farmerProfile?.landArea ?: "1"
        val location = farmerProfile?.location ?: "KOLAKA, SULTRA"
        return generateResponse(
            name = name,
            crop = crop,
            area = area,
            location = location,
            userInput = prompt,
            imageBase64 = imageBase64,
            mimeType = mimeType ?: "image/jpeg"
        )
    }
}
