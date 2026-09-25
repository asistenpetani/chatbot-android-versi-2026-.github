package com.asistenpetani.ui.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.asistenpetani.data.repository.AgriRepositoryImpl
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

@HiltViewModel
class RegisterViewModel @Inject constructor(
    private val auth: FirebaseAuth,
    private val repository: AgriRepositoryImpl
) : ViewModel() {

    var email by mutableStateOf("")
    var password by mutableStateOf("")
    var nama by mutableStateOf("")
    var lahan by mutableStateOf("")
    var jenis by mutableStateOf("")
    var isLoading by mutableStateOf(false)
    var errorMessage by mutableStateOf<String?>(null)
    var isSuccess by mutableStateOf(false)

    fun register() {
        if (email.isBlank() || password.isBlank() || nama.isBlank()) {
            errorMessage = "Email, kata sandi, dan nama tidak boleh kosong"
            return
        }

        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            try {
                val authResult = auth.createUserWithEmailAndPassword(email, password).await()
                val uid = authResult.user?.uid
                if (uid != null) {
                    val farmerData = mapOf(
                        "name" to nama,
                        "landArea" to lahan.ifBlank { "1.0" },
                        "cropType" to jenis.ifBlank { "Tanaman" },
                        "email" to email,
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
                    isSuccess = true
                }
            } catch (e: Exception) {
                errorMessage = e.localizedMessage ?: "Terjadi kesalahan saat pendaftaran"
            } finally {
                isLoading = false
            }
        }
    }
}
