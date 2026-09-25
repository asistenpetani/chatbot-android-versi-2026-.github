package com.asistenpetani.util

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build

/**
 * Alat Keamanan untuk mendeteksi apakah aplikasi dikloning atau diinstal dari luar Play Store.
 */
object AntiCloneSecurity {

    /**
     * Mengecek apakah aplikasi diinstal dari toko aplikasi resmi (Play Store).
     */
    fun isInstalledFromPlayStore(context: Context): Boolean {
        // Daftar toko resmi (Play Store)
        val validInstallers = listOf("com.android.vending", "com.google.android.feedback")
        
        val installerPackageName = try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                // Untuk Android 11 ke atas
                context.packageManager.getInstallSourceInfo(context.packageName).installingPackageName
            } else {
                // Untuk Android 10 ke bawah
                @Suppress("DEPRECATION")
                context.packageManager.getInstallerPackageName(context.packageName)
            }
        } catch (e: Exception) {
            null
        }

        // Jika null, berarti di-install manual via file APK (bisa jadi bajakan)
        return installerPackageName != null && validInstallers.contains(installerPackageName)
    }
}
