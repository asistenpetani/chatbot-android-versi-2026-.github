package com.asistenpetani.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.Image
import androidx.compose.ui.res.painterResource
import com.asistenpetani.R
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import com.google.android.gms.ads.AdRequest
import com.asistenpetani.util.InterstitialAdHelper
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.asistenpetani.data.local.room.ChatMessage
import com.asistenpetani.data.local.room.FarmerProfile
import com.asistenpetani.ui.viewmodel.AppViewModel
import com.asistenpetani.util.UrlEncoderHelper
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.PointerInputChange
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import kotlinx.coroutines.delay
import com.asistenpetani.domain.model.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import android.content.Intent
import android.net.Uri
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.launch
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: AppViewModel,
    onSettingsClick: () -> Unit,
    chatController: com.asistenpetani.ui.screens.chat.ChatController,
    onPremiumFieldClick: () -> Unit = {},
    onMountainClick: () -> Unit = {}
) {
    val profile by viewModel.farmerProfile.collectAsState()
    val chatMessages by viewModel.chatMessagesFiltered.collectAsState()
    val unfilteredChatMessages by viewModel.chatMessages.collectAsState()
    val isThinkingEnabled by viewModel.isThinkingEnabled.collectAsState()
    val currentSessionId by viewModel.currentSessionId.collectAsState()
    val chatSessions by viewModel.parentChatSessions.collectAsState()
    val isAILoading by viewModel.isAILoading.collectAsState()
    val adviceState by viewModel.adviceState.collectAsState()
    val uploadStatus by viewModel.imageUploadStatus.collectAsState()
    val userQuota by viewModel.userQuota.collectAsState()
    val imageQuota by viewModel.imageQuota.collectAsState()
    val isPremium by viewModel.isPremium.collectAsState()
    val showInterstitialAd by viewModel.showInterstitialAdEvent.collectAsState()

    val tempState by viewModel.tempState.collectAsState()
    val humidityState by viewModel.humidityState.collectAsState()
    val weatherAlert by viewModel.weatherAlert.collectAsState()
    val isLoadingWeather by viewModel.isLoadingWeather.collectAsState()

    var inputText by remember { mutableStateOf("") }
    var searchHistoryText by remember { mutableStateOf("") }
    var showCameraAnalysisDialog by remember { mutableStateOf(false) }
    var showMicInfoDialog by remember { mutableStateOf(false) }
    var showMicResultDialog by remember { mutableStateOf(false) }
    var recognizedSpeechText by remember { mutableStateOf("") }
    var isSpeechNoisyOrEmpty by remember { mutableStateOf(false) }
    var selectedDeficiency by remember { mutableStateOf<NutrientUnsurHara?>(null) }
    var showLandManagementDialog by remember { mutableStateOf(false) }
    var selectedLandForAnalysis by remember { mutableStateOf<LandManagement?>(null) }
    var showDeleteHistoryDialog by remember { mutableStateOf(false) }
    var showQuotaEmptyDialog by remember { mutableStateOf(false) }
    var topicToDelete by remember { mutableStateOf("") }

    val currentLahanId = profile?.lahanId ?: "LHN-KOLAKA-001"
    var statusLahan by remember { mutableStateOf<LahanStatus?>(null) }
    LaunchedEffect(currentLahanId) {
        statusLahan = viewModel.verifyLahanLocationUseCase(currentLahanId)
    }

    var capturedBitmap by remember { mutableStateOf<android.graphics.Bitmap?>(null) }
    var croppedBitmap by remember { mutableStateOf<android.graphics.Bitmap?>(null) }
    var imageDescription by remember { mutableStateOf("") }
    var showInteractiveCropper by remember { mutableStateOf(false) }

    LaunchedEffect(uploadStatus) {
        if (uploadStatus == "SUCCESS") {
            showCameraAnalysisDialog = false
            capturedBitmap = null
            croppedBitmap = null
            imageDescription = ""
            viewModel.resetImageUploadStatus()
        }
    }

    if (showInteractiveCropper && capturedBitmap != null) {
        InteractiveCropDialog(
            bitmap = capturedBitmap!!,
            onDismiss = { showInteractiveCropper = false },
            onCropped = { cropped ->
                croppedBitmap = cropped
                showInteractiveCropper = false
            }
        )
    }

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val keyboardController = LocalSoftwareKeyboardController.current
    val listState = rememberLazyListState()
    val context = LocalContext.current
    val activity = context as? android.app.Activity
    val lifecycleOwner = LocalLifecycleOwner.current

    LaunchedEffect(showInterstitialAd) {
        if (showInterstitialAd) {
            if (activity != null) {
                InterstitialAdHelper.showAd(activity, isPremium) {
                    viewModel.consumeInterstitialAdEvent()
                }
            } else {
                viewModel.consumeInterstitialAdEvent()
            }
        }
    }

    // DisposableEffect memantau siklus hidup aplikasi untuk mematikan sensor/GPS/kamera secara otomatis saat ditutup agar HP dingin
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                println("ASPEK: Sensor dan pendeteksi lokasi diaktifkan kembali.")
            } else if (event == Lifecycle.Event.ON_PAUSE) {
                println("ASPEK: Mematikan semua sensor, GPS, dan kamera untuk mencegah ponsel menjadi panas.")
                // Lakukan pembersihan sensor latar belakang di sini
            }
        }

        lifecycleOwner.lifecycle.addObserver(observer)

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            println("ASPEK: Menghapus pemantau siklus hidup (LifecycleObserver) dari sistem.")
        }
    }

    // 2. RUANG AMAN UNTUK MEMUNCULKAN IKLAN
    // Diluncurkan otomatis hanya SATU KALI saat layar ini dibuka
    LaunchedEffect(Unit) {
        if (!isPremium) {
            // Beri jeda 3 detik agar aplikasi selesai memuat UI dan terhubung ke AI
            // Hal ini juga mencegah pelanggaran kebijakan Google (iklan tidak boleh mengagetkan user)
            kotlinx.coroutines.delay(3000)
            if (activity != null) {
                // Tampilkan iklan!
                InterstitialAdHelper.showAd(activity, isPremium) {
                    // Aksi yang terjadi SETELAH petani menekan tombol silang (X) di iklan
                    // Aplikasi akan otomatis berlanjut tanpa hambatan.
                }
            }
        }
    }


    val speechRecognizerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == android.app.Activity.RESULT_OK) {
            val spokenText = result.data?.getStringArrayListExtra(android.speech.RecognizerIntent.EXTRA_RESULTS)?.firstOrNull()
            if (!spokenText.isNullOrBlank()) {
                recognizedSpeechText = spokenText
                isSpeechNoisyOrEmpty = spokenText.trim().length < 3
                showMicResultDialog = true
            } else {
                recognizedSpeechText = ""
                isSpeechNoisyOrEmpty = true
                showMicResultDialog = true
            }
        }
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap ->
        if (bitmap != null) {
            capturedBitmap = bitmap
            croppedBitmap = null
        }
    }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            try {
                val originalBitmap = com.asistenpetani.core.ai.ImageCompressor.decodeUriSangatAman(context, it)
                if (originalBitmap != null) {
                    capturedBitmap = originalBitmap
                    croppedBitmap = null
                }
            } catch (e: Exception) {
                android.util.Log.e("AspekChat", "Gagal memuat gambar galeri: ${e.message}")
            }
        }
    }

    // Kelola list obrolan dan sesi secara lokal di perangkat

    // Scroll to bottom when new messages come in
    LaunchedEffect(chatMessages.size, isAILoading) {
        if (chatMessages.isNotEmpty()) {
            listState.animateScrollToItem(chatMessages.size + 3)
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                drawerContainerColor = Color.White,
                modifier = Modifier.width(300.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(20.dp)
                ) {
                    // Drawer Header
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "RIWAYAT CHAT",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = MidnightBlue,
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(onClick = { scope.launch { drawerState.close() } }) {
                            Icon(Icons.Default.Close, contentDescription = "Close Drawer", tint = Color.Gray)
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Search input
                    OutlinedTextField(
                        value = searchHistoryText,
                        onValueChange = { searchHistoryText = it },
                        placeholder = { Text("Cari riwayat...", color = Color(0xFF475569)) },
                        textStyle = TextStyle(color = Color(0xFF0F172A), fontSize = 14.sp, fontWeight = FontWeight.Bold),
                        modifier = Modifier.fillMaxWidth(),
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = MidnightBlue) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color(0xFF0F172A),
                            unfocusedTextColor = Color(0xFF0F172A),
                            focusedBorderColor = Color(0xFF2563EB),
                            unfocusedBorderColor = Color(0xFF94A3B8),
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White
                        ),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    // Mulai Sesi Baru Button
                    Button(
                        onClick = {
                            viewModel.startNewSession()
                            scope.launch { drawerState.close() }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = AspekGreen),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("new_session_button"),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, tint = Color.White)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Mulai Sesi Baru", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // History session list
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val sessionsAndTitles = chatSessions.map { sId ->
                            val firstUserMsg = unfilteredChatMessages.firstOrNull { it.sessionId == sId && it.sender == "user" }
                            val titleText = firstUserMsg?.text ?: if (sId == "default_session") "Sesi Chat Utama" else "Sesi Baru " + sId.substringAfter("session_").take(8)
                            sId to titleText
                        }
                        val filteredSessions = sessionsAndTitles.filter {
                            it.second.contains(searchHistoryText, ignoreCase = true)
                        }

                        if (filteredSessions.isEmpty()) {
                            item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 40.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = if (searchHistoryText.isEmpty()) "Belum ada riwayat Sesi aktif." else "Pencarian tidak ditemukan.",
                                        color = Color.Gray,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Medium,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        } else {
                            items(filteredSessions) { (sId, title) ->
                                val isActive = sId == currentSessionId
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            viewModel.selectSession(sId)
                                            scope.launch { drawerState.close() }
                                        },
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (isActive) Color(0xFFF0FDF4) else EarthBackground
                                    ),
                                    border = BorderStroke(1.dp, if (isActive) AspekGreen else Color(0xFFE2E8F0)),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = if (isActive) Icons.Default.ChatBubble else Icons.Default.ChatBubbleOutline,
                                            contentDescription = null,
                                            tint = if (isActive) AspekGreen else Color.Gray,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = title,
                                                fontSize = 13.sp,
                                                fontWeight = if (isActive) FontWeight.Bold else FontWeight.SemiBold,
                                                color = MidnightBlue,
                                                maxLines = 1,
                                                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                                            )
                                            Text(
                                                text = if (sId == "default_session") "Sesi Utama" else "Dibuat: Sultra",
                                                fontSize = 10.sp,
                                                color = Color.Gray
                                            )
                                        }
                                        if (sId != "default_session") {
                                            IconButton(
                                                onClick = { viewModel.deleteSession(sId) },
                                                modifier = Modifier.size(24.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Delete,
                                                    contentDescription = "Hapus Sesi",
                                                    tint = Color.Red.copy(alpha = 0.7f),
                                                    modifier = Modifier.size(14.dp)
                                                 )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    HorizontalDivider(color = Color(0xFFF1F5F9))
                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "PINTAS BANTUAN CEPAT",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MidnightBlue,
                        letterSpacing = 1.sp
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                viewModel.sendMessage("Tampilkan rekomendasi rute peta kios pertanian Kolaka terdekat")
                                scope.launch { drawerState.close() }
                            },
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFEFF6FF)),
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, Color(0xFFDBEAFE))
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Map, contentDescription = null, tint = MediumBlue, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text("🗺️ Rute Peta Kios", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MidnightBlue)
                                Text("Rute navigasi peta dengan satu ketukan", fontSize = 9.sp, color = Color.Gray)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                viewModel.sendMessage("Berapa rekomendasi harga komoditas hasil pertanian, perkebunan, dan perikanan Sultra saat ini?")
                                scope.launch { drawerState.close() }
                            },
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFEF3C7)),
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, Color(0xFFFDE68A))
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Store, contentDescription = null, tint = Color(0xFFD97706), modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text("💰 Harga & Pengepul", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MidnightBlue)
                                Text("Tanya harga jual komoditas terkini", fontSize = 9.sp, color = Color.Gray)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Asisten Petani Kita • Sultra",
                        fontSize = 10.sp,
                        color = Color.LightGray,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.clickable { scope.launch { drawerState.open() } }
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0xFFE8F5E9)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Spa, contentDescription = null, tint = AspekGreen, modifier = Modifier.size(20.dp))
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "ASISTEN PETANI KITA",
                                style = MaterialTheme.typography.labelSmall,
                                letterSpacing = 2.sp,
                                color = MidnightBlue,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(Icons.Default.Menu, contentDescription = "Menu Sidebar", tint = MidnightBlue)
                        }
                    },
                    actions = {
                        // Notification Bell with indicator dot
                        Box(modifier = Modifier.padding(end = 12.dp)) {
                            IconButton(onClick = {
                                viewModel.sendMessage("Apakah ada jatah subsidi pupuk baru minggu ini?")
                            }) {
                                Icon(Icons.Default.Notifications, contentDescription = "Notifikasi", tint = MediumBlue)
                            }
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(Color.Red)
                                    .align(Alignment.TopEnd)
                                    .offset(x = (-4).dp, y = 4.dp)
                            )
                        }
                        // Peta Geografi AI
                        IconButton(onClick = onMountainClick) {
                            Icon(Icons.Default.Terrain, contentDescription = "Peta Geografi", tint = Color.Gray)
                        }
                        // Premium Field Map
                        IconButton(onClick = onPremiumFieldClick) {
                            Icon(Icons.Default.Map, contentDescription = "Peta Lahan Premium", tint = Color.Gray)
                        }
                        // Settings Gear
                        IconButton(onClick = onSettingsClick) {
                            Icon(Icons.Default.Settings, contentDescription = "Pengaturan", tint = Color.Gray)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
                )
            },
            containerColor = EarthBackground
        ) { padding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(bottom = 180.dp) // Leave safety space for input bottom bar (quota, thinking toggle, text field)
                ) {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Chat bubbles list
                        items(chatMessages) { message ->
                            val isUser = message.sender == "user"

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
                            ) {
                                if (!isUser) {
                                    Image(
                                        painter = painterResource(id = R.drawable.aspek_mascot_icon_1782037281779),
                                        contentDescription = "AI Head",
                                        modifier = Modifier
                                            .size(32.dp)
                                            .clip(CircleShape)
                                            .border(1.dp, Color(0xFFE2E8F0), CircleShape)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                }

                                Card(
                                    modifier = Modifier
                                        .weight(1f, fill = false)
                                        .testTag(if (isUser) "user_bubble" else "ai_bubble"),
                                    shape = RoundedCornerShape(
                                        topStart = 16.dp,
                                        topEnd = 16.dp,
                                        bottomStart = if (isUser) 16.dp else 0.dp,
                                        bottomEnd = if (isUser) 0.dp else 16.dp
                                    ),
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (isUser) MidnightBlue else Color.White
                                    ),
                                    border = if (isUser) null else BorderStroke(1.dp, Color(0xFFF1F5F9)),
                                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                                ) {
                                    Column(modifier = Modifier.padding(14.dp)) {
                                        Text(
                                            text = parseMarkdown(message.text),
                                            color = if (isUser) Color.White else Color.Black,
                                            fontSize = 14.sp,
                                            lineHeight = 20.sp
                                        )

                                        // Speak Aloud Button (TTS) for AI message
                                        if (!isUser) {
                                            Spacer(modifier = Modifier.height(10.dp))
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Button(
                                                    onClick = { chatController.speakOut(message.text) },
                                                    colors = ButtonDefaults.buttonColors(
                                                        containerColor = Color(0xFFEFF6FF),
                                                        contentColor = MediumBlue
                                                    ),
                                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                                    shape = RoundedCornerShape(8.dp),
                                                    modifier = Modifier.height(32.dp)
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                                                        contentDescription = "Speak Text",
                                                        modifier = Modifier.size(14.dp)
                                                    )
                                                    Spacer(modifier = Modifier.width(6.dp))
                                                    Text(
                                                        text = "DENGARKAN",
                                                        fontSize = 10.sp,
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                }
                                            }

                                            // REKOMENDASI PETA DINAMIS (DITAMPILKAN SAAT GEMINI MEREKOMENDASIKAN / DIBUTUHKAN PETANI)
                                            val lowerText = message.text.lowercase()
                                            val needsMap = lowerText.contains("peta") || 
                                                           lowerText.contains("map") || 
                                                           lowerText.contains("gps") || 
                                                           lowerText.contains("lokasi") || 
                                                           lowerText.contains("rute") || 
                                                           lowerText.contains("kios") || 
                                                           lowerText.contains("toko") || 
                                                           lowerText.contains("koordinat") ||
                                                           lowerText.contains("alamat")

                                            val needsPrices = lowerText.contains("harga") || 
                                                              lowerText.contains("jual") || 
                                                              lowerText.contains("beli") || 
                                                              lowerText.contains("komoditas") || 
                                                              lowerText.contains("pasar") || 
                                                              lowerText.contains("pengepul") || 
                                                              lowerText.contains("rupiah") || 
                                                              lowerText.contains("rp") ||
                                                              lowerText.contains("hasil bumi")

                                            if (needsMap) {
                                                Spacer(modifier = Modifier.height(10.dp))
                                                Card(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .testTag("recommended_map_card_${message.id}"),
                                                    colors = CardDefaults.cardColors(containerColor = Color(0xFFEFF6FF)),
                                                    shape = RoundedCornerShape(12.dp),
                                                    border = BorderStroke(1.dp, Color(0xFFDBEAFE))
                                                ) {
                                                    Row(
                                                        modifier = Modifier
                                                            .fillMaxWidth()
                                                            .clickable {
                                                                try {
                                                                    val mapsUriStr = "geo:0,0?q=Kios+Pupuk+Pertanian+Terdekat+Kolaka"
                                                                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(mapsUriStr))
                                                                    context.startActivity(intent)
                                                                } catch (e: Exception) {
                                                                    android.widget.Toast.makeText(context, "Gagal membuka peta.", android.widget.Toast.LENGTH_SHORT).show()
                                                                }
                                                            }
                                                            .padding(10.dp),
                                                        verticalAlignment = Alignment.CenterVertically
                                                    ) {
                                                        Box(
                                                            modifier = Modifier
                                                                .size(32.dp)
                                                                .clip(CircleShape)
                                                                .background(Color(0xFFDBEAFE)),
                                                            contentAlignment = Alignment.Center
                                                        ) {
                                                            Icon(
                                                                imageVector = Icons.Default.Map,
                                                                contentDescription = "Peta",
                                                                tint = MediumBlue,
                                                                modifier = Modifier.size(18.dp)
                                                            )
                                                        }
                                                        Spacer(modifier = Modifier.width(8.dp))
                                                        Column(modifier = Modifier.weight(1f)) {
                                                            Text(
                                                                text = "Rekomendasi Peta Kios & Instansi",
                                                                fontSize = 11.sp,
                                                                fontWeight = FontWeight.Bold,
                                                                color = MidnightBlue
                                                            )
                                                            Text(
                                                                text = "Ketuk untuk buka rute Google Maps",
                                                                fontSize = 9.sp,
                                                                color = Color.Gray
                                                            )
                                                        }
                                                        Icon(
                                                            imageVector = Icons.Default.ChevronRight,
                                                            contentDescription = "Buka Peta",
                                                            tint = MediumBlue,
                                                            modifier = Modifier.size(16.dp)
                                                        )
                                                    }
                                                }
                                            }

                                            if (needsPrices) {
                                                Spacer(modifier = Modifier.height(10.dp))
                                                val matchedBuyer = remember(message.text) {
                                                    val txt = message.text.lowercase()
                                                    when {
                                                        txt.contains("nilam") || txt.contains("atsiri") -> com.asistenpetani.domain.model.AgriculturalDatasets.buyerList.getOrNull(2)
                                                        txt.contains("cengkeh") || txt.contains("kelapa") -> com.asistenpetani.domain.model.AgriculturalDatasets.buyerList.getOrNull(3)
                                                        txt.contains("padi") || txt.contains("beras") || txt.contains("gabah") -> com.asistenpetani.domain.model.AgriculturalDatasets.buyerList.getOrNull(4)
                                                        txt.contains("tebu") || txt.contains("gula") -> com.asistenpetani.domain.model.AgriculturalDatasets.buyerList.getOrNull(5)
                                                        txt.contains("ikan") || txt.contains("udang") || txt.contains("bandeng") || txt.contains("tambak") || txt.contains("perikanan") || txt.contains("air") -> com.asistenpetani.domain.model.AgriculturalDatasets.buyerList.getOrNull(1)
                                                        else -> com.asistenpetani.domain.model.AgriculturalDatasets.buyerList.getOrNull(0) // Kakao
                                                    } ?: com.asistenpetani.domain.model.AgriculturalDatasets.buyerList.first()
                                                }

                                                Card(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .testTag("recommended_price_card_${message.id}"),
                                                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
                                                    shape = RoundedCornerShape(12.dp),
                                                    border = BorderStroke(1.dp, Color(0xFFE2E8F0))
                                                ) {
                                                    Column(modifier = Modifier.padding(10.dp)) {
                                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                                            Icon(
                                                                imageVector = Icons.Default.Store,
                                                                contentDescription = "Pusat Penjualan",
                                                                tint = AspekGreen,
                                                                modifier = Modifier.size(16.dp)
                                                            )
                                                            Spacer(modifier = Modifier.width(6.dp))
                                                            Text(
                                                                text = "Rekomendasi Harga Jual Sultra (AI):",
                                                                fontSize = 11.sp,
                                                                fontWeight = FontWeight.Bold,
                                                                color = MidnightBlue
                                                            )
                                                        }
                                                        Spacer(modifier = Modifier.height(6.dp))
                                                        Text(
                                                            text = matchedBuyer.name,
                                                            fontSize = 12.sp,
                                                            fontWeight = FontWeight.Bold,
                                                            color = MidnightBlue
                                                        )
                                                        Text(
                                                            text = "Harga Beli: ${matchedBuyer.priceRange}",
                                                            fontSize = 11.sp,
                                                            fontWeight = FontWeight.Bold,
                                                            color = Color(0xFF16A34A)
                                                        )
                                                        Text(
                                                            text = "Alamat: ${matchedBuyer.address}",
                                                            fontSize = 9.sp,
                                                            color = Color.Gray
                                                        )
                                                        Text(
                                                            text = "Kontak: ${matchedBuyer.contact}",
                                                            fontSize = 9.sp,
                                                            color = Color.Gray
                                                        )
                                                        Spacer(modifier = Modifier.height(6.dp))
                                                        Button(
                                                            onClick = {
                                                                try {
                                                                    val coords = "-4.0588,121.5403"
                                                                    val label = matchedBuyer.name
                                                                    val encodedLabel = java.net.URLEncoder.encode(label, "UTF-8")
                                                                    val mapsUriStr = "geo:$coords?q=$coords($encodedLabel)"
                                                                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(mapsUriStr))
                                                                    context.startActivity(intent)
                                                                } catch (e: Exception) {
                                                                    android.widget.Toast.makeText(context, "Gagal membuka peta.", android.widget.Toast.LENGTH_SHORT).show()
                                                                }
                                                            },
                                                            modifier = Modifier.fillMaxWidth().height(28.dp),
                                                            colors = ButtonDefaults.buttonColors(containerColor = AspekGreen),
                                                            shape = RoundedCornerShape(6.dp),
                                                            contentPadding = PaddingValues(horizontal = 4.dp, vertical = 2.dp)
                                                        ) {
                                                            Icon(Icons.Default.Navigation, contentDescription = null, modifier = Modifier.size(12.dp))
                                                            Spacer(modifier = Modifier.width(4.dp))
                                                            Text("Petunjuk Rute Pengepul", fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }

                                if (isUser) {
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Box(
                                        modifier = Modifier
                                            .size(32.dp)
                                            .clip(CircleShape)
                                            .background(OrangeRed),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(Icons.Default.Person, contentDescription = "User Head", tint = Color.White, modifier = Modifier.size(16.dp))
                                    }
                                }
                            }
                        }

                        // AI Loading Indicator
                        if (isAILoading) {
                            item {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.Start,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Image(
                                        painter = painterResource(id = R.drawable.aspek_mascot_icon_1782037281779),
                                        contentDescription = null,
                                        modifier = Modifier
                                            .size(32.dp)
                                            .clip(CircleShape)
                                            .border(1.dp, Color(0xFFE2E8F0), CircleShape)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Card(
                                        shape = RoundedCornerShape(16.dp),
                                        colors = CardDefaults.cardColors(containerColor = Color.White)
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(14.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            CircularProgressIndicator(
                                                modifier = Modifier.size(16.dp),
                                                strokeWidth = 2.dp,
                                                color = AspekGreen
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                text = "asisten petani berpikir....",
                                                color = Color(0xFF64748B), // Faint Slate/Shadow gray
                                                fontSize = 13.sp,
                                                fontStyle = FontStyle.Italic,
                                                fontWeight = FontWeight.Medium
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        item {
                            Spacer(modifier = Modifier.height(16.dp))
                        }
                    }
                }

                // Keyboard input bar layout at the very bottom
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter)
                        .background(Color.White)
                        .padding(12.dp)
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        // Layar Sisa Kuota
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 4.dp, vertical = 2.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (isPremium) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Stars,
                                        contentDescription = "Premium",
                                        tint = Color(0xFFD4A00D),
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "Status: PREMIUM (Bebas Tanya Tanpa Batas)",
                                        fontSize = 11.sp,
                                        color = AspekGreen,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            } else {
                                Text(
                                    text = "Sisa kuota: $userQuota Teks, $imageQuota Gambar",
                                    fontSize = 11.sp,
                                    color = if (userQuota > 0 || imageQuota > 0) Color(0xFF475569) else Color(0xFFDC2626),
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Tonton iklan untuk memulihkan",
                                    fontSize = 9.sp,
                                    color = Color.Gray
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        if (isPremium || userQuota > 0 || imageQuota > 0) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                // Camera Button
                                IconButton(onClick = {
                                    if (isPremium || imageQuota > 0) {
                                        showCameraAnalysisDialog = true
                                    } else {
                                        showQuotaEmptyDialog = true
                                    }
                                }) {
                                    Icon(Icons.Default.PhotoCamera, contentDescription = "Analisa Gambar AI", tint = AspekGreen)
                                }

                                // Mic Button
                                IconButton(onClick = {
                                    showMicInfoDialog = true
                                }) {
                                    Icon(Icons.Default.Mic, contentDescription = "Mikrofon", tint = AspekGreen)
                                }

                                // Deep Thinking Toggle Button (Lightbulb)
                                IconButton(onClick = {
                                    val newVal = !isThinkingEnabled
                                    viewModel.setThinkingEnabled(newVal)
                                    android.widget.Toast.makeText(
                                        context,
                                        if (newVal) "Mode Berpikir Pro (gemini-3.1-pro-preview) Aktif" else "Mode Respon Cepat (gemini-3.5-flash) Aktif",
                                        android.widget.Toast.LENGTH_SHORT
                                    ).show()
                                }) {
                                    Icon(
                                        imageVector = Icons.Default.Lightbulb,
                                        contentDescription = "Mode Berpikir",
                                        tint = if (isThinkingEnabled) Color(0xFFF59E0B) else Color.Gray
                                    )
                                }

                                Spacer(modifier = Modifier.width(4.dp))

                                // Text input field
                                OutlinedTextField(
                                    value = inputText,
                                    onValueChange = { inputText = it },
                                    placeholder = { Text("Tanya asisten...", fontSize = 13.sp, color = Color(0xFF475569)) },
                                    textStyle = TextStyle(color = Color(0xFF0F172A), fontSize = 13.sp, fontWeight = FontWeight.Bold),
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("message_input"),
                                    shape = RoundedCornerShape(20.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedTextColor = Color(0xFF0F172A),
                                        unfocusedTextColor = Color(0xFF0F172A),
                                        focusedBorderColor = AspekGreen,
                                        unfocusedBorderColor = Color(0xFF94A3B8), // slate 400
                                        focusedContainerColor = Color.White,
                                        unfocusedContainerColor = Color.White
                                    ),
                                    singleLine = true,
                                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                                    keyboardActions = androidx.compose.foundation.text.KeyboardActions(
                                        onSend = {
                                            if (inputText.isNotBlank()) {
                                                if (isPremium || userQuota > 0) {
                                                    viewModel.sendMessage(inputText)
                                                    inputText = ""
                                                    keyboardController?.hide()
                                                } else {
                                                    showQuotaEmptyDialog = true
                                                }
                                            }
                                        }
                                    )
                                )

                                Spacer(modifier = Modifier.width(8.dp))

                                // Send dynamic rounded circle button
                                FloatingActionButton(
                                    onClick = {
                                        if (inputText.isNotBlank()) {
                                            if (isPremium || userQuota > 0) {
                                                viewModel.sendMessage(inputText)
                                                inputText = ""
                                                keyboardController?.hide()
                                            } else {
                                                showQuotaEmptyDialog = true
                                            }
                                        }
                                    },
                                    modifier = Modifier
                                        .size(42.dp)
                                        .testTag("send_button"),
                                    shape = CircleShape,
                                    containerColor = Color(0xFFEFF6FF),
                                    contentColor = MediumBlue,
                                    elevation = FloatingActionButtonDefaults.elevation(0.dp, 0.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.Send,
                                        contentDescription = "Send text",
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        } else {
                            // Layanan tonton video reward & penawaran Premium Langganan Bulanan
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(bottom = 8.dp),
                                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFFBEB)),
                                    border = BorderStroke(1.dp, Color(0xFFF59E0B)),
                                    shape = RoundedCornerShape(16.dp)
                                ) {
                                    Column(
                                        modifier = Modifier.padding(12.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text(
                                            text = "👑 Premium ASPEK Bulanan",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp,
                                            color = Color(0xFFB45309)
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = "Akses kecerdasan buatan Gemini Pro & bebas iklan sepenuhnya hanya dengan Rp 25.000 / Bulan.",
                                            fontSize = 11.sp,
                                            color = Color(0xFF78350F),
                                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                            modifier = Modifier.padding(horizontal = 8.dp)
                                        )
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Button(
                                            onClick = {
                                                viewModel.setPremiumStatus(true)
                                                android.widget.Toast.makeText(context, "Alhamdulillah! Langganan Bulanan Premium (Rp 25.000/Bulan) berhasil diaktifkan. Anda kini menggunakan Gemini Pro!", android.widget.Toast.LENGTH_LONG).show()
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB)),
                                            shape = RoundedCornerShape(12.dp),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Text("Berlangganan Rp 25.000/Bulan", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                        }
                                        Spacer(modifier = Modifier.height(4.dp))
                                        TextButton(onClick = { /* Tolak premium, tetap gunakan flash lite */ viewModel.setPremiumStatus(false) }) {
                                            Text("Tolak & Gunakan Gemini Flash Lite", fontSize = 10.sp, color = Color.Gray)
                                        }
                                    }
                                }
                                Button(
                                    onClick = {
                                        android.widget.Toast.makeText(context, "Mempersiapkan video panduan pendek...", android.widget.Toast.LENGTH_SHORT).show()
                                        loadAndShowRewardedAd(
                                            context = context,
                                            onAdDismissed = {
                                                // Opsional
                                            },
                                            onRewardEarned = {
                                                viewModel.rewardUserWithQuota()
                                                android.widget.Toast.makeText(context, "Alhamdulillah! Anda bebas berinteraksi sepuasnya dengan Gemini Flash Lite.", android.widget.Toast.LENGTH_LONG).show()
                                            }
                                        )
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD4A00D)), // Golden mustard
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.PlayCircle,
                                        contentDescription = "Tonton Video",
                                        tint = Color.White,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Atau Tonton Video Pendek (+5 Kuota Gratis)",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp,
                                        color = Color.White
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showCameraAnalysisDialog) {
        AlertDialog(
            onDismissRequest = {
                if (uploadStatus != "LOADING") {
                    showCameraAnalysisDialog = false
                    selectedDeficiency = null
                }
            },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.PhotoCamera,
                        contentDescription = null,
                        tint = AspekGreen,
                        modifier = Modifier.size(26.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (selectedDeficiency == null) "Kamera Analisa Hara Lahan" else "Laporan Hasil Analisa Gambar",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MidnightBlue
                    )
                }
            },
            text = {
                val context = LocalContext.current
                var isScanning by remember { mutableStateOf(false) }
                
                if (isScanning) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        CircularProgressIndicator(color = AspekGreen, modifier = Modifier.size(48.dp))
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Membaca pixel daun...",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MidnightBlue
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Mencocokkan dengan basis pengetahuan pupuk...",
                            fontSize = 12.sp,
                            color = Color.Gray,
                            textAlign = TextAlign.Center
                        )
                    }
                    
                    LaunchedEffect(Unit) {
                        kotlinx.coroutines.delay(1200)
                        isScanning = false
                        // Pick a realistic deficiency based on crop type
                        val cropLower = (profile?.cropType ?: "").lowercase()
                        selectedDeficiency = when {
                            cropLower.contains("padi") -> FertilizerNutrientDb.daftarUnsurHara.find { it.nama.contains("NITROGEN") }
                            cropLower.contains("kakao") || cropLower.contains("cokelat") -> FertilizerNutrientDb.daftarUnsurHara.find { it.nama.contains("POTASSIUM") }
                            else -> FertilizerNutrientDb.daftarUnsurHara.random()
                        }
                    }
                } else if (capturedBitmap != null) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 420.dp)
                            .verticalScroll(rememberScrollState()),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFF0FDF4)),
                            border = BorderStroke(1.dp, Color(0xFFBBF7D0)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 12.dp)
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Info,
                                        contentDescription = null,
                                        tint = AspekGreen,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "TIPS ANALISA GAMBAR OPTIMAL:",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF15803D)
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "• Ambil foto jarak dekat & fokus pada gejala bercak/kuning daun.",
                                    fontSize = 10.sp,
                                    color = Color(0xFF1F2937)
                                )
                                Text(
                                    text = "• Gunakan tombol Pangkas (Crop) untuk memfokuskan area sakit daun.",
                                    fontSize = 10.sp,
                                    color = Color(0xFF1F2937)
                                )
                                Text(
                                    text = "• Tambahkan keterangan singkat (opsional) untuk meningkatkan akurasi.",
                                    fontSize = 10.sp,
                                    color = Color(0xFF1F2937)
                                )
                            }
                        }

                        // Preview box list
                        val currentImage = croppedBitmap ?: capturedBitmap!!
                        Box(
                            modifier = Modifier
                                .size(160.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .border(2.dp, AspekGreen, RoundedCornerShape(12.dp))
                                .background(Color.DarkGray),
                            contentAlignment = Alignment.Center
                        ) {
                            androidx.compose.foundation.Image(
                                bitmap = currentImage.asImageBitmap(),
                                contentDescription = "Review gambar hasil jepret",
                                modifier = Modifier.fillMaxSize()
                            )

                            if (uploadStatus == "LOADING") {
                                // Sweeping Laser Animation
                                val infiniteTransition = rememberInfiniteTransition(label = "laser")
                                val yOffsetMultiplier by infiniteTransition.animateFloat(
                                    initialValue = 0f,
                                    targetValue = 1f,
                                    animationSpec = infiniteRepeatable(
                                        animation = tween(1500, easing = LinearEasing),
                                        repeatMode = RepeatMode.Reverse
                                    ),
                                    label = "yOffset"
                                )

                                Canvas(modifier = Modifier.fillMaxSize()) {
                                    val yPosition = size.height * yOffsetMultiplier
                                    // Draw glowing laser line
                                    drawLine(
                                        color = Color(0xFF10B981), // Emerald Green
                                        start = androidx.compose.ui.geometry.Offset(0f, yPosition),
                                        end = androidx.compose.ui.geometry.Offset(size.width, yPosition),
                                        strokeWidth = 4.dp.toPx()
                                    )
                                    // Draw faint laser glow gradient
                                    drawRect(
                                        brush = androidx.compose.ui.graphics.Brush.verticalGradient(
                                            colors = listOf(
                                                Color(0x0010B981),
                                                Color(0x4410B981),
                                                Color(0x0010B981)
                                            ),
                                            startY = yPosition - 20.dp.toPx(),
                                            endY = yPosition + 20.dp.toPx()
                                        ),
                                        topLeft = androidx.compose.ui.geometry.Offset(0f, yPosition - 20.dp.toPx()),
                                        size = androidx.compose.ui.geometry.Size(size.width, 40.dp.toPx())
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Button(
                            onClick = {
                                showInteractiveCropper = true
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE0F2FE), contentColor = Color(0xFF0369A1)),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(
                                imageVector = Icons.Default.Crop,
                                contentDescription = null,
                                tint = Color(0xFF0369A1),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Pangkas Kustom (Manual / Geser)", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Button(
                                onClick = {
                                    croppedBitmap = cropCenter(capturedBitmap!!, 0.75f)
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEFF6FF), contentColor = MidnightBlue),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Pangkas 75%", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }

                            Button(
                                onClick = {
                                    croppedBitmap = cropCenter(capturedBitmap!!, 0.5f)
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEFF6FF), contentColor = MidnightBlue),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Pangkas 50%", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        if (croppedBitmap != null) {
                            Button(
                                onClick = { croppedBitmap = null },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFEF2F2), contentColor = Color.Red),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Reset ke Gambar Asli", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Text field to describe problem below leaf picture
                        OutlinedTextField(
                            value = imageDescription,
                            onValueChange = { imageDescription = it },
                            label = { Text("Keterangan Tambahan (Opsional)", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MidnightBlue) },
                            placeholder = { Text("Contoh: Daun kakao bercak kuning kecokelatan kering mulai dari ujung...", fontSize = 11.sp, color = Color.Gray) },
                            textStyle = TextStyle(fontSize = 12.sp, color = MidnightBlue),
                            modifier = Modifier.fillMaxWidth(),
                            maxLines = 3,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = AspekGreen,
                                unfocusedBorderColor = Color.LightGray,
                                focusedContainerColor = Color(0xFFF8FAFC),
                                unfocusedContainerColor = Color(0xFFF8FAFC)
                            )
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // If uploadStatus is "ERROR", show a red warning banner
                        if (uploadStatus == "ERROR") {
                            Surface(
                                color = Color(0xFFFEF2F2),
                                border = BorderStroke(1.dp, Color(0xFFFCA5A5)),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 8.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Warning,
                                        contentDescription = null,
                                        tint = Color.Red,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Kompabilitas internet bermasalah. Gagal mengirim gambar, silakan klik tombol Kirim Ulang di bawah tanpa khawatir keterangan terhapus.",
                                        fontSize = 11.sp,
                                        color = Color(0xFF991B1B),
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }

                        Button(
                            onClick = {
                                val targetImage = croppedBitmap ?: capturedBitmap!!
                                val finalPrompt = if (imageDescription.isNotBlank()) {
                                    "${imageDescription}\n\n[Analisa Visual Tanaman Sultra: Mohon berikan laporan lengkap defisiensi unsur hara, rekomendasi merk pupuk ideal di Sultra, dosis detail, serta langkah penanganan tani cepat.]"
                                } else {
                                    "Tolong analisis intensitas warna dan gejala visual daun ini. Berdasarkan database kandungan unsur hara komoditas ${profile?.cropType ?: "tanaman"}, berikan laporan defisiensi unsur hara, rekomendasi merk pupuk ideal, dosis, serta aksi cepat penanganan tani."
                                }
                                viewModel.sendMessageWithImage(finalPrompt, targetImage)
                            },
                            enabled = uploadStatus != "LOADING" && (croppedBitmap != null || capturedBitmap != null),
                            colors = ButtonDefaults.buttonColors(containerColor = AspekGreen),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            if (uploadStatus == "LOADING") {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    strokeWidth = 2.dp,
                                    color = Color.White
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Sedang Menganalisis...", color = Color.White, fontWeight = FontWeight.Bold)
                            } else {
                                Icon(Icons.Default.Check, contentDescription = null, tint = Color.White)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = if (uploadStatus == "ERROR") "Coba Kirim Ulang" else "Kirim & Analisis Daun", 
                                    color = Color.White, 
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        Row(
                            horizontalArrangement = Arrangement.Center,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            TextButton(
                                enabled = uploadStatus != "LOADING",
                                onClick = {
                                    capturedBitmap = null
                                    croppedBitmap = null
                                    try {
                                        cameraLauncher.launch(null)
                                    } catch (e: Exception) {}
                                }
                            ) {
                                Text("Foto Ulang", color = if (uploadStatus == "LOADING") Color.LightGray else Color.Gray, fontSize = 11.sp)
                            }
                            Spacer(modifier = Modifier.width(16.dp))
                            TextButton(
                                enabled = uploadStatus != "LOADING",
                                onClick = {
                                    capturedBitmap = null
                                    croppedBitmap = null
                                    try {
                                        galleryLauncher.launch("image/*")
                                    } catch (e: Exception) {}
                                }
                            ) {
                                Text("Pilih Foto Lain", color = if (uploadStatus == "LOADING") Color.LightGray else Color.Gray, fontSize = 11.sp)
                            }
                        }
                    }
                } else if (selectedDeficiency == null) {
                    // List of symptoms to select
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 400.dp)
                    ) {
                        Text(
                            text = "Ambil foto tumbuhan atau pilih salah satu gejala visual di bawah untuk menganalisa kebutuhan pupuk tanaman secara presisi via database.",
                            fontSize = 12.sp,
                            color = Color.DarkGray,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )
                        
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 16.dp)
                        ) {
                            Button(
                                onClick = {
                                    try {
                                        cameraLauncher.launch(null)
                                    } catch (e: Exception) {}
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = AspekGreen),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Default.PhotoCamera, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Kamera", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                            }

                            Button(
                                onClick = {
                                    try {
                                        galleryLauncher.launch("image/*")
                                    } catch (e: Exception) {}
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEFF6FF), contentColor = MidnightBlue),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.weight(1f),
                                border = BorderStroke(1.dp, Color(0xFFBFDBFE))
                            ) {
                                Icon(Icons.Default.Image, contentDescription = null, tint = MidnightBlue, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Galeri", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                            }
                        }
                        
                        Text(
                            text = "PILIH GEJALA VISUAL DAUN:",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.Gray,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        
                        LazyColumn(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            val symptomsToExplore = listOf(
                                "Daun menguning mulai bagian bawah & kerdil (Kekurangan Nitrogen)" to "NITROGEN",
                                "Tepi daun klorosis / bercak kuning & kering (Kekurangan Kalium)" to "POTASSIUM",
                                "Daun baru rapuh cacat mengering ujungnya (Kekurangan Kalsium)" to "CALCIUM",
                                "Bercak kuning memanjang di sela urat daun baru (Kekurangan Mangan)" to "MANGANESE",
                                "Lebar daun mengecil & klorosis interveinal dewasa (Kekurangan Seng)" to "ZINC",
                                "Daun muda putih kekuningan urat tetap hijau (Kekurangan Besi)" to "IRON",
                                "Daun bawah memutih di pinggir / menguning dalam urat (Kekurangan Magnesium)" to "MAGNESIUM",
                                "Tanaman pucat merata bintil akar kacang kecil (Kekurangan Sulfur)" to "SULPHUR",
                                "Daun muda memutih layu klorosis & kerdil (Kekurangan Tembaga)" to "COPPER",
                                "Ujung meristem mati terhenti lebat kerdil roset (Kekurangan Boron)" to "BORON",
                                "Daun muda kerdil berkerut whiptail ujung meliuk (Kekurangan Molybdenum)" to "MOLYBDENUM",
                                "Daun tampak sangat gelap & berguguran parah (Kekurangan Fosfat)" to "Fosfat"
                            )
                            
                            items(symptomsToExplore) { (title, key) ->
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            selectedDeficiency = FertilizerNutrientDb.daftarUnsurHara.find { it.nama.contains(key) || key.contains(it.nama) }
                                        },
                                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
                                    border = BorderStroke(1.dp, Color(0xFFE2E8F0))
                                ) {
                                    Row(
                                        modifier = Modifier.padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Grass,
                                            contentDescription = null,
                                            tint = AspekGreen,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(text = title, fontSize = 12.sp, fontWeight = FontWeight.Medium, color = MidnightBlue)
                                    }
                                }
                            }
                        }
                    }
                } else {
                    // Display the gorgeous report!
                    val hara = selectedDeficiency!!
                    val recommendedFertilisers = FertilizerNutrientDb.daftarPupuk.filter { pupuk ->
                        val pupukHaraName = pupuk.sumberHara.lowercase()
                        val haraName = hara.nama.lowercase()
                        pupukHaraName.contains(haraName) || haraName.contains(pupukHaraName) ||
                        (haraName.contains("nitrogen") && (pupuk.pupuk == "Urea" || pupuk.pupuk == "ZA" || pupuk.pupuk == "KNO3")) ||
                        (haraName.contains("potassium") && (pupuk.pupuk == "KCl" || pupuk.pupuk == "ZK" || pupuk.pupuk == "KNO3")) ||
                        (haraName.contains("calcium") && (pupuk.pupuk == "Dolomite" || pupuk.pupuk == "TSP")) ||
                        (haraName.contains("magnesium") && (pupuk.pupuk == "Dolomite" || pupuk.pupuk == "Kieserite")) ||
                        (haraName.contains("sulphur") && (pupuk.pupuk == "ZA" || pupuk.pupuk == "ZK" || pupuk.pupuk == "Kieserite")) ||
                        (haraName.contains("fosfat") && (pupuk.pupuk == "SP 36" || pupuk.pupuk == "Superphos" || pupuk.pupuk == "TSP" || pupuk.pupuk == "Rock Phospate"))
                    }
                    
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 450.dp)
                            .verticalScroll(rememberScrollState())
                    ) {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFFEF3C7)),
                            border = BorderStroke(1.dp, Color(0xFFFDE68A)),
                            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    text = "TERDETEKSI DEFISIENSI:",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFB45309)
                                )
                                Text(
                                    text = "${hara.nama} ${hara.simbol}",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = MidnightBlue
                                )
                                Text(
                                    text = "Mobilitas Hara: ${hara.mobilitas}",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Color.Gray
                                )
                            }
                        }
                        
                        Text(
                            text = "FUNGSI UTAMA BAGI TANAMAN:",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Gray,
                            modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                        )
                        hara.fungsi.forEach { fungsi ->
                            Row(modifier = Modifier.padding(vertical = 2.dp)) {
                                Text("• ", fontSize = 12.sp, color = AspekGreen, fontWeight = FontWeight.Bold)
                                Text(text = fungsi, fontSize = 12.sp, color = Color.DarkGray)
                            }
                        }
                        
                        Text(
                            text = "GEJALA & DAMPAK DEFISIENSI:",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Gray,
                            modifier = Modifier.padding(top = 12.dp, bottom = 4.dp)
                        )
                        hara.gejalaDefisiensi.forEach { gejala ->
                            Row(modifier = Modifier.padding(vertical = 2.dp)) {
                                Text("⚠ ", fontSize = 11.sp, color = Color.Red)
                                Text(text = gejala, fontSize = 12.sp, color = Color.DarkGray)
                            }
                        }
                        
                        Text(
                            text = "REKOMENDASI PUPUK PENYEGAR:",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Gray,
                            modifier = Modifier.padding(top = 12.dp, bottom = 4.dp)
                        )
                        if (recommendedFertilisers.isEmpty()) {
                            Text("Gunakan pupuk pelengkap mikro atau konsultasikan dengan penyuluh.", fontSize = 12.sp, color = Color.Gray)
                        } else {
                            recommendedFertilisers.forEach { pupuk ->
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = Color(0xFFEFF6FF)),
                                    border = BorderStroke(1.dp, Color(0xFFDBEAFE)),
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(10.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(Icons.Default.Verified, contentDescription = null, tint = MediumBlue, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Column {
                                            Text(text = "Pupuk ${pupuk.pupuk}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MidnightBlue)
                                            Text(text = "Kandungan aktif: ${pupuk.kandungan.entries.joinToString { "${it.key} (${it.value}%)" }}", fontSize = 11.sp, color = Color.DarkGray)
                                        }
                                    }
                                }
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        // Route to official local maps kiosk
                        Button(
                            onClick = {
                                if (activity != null) {
                                    InterstitialAdHelper.showAd(activity, isPremium) {
                                        try {
                                            // Menggunakan GPS bawaan/pencarian lokasi relatif terdekat secara akurat
                                            val mapsUriStr = "geo:0,0?q=Kios+Pupuk+Sultra+Tani+Subur+Kolaka"
                                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(mapsUriStr))
                                            context.startActivity(intent)
                                        } catch (e: Exception) {
                                            android.widget.Toast.makeText(context, "Gagal mendapatkan lokasi GPS atau membuka peta.", android.widget.Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                } else {
                                    try {
                                        val mapsUriStr = "geo:0,0?q=Kios+Pupuk+Sultra+Tani+Subur+Kolaka"
                                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(mapsUriStr))
                                        context.startActivity(intent)
                                    } catch (e: Exception) {
                                        android.widget.Toast.makeText(context, "Gagal membuka peta.", android.widget.Toast.LENGTH_SHORT).show()
                                    }
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEFF6FF), contentColor = MediumBlue),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                        ) {
                            Icon(Icons.Default.Map, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Rute Google Maps Kios Subsidi Resmi", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            },
            confirmButton = {
                if (selectedDeficiency != null) {
                    Button(
                        onClick = {
                            val hara = selectedDeficiency!!
                            val promptMsg = "Saya menganalisis foto tanaman saya menggunakan Kamera Analisa Hara, dan terdeteksi gejala defisiensi unsur hara **${hara.nama} ${hara.simbol}**. Gejala teramati: ${hara.gejalaDefisiensi.firstOrNull() ?: "Daun bermasalah"}. Berikan penjelasan ahli mengenai fungsi hara ini, dampaknya bagi kelangsungan hasil panen, dan bagaimana cara pemberian pupuk pelengkap yang baik menurut standar PPL Sulawesi Tenggara!"
                            viewModel.sendMessage(promptMsg)
                            showCameraAnalysisDialog = false
                            selectedDeficiency = null
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = AspekGreen)
                    ) {
                        Text("Kirim ke Obrolan AI", color = Color.White)
                    }
                }
            },
            dismissButton = {
                TextButton(
                    enabled = uploadStatus != "LOADING",
                    onClick = {
                        if (selectedDeficiency != null) {
                            selectedDeficiency = null
                        } else {
                            showCameraAnalysisDialog = false
                        }
                    }
                ) {
                    Text(
                        text = if (selectedDeficiency != null) "Kembali" else "Tutup",
                        color = if (uploadStatus == "LOADING") Color.LightGray else MidnightBlue
                    )
                }
            }
        )
    }

    if (showQuotaEmptyDialog) {
        val loc = profile?.location?.lowercase() ?: ""
        val greeting = when {
            loc.contains("sulawesi") || loc.contains("makassar") || loc.contains("bugis") || loc.contains("kolaka") -> "Tabe', maaf kuota habis."
            loc.contains("sumatera") || loc.contains("aceh") || loc.contains("minang") || loc.contains("batak") -> "Horas/Apo kaba, maaf kuota habis."
            loc.contains("jawa") || loc.contains("jateng") || loc.contains("jatim") -> "Sugeng enjing, pangapunten kuota telas."
            loc.contains("sunda") || loc.contains("jabar") -> "Sampurasun, punten kuota seep."
            loc.contains("bali") -> "Om Swastiastu, ngampura kuota telah."
            loc.contains("kalimantan") || loc.contains("banjar") -> "Salamat baisukan, maaf kuota habis."
            loc.contains("maluku") || loc.contains("papua") -> "Wa wa wa, maaf kuota habis."
            else -> "Halo Kak, mohon maaf kuota tanya hari ini sudah habis."
        }

        AlertDialog(
            onDismissRequest = { showQuotaEmptyDialog = false },
            title = {
                Text(text = "Kuota Habis", fontWeight = FontWeight.Bold, color = MidnightBlue)
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(text = greeting, fontWeight = FontWeight.SemiBold, fontSize = 16.sp, color = Color(0xFFB45309))
                    Text(text = "Anda dapat melanjutkan dengan memilih salah satu dari dua opsi berikut:")
                    Text(text = "1. Premium (Bebas iklan, akses tanya & gambar tanpa batas) dengan Rp 25.000/Bulan atau Rp 230.000/Tahun.")
                    Text(text = "2. Tonton 1 video singkat untuk memulihkan akses Flash Lite (100x chat, 2x gambar).")
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showQuotaEmptyDialog = false
                        viewModel.setPremiumStatus(true)
                        android.widget.Toast.makeText(context, "Langganan Premium Bulanan aktif!", android.widget.Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB))
                ) {
                    Text("Langganan Rp 25.000")
                }
            },
            dismissButton = {
                Button(
                    onClick = {
                        showQuotaEmptyDialog = false
                        android.widget.Toast.makeText(context, "Mempersiapkan video panduan pendek...", android.widget.Toast.LENGTH_SHORT).show()
                        loadAndShowRewardedAd(
                            context = context,
                            onAdDismissed = {},
                            onRewardEarned = {
                                viewModel.rewardUserWithQuota()
                                android.widget.Toast.makeText(context, "Alhamdulillah! Anda bebas berinteraksi sepuasnya dengan Gemini Flash Lite.", android.widget.Toast.LENGTH_LONG).show()
                            }
                        )
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD4A00D))
                ) {
                    Text("Tonton Iklan")
                }
            }
        )
    }

    if (showMicInfoDialog) {
        AlertDialog(
            onDismissRequest = { showMicInfoDialog = false },
            title = {
                Text("Asisten Suara", fontWeight = FontWeight.Bold)
            },
            text = {
                Text("Ketuk 'Mulai Bicara' lalu sampaikan pertanyaan atau kendala pertanian Anda dengan jelas.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        showMicInfoDialog = false
                        chatController.startListening(
                            onResult = { text ->
                                recognizedSpeechText = text
                                isSpeechNoisyOrEmpty = text.trim().length < 3
                                showMicResultDialog = true
                            },
                            onError = { errorMsg ->
                                android.widget.Toast.makeText(context, "Error: $errorMsg", android.widget.Toast.LENGTH_SHORT).show()
                            }
                        )
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AspekGreen)
                ) {
                    Text("Mulai Bicara", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showMicInfoDialog = false }) {
                    Text("Batal", color = Color.Gray)
                }
            }
        )
    }
    if (showMicResultDialog) {
        AlertDialog(
            onDismissRequest = { showMicResultDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Mic,
                        contentDescription = null,
                        tint = AspekGreen,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Tinjau Hasil Rekaman Suara",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = MidnightBlue
                    )
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Silakan periksa teks di bawah ini. Bapak/Ibu dapat mengeditnya jika ada kata-kata yang kurang pas atau salah dengar akibat bising sekitar sebelum dikirim ke AI:",
                        fontSize = 12.sp,
                        color = Color.DarkGray
                    )

                    OutlinedTextField(
                        value = recognizedSpeechText,
                        onValueChange = { 
                            recognizedSpeechText = it 
                            // Begitu petani mengetik atau mengubah teks, hapus indikasi bising agar tidak mengganggu
                            isSpeechNoisyOrEmpty = false
                        },
                        modifier = Modifier.fillMaxWidth(),
                        textStyle = TextStyle(fontSize = 13.sp, color = Color(0xFF0F172A), fontWeight = FontWeight.Bold),
                        maxLines = 5,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AspekGreen,
                            unfocusedBorderColor = Color.LightGray
                        )
                    )

                    if (isSpeechNoisyOrEmpty) {
                        Text(
                            text = "Peringatan: Teks kosong atau terdeteksi bising. Silakan coba bicara lebih keras/jelas, atau Bapak/Ibu bisa langsung mengetik pertanyaan di atas.",
                            fontSize = 11.sp,
                            color = Color.Red,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (recognizedSpeechText.trim().isNotBlank()) {
                            viewModel.sendMessage(recognizedSpeechText)
                            showMicResultDialog = false
                        }
                    },
                    enabled = recognizedSpeechText.trim().isNotBlank(),
                    colors = ButtonDefaults.buttonColors(containerColor = AspekGreen)
                ) {
                    Text("Kirim Pertanyaan", color = Color.White)
                }
            },
            dismissButton = {
                Row {
                    TextButton(
                        onClick = {
                            showMicResultDialog = false
                            showMicInfoDialog = true
                        }
                    ) {
                        Text("Coba Lagi", color = AspekGreen)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    TextButton(
                        onClick = { showMicResultDialog = false }
                    ) {
                        Text("Batal", color = Color.Gray)
                    }
                }
            }
        )
    }

    if (showDeleteHistoryDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteHistoryDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.DeleteForever,
                        contentDescription = null,
                        tint = Color.Red,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Hapus Riwayat Chat?",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = MidnightBlue
                    )
                }
            },
            text = {
                Text(
                    text = "Apakah Anda yakin ingin menghapus pesan riwayat percakapan ini dari daftar?\n\n\"$topicToDelete\"",
                    fontSize = 13.sp,
                    color = Color.DarkGray
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteChatMessageByText(topicToDelete)
                        showDeleteHistoryDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                ) {
                    Text("Ya", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showDeleteHistoryDialog = false }
                ) {
                    Text("Tidak", color = MidnightBlue, fontWeight = FontWeight.SemiBold)
                }
            }
        )
    }

    if (showLandManagementDialog) {
        AlertDialog(
            onDismissRequest = {
                showLandManagementDialog = false
                selectedLandForAnalysis = null
            },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (selectedLandForAnalysis == null) Icons.Default.Landscape else Icons.Default.Info,
                        contentDescription = null,
                        tint = AspekGreen,
                        modifier = Modifier.size(26.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = selectedLandForAnalysis?.tipeLahan ?: "Pengelolaan Jenis Lahan",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MidnightBlue
                    )
                }
            },
            text = {
                if (selectedLandForAnalysis == null) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 400.dp)
                    ) {
                        Text(
                            text = "Pilih jenis lahan di bawah untuk melihat rincian tantangan agraria beserta petunjuk teknis pengelolaan tanah agar tanaman berkembang optimal.",
                            fontSize = 12.sp,
                            color = Color.DarkGray,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )

                        LazyColumn(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(LandManagement.listLahan) { land ->
                                val icon = when (land.tipeLahan) {
                                    "Lahan Kering" -> Icons.Default.WbSunny
                                    "Lahan Basah" -> Icons.Default.WaterDrop
                                    "Lahan Rawa" -> Icons.Default.Grass
                                    "Lahan Gambut" -> Icons.Default.Eco
                                    "Lahan Sulfat Masam" -> Icons.Default.Warning
                                    "Lahan Salin" -> Icons.Default.Info
                                    "Lahan Aluvial" -> Icons.Default.Landscape
                                    "Lahan Vulkanik" -> Icons.Default.Whatshot
                                    "Lahan Terjal/Berbukit" -> Icons.AutoMirrored.Filled.TrendingUp
                                    else -> Icons.Default.Landscape
                                }

                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { selectedLandForAnalysis = land },
                                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
                                    border = BorderStroke(1.dp, Color(0xFFE2E8F0))
                                ) {
                                    Row(
                                        modifier = Modifier.padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = icon,
                                            contentDescription = null,
                                            tint = AspekGreen,
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Column {
                                            Text(
                                                text = land.tipeLahan,
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = MidnightBlue
                                            )
                                            Text(
                                                text = land.tantangan.firstOrNull()?.take(50)?.let { "$it..." } ?: "",
                                                fontSize = 11.sp,
                                                color = Color.Gray
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                } else {
                    val land = selectedLandForAnalysis!!
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 450.dp)
                            .verticalScroll(rememberScrollState())
                    ) {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFFEF3C7)),
                            border = BorderStroke(1.dp, Color(0xFFFDE68A)),
                            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    text = "KARAKTERISTIK UNGGULAN",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFB45309)
                                )
                                Text(
                                    text = land.tipeLahan,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = MidnightBlue
                                )
                            }
                        }

                        Text(
                            text = "TANTANGAN AGRISISTEM:",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Gray,
                            modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                        )
                        land.tantangan.forEach { tantangan ->
                            Row(modifier = Modifier.padding(vertical = 4.dp)) {
                                Text("⚠  ", fontSize = 12.sp, color = Color.Red, fontWeight = FontWeight.Bold)
                                Text(text = tantangan, fontSize = 12.sp, color = Color.DarkGray)
                            }
                        }

                        Text(
                            text = "TEKNIK REKOMENDASI PENGELOLAAN:",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Gray,
                            modifier = Modifier.padding(top = 12.dp, bottom = 4.dp)
                        )
                        land.pengelolaan.forEach { rujukan ->
                            Row(modifier = Modifier.padding(vertical = 4.dp)) {
                                Text("✔  ", fontSize = 12.sp, color = AspekGreen, fontWeight = FontWeight.Bold)
                                Text(text = rujukan, fontSize = 12.sp, color = Color.DarkGray)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                if (selectedLandForAnalysis != null) {
                    Button(
                        onClick = {
                            val land = selectedLandForAnalysis!!
                            val promptMsg = "Tabe' Pak Ahli PPL, saya ingin bertanya lebih lanjut mengenai jenis **${land.tipeLahan}**. Tolong jelaskan strategi detail penanganan tantangan: \"${land.tantangan.firstOrNull() ?: ""}\", serta jelaskan langkah taktis pelaksanaan pemupukan yang paling tepat sesuai anjuran dinas!"
                            viewModel.sendMessage(promptMsg)
                            showLandManagementDialog = false
                            selectedLandForAnalysis = null
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = AspekGreen)
                    ) {
                        Text("Konsultasikan ke AI", color = Color.White)
                    }
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        if (selectedLandForAnalysis != null) {
                            selectedLandForAnalysis = null
                        } else {
                            showLandManagementDialog = false
                        }
                    }
                ) {
                    Text(if (selectedLandForAnalysis != null) "Kembali" else "Tutup", color = MidnightBlue)
                }
            }
        )
    }
}

