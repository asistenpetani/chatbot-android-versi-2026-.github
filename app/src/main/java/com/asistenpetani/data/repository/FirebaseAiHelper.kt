package com.asistenpetani.data.repository

import android.content.Context
import android.util.Log
import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import com.google.firebase.ai.OnDeviceConfig
import com.google.firebase.ai.InferenceMode
import com.google.firebase.ai.ondevice.FirebaseAIOnDevice
import com.google.firebase.ai.ondevice.OnDeviceModelStatus
import com.google.firebase.ai.ondevice.DownloadStatus
import com.google.firebase.ai.type.generationConfig
import com.google.firebase.ai.type.content
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

import android.graphics.BitmapFactory

@Suppress("OPT_IN_USAGE", "OPT_IN_USAGE_WARNING", "OPT_IN_USAGE_ERROR")
object FirebaseAiHelper {
    private const val TAG = "FirebaseAiHelper"
    
    private val _downloadStatusFlow = MutableStateFlow<String>("IDLE")
    val downloadStatusFlow = _downloadStatusFlow.asStateFlow()

    private val _onDeviceAvailable = MutableStateFlow<Boolean>(false)
    val onDeviceAvailable = _onDeviceAvailable.asStateFlow()

    /**
     * Memeriksa status model on-device dan memulai pengunduhan model jika diperlukan.
     */
    fun checkAndPrepareOnDeviceModel(context: Context, coroutineScope: CoroutineScope) {
        coroutineScope.launch(Dispatchers.IO) {
            try {
                val status = FirebaseAIOnDevice.checkStatus()
                Log.d(TAG, "Status model On-Device Firebase AI saat ini: $status")
                
                when (status) {
                    OnDeviceModelStatus.UNAVAILABLE -> {
                        Log.w(TAG, "Model On-Device tidak tersedia pada perangkat ini.")
                        _downloadStatusFlow.value = "UNAVAILABLE"
                        _onDeviceAvailable.value = false
                    }
                    OnDeviceModelStatus.DOWNLOADABLE -> {
                        Log.i(TAG, "Model On-Device dapat diunduh. Memulai proses download...")
                        _downloadStatusFlow.value = "STARTING_DOWNLOAD"
                        
                        FirebaseAIOnDevice.download().collect { downloadStatus ->
                            val className = downloadStatus.javaClass.simpleName
                            Log.d(TAG, "Download status update: $className")
                            
                            when {
                                className.contains("Started") -> {
                                    _downloadStatusFlow.value = "DOWNLOADING: 0%"
                                }
                                className.contains("InProgress") -> {
                                    // Menggunakan refleksi aman untuk mengambil persentase jika properti tersedia,
                                    // jika tidak gunakan progress default untuk keamanan ekstra.
                                    var progressText = "DOWNLOADING..."
                                    try {
                                        val progressField = downloadStatus.javaClass.declaredFields.firstOrNull { 
                                            it.type == Long::class.javaPrimitiveType || it.type == Long::class.java 
                                        }
                                        if (progressField != null) {
                                            progressField.isAccessible = true
                                            val valBytes = progressField.get(downloadStatus) as? Long ?: 0L
                                            if (valBytes > 0) {
                                                progressText = "DOWNLOADING (${valBytes / 1024 / 1024} MB)"
                                            }
                                        }
                                    } catch (ex: Exception) {
                                        // Ignore reflection errors and keep default label
                                    }
                                    _downloadStatusFlow.value = progressText
                                }
                                className.contains("Completed") -> {
                                    Log.i(TAG, "Pengunduhan model On-Device selesai dan siap digunakan!")
                                    _downloadStatusFlow.value = "COMPLETED"
                                    _onDeviceAvailable.value = true
                                }
                                className.contains("Failed") -> {
                                    Log.e(TAG, "Pengunduhan model On-Device gagal!")
                                    _downloadStatusFlow.value = "FAILED"
                                    _onDeviceAvailable.value = false
                                }
                                else -> {
                                    _downloadStatusFlow.value = "DOWNLOADING..."
                                }
                            }
                        }
                    }
                    OnDeviceModelStatus.DOWNLOADING -> {
                        Log.w(TAG, "Model On-Device saat ini sedang dalam proses pengunduhan.")
                        _downloadStatusFlow.value = "DOWNLOADING_IN_BACKGROUND"
                    }
                    OnDeviceModelStatus.AVAILABLE -> {
                        Log.i(TAG, "Model On-Device tersedia lokal pada perangkat dan siap dijalankan.")
                        _downloadStatusFlow.value = "AVAILABLE"
                        _onDeviceAvailable.value = true
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Kesalahan saat memeriksa status Firebase AI On-Device: ${e.message}", e)
                _downloadStatusFlow.value = "ERROR: ${e.localizedMessage}"
            }
        }
    }

    /**
     * Menghasilkan teks balasan menggunakan model Firebase AI.
     * Menggunakan PREFER_ON_DEVICE untuk mengutamakan inference lokal jika model telah siap.
     */
    suspend fun generateWithFirebaseAi(
        prompt: String,
        systemInstruction: String,
        modelName: String = "gemini-3.5-flash",
        historyList: List<com.asistenpetani.data.local.room.ChatMessage> = emptyList(),
        imageBytes: ByteArray? = null,
        mimeType: String? = null
    ): String? {
        return try {
            val model = Firebase.ai
                .generativeModel(
                    modelName = modelName,
                    systemInstruction = content { text(systemInstruction) },
                    generationConfig = generationConfig {
                        temperature = 0.8f
                        topK = 10
                    },
                    onDeviceConfig = OnDeviceConfig(mode = InferenceMode.PREFER_ON_DEVICE)
                )
            
            if (imageBytes != null) {
                // If there's an image, we send it as a multimodal prompt
                val bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
                val response = model.generateContent(
                    content {
                        if (bitmap != null) {
                            image(bitmap)
                        }
                        text(prompt)
                    }
                )
                response.text
            } else if (historyList.isNotEmpty()) {
                val chatHistory = historyList.mapNotNull { msg ->
                    if (msg.sender == "user" || msg.sender == "ai") {
                        val roleStr = if (msg.sender == "user") "user" else "model"
                        content(role = roleStr) { text(msg.text) }
                    } else null
                }
                val chat = model.startChat(history = chatHistory)
                val response = chat.sendMessage(prompt)
                response.text
            } else {
                val response = model.generateContent(prompt)
                response.text
            }
        } catch (e: Exception) {
            Log.e(TAG, "Gagal memproses via SDK Firebase AI: ${e.message}", e)
            null
        }
    }
}
