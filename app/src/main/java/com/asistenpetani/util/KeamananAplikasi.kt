package com.asistenpetani.util

import android.content.Context
import android.util.Log
import com.google.android.play.core.integrity.IntegrityManagerFactory
import com.google.android.play.core.integrity.IntegrityTokenRequest
import com.google.android.play.core.integrity.IntegrityTokenResponse

class KeamananAplikasi(private val context: Context) {

    fun periksaKeaslianAplikasi() {
        // Membuat pengelola integritas
        val integrityManager = IntegrityManagerFactory.create(context)

        // Nomor acak ini idealnya dihasilkan dari server backend Anda agar tidak bisa ditebak
        val nonceTerkode = "GANTIKAN_DENGAN_TEKS_ACAK_DARI_SERVER_ANDA"

        // Membuat permintaan bukti keaslian
        val integrityTokenRequest = IntegrityTokenRequest.builder()
            .setNonce(nonceTerkode)
            .build()

        // Menjalankan tugas pemeriksaan di latar belakang
        val tokenTask = integrityManager.requestIntegrityToken(integrityTokenRequest)

        tokenTask.addOnSuccessListener { response: IntegrityTokenResponse ->
            val tokenKeaslian = response.token()
            Log.d("Keamanan", "Token berhasil didapat! Aplikasi ini asli.")
            // Langkah selanjutnya: Kirim token ini ke server Anda untuk divalidasi
        }

        tokenTask.addOnFailureListener { exception ->
            Log.e("Keamanan", "Peringatan: Gagal memverifikasi keaslian aplikasi!", exception)
            // Tindakan: Hentikan akses ke fitur penting jika ini terjadi
        }
    }
}
