package com.asistenpetani.util

import android.content.Context
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.security.ProviderInstaller
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object SecurityPatches {

    /**
     * Mengupdate Security Provider perangkat untuk menambal semua celah CVE masa lalu (seperti OpenSSL vuln).
     * Jika Google Play Services mendeteksi library enkripsi HP petani usang, ini akan memperbaruinya secara instan.
     */
    suspend fun patchSecurityProvider(context: Context): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val availability = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(context)
                if (availability != ConnectionResult.SUCCESS) {
                    return@withContext false
                }
                // Memaksa Google Play Services menyuntikkan patch keamanan terbaru ke aplikasi kita
                ProviderInstaller.installIfNeeded(context)
                true // Sukses aman dari celah CVE SSL lama
            } catch (e: Exception) {
                // Gagal biasanya karena Google Play Services di HP tersebut sangat jadul atau dimatikan
                false
            }
        }
    }
}
