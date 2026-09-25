package com.asistenpetani.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

@Composable
fun LayarASPEKAman() {
    // 1. Variabel penyimpan data
    var dataPetani by remember { mutableStateOf("Belum memuat data") }

    Column {
        Text(text = "Status: $dataPetani")

        // CARA AMAN 1: Mengubah state di dalam "Pemicu Aksi" (Event)
        // Recomposition hanya terjadi SATU KALI saat tombol ditekan.
        Button(onClick = { 
            dataPetani = "Memuat data lahan..." // Aman!
        }) {
            Text("Muat Data")
        }
    }

    // CARA AMAN 2: Menggunakan "Ruang Aman" LaunchedEffect
    // Kode di dalam blok ini hanya akan dijalankan SATU KALI saat layar pertama kali dibuka.
    // Sangat cocok untuk memanggil server atau database secara otomatis di awal.
    LaunchedEffect(Unit) {
        // Panggil fungsi API atau server Anda di sini
        // Misalnya: data petani atau progres tanaman petani dan lainya = panggil Server asisten petani-a–app()
        dataPetani = "Data berhasil dimuat otomatis saat layar dibuka" // Aman!
    }
}
