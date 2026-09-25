package com.asistenpetani

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.lifecycleScope
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialException
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.GoogleAuthProvider
import com.asistenpetani.ui.screens.DashboardScreen
import com.asistenpetani.ui.screens.SettingsScreen
import com.asistenpetani.ui.theme.MyApplicationTheme
import com.asistenpetani.ui.viewmodel.AppViewModel
import com.google.android.gms.ads.MobileAds
import com.google.firebase.FirebaseApp
import com.google.firebase.appcheck.FirebaseAppCheck
import com.google.firebase.appcheck.playintegrity.PlayIntegrityAppCheckProviderFactory
import kotlinx.coroutines.launch

import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private fun performGoogleSignIn(authViewModel: com.asistenpetani.ui.viewmodel.AuthViewModel, name: String, crop: String, area: String, kategori: String = "Tanaman", lokasiDiizinkan: Boolean = false) {
        val credentialManager = CredentialManager.create(this)
        val webClientId = if (authViewModel.webClientId.isNotBlank()) authViewModel.webClientId else getString(R.string.default_web_client_id)
        val googleIdOption = GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(false)
            .setServerClientId(webClientId)
            .setAutoSelectEnabled(false)
            .build()

        val request = GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()

        lifecycleScope.launch {
            try {
                val result = credentialManager.getCredential(this@MainActivity, request)
                val credential = result.credential
                if (credential is androidx.credentials.CustomCredential &&
                    credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                    val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                    val idToken = googleIdTokenCredential.idToken
                    val firebaseCredential = GoogleAuthProvider.getCredential(idToken, null)
                    authViewModel.signInWithGoogle(firebaseCredential, name, crop, area, kategori, lokasiDiizinkan)
                } else {
                    Toast.makeText(this@MainActivity, "Credential type not supported", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Log.e("MainActivity", "Google Sign In Failed", e)
                Toast.makeText(this@MainActivity, "Login Google gagal: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private val viewModel: AppViewModel by viewModels()
    private lateinit var chatController: com.asistenpetani.ui.screens.chat.ChatController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        chatController = com.asistenpetani.ui.screens.chat.ChatController(this)
        
        // Inisialisasi Google Play Billing untuk mode langganan
        com.asistenpetani.util.PengelolaPenagihan(this).mulaiKoneksi()

        try {
            val webViewCacheDir = java.io.File(cacheDir, "WebView/Default/HTTP Cache/Code Cache")
            val jsDir = java.io.File(webViewCacheDir, "js")
            val wasmDir = java.io.File(webViewCacheDir, "wasm")
            jsDir.mkdirs()
            wasmDir.mkdirs()
            java.io.File(jsDir, ".placeholder").createNewFile()
            java.io.File(wasmDir, ".placeholder").createNewFile()
        } catch (e: Exception) {
            Log.e("MainActivity", "Failed to create WebView cache directories: ${e.message}")
        }

        // 1. Memastikan Firebase utama sudah menyala
        FirebaseApp.initializeApp(this)

        // 2. Menghidupkan Firebase App Check dengan standar Play Integrity
        // [ARSITEK NOTE]: DINONAKTIFKAN SEMENTARA untuk masa uji coba. 
        // Play Integrity akan memblokir login jika APK dibagikan langsung tanpa lewat Play Store.
        /*
        val firebaseAppCheck = FirebaseAppCheck.getInstance()
        if (BuildConfig.DEBUG) {
            firebaseAppCheck.installAppCheckProviderFactory(
                com.google.firebase.appcheck.debug.DebugAppCheckProviderFactory.getInstance()
            )
        } else {
            firebaseAppCheck.installAppCheckProviderFactory(
                PlayIntegrityAppCheckProviderFactory.getInstance()
            )
        }
        */
        
        // Lapis 1: Keamanan Anti-Clone (Installer Source Check)
        // [ARSITEK NOTE]: DINONAKTIFKAN SEMENTARA untuk masa uji coba.
        // Mencegah aplikasi force close saat diinstal via APK (Sideload).
        /*
        if (!BuildConfig.DEBUG) {
            if (!com.asistenpetani.util.AntiCloneSecurity.isInstalledFromPlayStore(this)) {
                Toast.makeText(this, "Uji Aplikasi Tidak Resmi! Harap instal ASPEK dari Google Play Store resmi.", Toast.LENGTH_LONG).show()
                finishAffinity()
                return
            }
        }
        */

        // Lapis 2: GMS Security Provider Update
        lifecycleScope.launch {
            try {
                val isSecure = com.asistenpetani.util.SecurityPatches.patchSecurityProvider(applicationContext)
                if (!isSecure) {
                    Log.w("MainActivity", "Google Play Protect / GMS Security Provider tidak tersedia di lingkungan ini.")
                }
            } catch (e: Exception) {
                Log.e("MainActivity", "Terjadi galat sistem (Security Patch): ${e.message}")
            }
        }
        
        // Inisialisasi SDK AdMob di latar belakang (Hanya jika Google Play Services tersedia)
        try {
            val availability = com.google.android.gms.common.GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(this)
            if (availability == com.google.android.gms.common.ConnectionResult.SUCCESS) {
                MobileAds.initialize(this) {}
                com.asistenpetani.util.InterstitialAdHelper.loadAd(this)
            } else {
                Log.w("AspekAdMob", "Google Play Services tidak tersedia ($availability), melewati inisialisasi MobileAds.")
            }
        } catch (e: Exception) {
            Log.e("AspekAdMob", "Gagal inisialisasi MobileAds: ${e.message}")
        }

        // Lapis 3: Inisialisasi Firebase AI On-Device Model Check & Download
        try {
            com.asistenpetani.data.repository.FirebaseAiHelper.checkAndPrepareOnDeviceModel(
                applicationContext,
                lifecycleScope
            )
        } catch (e: Exception) {
            Log.e("MainActivity", "Gagal inisialisasi on-device model: ${e.message}")
        }

        // Lapis 4: Inisialisasi Sinkronisasi Cadangan Database berkala ke Firebase Storage
        try {
            com.asistenpetani.service.sync.DatabaseSyncScheduler.schedulePeriodicSync(applicationContext)
            com.asistenpetani.service.sync.DatabaseSyncScheduler.scheduleImmediateSync(applicationContext)
        } catch (e: Exception) {
            Log.e("MainActivity", "Gagal menjadwalkan sinkronisasi database: ${e.message}")
        }
        
        // Lapis 5: Penjadwalan Pekerja Pengguna Tidak Aktif (Notifikasi Cuaca)
        try {
            val prefs = getSharedPreferences("aspek_ads_prefs", android.content.Context.MODE_PRIVATE)
            prefs.edit().putLong("last_app_open_time", System.currentTimeMillis()).apply()

            val workRequest = androidx.work.PeriodicWorkRequestBuilder<com.asistenpetani.service.worker.InactiveUserWorker>(24, java.util.concurrent.TimeUnit.HOURS)
                .build()
            androidx.work.WorkManager.getInstance(applicationContext).enqueueUniquePeriodicWork(
                "inactive_user_weather_worker",
                androidx.work.ExistingPeriodicWorkPolicy.KEEP, // Pakai KEEP agar jadwal tidak ter-reset berulang kali jika sering dibuka
                workRequest
            )
        } catch (e: Exception) {
            Log.e("MainActivity", "Gagal menjadwalkan InactiveUserWorker: ${e.message}")
        }

        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AspekAppContent(
                        viewModel = viewModel,
                        chatController = chatController,
                        onGoogleSignInClick = { authViewModel, name, crop, area, kategori, lokasiDiizinkan -> performGoogleSignIn(authViewModel, name, crop, area, kategori, lokasiDiizinkan) }
                    )
                }
            }
        }
    }

    override fun onDestroy() {
        chatController.destroy()
        super.onDestroy()
    }
}

@Composable
fun AspekAppContent(
    onGoogleSignInClick: (com.asistenpetani.ui.viewmodel.AuthViewModel, String, String, String, String, Boolean) -> Unit = { _, _, _, _, _, _ -> },
    viewModel: AppViewModel,
    chatController: com.asistenpetani.ui.screens.chat.ChatController
) {
    val authViewModel: com.asistenpetani.ui.viewmodel.AuthViewModel = androidx.hilt.navigation.compose.hiltViewModel()
    val user by authViewModel.user.collectAsState()
    val welcomeMessage by authViewModel.welcomeMessage.collectAsState()
    val errorMessage by authViewModel.errorMessage.collectAsState()
    var pendingSyncData by remember { mutableStateOf<Triple<String, String, String>?>(null) }
    
    val context = androidx.compose.ui.platform.LocalContext.current

    androidx.compose.runtime.LaunchedEffect(errorMessage) {
        errorMessage?.let { msg ->
            android.widget.Toast.makeText(context, "Login Gagal: $msg", android.widget.Toast.LENGTH_LONG).show()
            authViewModel.clearErrorMessage()
        }
    }
    androidx.compose.runtime.LaunchedEffect(welcomeMessage) {
        welcomeMessage?.let {
            android.widget.Toast.makeText(context, it, android.widget.Toast.LENGTH_LONG).show()
        }
    }
    
    var showSplash by remember { mutableStateOf(true) }
    var currentScreen by remember { mutableStateOf("home") }

    var showRegister by remember { mutableStateOf(false) }
    
    androidx.compose.runtime.LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(1500)
        showSplash = false
    }

    if (showSplash) {
        com.asistenpetani.ui.components.LogoAsistenPetaniSplash()
    } else {
        if (user == null) {
            if (showRegister) {
                com.asistenpetani.ui.screens.RegisterScreen(
                    onRegisterSuccess = { showRegister = false },
                    onBackToLoginClick = { showRegister = false }
                )
            } else {
                com.asistenpetani.ui.screens.LoginScreen(
                    onLoginClick = { name, crop, area, kategori, lokasiDiizinkan ->
                        pendingSyncData = Triple(name, crop, area)
                        onGoogleSignInClick(authViewModel, name, crop, area, kategori, lokasiDiizinkan)
                    },
                    onGoogleSignInClick = {
                        pendingSyncData = Triple("", "Jenis Tanaman", "1.0")
                        onGoogleSignInClick(authViewModel, "", "Jenis Tanaman", "1.0", "Tanaman", false)
                    },
                    onAdminLoginClick = { email, password ->
                        authViewModel.registerPetani(email, password, "Pemilik Aplikasi", "Premium", "Semua Komoditas")
                    },
                    onAnonymousLoginClick = { name, crop, area, kategori, lokasiDiizinkan ->
                        pendingSyncData = Triple(name, crop, area)
                        authViewModel.signInAnonymously(name, crop, area, kategori, lokasiDiizinkan)
                    },
                    onRegisterClick = {
                        showRegister = true
                    },
                    onEmailLoginClick = { email, password ->
                        authViewModel.signInWithEmailAndPassword(email, password)
                    }
                )
            }
        } else {
            val isOwner = user?.email == "indra.oktawan@gmail.com" || user?.displayName == "Pemilik Aplikasi"
            androidx.compose.runtime.LaunchedEffect(user?.uid) {
                if (user != null && (viewModel.farmerProfile.value == null || !viewModel.farmerProfile.value!!.isLoggedIn)) {
                    if (isOwner) {
                        viewModel.loginAndSync("Pemilik Aplikasi (Admin)", "Semua Komoditas", "Premium", "ADMIN-001")
                    } else {
                        if (pendingSyncData != null) {
                            val name = if (pendingSyncData!!.first.isBlank()) user?.displayName ?: "Petani" else pendingSyncData!!.first
                            viewModel.loginAndSync(name, pendingSyncData!!.second, pendingSyncData!!.third, "lahan_1")
                            pendingSyncData = null
                        } else {
                            val profile = authViewModel.getFarmerProfile(user!!.uid)
                            val name = profile?.get("name") as? String ?: user?.displayName ?: "Petani"
                            val crop = profile?.get("cropType") as? String ?: "Tanaman"
                            val area = profile?.get("landArea") as? String ?: "1.0"
                            viewModel.loginAndSync(name, crop, area, "lahan_1")
                        }
                    }
                }
            }

            com.asistenpetani.ui.components.AspekPermissionManager {
                when (currentScreen) {
                    "home" -> {
                        DashboardScreen(
                            viewModel = viewModel,
                            onSettingsClick = { currentScreen = "settings" },
                            chatController = chatController,
                            onPremiumFieldClick = { currentScreen = "premium_field" },
                            onMountainClick = { currentScreen = "mountain" }
                        )
                    }
                    "settings" -> {
                        val context = androidx.compose.ui.platform.LocalContext.current
                        val isAnon = authViewModel.user.collectAsState().value?.isAnonymous ?: false
                        SettingsScreen(
                            viewModel = viewModel,
                            isAnonymous = isAnon,
                            onBackClick = { currentScreen = "home" },
                            onLogoutClick = {
                                authViewModel.signOut()
                                viewModel.logout()
                                currentScreen = "home"
                            },
                            onBukaLangganan = { currentScreen = "langganan" },
                            onBukaKebijakanPrivasi = { currentScreen = "privacy_policy" },
                            onVerifikasiEmail = { email, password ->
                                authViewModel.linkEmailAndPassword(
                                    email = email,
                                    sandi = password,
                                    onSuccess = {
                                        android.widget.Toast.makeText(context, "Berhasil menautkan email! Akun Anda kini aman.", android.widget.Toast.LENGTH_SHORT).show()
                                    },
                                    onError = { errorMsg ->
                                        android.widget.Toast.makeText(context, errorMsg, android.widget.Toast.LENGTH_LONG).show()
                                    }
                                )
                            }
                        )
                    }
                    "privacy_policy" -> {
                        com.asistenpetani.ui.screens.PrivacyPolicyScreen(
                            onBackClick = { currentScreen = "settings" }
                        )
                    }
                    "langganan" -> {
                        com.asistenpetani.ui.screens.LayarLangganan(
                            onBeliBulananKlik = {
                                android.util.Log.d("Langganan", "Paket Bulanan Dipilih")
                            },
                            onBeliTahunanKlik = {
                                android.util.Log.d("Langganan", "Paket Tahunan Dipilih")
                            },
                            onBackClick = { currentScreen = "settings" }
                        )
                    }
                    "premium_field" -> {
                        com.asistenpetani.ui.screens.PremiumFieldScreen(
                            onBackClick = { currentScreen = "home" }
                        )
                    }
                    "mountain" -> {
                        com.asistenpetani.ui.features.maps.MapScreen(
                            onBackClick = { currentScreen = "home" }
                        )
                    }
                }
            }
        }
    }
}
