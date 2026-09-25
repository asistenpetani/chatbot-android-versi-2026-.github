package com.asistenpetani.ui.screens

import android.app.Activity
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.asistenpetani.ui.viewmodel.SubscriptionViewModel

@Composable
fun UpgradePremiumScreen(
    viewModel: SubscriptionViewModel = viewModel()
) {
    val context = LocalContext.current
    val activity = context as? Activity
    
    // Memantau notifikasi dari ViewModel
    val pesanNotifikasi by viewModel.pesanNotifikasi.collectAsState()

    // Menampilkan Toast jika ada pesan notifikasi
    LaunchedEffect(pesanNotifikasi) {
        if (pesanNotifikasi.isNotEmpty()) {
            Toast.makeText(context, pesanNotifikasi, Toast.LENGTH_LONG).show()
            viewModel.clearNotification() // Reset agar tidak muncul terus
        }
    }

    Column(modifier = Modifier.padding(16.dp)) {
        Text("Aktifkan Mode Primer - Rp 25.000/bulan")
        
        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                // PANGGIL POP-UP PEMBAYARAN GOOGLE PLAY!
                if (activity != null) {
                    viewModel.billingManager.launchBillingFlow(activity)
                } else {
                    Toast.makeText(context, "Gagal memuat pembayaran Google Play", Toast.LENGTH_SHORT).show()
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Lanjutkan ke Pembayaran (Google Pay)")
        }
    }
}