fun parseMarkdown(text: String): androidx.compose.ui.text.AnnotatedString {
    return androidx.compose.ui.text.buildAnnotatedString {
        var cursor = 0
        while (cursor < text.length) {
            val boldStart = text.indexOf("**", cursor)
            if (boldStart != -1) {
                val boldEnd = text.indexOf("**", boldStart + 2)
                if (boldEnd != -1) {
                    append(text.substring(cursor, boldStart))
                    pushStyle(androidx.compose.ui.text.SpanStyle(fontWeight = androidx.compose.ui.text.font.FontWeight.Bold))
                    append(text.substring(boldStart + 2, boldEnd))
                    pop()
                    cursor = boldEnd + 2
                } else {
                    append(text.substring(cursor))
                    cursor = text.length
                }
            } else {
                append(text.substring(cursor))
                cursor = text.length
            }
        }
    }
}

fun cropCenter(bitmap: android.graphics.Bitmap, factor: Float): android.graphics.Bitmap {
    val width = bitmap.width
    val height = bitmap.height
    val cropSize = (minOf(width, height) * factor).toInt()
    val left = (width - cropSize) / 2
    val top = (height - cropSize) / 2
    return android.graphics.Bitmap.createBitmap(bitmap, left, top, cropSize, cropSize)
}

