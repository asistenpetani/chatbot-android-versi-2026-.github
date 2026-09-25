package com.asistenpetani.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material.icons.filled.Landscape
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Spa
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import com.asistenpetani.ui.components.PrivacyPolicyDialog
import com.asistenpetani.ui.components.TermsOfServiceAgreement
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// ASPEK Colors match blueprint
val MidnightBlue = Color(0xFF191970)
val MediumBlue = Color(0xFF0000CD)
val OrangeRed = Color(0xFFFF4500)
val EarthBackground = Color(0xFFF0FDF4)
val AspekGreen = Color(0xFF16A34A)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    onLoginClick: (name: String, crop: String, area: String, kategori: String, lokasi: Boolean) -> Unit,
    onAdminLoginClick: (email: String, sandi: String) -> Unit,
    onGoogleSignInClick: () -> Unit = {},
    onAnonymousLoginClick: (name: String, crop: String, area: String, kategori: String, lokasi: Boolean) -> Unit = { _, _, _, _, _ -> },
    onRegisterClick: () -> Unit = {},
    onEmailLoginClick: (String, String) -> Unit = { _, _ -> }
) {
    var nama by remember { mutableStateOf("") }
    var emailAdmin by remember { mutableStateOf("") }
    var sandiAdmin by remember { mutableStateOf("") }
    var emailAdminError by remember { mutableStateOf("") }
    var kategoriBudidaya by remember { mutableStateOf("Budidaya Tanaman") }
    var jenisTanaman by remember { mutableStateOf("") }
    var luasLahan by remember { mutableStateOf("1.5") }
    
    var isLokasiAktif by remember { mutableStateOf(true) }
    var isPrivasiDisetujui by remember { mutableStateOf(true) }
    var showPrivacyDialog by remember { mutableStateOf(false) }
    var showAdminDialog by remember { mutableStateOf(false) }

    var namaError by remember { mutableStateOf(false) }
    var privasiError by remember { mutableStateOf(false) }

    var expanded by remember { mutableStateOf(false) }
    val tanamanOptions = listOf("Padi Sultra", "Jenis Tanaman", "Bawang Merah", "Jagung", "Kelapa Sawit", "Kopi", "Cengkeh")

    val keyboardController = LocalSoftwareKeyboardController.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(EarthBackground, Color.White)
                )
            )
            .windowInsetsPadding(WindowInsets.safeDrawing)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            // Top App Bar (Material 3 Style matching HTML Design)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp, horizontal = 4.dp),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Color.White),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Spa,
                        contentDescription = "Leaf Logo",
                        tint = AspekGreen,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Branding Section (Matching HTML layout & text style)
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(bottom = 24.dp)
            ) {
                Text(
                    text = "Asisten Petani Kita",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Black,
                    color = Color(0xFF2563EB),
                    letterSpacing = (-1.5).sp,
                    lineHeight = 48.sp
                )
            }

            // Registration Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("login_form_card"),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp), // Slate shadowed looks
                shape = RoundedCornerShape(32.dp),
                border = BorderStroke(2.dp, Color(0xFFF1F5F9)) // 2px border slate matching HTML border-2 border-slate-100
            ) {
                Column(
                    modifier = Modifier.padding(24.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Mulai Bersama",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = MidnightBlue
                        )
                        Box(
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(EarthBackground)
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "V9.0 RELEASE",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = AspekGreen,
                                letterSpacing = 0.5.sp
                            )
                        }
                    }

                    // QUICK FARMER REGISTRATION
                    // Input Nama Lengkap
                        OutlinedTextField(
                            value = nama,
                            onValueChange = { 
                                nama = it 
                                if (it.isNotBlank()) namaError = false
                            },
                            label = { Text("Masukkan nama lengkap", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.ExtraBold) },
                            placeholder = { Text("Nama lengkap Anda") },
                            textStyle = TextStyle(color = Color(0xFF0F172A), fontSize = 16.sp, fontWeight = FontWeight.Bold),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("nama_input"),
                            leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = MidnightBlue) },
                            isError = namaError,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color(0xFF0F172A),
                                unfocusedTextColor = Color(0xFF0F172A),
                                focusedBorderColor = Color(0xFF2563EB),
                                unfocusedBorderColor = Color(0xFF94A3B8),
                                focusedContainerColor = Color.White,
                                unfocusedContainerColor = Color.White,
                                focusedLabelColor = Color(0xFF2563EB),
                                unfocusedLabelColor = Color(0xFF475569),
                                errorBorderColor = OrangeRed,
                                errorLabelColor = OrangeRed,
                                errorLeadingIconColor = OrangeRed
                            ),
                            singleLine = true,
                            shape = RoundedCornerShape(16.dp)
                        )
                        if (namaError) {
                            Text(
                                text = "⚠ Nama lengkap wajib diisi untuk mendaftar.",
                                color = OrangeRed,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(start = 4.dp, top = 4.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = "Kategori Budidaya",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = MidnightBlue,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            // Card 1: Budidaya Tanaman
                            Card(
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { 
                                        kategoriBudidaya = "Budidaya Tanaman" 
                                        jenisTanaman = ""
                                    }
                                    .testTag("kategori_tanaman_card"),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (kategoriBudidaya == "Budidaya Tanaman") Color(0xFFEFF6FF) else Color(0xFFF8FAFC)
                                ),
                                border = BorderStroke(
                                    width = 1.5.dp,
                                    color = if (kategoriBudidaya == "Budidaya Tanaman") Color(0xFF2563EB) else Color(0xFFE2E8F0)
                                ),
                                shape = RoundedCornerShape(16.dp)
                            ) {
                                Column(
                                    modifier = Modifier.padding(12.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Spa,
                                        contentDescription = "Tanaman",
                                        tint = if (kategoriBudidaya == "Budidaya Tanaman") Color(0xFF2563EB) else Color.Gray,
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "Tanaman",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (kategoriBudidaya == "Budidaya Tanaman") Color(0xFF2563EB) else Color.Gray
                                    )
                                }
                            }

                            // Card 2: Budidaya Perikanan
                            Card(
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { 
                                        kategoriBudidaya = "Budidaya Perikanan" 
                                        jenisTanaman = ""
                                    }
                                    .testTag("kategori_perikanan_card"),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (kategoriBudidaya == "Budidaya Perikanan") Color(0xFFF0FDF4) else Color(0xFFF8FAFC)
                                ),
                                border = BorderStroke(
                                    width = 1.5.dp,
                                    color = if (kategoriBudidaya == "Budidaya Perikanan") AspekGreen else Color(0xFFE2E8F0)
                                ),
                                shape = RoundedCornerShape(16.dp)
                            ) {
                                Column(
                                    modifier = Modifier.padding(12.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.WaterDrop,
                                        contentDescription = "Perikanan",
                                        tint = if (kategoriBudidaya == "Budidaya Perikanan") AspekGreen else Color.Gray,
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "Perikanan",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (kategoriBudidaya == "Budidaya Perikanan") AspekGreen else Color.Gray
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            // Bebas input commodity text field
                            OutlinedTextField(
                                value = jenisTanaman,
                                onValueChange = { jenisTanaman = it },
                                label = { 
                                    Text(
                                        text = if (kategoriBudidaya == "Budidaya Tanaman") "Komoditas Tanaman (Bebas)" else "Komoditas Air (Bebas)", 
                                        fontSize = 11.sp, 
                                        fontWeight = FontWeight.ExtraBold, 
                                        color = Color(0xFF1E293B)
                                    ) 
                                },
                                textStyle = TextStyle(color = Color(0xFF0F172A), fontSize = 15.sp, fontWeight = FontWeight.Bold),
                                leadingIcon = { 
                                    Icon(
                                        imageVector = if (kategoriBudidaya == "Budidaya Tanaman") Icons.Default.Spa else Icons.Default.WaterDrop, 
                                        contentDescription = null, 
                                        tint = MidnightBlue
                                    ) 
                                },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = Color(0xFF0F172A),
                                    unfocusedTextColor = Color(0xFF0F172A),
                                    focusedBorderColor = if (kategoriBudidaya == "Budidaya Tanaman") Color(0xFF2563EB) else AspekGreen,
                                    unfocusedBorderColor = Color(0xFF94A3B8),
                                    focusedContainerColor = Color.White,
                                    unfocusedContainerColor = Color.White,
                                    focusedLabelColor = if (kategoriBudidaya == "Budidaya Tanaman") Color(0xFF2563EB) else AspekGreen,
                                    unfocusedLabelColor = Color(0xFF475569)
                                ),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("tanaman_input"),
                                singleLine = true,
                                shape = RoundedCornerShape(16.dp)
                            )

                            // Input Luas Lahan (Ha)
                            OutlinedTextField(
                                value = luasLahan,
                                onValueChange = { luasLahan = it },
                                label = { 
                                    Text(
                                        text = if (kategoriBudidaya == "Budidaya Tanaman") "Luas (Ha)" else "Luas (Ha/M²)", 
                                        fontSize = 11.sp, 
                                        fontWeight = FontWeight.ExtraBold, 
                                        color = Color(0xFF1E293B)
                                    ) 
                                },
                                textStyle = TextStyle(color = Color(0xFF0F172A), fontSize = 15.sp, fontWeight = FontWeight.Bold),
                                modifier = Modifier
                                    .width(100.dp)
                                    .testTag("lahan_input"),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = Color(0xFF0F172A),
                                    unfocusedTextColor = Color(0xFF0F172A),
                                    focusedBorderColor = if (kategoriBudidaya == "Budidaya Tanaman") Color(0xFF2563EB) else AspekGreen,
                                    unfocusedBorderColor = Color(0xFF94A3B8),
                                    focusedContainerColor = Color.White,
                                    unfocusedContainerColor = Color.White,
                                    focusedLabelColor = if (kategoriBudidaya == "Budidaya Tanaman") Color(0xFF2563EB) else AspekGreen,
                                    unfocusedLabelColor = Color(0xFF475569)
                                ),
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Number,
                                    imeAction = ImeAction.Done
                                ),
                                keyboardActions = KeyboardActions(onDone = { keyboardController?.hide() }),
                                singleLine = true,
                                shape = RoundedCornerShape(16.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Terms of Service & Privacy Policy Acceptance
                        TermsOfServiceAgreement(
                            isAccepted = isPrivasiDisetujui,
                            onAcceptanceChanged = { 
                                isPrivasiDisetujui = it 
                                if (it) privasiError = false
                            },
                            onReadPolicyClick = { showPrivacyDialog = true },
                            hasError = privasiError
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        Button(
                            onClick = {
                                if (nama.isBlank()) {
                                    namaError = true
                                }
                                if (!isPrivasiDisetujui) {
                                    privasiError = true
                                }
                                if (nama.isNotBlank() && isPrivasiDisetujui) {
                                    val cropSelection = if (jenisTanaman.isBlank()) "Jenis Tanaman" else jenisTanaman
                                    val areaValue = if (luasLahan.isBlank()) "1.2" else luasLahan
                                    onAnonymousLoginClick(nama, cropSelection, areaValue, kategoriBudidaya, false)
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp)
                                .testTag("login_sync_button"),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF1D4ED8),
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(14.dp),
                            elevation = ButtonDefaults.buttonElevation(defaultElevation = 3.dp)
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "MASUK SEKARANG",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color.White,
                                    letterSpacing = 1.sp
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Icon(
                                    imageVector = Icons.Default.Sync,
                                    contentDescription = "Sync",
                                    modifier = Modifier.size(20.dp),
                                    tint = Color.White
                                )
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        OutlinedButton(
                            onClick = {
                                if (!isPrivasiDisetujui) {
                                    privasiError = true
                                } else {
                                    val namaPengguna = if (nama.isBlank()) "Petani" else nama
                                    val cropSelection = if (jenisTanaman.isBlank()) "Jenis Tanaman" else jenisTanaman
                                    val areaValue = if (luasLahan.isBlank()) "1.2" else luasLahan
                                    onLoginClick(namaPengguna, cropSelection, areaValue, kategoriBudidaya, false)
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                            shape = RoundedCornerShape(14.dp),
                            border = BorderStroke(1.dp, Color(0xFFCBD5E1)),
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = Color.White,
                                contentColor = Color(0xFF334155)
                            )
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "MASUK DENGAN GOOGLE",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Black,
                                    letterSpacing = 1.sp
                                )
                            }
                        Spacer(modifier = Modifier.height(16.dp))
                        OutlinedButton(
                            onClick = { showAdminDialog = true },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                            shape = RoundedCornerShape(14.dp),
                            border = BorderStroke(1.dp, Color(0xFFCBD5E1)),
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = Color(0xFFF1F5F9),
                                contentColor = Color(0xFF334155)
                            )
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "LOGIN DENGAN EMAIL & SANDI",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 0.5.sp
                                )
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        TextButton(
                            onClick = onRegisterClick,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "Belum punya akun? Daftar Akun Baru",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1D4ED8)
                            )
                        }
                        }
                 }
             }

            Spacer(modifier = Modifier.height(24.dp))

            // Support info pulse tag from the HTML theme
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier
                    .clip(CircleShape)
                    .background(Color(0xFFEFF6FF))
                    .border(BorderStroke(1.dp, Color(0xFFDBEAFE)), CircleShape)
                    .padding(horizontal = 16.dp, vertical = 6.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF3B82F6))
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "GEMINI 3.5 FLASH INTEGRATED",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Black,
                    color = Color(0xFF2563EB),
                    letterSpacing = 0.5.sp
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Fine Print Footer Footer consistent with Sultra Design HTML
            Text(
                "SUTRA FARMER OPTIMIZATION • KOLAKA, INDONESIA",
                fontSize = 9.sp,
                color = Color.Gray,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                "© 2026 Indra Oktawan • Proprietary Software",
                fontSize = 8.sp,
                color = Color.LightGray,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    if (showPrivacyDialog) {
        PrivacyPolicyDialog(onDismiss = { showPrivacyDialog = false })
    }
    if (showAdminDialog) {
        AlertDialog(
            onDismissRequest = { showAdminDialog = false },
            title = { Text("Login dengan Email", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    OutlinedTextField(
                        value = emailAdmin,
                        onValueChange = { emailAdmin = it },
                        label = { Text("Email") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = sandiAdmin,
                        onValueChange = { sandiAdmin = it },
                        label = { Text("Kata Sandi") },
                        singleLine = true,
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(onClick = {
                    showAdminDialog = false
                    onEmailLoginClick(emailAdmin, sandiAdmin)
                }) {
                    Text("Masuk")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAdminDialog = false }) {
                    Text("Batal")
                }
            }
        )
    }
}

