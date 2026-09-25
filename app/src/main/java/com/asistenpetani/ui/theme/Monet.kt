package com.asistenpetani.ui.theme

import android.os.Build
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

/**
 * Fungsi ini bertugas menghasilkan skema warna dinamis (Monet/Material You).
 * Jika HP petani menggunakan Android 12 (API 31) ke atas, aplikasi akan mengikuti
 * Warna yang sesuai kebutuhan agar semuanya terlihat lebih jelas.
 */
@Composable
fun getMonetColorScheme(
    darkTheme: Boolean,
    defaultLightColors: ColorScheme,
    defaultDarkColors: ColorScheme
): ColorScheme {
    // Mengecek apakah HP mendukung warna dinamis (Android 12+)
    val isDynamicColorSupported = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
    val context = LocalContext.current

    return when {
        // Jika mendukung dan sedang mode gelap
        isDynamicColorSupported && darkTheme -> dynamicDarkColorScheme(context)
        
        // Jika mendukung dan sedang mode terang
        isDynamicColorSupported && !darkTheme -> dynamicLightColorScheme(context)
        
        // Jika HP versi lama, gunakan warna default yang kita buat sendiri
        darkTheme -> defaultDarkColors
        else -> defaultLightColors
    }
}
