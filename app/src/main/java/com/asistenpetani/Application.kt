package com.asistenpetani

import android.app.Application
import android.content.Context
import com.google.firebase.FirebaseApp
import com.google.firebase.appcheck.FirebaseAppCheck
import com.google.firebase.appcheck.playintegrity.PlayIntegrityAppCheckProviderFactory
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class Application : Application() {
    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(base)
        try {
            val webViewCacheDir = java.io.File(base.cacheDir, "WebView/Default/HTTP Cache/Code Cache")
            val jsDir = java.io.File(webViewCacheDir, "js")
            val wasmDir = java.io.File(webViewCacheDir, "wasm")
            
            jsDir.mkdirs()
            wasmDir.mkdirs()
            
            java.io.File(jsDir, ".placeholder").createNewFile()
            java.io.File(wasmDir, ".placeholder").createNewFile()
        } catch (e: Exception) {
            android.util.Log.e("AsistenPetaniApp", "Failed to create WebView cache directories in attachBaseContext: ${e.message}")
        }
    }

    override fun onCreate() {
        super.onCreate()

        try {
            FirebaseApp.initializeApp(this)
            
            // Lapis 2: Memasang Pengaman App Check (Play Integrity)
            // [ARSITEK NOTE]: DINONAKTIFKAN SEMENTARA untuk masa uji coba sideloading APK.
            /*
            val firebaseAppCheck = FirebaseAppCheck.getInstance()
            firebaseAppCheck.installAppCheckProviderFactory(
                PlayIntegrityAppCheckProviderFactory.getInstance()
            )
            */
        } catch (e: Exception) {
            android.util.Log.e("AsistenPetaniApp", "FirebaseApp/AppCheck initialization skipped or failed: ${e.message}")
        }
    }
}
