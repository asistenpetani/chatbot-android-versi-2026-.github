package com.asistenpetani.ui.components

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.*

@Composable
fun LocationPermissionHandler(onPermissionGranted: () -> Unit) {
    var showExplanationDialog by remember { mutableStateOf(false) }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            onPermissionGranted()
        } else {
            // Bisa menampilkan notifikasi bahwa fitur cuaca/rekomendasi butuh lokasi
        }
    }

    // Dialog transparan yang disukai Google Play
    if (showExplanationDialog) {
        AlertDialog(
            onDismissRequest = { showExplanationDialog = false },
            title = { Text("Izin Lokasi Lahan") },
            text = { Text("ASPEK memerlukan lokasi Anda untuk menganalisis cuaca lokal dan memberikan rekomendasi pupuk yang presisi. Data ini tidak akan dibagikan ke pihak ketiga.") },
            confirmButton = {
                Button(onClick = {
                    showExplanationDialog = false
                    permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                }) {
                    Text("Mengerti & Lanjutkan")
                }
            }
        )
    }
}
