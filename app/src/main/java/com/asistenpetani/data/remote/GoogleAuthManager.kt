package com.asistenpetani.data.remote

import android.content.Context
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth

/**
 * Mesin penggerak autentikasi Google.
 * Kode ini murni berjalan di latar belakang (Data Layer) dan tidak 
 * mempengaruhi tampilan (UI Layer) aplikasi sama sekali.
 */
class GoogleAuthManager(private val context: Context) {

    // Menyiapkan koneksi ke Firebase Auth
    private val firebaseAuth = FirebaseAuth.getInstance()

    /**
     * Parameter yang bisa disesuaikan: webClientId
     * Ini adalah kunci ID yang menghubungkan aplikasi Android Anda dengan server Google.
     * Nantinya, kunci ini bisa kita panggil dari file .env agar lebih aman.
     */
    private val webClientId by lazy { context.getString(com.asistenpetani.R.string.default_web_client_id) }

    // Mengonfigurasi permintaan data ke Google (kita minta Email dan ID Profil)
    private val googleSignInOptions by lazy {
        GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(webClientId)
            .requestEmail()
            .build()
    }

    // Membuat alat (Client) untuk memunculkan popup akun Google nantinya
    fun getGoogleSignInClient(): GoogleSignInClient {
        return GoogleSignIn.getClient(context, googleSignInOptions)
    }

    // Fungsi tambahan untuk mengecek apakah petani sudah login sebelumnya
    fun isPetaniSudahLogin(): Boolean {
        return firebaseAuth.currentUser != null
    }
}
