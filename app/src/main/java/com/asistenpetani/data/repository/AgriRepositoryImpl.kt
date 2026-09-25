package com.asistenpetani.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.asistenpetani.domain.model.UserProfile
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class AgriRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    private fun getCollectionName(): String {
        return "users"
    }

    suspend fun saveFarmerProfile(uid: String, profile: Map<String, Any>) {
        // Menyimpan ke koleksi 'profiles' (atau 'profiles_debug' di debug mode) dengan ID dokumen sama dengan UID Auth
        try {
            firestore.collection(getCollectionName()).document(uid).set(profile).await()
        } catch (e: Exception) {
            android.util.Log.w("AgriRepo", "Firestore profile save bypassed/offline: ${e.message}")
        }
    }

    suspend fun updateFarmerProfile(uid: String, updates: Map<String, Any>) {
        try {
            firestore.collection(getCollectionName()).document(uid).update(updates).await()
        } catch (e: Exception) {
            android.util.Log.w("AgriRepo", "Firestore profile update bypassed/offline: ${e.message}")
        }
    }

    suspend fun getFarmerProfile(uid: String): Map<String, Any>? {
        return try {
            val snapshot = firestore.collection(getCollectionName()).document(uid).get().await()
            snapshot.data
        } catch (e: Exception) {
            android.util.Log.w("AgriRepo", "Gagal mengambil profil: ${e.message}")
            null
        }
    }

    suspend fun saveDeviceInfo(uid: String, deviceInfo: Map<String, Any>) {
        try {
            // Simpan ke Firestore di koleksi 'profiles' -> 'devices' sebagai secure audit trail
            firestore.collection(getCollectionName())
                .document(uid)
                .collection("devices")
                .document(android.os.Build.ID)
                .set(deviceInfo)
                .await()
            android.util.Log.d("AgriRepo", "Device recognition registered successfully to Firestore")
        } catch (e: Exception) {
            android.util.Log.w("AgriRepo", "Firestore saveDeviceInfo bypassed/offline: ${e.message}")
        }
    }
}
