package com.asistenpetani.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.asistenpetani.ui.viewmodel.AppViewModel
import com.asistenpetani.ui.components.PrivacyPolicyDialog

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: AppViewModel,
    isAnonymous: Boolean = false,
    onBackClick: () -> Unit,
    onLogoutClick: () -> Unit,
    onBukaLangganan: () -> Unit = {},
    onBukaKebijakanPrivasi: () -> Unit = {},
    onVerifikasiEmail: (String, String) -> Unit = { _, _ -> }
) {
    val profile by viewModel.farmerProfile.collectAsState()
    val isPremium by viewModel.isPremium.collectAsState()
    val scrollState = rememberScrollState()
    val context = androidx.compose.ui.platform.LocalContext.current
    var showPrivacyDialog by remember { mutableStateOf(false) }
    var showLinkEmailDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Pengaturan Akun",
                        fontWeight = FontWeight.Bold,
                        color = MidnightBlue,
                        fontSize = 20.sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back to Dashboard",
                            tint = MidnightBlue
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        containerColor = EarthBackground
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(scrollState)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Profile Card Area
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("profile_card"),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.5.dp, Color(0xFFF1F5F9)),
                shape = RoundedCornerShape(24.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Row(
                    modifier = Modifier.padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color(0xFFEFF6FF)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.AccountCircle,
                            contentDescription = null,
                            tint = MediumBlue,
                            modifier = Modifier.size(44.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = profile?.name ?: "Indra Oktawan",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = MidnightBlue
                        )
                        Text(
                            text = "PETANI ${(profile?.cropType ?: "Kakao").uppercase()} • KOLAKA, SULTRA",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.LightGray,
                            letterSpacing = 0.5.sp
                        )
                    }
                }
            }

            // Jika pengguna adalah tamu, tampilkan peringatan untuk memverifikasi email
            if (isAnonymous) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showLinkEmailDialog = true },
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFEF2F2)),
                    border = BorderStroke(1.5.dp, Color(0xFFFCA5A5)),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = "Peringatan",
                            tint = Color(0xFFDC2626),
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Akun Tamu (Tidak Aman)",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF991B1B)
                            )
                            Text(
                                text = "Tautkan email Anda sekarang agar akun dan data pertanian Anda tidak terhapus otomatis dalam 30 hari.",
                                fontSize = 12.sp,
                                color = Color(0xFFB91C1C)
                            )
                        }
                        Icon(
                            imageVector = Icons.Default.ChevronRight,
                            contentDescription = null,
                            tint = Color(0xFFDC2626)
                        )
                    }
                }
            }

            // Premium Membership Option Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("premium_card"),
                colors = CardDefaults.cardColors(containerColor = if (isPremium) Color(0xFFEFF6FF) else Color(0xFFFFFBEB)),
                border = BorderStroke(2.dp, if (isPremium) Color(0xFF2563EB) else Color(0xFFFBBF24)),
                shape = RoundedCornerShape(24.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("👑", fontSize = 24.sp)
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "Premium ASPEK",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MidnightBlue
                                )
                                Text(
                                    text = "Rp 25.000 / Bulan",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFFD97706)
                                )
                            }
                        }
                        Switch(
                            checked = isPremium,
                            onCheckedChange = { viewModel.togglePremiumStatus() },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = Color(0xFF2563EB)
                            )
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(14.dp))
                    
                    // Subscription Perks List
                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color.White.copy(alpha = 0.6f))
                            .padding(12.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("✅", fontSize = 14.sp)
                            Text("Bebas sepenuhnya dari seluruh iklan", fontSize = 12.sp, fontWeight = FontWeight.Medium, color = MidnightBlue)
                        }
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("💬", fontSize = 14.sp)
                            Text("Konsultasi obrolan AI tanpa batasan kuota (Unlimited)", fontSize = 12.sp, fontWeight = FontWeight.Medium, color = MidnightBlue)
                        }
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("🛡️", fontSize = 14.sp)
                            Text("Pembayaran resmi & aman lewat sistem terverifikasi", fontSize = 12.sp, fontWeight = FontWeight.Medium, color = MidnightBlue)
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    if (isPremium) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFFDCFCE7))
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = "✨ Keanggotaan Aktif — Terimakasih atas Dukungan Anda!",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF15803D)
                            )
                        }
                    } else {
                        Button(
                            onClick = { viewModel.togglePremiumStatus() },
                            modifier = Modifier.fillMaxWidth().height(48.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF59E0B)),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Text("Langganan Sekarang via Sistem Pembayaran Resmi", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    
                    // Developer and compliance info
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = "Sistem Pembayaran Resmi & Terverifikasi",
                            fontSize = 9.sp,
                            color = Color.Gray,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "Developer Account: ID #5106747258949665772",
                            fontSize = 9.sp,
                            color = Color.Gray,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
                          // EDUKASI & PENGETAHUAN PERTANIAN SULTRA Section (Now stored safely in background AI memory)

            // Menu navigations options
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // GCP Environment Settings Card
                var showEnvSelector by remember { mutableStateOf(false) }
                val activeEnv by viewModel.selectedEnvironment.collectAsState()

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("gcp_env_card")
                        .clickable { showEnvSelector = !showEnvSelector },
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.5.dp, Color(0xFFF1F5F9)),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(Color(0xFFEFF6FF))
                                        .padding(8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Cloud,
                                        contentDescription = null,
                                        tint = Color(0xFF2563EB),
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Column {
                                    Text(
                                        "Server & Lingkungan GCP",
                                        fontWeight = FontWeight.Bold,
                                        color = MidnightBlue,
                                        fontSize = 14.sp
                                    )
                                    Text(
                                        text = activeEnv.name,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Color(0xFF15803D)
                                    )
                                }
                            }
                            Icon(
                                imageVector = if (showEnvSelector) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                contentDescription = null,
                                tint = Color.LightGray
                            )
                        }

                        if (showEnvSelector) {
                            HorizontalDivider(color = Color(0xFFF1F5F9))
                            Column(
                                verticalArrangement = Arrangement.spacedBy(10.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                viewModel.gcpEnvironments.forEach { env ->
                                    val isSelected = env.key == activeEnv.key
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(if (isSelected) Color(0xFFEFF6FF) else Color(0xFFF8FAFC))
                                            .border(
                                                1.5.dp,
                                                if (isSelected) Color(0xFF2563EB) else Color.Transparent,
                                                RoundedCornerShape(12.dp)
                                            )
                                            .clickable { viewModel.selectEnvironment(env.key) }
                                            .padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        RadioButton(
                                            selected = isSelected,
                                            onClick = { viewModel.selectEnvironment(env.key) },
                                            colors = RadioButtonDefaults.colors(
                                                selectedColor = Color(0xFF2563EB)
                                            )
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = env.name,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 12.sp,
                                                color = MidnightBlue
                                            )
                                            Text(
                                                text = env.description,
                                                fontSize = 10.sp,
                                                color = Color.Gray,
                                                lineHeight = 13.sp
                                            )
                                            Spacer(modifier = Modifier.height(2.dp))
                                            Text(
                                                text = env.url,
                                                fontStyle = FontStyle.Italic,
                                                fontSize = 9.sp,
                                                color = Color(0xFF2563EB),
                                                fontWeight = FontWeight.Medium
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                
                // Subscription button
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(onClick = onBukaLangganan),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.5.dp, Color(0xFFF1F5F9)),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(Color(0xFFFFFBEB))
                                    .padding(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Star,
                                    contentDescription = null,
                                    tint = Color(0xFFF59E0B),
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Text(
                                "Berlangganan Bebas Iklan",
                                fontWeight = FontWeight.Bold,
                                color = MidnightBlue,
                                fontSize = 14.sp
                            )
                        }
                        Icon(
                            imageVector = Icons.Default.ChevronRight,
                            contentDescription = null,
                            tint = Color.LightGray
                        )
                    }
                }

                // Settings button 1 - Detail Kebijakan Privasi Penuh
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            onBukaKebijakanPrivasi()
                        },
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.5.dp, Color(0xFFF1F5F9)),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(Color(0xFFF0FDF4))
                                    .padding(8.dp)
                                ) {
                                Icon(
                                    imageVector = Icons.Default.VerifiedUser,
                                    contentDescription = null,
                                    tint = AspekGreen,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Text(
                                "Kebijakan Privasi & Ketentuan Layanan",
                                fontWeight = FontWeight.Bold,
                                color = MidnightBlue,
                                fontSize = 14.sp
                            )
                        }
                        Icon(
                            imageVector = Icons.Default.ChevronRight,
                            contentDescription = null,
                            tint = Color.LightGray
                        )
                    }
                }

                // Settings button 1b - Dialog Dokumen Legal & Lisensi
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            showPrivacyDialog = true
                        },
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.5.dp, Color(0xFFF1F5F9)),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(Color(0xFFF0FDF4))
                                    .padding(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PrivacyTip,
                                    contentDescription = null,
                                    tint = AspekGreen,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Text(
                                "Dokumen Legal ASPEK (Lisensi & EULA)",
                                fontWeight = FontWeight.Bold,
                                color = MidnightBlue,
                                fontSize = 14.sp
                            )
                        }
                        Icon(
                            imageVector = Icons.Default.ChevronRight,
                            contentDescription = null,
                            tint = Color.LightGray
                        )
                    }
                }

                // OSS Licenses button
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            try {
                                context.startActivity(android.content.Intent(context, com.google.android.gms.oss.licenses.OssLicensesMenuActivity::class.java))
                            } catch (e: Exception) {
                                android.widget.Toast.makeText(context, "Lisensi Terbuka: MIT / Apache 2.0 / Google Play Services", android.widget.Toast.LENGTH_SHORT).show()
                            }
                        },
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.5.dp, Color(0xFFF1F5F9)),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(Color(0xFFEFF6FF))
                                    .padding(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Info,
                                    contentDescription = null,
                                    tint = MediumBlue,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Text(
                                "Lisensi Pihak Ketiga",
                                fontWeight = FontWeight.Bold,
                                color = MidnightBlue,
                                fontSize = 14.sp
                            )
                        }
                        Icon(
                            imageVector = Icons.Default.ChevronRight,
                            contentDescription = null,
                            tint = Color.LightGray
                        )
                    }
                }


                // Settings Logout Button
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(onClick = onLogoutClick),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.5.dp, Color(0xFFF1F5F9)),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color(0xFFFEF2F2))
                                .padding(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                                contentDescription = null,
                                tint = Color.Red,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Text(
                            "Keluar Akun",
                            fontWeight = FontWeight.Bold,
                            color = Color.Red,
                            fontSize = 14.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Footer Section
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    "SUTRA FARMER OPTIMIZATION • KOLAKA, INDONESIA",
                    fontSize = 9.sp,
                    color = Color.Gray,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    "© 2026 Asisten Petani Kita Project • Dikembangkan oleh PT. Bertani teknologi intelegensi",
                    fontSize = 8.sp,
                    color = Color.LightGray,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }

    if (showPrivacyDialog) {
        PrivacyPolicyDialog(onDismiss = { showPrivacyDialog = false })
    }

    if (showLinkEmailDialog) {
        AlertDialog(
            onDismissRequest = { showLinkEmailDialog = false },
            title = {
                Text(
                    text = "Amankan Akun Anda",
                    fontWeight = FontWeight.Bold,
                    color = MidnightBlue
                )
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Untuk keamanan maksimal dan pencadangan data otomatis tanpa ribet menghafal kata sandi, kami menyarankan Anda login menggunakan Akun Utama.\n\nSilakan keluar dari mode tamu dan klik tombol 'MASUK' di halaman awal.",
                        fontSize = 14.sp,
                        color = Color.Gray
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showLinkEmailDialog = false
                        onLogoutClick()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AspekGreen)
                ) {
                    Text("Keluar & Login", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showLinkEmailDialog = false }) {
                    Text("Batal", color = MidnightBlue)
                }
            },
            containerColor = Color.White
        )
    }
}