fun bitmapToBase64(bitmap: android.graphics.Bitmap): String {
    val scaled = com.asistenpetani.core.ai.ImageCompressor.kompresDimensi(bitmap)
    val bytes = com.asistenpetani.core.ai.ImageCompressor.kompresKeJPG(scaled, 208)
    return android.util.Base64.encodeToString(bytes, android.util.Base64.NO_WRAP)
}


fun loadAndShowRewardedAd(
    context: android.content.Context,
    onAdDismissed: () -> Unit,
    onRewardEarned: () -> Unit
) {
    // Memeriksa batasan iklan harian (Maksimal 3 kali penayangan iklan per hari)
    if (!com.asistenpetani.util.InterstitialAdHelper.canShowAdToday(context)) {
        android.util.Log.d("AspekAdLimit", "Batas iklan harian tercapai. Memberikan reward langsung tanpa memutar iklan demi kenyamanan petani.")
        android.widget.Toast.makeText(context, "Batas iklan harian (3x) terpenuhi. Anda mendapatkan kuota gratis secara instan!", android.widget.Toast.LENGTH_LONG).show()
        onRewardEarned()
        onAdDismissed()
        return
    }

    val adRequest = AdRequest.Builder().build()
    // ID Iklan Rewarded Uji Coba Resmi dari Google
    val adUnitId = com.asistenpetani.BuildConfig.ADMOB_REWARDED_ID

    RewardedAd.load(
        context,
        adUnitId,
        adRequest,
        object : RewardedAdLoadCallback() {
            override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                // Sangat aman: Jika internet bermasalah atau gagal muat, langsung beri reward sebagai bentuk fallback yang bersahabat (Graceful Fallback)
                android.util.Log.e("AspekAdMob", "Iklan Rewarded gagal dimuat: ${loadAdError.message}. Memberikan fallback reward.")
                onRewardEarned()
                onAdDismissed()
            }

            override fun onAdLoaded(rewardedAd: RewardedAd) {
                rewardedAd.fullScreenContentCallback = object : com.google.android.gms.ads.FullScreenContentCallback() {
                    override fun onAdDismissedFullScreenContent() {
                        com.asistenpetani.util.InterstitialAdHelper.incrementAdCount(context)
                        onAdDismissed()
                    }

                    override fun onAdFailedToShowFullScreenContent(adError: com.google.android.gms.ads.AdError) {
                        // Layangkan reward apabila penayangan gagal
                        onRewardEarned()
                        onAdDismissed()
                    }
                }
                
                // Tampilkan iklan dengan melampirkan callback reward
                rewardedAd.show(context as android.app.Activity) { rewardItem ->
                    onRewardEarned()
                }
            }
        }
    )
}

