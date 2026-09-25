package com.asistenpetani.data.local

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

class SecureStorage(context: Context) {

    // Membuat Kunci Utama yang disimpan aman di Android Keystore
    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    // Membuat brankas penyimpanan yang terenkripsi
    private val sharedPreferences = EncryptedSharedPreferences.create(
        context,
        "aspek_secure_prefs",
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    fun saveOfflineMessageQueue(messages: String) {
        sharedPreferences.edit().putString("failed_messages", messages).apply()
    }

    fun getOfflineMessageQueue(): String? {
        return sharedPreferences.getString("failed_messages", null)
    }
}
