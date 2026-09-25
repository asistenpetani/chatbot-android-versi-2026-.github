package com.asistenpetani.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.asistenpetani.data.repository.AgriRepositoryImpl
import com.asistenpetani.domain.repository.GenerativeAiRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val auth: FirebaseAuth,
    private val repository: AgriRepositoryImpl, // Suntikkan repository
    private val geminiRepository: GenerativeAiRepository? = null, // Optional injection for Gemini
    @javax.inject.Named("webClientId") val webClientId: String = ""
) : ViewModel() {

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()
    fun clearErrorMessage() { _errorMessage.value = null }

    // Status pengguna real-time
    private val _user = MutableStateFlow<FirebaseUser?>(auth.currentUser)
    val user: StateFlow<FirebaseUser?> = _user.asStateFlow()

    private val _welcomeMessage = MutableStateFlow<String?>(null)
    val welcomeMessage: StateFlow<String?> = _welcomeMessage.asStateFlow()

    init {
        // Memantau perubahan status login secara otomatis
        auth.addAuthStateListener { firebaseAuth ->
            val currentUser = firebaseAuth.currentUser
            _user.value = currentUser
            if (currentUser != null) {
                // Tampilkan pesan selamat datang
                val name = currentUser.displayName ?: "Akang/Teteh"
                
                if (geminiRepository != null) {
                    viewModelScope.launch {
                        try {
                            val prompt = "Beri sambutan hangat (1 kalimat singkat bahasa daerah) untuk petani bernama $name yang baru saja berhasil login pintar ke aplikasi Asisten Petani Kita."
                            val response = geminiRepository.generateResponse(
                                systemPrompt = "Anda adalah AI Asisten Petani yang ramah.",
                                userInput = prompt,
                                imageBase64 = null,
                                mimeType = null,
                                thinkingLevel = null
                            )
                            _welcomeMessage.value = response
                        } catch (e: Exception) {
                            _welcomeMessage.value = "Wilujeng Sumping deui, $name!"
                        }
                    }
                } else {
                    _welcomeMessage.value = "Wilujeng Sumping deui, $name!"
                }
            } else {
                _welcomeMessage.value = null
            }
        }
    }

    fun signInWithGoogle(credential: com.google.firebase.auth.AuthCredential, nama: String, crop: String, area: String, kategori: String = "Tanaman", lokasiDiizinkan: Boolean = false) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val result = auth.signInWithCredential(credential).await()
                val uid = result.user?.uid
                if (uid != null) {
                    val farmerData = mapOf(
                        "name" to nama.ifBlank { result.user?.displayName ?: "Petani" },
                        "landArea" to area,
                        "cropType" to crop,
                        "kategori_budidaya" to kategori,
                        "lokasi_diizinkan" to lokasiDiizinkan,
                        "email" to (result.user?.email ?: ""),
                        "createdAt" to com.google.firebase.Timestamp.now()
                    )
                    repository.saveFarmerProfile(uid, farmerData)

                    val deviceInfoMap = mapOf(
                        "deviceModel" to android.os.Build.MODEL,
                        "manufacturer" to android.os.Build.MANUFACTURER,
                        "osVersion" to android.os.Build.VERSION.RELEASE,
                        "apiLevel" to android.os.Build.VERSION.SDK_INT,
                        "platform" to "Android",
                        "lastSeen" to com.google.firebase.Timestamp.now()
                    )
                    repository.saveDeviceInfo(uid, deviceInfoMap)
                }
            } catch (e: Exception) {
                android.util.Log.e("AuthViewModel", "Authentication Error: ${e.message}", e); _errorMessage.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun signInAnonymously(nama: String, crop: String, area: String, kategori: String = "Tanaman", lokasiDiizinkan: Boolean = false) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val result = auth.signInAnonymously().await()
                val uid = result.user?.uid
                if (uid != null) {
                    val farmerData = mapOf(
                        "name" to nama.ifBlank { "Petani Tamu" },
                        "landArea" to area,
                        "cropType" to crop,
                        "kategori_budidaya" to kategori,
                        "lokasi_diizinkan" to lokasiDiizinkan,
                        "email" to "anon@guest.local",
                        "createdAt" to com.google.firebase.Timestamp.now(),
                        "isAnonymous" to true
                    )
                    repository.saveFarmerProfile(uid, farmerData)
                }
            } catch (e: Exception) {
                android.util.Log.e("AuthViewModel", "Authentication Error: ${e.message}", e); _errorMessage.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun signOut() {
        auth.signOut()
    }

    suspend fun getFarmerProfile(uid: String): Map<String, Any>? {
        return repository.getFarmerProfile(uid)
    }

    fun signInWithEmailAndPassword(email: String, sandi: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val result = auth.signInWithEmailAndPassword(email, sandi).await()
                val uid = result.user?.uid
                if (uid != null) {
                    val deviceInfoMap = mapOf(
                        "deviceModel" to android.os.Build.MODEL,
                        "manufacturer" to android.os.Build.MANUFACTURER,
                        "osVersion" to android.os.Build.VERSION.RELEASE,
                        "apiLevel" to android.os.Build.VERSION.SDK_INT,
                        "platform" to "Android",
                        "lastSeen" to com.google.firebase.Timestamp.now()
                    )
                    repository.saveDeviceInfo(uid, deviceInfoMap)
                }
            } catch (e: Exception) {
                android.util.Log.e("AuthViewModel", "Authentication Error: ${e.message}", e); _errorMessage.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun registerPetani(email: String, sandi: String, nama: String, lahan: String, jenis: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                // 1. Buat akun di Firebase Auth
                val authResult = auth.createUserWithEmailAndPassword(email, sandi).await()
                val uid = authResult.user?.uid
                if (uid != null) {
                    // 2. Siapkan data untuk disimpan ke Firestore
                    val farmerData = mapOf(
                        "name" to nama,
                        "landArea" to lahan,
                        "cropType" to jenis,
                        "email" to email,
                        "createdAt" to com.google.firebase.Timestamp.now()
                    )
                    // 3. Simpan ke Firestore lewat Repository
                    repository.saveFarmerProfile(uid, farmerData)

                    // 4. Daftarkan identifikasi hardware secara otomatis (Device Recognition)
                    val deviceInfoMap = mapOf(
                        "deviceModel" to android.os.Build.MODEL,
                        "manufacturer" to android.os.Build.MANUFACTURER,
                        "osVersion" to android.os.Build.VERSION.RELEASE,
                        "apiLevel" to android.os.Build.VERSION.SDK_INT,
                        "platform" to "Android",
                        "lastSeen" to com.google.firebase.Timestamp.now()
                    )
                    repository.saveDeviceInfo(uid, deviceInfoMap)
                }
            } catch (e: Exception) {
                // Handle Error (Misal: Email sudah terdaftar)
                try {
                    val loginResult = auth.signInWithEmailAndPassword(email, sandi).await()
                    val uid = loginResult.user?.uid
                    if (uid != null) {
                        val deviceInfoMap = mapOf(
                            "deviceModel" to android.os.Build.MODEL,
                            "manufacturer" to android.os.Build.MANUFACTURER,
                            "osVersion" to android.os.Build.VERSION.RELEASE,
                            "apiLevel" to android.os.Build.VERSION.SDK_INT,
                            "platform" to "Android",
                            "lastSeen" to com.google.firebase.Timestamp.now()
                        )
                        repository.saveDeviceInfo(uid, deviceInfoMap)
                    }
                } catch (e2: Exception) {
                    // Login gagal
                    android.util.Log.e("AuthViewModel", "Authentication Error: ${e2.message}", e2); _errorMessage.value = e2.message
                }
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun linkEmailAndPassword(email: String, sandi: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val currentUser = auth.currentUser
                if (currentUser != null && currentUser.isAnonymous) {
                    val credential = com.google.firebase.auth.EmailAuthProvider.getCredential(email, sandi)
                    currentUser.linkWithCredential(credential).await()
                    // Update profil
                    val uid = currentUser.uid
                    val updateData = mapOf(
                        "email" to email,
                        "isAnonymous" to false
                    )
                    repository.updateFarmerProfile(uid, updateData)
                    onSuccess()
                } else {
                    onError("Pengguna tidak valid atau bukan akun tamu.")
                }
            } catch (e: Exception) {
                onError(e.localizedMessage ?: "Gagal menautkan email.")
            } finally {
                _isLoading.value = false
            }
        }
    }
}
