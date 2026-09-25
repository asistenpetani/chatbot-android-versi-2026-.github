package com.asistenpetani.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Policy
import androidx.compose.material.icons.filled.PrivacyTip
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun PrivacyPolicyDialog(
    onDismiss: () -> Unit
) {
    var selectedTab by remember { mutableStateOf(0) } // 0 = Privasi, 1 = Lisensi SLA, 2 = Regulasi & Dep

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF16A34A) // AspekGreen
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Tutup & Setuju", fontWeight = FontWeight.Bold, color = Color.White)
            }
        },
        title = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(
                        imageVector = if (selectedTab == 0) Icons.Default.PrivacyTip else Icons.Default.VerifiedUser,
                        contentDescription = "Kebijakan & Lisensi",
                        tint = Color(0xFF16A34A),
                        modifier = Modifier.size(28.dp)
                    )
                    Text(
                        text = "Dokumen Legal ASPEK",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = Color(0xFF191970) // MidnightBlue
                    )
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // Segments Tab Row for beautiful design navigation switching
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = Color.Transparent,
                    contentColor = Color(0xFF16A34A),
                    modifier = Modifier.fillMaxWidth().height(42.dp),
                    indicator = { tabPositions ->
                        if (tabPositions.isNotEmpty() && selectedTab < tabPositions.size) {
                            TabRowDefaults.SecondaryIndicator(
                                Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                                color = Color(0xFF16A34A)
                            )
                        }
                    }
                ) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = { Text("Privasi", fontSize = 10.sp, fontWeight = FontWeight.Bold) }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = { Text("Ketentuan", fontSize = 10.sp, fontWeight = FontWeight.Bold) }
                    )
                    Tab(
                        selected = selectedTab == 2,
                        onClick = { selectedTab = 2 },
                        text = { Text("Hak Cipta", fontSize = 10.sp, fontWeight = FontWeight.Bold) }
                    )
                }
            }
        },
        text = {
            val scrollState = rememberScrollState()
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 400.dp)
                    .verticalScroll(scrollState),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                when (selectedTab) {
                    0 -> {
                        // TAB 1: Privasi - Fokus: Perlindungan data pengguna dan kepatuhan UU PDP
                        Column(
                            verticalArrangement = Arrangement.spacedBy(14.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "Kebijakan Privasi & Perlindungan Data",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = Color(0xFF191970)
                            )
                            Text(
                                text = "Terima kasih telah menggunakan Asisten Petani Kita (ASPEK). Kebijakan ini menjelaskan bagaimana kami mengumpulkan, menggunakan, dan melindungi informasi Anda sesuai dengan UU No. 27 Tahun 2022 tentang Pelindungan Data Pribadi (UU PDP).",
                                fontSize = 13.sp,
                                lineHeight = 18.sp,
                                color = Color(0xFF334155)
                            )

                            HorizontalDivider(color = Color(0xFFE2E8F0))

                            PolicyBulletItem(
                                title = "Hak Subjek Data:",
                                description = "Pengguna memiliki hak untuk mengakses, memutakhirkan, membatasi pemrosesan, menarik kembali persetujuan, dan meminta penghapusan data profil atau riwayat tanya-jawab. Untuk menjalankan hak ini, silakan hubungi kami di: petaniasisten@gmail.com."
                            )
                            PolicyBulletItem(
                                title = "Metode Penyimpanan:",
                                description = "Data disimpan secara luring (offline) di penyimpanan lokal telepon Anda (menggunakan Offline-First Encrypted State di Room SQLite). Seluruh transmisi data selama konsultasi AI dienkripsi menggunakan protokol TLS 1.3."
                            )
                            PolicyBulletItem(
                                title = "Data Identitas:",
                                description = "Detail lahan (nama, lokasi, komoditas) digunakan eksklusif untuk personalisasi saran nutrisi tanaman Anda."
                            )
                            PolicyBulletItem(
                                title = "Anonimisasi AI:",
                                description = "Hasil analisa foto dianonimkan secara otomatis sebelum diproses oleh Google Gemini. Kami tidak mengirimkan identitas asli Anda ke model AI."
                            )
                            PolicyBulletItem(
                                title = "Keamanan & Autentikasi:",
                                description = "Kami menggunakan infrastruktur Google Firebase untuk mengelola otentikasi akun yang aman. Domain default projek awan Firebase terdaftar resmi di asistenpetani-ai-app.web.app dan asistenpetani-ai-app.firebaseapp.com untuk menangani alur kredensial masuk Android."
                            )
                        }
                    }
                    1 -> {
                        // TAB 2: Ketentuan Layanan - Aturan penggunaan aplikasi dan batasan AI
                        Column(
                            verticalArrangement = Arrangement.spacedBy(14.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "Ketentuan Layanan Pengguna",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = Color(0xFF191970)
                            )
                            Text(
                                text = "Dengan menggunakan ASPEK, Anda menyetujui ketentuan berikut:",
                                fontSize = 13.sp,
                                lineHeight = 18.sp,
                                color = Color(0xFF334155)
                            )

                            HorizontalDivider(color = Color(0xFFE2E8F0))

                            PolicyBulletItem(
                                title = "Batasan AI:",
                                description = "Asisten Petani Kita menggunakan teknologi AI untuk memberikan wawasan pertanian. Output AI bersifat sebagai rekomendasi pendukung. Petani diwajibkan untuk tetap berkoordinasi sebelum menerapkan saran dari kecerdasan buatan."
                            )
                            PolicyBulletItem(
                                title = "Merek Dagang Pihak Ketiga:",
                                description = "Seluruh penyebutan merek dagang, nama produk, dan nama perusahaan industri pihak ketiga oleh AI Assistant adalah murni bagian dari rekomendasi kecerdasan buatan. Hal tersebut tidak menyiratkan afiliasi, sponsor, atau dukungan dari pemilik merek terkait. Semua merek adalah hak milik masing-masing perusahaannya."
                            )
                            PolicyBulletItem(
                                title = "Larangan Rekayasa Balik:",
                                description = "Reverse engineering, dekompilasi, atau upaya ekstraksi kode sumber dari aplikasi ini dilarang keras dan melanggar perjanjian ini."
                            )
                            PolicyBulletItem(
                                title = "Penggunaan Teknologi:",
                                description = "Integrasi Google Gemini dalam aplikasi didasarkan pada konfigurasi khusus milik pengembang. Penggunaan, replikasi, atau ekstraksi metode integrasi ini tanpa izin tertulis adalah pelanggaran."
                            )
                            PolicyBulletItem(
                                title = "Perubahan Layanan:",
                                description = "Kami berhak melakukan pembaruan sistem untuk meningkatkan akurasi data pertanian demi kenyamanan pengguna aplikasi kami."
                            )
                        }
                    }
                    2 -> {
                        // TAB 3: Hak Cipta - 3. Kepemilikan Data, Kode, & Atribusi
                        Column(
                            verticalArrangement = Arrangement.spacedBy(14.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "3. Kepemilikan Data, Kode, & Atribusi",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = Color(0xFF191970)
                            )

                            HorizontalDivider(color = Color(0xFFE2E8F0))

                            PolicyBulletItem(
                                title = "Hak Eksklusif (SaaS & EULA):",
                                description = "Aplikasi ini dilisensikan di bawah model Proprietary Software-as-a-Service (SaaS) EULA. Seluruh basis pengetahuan, algoritma prompt, desain antarmuka, dan arsitektur data (termasuk pengetahuan_analisa_gambar.json) adalah kekayaan intelektual dan rahasia dagang (Trade Secret) eksklusif milik Indra Oktawan."
                            )
                            PolicyBulletItem(
                                title = "Atribusi Pihak Ketiga:",
                                description = "Aplikasi ini menggunakan beberapa pustaka kode yang dilisensikan di bawah MIT License. Penggunaan pustaka tersebut tunduk pada persyaratan lisensi masing-masing. Kode sumber asli dari pustaka pihak ketiga tersebut tetap menjadi milik pemegang hak ciptanya masing-masing."
                            )
                            PolicyBulletItem(
                                title = "Larangan Penggunaan:",
                                description = "Dilarang mengekstraksi, mereplikasi, atau menggunakan basis pengetahuan dan logika internal ASPEK untuk tujuan komersial atau pengembangan aplikasi lain tanpa izin tertulis dari pengembang."
                            )

                            HorizontalDivider(color = Color(0xFFE2E8F0))

                            Surface(
                                color = Color(0xFFEFF6FF),
                                shape = RoundedCornerShape(12.dp),
                                border = BorderStroke(1.dp, Color(0xFFBFDBFE)),
                                modifier = Modifier.fillMaxWidth().padding(top = 4.dp)
                            ) {
                                Column(
                                    modifier = Modifier.padding(12.dp),
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(
                                        text = "Copyright (c) 2026 Indra Oktawan. All Rights Reserved.\nProprietary Software & Trade Secret.",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF1E40AF)
                                    )
                                    Text(
                                        text = "Email: indra.oktawan@gmail.com",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF16A34A)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        },
        shape = RoundedCornerShape(24.dp),
        containerColor = Color.White
    )
}

@Composable
private fun SlaSection(num: String, title: String, desc: String) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "$num. $title",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF0F172A)
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = desc,
            fontSize = 11.sp,
            lineHeight = 15.sp,
            color = Color(0xFF475569)
        )
    }
}

@Composable
private fun PolicySectionTitle(text: String) {
    Text(
        text = text,
        fontWeight = FontWeight.Bold,
        fontSize = 13.sp,
        color = Color(0xFF191970) // MidnightBlue
    )
}

@Composable
private fun PolicyBulletItem(
    title: String? = null,
    description: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 8.dp),
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text("•", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF16A34A))
        Text(
            text = androidx.compose.ui.text.buildAnnotatedString {
                if (title != null) {
                    append(title)
                    append(" ")
                }
                append(description)
            },
            fontSize = 12.sp,
            lineHeight = 16.sp,
            color = Color(0xFF334155)
        )
    }
}
