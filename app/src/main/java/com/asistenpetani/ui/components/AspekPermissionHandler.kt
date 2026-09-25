package com.asistenpetani.ui.components

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun AspekPermissionHandler() {
    // 1. Siapkan daftar izin yang ingin kita minta
    val permissionsToRequest = mutableListOf(
        Manifest.permission.CAMERA,
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION,
        Manifest.permission.RECORD_AUDIO
    ).apply {
        // Jika perangkat menggunakan Android 13 (Tiramisu) ke atas, tambahkan izin notifikasi
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            add(Manifest.permission.POST_NOTIFICATIONS)
        }
    }.toTypedArray()

    // 2. Buat launcher untuk menampilkan pop-up sistem Android
    val multiplePermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions(),
        onResult = { permissions ->
            // Blok ini akan berjalan setelah pengguna memilih "Izinkan" atau "Tolak"
            permissions.entries.forEach { entry ->
                val permissionName = entry.key
                val isGranted = entry.value
                
                if (isGranted) {
                    println("Izin $permissionName diberikan! Fitur bisa digunakan.")
                    // TODO: Aktifkan fitur terkait (misalnya tombol buka kamera)
                } else {
                    println("Izin $permissionName ditolak oleh petani.")
                    // TODO: Berikan pesan ramah bahwa fitur tidak bisa berjalan tanpa izin ini
                }
            }
        }
    )

    // 3. Tombol UI sederhana untuk memicu permintaan izin
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = "ASPEK butuh akses Kamera & Lokasi untuk membantu pertanian Anda.")
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = {
                // Saat tombol diklik, munculkan pop-up izin
                multiplePermissionLauncher.launch(permissionsToRequest)
            }
        ) {
            Text("Berikan Izin Aplikasi")
        }
    }
}
