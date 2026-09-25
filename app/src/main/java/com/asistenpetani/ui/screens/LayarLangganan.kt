package com.asistenpetani.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack

@Composable
fun LayarLangganan(
    onBeliBulananKlik: () -> Unit,
    onBeliTahunanKlik: () -> Unit,
    onBackClick: () -> Unit = {}
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Start
        ) {
            IconButton(onClick = onBackClick) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Kembali"
                )
            }
        }

        Text(
            text = "Tingkatkan Pengalaman Anda!",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = "Nikmati fitur Asisten Petani secara penuh tanpa gangguan iklan.",
            fontSize = 16.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        // Memanggil cetakan kartu untuk Paket Bulanan
        KartuPaket(
            judul = "Paket Bulanan",
            harga = "Rp 25.000 / bulan",
            deskripsi = "Bebas iklan selama 30 hari.",
            onClick = onBeliBulananKlik
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Memanggil cetakan kartu untuk Paket Tahunan
        KartuPaket(
            judul = "Paket Tahunan (Hemat!)",
            harga = "Rp 250.000 / tahun",
            deskripsi = "Bebas iklan setahun penuh untuk hasil panen maksimal.",
            onClick = onBeliTahunanKlik
        )
    }
}

// Komponen cetakan khusus untuk kartu harga agar kode lebih rapi dan tidak berulang
@Composable
fun KartuPaket(
    judul: String,
    harga: String,
    deskripsi: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = judul, fontWeight = FontWeight.Bold, fontSize = 20.sp)
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(text = harga, fontSize = 18.sp, color = MaterialTheme.colorScheme.secondary)
            Spacer(modifier = Modifier.height(4.dp))
            
            Text(text = deskripsi, fontSize = 14.sp, textAlign = TextAlign.Center)
            Spacer(modifier = Modifier.height(16.dp))
            
            Button(
                onClick = onClick, 
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Pilih Paket Ini")
            }
        }
    }
}
