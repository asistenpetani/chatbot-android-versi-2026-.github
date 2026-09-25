package com.asistenpetani.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun PrivacyPolicyScreen(
    onBackClick: (() -> Unit)? = null
) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (onBackClick != null) {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = onBackClick) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Kembali"
                            )
                        }
                        Text(
                            text = "Kebijakan Privasi & Lisensi",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Header
                        Text(
                            text = "Kebijakan Privasi - ASPEK",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Terakhir Diperbarui: 12 Juni 2026",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.outline
                        )

                        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

                        // Intro
                        Text(
                            text = "Aplikasi ASPEK (Asisten Petani Kita) berkomitmen penuh untuk melindungi privasi penggunanya. Kebijakan Privasi ini menjelaskan bagaimana kami mengumpulkan, menggunakan, menyimpan, dan melindungi data Anda saat Anda menggunakan aplikasi seluler kami.",
                            style = MaterialTheme.typography.bodyMedium
                        )

                        // 1. Informasi yang Kami Kumpulkan
                        PolicySectionTitle(title = "1. Informasi yang Kami Kumpulkan")
                        Text(
                            text = "ASPEK dirancang untuk menyimpan sebagian besar data Anda secara lokal di perangkat Anda demi kenyamanan, kecepatan, dan privasi yang optimal. Namun, kami juga mengumpulkan data berikut:",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        PolicyBulletPoint(boldText = "Profil Petani: ", text = "Nama, lokasi lahan, komoditas tanaman, luas area, dan foto daun yang Anda buat (disimpan secara lokal).")
                        PolicyBulletPoint(boldText = "Konten Kamera: ", text = "Foto daun tanaman yang Anda ambil secara lokal untuk analisis warna hara melalui asisten Gemini AI.")
                        PolicyBulletPoint(boldText = "Data Lokasi (GPS): ", text = "Digunakan untuk memberikan informasi cuaca ekstrem hiper-lokal dan memetakan jarak kios pupuk terdekat.")
                        PolicyBulletPoint(boldText = "Riwayat Obrolan: ", text = "Riwayat percakapan Anda dengan Gemini AI disimpan di basis data lokal SQLite (Room) di perangkat Anda.")

                        // 2. Penggunaan Informasi
                        PolicySectionTitle(title = "2. Penggunaan Informasi")
                        Text(
                            text = "Kami menggunakan data yang dikumpulkan untuk tujuan berikut:",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        PolicyBulletPoint(text = "• Memberikan rekomendasi pertanian, diagnosis hara, takaran dosis pupuk, dan mitigasi cuaca.")
                        PolicyBulletPoint(text = "• Memproses percakapan interaktif menggunakan Google Gemini AI secara terenkripsi dan anonim.")
                        PolicyBulletPoint(text = "• Memungkinkan pengiriman pemberitahuan dan alarm pengingat kegiatan pertanian (tebus pupuk, penyemprotan, pemupukan).")

                        // 3. Keamanan Data & Penyimpanan Lokal
                        PolicySectionTitle(title = "3. Keamanan Data & Penyimpanan Lokal")
                        Text(
                            text = "Aplikasi tidak mengunggah data pribadi Anda ke server pihak ketiga mana pun untuk tujuan komersial atau pengiklanan. Jembatan komunikasi ke Google AI Studio/Gemini menggunakan komunikasi HTTPS yang telah terenkripsi guna memastikan integritas data.",
                            style = MaterialTheme.typography.bodyMedium
                        )

                        // 4. Perizinan Perangkat (App Permissions)
                        PolicySectionTitle(title = "4. Perizinan Perangkat (App Permissions)")
                        Text(
                            text = "Aplikasi memerlukan beberapa izin fungsional berikut:",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        PolicyBulletPoint(boldText = "Internet & Network State: ", text = "Untuk memanggil API Gemini dan sinkronisasi cuaca.")
                        PolicyBulletPoint(boldText = "Kamera (Camera): ", text = "Untuk memotret daun tanaman yang akan didiagnosa.")
                        PolicyBulletPoint(boldText = "Lokasi (GPS Fine/Coarse): ", text = "Untuk pencari kios terdekat dan cuaca lokal.")
                        PolicyBulletPoint(boldText = "Notification: ", text = "Untuk alarm atau pengingat jadwal tani.")

                        // 5. Perubahan Kebijakan Privasi
                        PolicySectionTitle(title = "5. Perubahan Kebijakan Privasi")
                        Text(
                            text = "Kami dapat memperbarui Kebijakan Privasi ini dari waktu ke waktu secara berkala. Perubahan akan segera berlaku setelah dipublikasikan pada halaman ini.",
                            style = MaterialTheme.typography.bodyMedium
                        )

                        // 6. Kontak Kami
                        PolicySectionTitle(title = "6. Kontak Kami")
                        Text(
                            text = "Jika Anda memiliki pertanyaan tentang Kebijakan Privasi ini, silakan hubungi kami di: indra.oktawan@gmail.com",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "© 2026 PT.BERTANI TEKNOLOGI INTELEGENSI. Semua Hak Dilindungi.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.outline
                        )

                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                        // SOFTWARE LICENSE AGREEMENT
                        Text(
                            text = "SOFTWARE LICENSE AGREEMENT: \"ASISTEN PETANI\" (ASPEK)",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "Copyright (c) 2026 Indra Oktawan. All Rights Reserved.",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.SemiBold
                        )

                        PolicySectionTitle(title = "1. OWNERSHIP")
                        Text(
                            text = "This software, including but not limited to its source code, original designs, and AI logic, is the exclusive legal property of Indra Oktawan.",
                            style = MaterialTheme.typography.bodyMedium
                        )

                        PolicySectionTitle(title = "2. DISTRIBUTION RESTRICTIONS")
                        Text(
                            text = "The right to distribute, publish, or sell this software in any form (including APK, source code, or API services) is strictly reserved for the authorized account holder, Indra Oktawan.",
                            style = MaterialTheme.typography.bodyMedium
                        )

                        PolicySectionTitle(title = "3. PERSONAL USE ONLY")
                        Text(
                            text = "Users are granted permission to use this application solely for the purpose of assisting with agricultural activities. Users are strictly prohibited from duplicating, modifying, or redistributing the software without express written consent from the copyright owner.",
                            style = MaterialTheme.typography.bodyMedium
                        )

                        PolicySectionTitle(title = "4. PROHIBITION OF REVERSE ENGINEERING")
                        Text(
                            text = "Reverse engineering, decompilation, or any attempt to extract the source code from the compiled application is strictly prohibited and constitutes a violation of this agreement.",
                            style = MaterialTheme.typography.bodyMedium
                        )

                        PolicySectionTitle(title = "5. PROPRIETARY AI TECHNOLOGY")
                        Text(
                            text = "The integration of Google Gemini technology within this application is based on a proprietary configuration developed by the owner. Any unauthorized use, replication, or extraction of this integration method is strictly forbidden.",
                            style = MaterialTheme.typography.bodyMedium
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    text = "⚠️ COPYRIGHT & LICENSING NOTICE",
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.error
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "The Asisten Petani Kita (ASPEK) project is PROPRIETARY software.\n" +
                                            "• NO REDISTRIBUTION: This code is not licensed under Open Source terms. Sole distribution rights are held exclusively by Indra Oktawan.\n" +
                                            "• COMMERCIAL USE: It is strictly forbidden to use, copy, or resell any part of this technology for personal or group financial gain without written authorization.\n" +
                                            "• MONITORING & ENFORCEMENT: Any form of cloning or development of similar applications proven to use the codebase or underlying logic of this project will be reported as a copyright infringement (DMCA).\n" +
                                            "ANY VIOLATION OF THESE TERMS WILL BE PROSECUTED TO THE FULLEST EXTENT OF APPLICABLE COPYRIGHT LAWS.",
                                    fontSize = 12.sp,
                                    lineHeight = 16.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PolicySectionTitle(title: String) {
    Spacer(modifier = Modifier.height(6.dp))
    Text(
        text = title,
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary
    )
}

@Composable
fun PolicyBulletPoint(boldText: String = "", text: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 4.dp),
        horizontalArrangement = Arrangement.Start
    ) {
        if (boldText.isEmpty()) {
            Text(
                text = text,
                style = MaterialTheme.typography.bodyMedium
            )
        } else {
            Text(
                text = "• ",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold
            )
            Column {
                Text(
                    text = boldText + text,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}