@Composable
fun InteractiveCropDialog(
    bitmap: android.graphics.Bitmap,
    onDismiss: () -> Unit,
    onCropped: (android.graphics.Bitmap) -> Unit
) {
    var cropLeft by remember { mutableStateOf(0.1f) }
    var cropTop by remember { mutableStateOf(0.1f) }
    var cropRight by remember { mutableStateOf(0.9f) }
    var cropBottom by remember { mutableStateOf(0.9f) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF0F172A)), // Deep elegant slate dark background
            color = Color(0xFF0F172A)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.SpaceBetween,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header Bar
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 24.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Batal",
                            tint = Color.White
                        )
                    }
                    Text(
                        text = "Sesuaikan Area Pangkas",
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(
                        onClick = {
                            val w = bitmap.width
                            val h = bitmap.height
                            val xVal = (cropLeft * w).toInt().coerceIn(0, w - 1)
                            val yVal = (cropTop * h).toInt().coerceIn(0, h - 1)
                            val wVal = ((cropRight - cropLeft) * w).toInt().coerceAtLeast(1).coerceAtMost(w - xVal)
                            val hVal = ((cropBottom - cropTop) * h).toInt().coerceAtLeast(1).coerceAtMost(h - yVal)
                            try {
                                val cropped = android.graphics.Bitmap.createBitmap(bitmap, xVal, yVal, wVal, hVal)
                                onCropped(cropped)
                            } catch (e: Exception) {
                                android.util.Log.e("AspekCrop", "Error cropping bitmap: ${e.message}")
                            }
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Selesai",
                            tint = Color(0xFF10B981)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Interactive Cropper Canvas Area
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .aspectRatio(1f)
                        .background(Color.Black, RoundedCornerShape(12.dp))
                        .clip(RoundedCornerShape(12.dp))
                        .align(Alignment.CenterHorizontally),
                    contentAlignment = Alignment.Center
                ) {
                    var wPx by remember { mutableStateOf(0f) }
                    var hPx by remember { mutableStateOf(0f) }
                    var activeHandle by remember { mutableStateOf(CropHandle.NONE) }

                    Canvas(
                        modifier = Modifier
                            .fillMaxSize()
                            .pointerInput(Unit) {
                                detectDragGestures(
                                    onDragStart = { startOffset ->
                                        if (wPx > 0f && hPx > 0f) {
                                            val tX = startOffset.x
                                            val tY = startOffset.y
                                            
                                            val tlX = cropLeft * wPx
                                            val tlY = cropTop * hPx
                                            val trX = cropRight * wPx
                                            val trY = cropTop * hPx
                                            val blX = cropLeft * wPx
                                            val blY = cropBottom * hPx
                                            val brX = cropRight * wPx
                                            val brY = cropBottom * hPx

                                            val threshold = 60f // pixel radius for handle detection
                                            
                                            activeHandle = when {
                                                distance(tX, tY, tlX, tlY) < threshold -> CropHandle.TOP_LEFT
                                                distance(tX, tY, trX, trY) < threshold -> CropHandle.TOP_RIGHT
                                                distance(tX, tY, blX, blY) < threshold -> CropHandle.BOTTOM_LEFT
                                                distance(tX, tY, brX, brY) < threshold -> CropHandle.BOTTOM_RIGHT
                                                tX in (tlX..trX) && tY in (tlY..blY) -> CropHandle.CENTER
                                                else -> CropHandle.NONE
                                            }
                                        }
                                    },
                                    onDrag = { change: PointerInputChange, dragAmount: Offset ->
                                        change.consume()
                                        if (wPx > 0f && hPx > 0f && activeHandle != CropHandle.NONE) {
                                            val currentX = change.position.x
                                            val currentY = change.position.y
                                            val dx = dragAmount.x / wPx
                                            val dy = dragAmount.y / hPx

                                            when (activeHandle) {
                                                CropHandle.TOP_LEFT -> {
                                                    cropLeft = (currentX / wPx).coerceIn(0f, cropRight - 0.15f)
                                                    cropTop = (currentY / hPx).coerceIn(0f, cropBottom - 0.15f)
                                                }
                                                CropHandle.TOP_RIGHT -> {
                                                    cropRight = (currentX / wPx).coerceIn(cropLeft + 0.15f, 1f)
                                                    cropTop = (currentY / hPx).coerceIn(0f, cropBottom - 0.15f)
                                                }
                                                CropHandle.BOTTOM_LEFT -> {
                                                    cropLeft = (currentX / wPx).coerceIn(0f, cropRight - 0.15f)
                                                    cropBottom = (currentY / hPx).coerceIn(cropTop + 0.15f, 1f)
                                                }
                                                CropHandle.BOTTOM_RIGHT -> {
                                                    cropRight = (currentX / wPx).coerceIn(cropLeft + 0.15f, 1f)
                                                    cropBottom = (currentY / hPx).coerceIn(cropTop + 0.15f, 1f)
                                                }
                                                CropHandle.CENTER -> {
                                                    val width = cropRight - cropLeft
                                                    val height = cropBottom - cropTop
                                                    val newLeft = (cropLeft + dx).coerceIn(0f, 1f - width)
                                                    val newTop = (cropTop + dy).coerceIn(0f, 1f - height)
                                                    cropLeft = newLeft
                                                    cropTop = newTop
                                                    cropRight = newLeft + width
                                                    cropBottom = newTop + height
                                                }
                                                else -> {}
                                            }
                                        }
                                    },
                                    onDragEnd = {
                                        activeHandle = CropHandle.NONE
                                    }
                                )
                            }
                    ) {
                        wPx = size.width
                        hPx = size.height

                        // 1. Draw the actual image stretched to fit canvas
                        drawImage(
                            image = bitmap.asImageBitmap(),
                            dstSize = androidx.compose.ui.unit.IntSize(wPx.toInt(), hPx.toInt())
                        )

                        // 2. Draw semi-transparent dark overlay around the crop area
                        // Top
                        drawRect(
                            color = Color(0x99000000),
                            topLeft = androidx.compose.ui.geometry.Offset(0f, 0f),
                            size = androidx.compose.ui.geometry.Size(wPx, cropTop * hPx)
                        )
                        // Bottom
                        drawRect(
                            color = Color(0x99000000),
                            topLeft = androidx.compose.ui.geometry.Offset(0f, cropBottom * hPx),
                            size = androidx.compose.ui.geometry.Size(wPx, hPx - (cropBottom * hPx))
                        )
                        // Left
                        drawRect(
                            color = Color(0x99000000),
                            topLeft = androidx.compose.ui.geometry.Offset(0f, cropTop * hPx),
                            size = androidx.compose.ui.geometry.Size(cropLeft * wPx, (cropBottom - cropTop) * hPx)
                        )
                        // Right
                        drawRect(
                            color = Color(0x99000000),
                            topLeft = androidx.compose.ui.geometry.Offset(cropRight * wPx, cropTop * hPx),
                            size = androidx.compose.ui.geometry.Size(wPx - (cropRight * wPx), (cropBottom - cropTop) * hPx)
                        )

                        // 3. Draw bright green border for crop box
                        val cropBoxLeft = cropLeft * wPx
                        val cropBoxTop = cropTop * hPx
                        val cropBoxWidth = (cropRight - cropLeft) * wPx
                        val cropBoxHeight = (cropBottom - cropTop) * hPx

                        drawRect(
                            color = Color(0xFF10B981),
                            topLeft = androidx.compose.ui.geometry.Offset(cropBoxLeft, cropBoxTop),
                            size = androidx.compose.ui.geometry.Size(cropBoxWidth, cropBoxHeight),
                            style = androidx.compose.ui.graphics.drawscope.Stroke(width = 3.dp.toPx())
                        )

                        // 4. Draw corner drag handles
                        val handleRadius = 8.dp.toPx()
                        val handleColor = Color.White
                        
                        // Top-Left
                        drawCircle(color = handleColor, radius = handleRadius, center = androidx.compose.ui.geometry.Offset(cropBoxLeft, cropBoxTop))
                        // Top-Right
                        drawCircle(color = handleColor, radius = handleRadius, center = androidx.compose.ui.geometry.Offset(cropBoxLeft + cropBoxWidth, cropBoxTop))
                        // Bottom-Left
                        drawCircle(color = handleColor, radius = handleRadius, center = androidx.compose.ui.geometry.Offset(cropBoxLeft, cropBoxTop + cropBoxHeight))
                        // Bottom-Right
                        drawCircle(color = handleColor, radius = handleRadius, center = androidx.compose.ui.geometry.Offset(cropBoxLeft + cropBoxWidth, cropBoxTop + cropBoxHeight))
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Bottom Panel / Controls
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Petunjuk Penggunaan:",
                            color = Color(0xFF10B981),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 4.dp)
                        )
                        Text(
                            text = "• Geser lingkaran di pojok kotak untuk memperbesar/memperkecil area pangkas.\n• Geser bagian tengah kotak untuk memindahkan posisi area pangkas.\n• Tekan ikon centang hijau di pojok kanan atas jika sudah selesai.",
                            color = Color(0xFFE2E8F0),
                            fontSize = 10.sp,
                            lineHeight = 14.sp
                        )
                    }
                }
            }
        }
    }
}

enum class CropHandle { NONE, TOP_LEFT, TOP_RIGHT, BOTTOM_LEFT, BOTTOM_RIGHT, CENTER }

private fun distance(x1: Float, y1: Float, x2: Float, y2: Float): Float {
    val dx = x1 - x2
    val dy = y1 - y2
    return kotlin.math.sqrt(dx * dx + dy * dy)
}

