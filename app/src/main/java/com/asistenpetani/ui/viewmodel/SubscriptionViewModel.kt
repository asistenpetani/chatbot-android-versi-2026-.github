package com.asistenpetani.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.asistenpetani.util.GoogleBillingManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class SubscriptionViewModel(application: Application) : AndroidViewModel(application) {

    val billingManager = GoogleBillingManager(application)
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private val _pesanNotifikasi = MutableStateFlow("")
    val pesanNotifikasi: StateFlow<String> = _pesanNotifikasi

    init {
        // Menangkap sinyal sukses dari Mesin Pembayaran Google
        billingManager.onPurchaseSuccess = {
            _pesanNotifikasi.value = "Pembayaran Berhasil! Mengaktifkan Mode Premium..."
            updatePremiumStatusToFirebase()
        }

        // Menangkap sinyal gagal tanpa membuat error/crash (Sesuai instruksi Anda)
        billingManager.onPurchaseError = { errorMessage ->
            _pesanNotifikasi.value = errorMessage
        }
    }

    private fun updatePremiumStatusToFirebase() {
        val uid = auth.currentUser?.uid ?: return
        
        viewModelScope.launch {
            try {
                // Update ke Firebase
                db.collection("users").document(uid)
                    .update("isPremium", true)
                    .addOnSuccessListener {
                        _pesanNotifikasi.value = "Premium Aktif! Bebas Iklan & Chat Sepuasnya."
                    }
            } catch (e: Exception) {
                _pesanNotifikasi.value = "Gagal mengupdate database: ${e.message}"
            }
        }
    }
    
    // Fungsi untuk reset notifikasi (Toast)
    fun clearNotification() {
        _pesanNotifikasi.value = ""
    }
}
