package com.asistenpetani.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.asistenpetani.data.local.room.AppDatabase
import com.asistenpetani.data.local.room.ChatMessage
import com.asistenpetani.data.local.room.FarmerProfile
import com.asistenpetani.data.repository.AiAnalysisRepositoryImpl
import com.asistenpetani.data.repository.AuthRepositoryImpl
import com.asistenpetani.data.repository.LahanRepositoryImpl
import com.asistenpetani.data.repository.MarketRepositoryImpl
import com.asistenpetani.data.repository.MapsRepositoryImpl
import com.asistenpetani.data.repository.ExpertRepositoryImpl
import com.asistenpetani.data.repository.FarmingRepositoryImpl
import com.asistenpetani.domain.usecase.auth.LoginFarmerUseCase
import com.asistenpetani.domain.usecase.database.VerifyLahanLocationUseCase
import com.asistenpetani.domain.usecase.maps.GetNearbyStoresUseCase
import com.asistenpetani.domain.usecase.maps.GetBuyerLocationUseCase
import com.asistenpetani.domain.usecase.market.GetCurrentPricesUseCase
import com.asistenpetani.domain.usecase.farming.GetLandRecommendationUseCase
import com.asistenpetani.domain.usecase.farming.CalculateHarvestUseCase
import com.asistenpetani.domain.usecase.notification.SetFarmingReminderUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import android.util.Log
import kotlinx.coroutines.Dispatchers
import com.asistenpetani.data.remote.GcpEnvironment
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody

class AppViewModel(application: Application) : AndroidViewModel(application) {


    private val database = AppDatabase.getDatabase(application)
    private val repository = AiAnalysisRepositoryImpl(database.appDao())

    // Single source of truth credentials from Hilt Module
    private val workloadCredentials = com.asistenpetani.di.NetworkModule.provideWorkloadCredentials(application)
    
    // Abstracted Repository implementation provided via Repository DI Module
    val aspekRepository: com.asistenpetani.domain.repository.AspekRepository = com.asistenpetani.di.RepositoryModule.provideAspekRepository(repository, workloadCredentials)

    // Sealed Class UiState wrapper pattern for robust state management
    private val _adviceState = MutableStateFlow<com.asistenpetani.util.UiState<com.asistenpetani.domain.model.DataResult>>(
        com.asistenpetani.util.UiState.Success(com.asistenpetani.domain.model.DataResult("", "Silakan masukkan pertanyaan untuk berkonsultasi mengenai info pertanian."))
    )
    val adviceState: StateFlow<com.asistenpetani.util.UiState<com.asistenpetani.domain.model.DataResult>> = _adviceState.asStateFlow()

    // Direct domain layer architecture wiring (Clean architecture without heavy DI boilerplate)
    private val customAuthRepo = AuthRepositoryImpl(database.appDao())
    private val customLahanRepo = LahanRepositoryImpl()
    private val customMarketRepo = MarketRepositoryImpl()
    private val customMapsRepo = MapsRepositoryImpl()
    private val customExpertRepo = ExpertRepositoryImpl()
    private val customFarmingRepo = FarmingRepositoryImpl()

    private val loginFarmerUseCase = LoginFarmerUseCase(customAuthRepo)
    val verifyLahanLocationUseCase = VerifyLahanLocationUseCase(customLahanRepo)
    val getNearbyStoresUseCase = GetNearbyStoresUseCase(customMapsRepo)
    val getBuyerLocationUseCase = GetBuyerLocationUseCase(customMapsRepo)
    val getCurrentPricesUseCase = GetCurrentPricesUseCase(customMarketRepo)
    val getLandRecommendationUseCase = GetLandRecommendationUseCase(customFarmingRepo)
    val calculateHarvestUseCase = CalculateHarvestUseCase(customFarmingRepo)
    val setFarmingReminderUseCase = SetFarmingReminderUseCase()

    private val customGenerativeAiRepo = com.asistenpetani.di.RepositoryModule.provideGenerativeAiRepository(application)
    private val imageHelper = com.asistenpetani.util.ImageHelper(application)

    private fun getSystemPrompt(location: String, profile: com.asistenpetani.data.local.room.FarmerProfile?): String {
        return """
            Anda adalah ASPEK (Asisten Petani AI).
            Lokasi pengguna: $location. 
            Nama: ${profile?.name ?: "Petani"}. 
            Komoditas: ${profile?.cropType ?: "Tanaman"}. 
            Luas lahan: ${profile?.landArea ?: "1"} Hektar.
            
            Instruksi Utama:
            1. Pahami Konteks Pembahasan: Anda harus memahami alur percakapan. Jika pertanyaan atau pernyataan petani (manusia) kurang jelas, ambigu, atau kurang spesifik (misalnya: tidak menyebutkan jenis hama, gejala tanaman, atau usia tanaman), JANGAN asal menebak.
            2. Bertanya untuk Klarifikasi: Anda diwajibkan untuk bertanya kembali kepada petani jika Anda kurang paham dari perkataan mereka. Bertanyalah dengan sopan untuk menggali informasi lebih dalam (contoh: "Maaf Pak/Bu, agar saya bisa memberikan saran yang tepat, bisa tolong jelaskan detailnya seperti apa?").
            3. Empati dan Ramah: Berikan jawaban yang ramah, gunakan sapaan khas daerah jika sesuai, dan tunjukkan empati.
            4. Jawaban Tepat dan Solutif: Jika informasi sudah jelas, berikan solusi praktis, langsung pada intinya, dan didasarkan pada pengetahuan pertanian yang valid.
            
            Jawab dalam format Markdown yang rapi.
        """.trimIndent()
    }


    

    val farmerProfile: StateFlow<FarmerProfile?> = repository.farmerProfile
        .catch { e ->
            Log.e("AspekAuth", "Gagal membaca profil petani dari basis data: ${e.message}")
            emit(null)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    val chatMessages: StateFlow<List<ChatMessage>> = repository.chatMessages
        .catch { e ->
            Log.e("AspekAuth", "Gagal membaca riwayat obrolan dari basis data: ${e.message}")
            emit(emptyList())
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _isThinkingEnabled = MutableStateFlow(false)
    val isThinkingEnabled: StateFlow<Boolean> = _isThinkingEnabled.asStateFlow()

    fun setThinkingEnabled(enabled: Boolean) {
        _isThinkingEnabled.value = enabled
    }

    val currentSessionId = MutableStateFlow("default_session")
    private val prefs = application.getSharedPreferences("asisten_petani_prefs", android.content.Context.MODE_PRIVATE)

    val gcpEnvironments = listOf(
        GcpEnvironment(
            key = "production_mobile",
            name = "Produksi Utama (Mobile Backend)",
            description = "Server utama skala penuh untuk melayani analisis gambar hara para petani.",
            url = "https://ais-pre-orbmbooub6hjfofqfik3f7-812982832201.asia-southeast1.run.app/"
        ),
        GcpEnvironment(
            key = "preview",
            name = "Uji Coba Pra-Rilis (Preview Backend)",
            description = "Lingkungan evaluasi fitur baru sebelum diluncurkan ke Play Store.",
            url = "https://ais-pre-orbmbooub6hjfofqfik3f7-812982832201.asia-southeast1.run.app/"
        ),
        GcpEnvironment(
            key = "shared",
            name = "Kolaborasi Publik (Shared Backend)",
            description = "Server demo bersama untuk pengujian integrasi antar-partner.",
            url = "https://ais-pre-orbmbooub6hjfofqfik3f7-812982832201.asia-southeast1.run.app/"
        ),
        GcpEnvironment(
            key = "development",
            name = "Pengembangan Aktif (Development Backend)",
            description = "Server lokal/debug untuk pengujian kode secara real-time oleh pengembang.",
            url = "https://ais-dev-orbmbooub6hjfofqfik3f7-812982832201.asia-southeast1.run.app/"
        ),
        GcpEnvironment(
            key = "cloudshell",
            name = "Uji Coba Cloud Shell (FastAPI)",
            description = "Server Cloud Shell untuk pengembangan Python FastAPI.",
            url = "https://8080-cs-537028d4-954c-4602-a4c0-ec9c4d2fe293.cs-asia-southeast1-palm.cloudshell.dev/"
        ),
        GcpEnvironment(
            key = "firebase_hosting",
            name = "Hosting & Landing Page (Firebase)",
            description = "Halaman web resmi & tautan informasi aplikasi.",
            url = "https://asistenpetani-ai-app.firebaseapp.com/"
        )
    )

    private val _selectedEnvironment = MutableStateFlow(
        gcpEnvironments.find { it.key == prefs.getString("selected_gcp_env_key", "production_mobile") }
            ?: gcpEnvironments.first()
    )
    val selectedEnvironment: StateFlow<GcpEnvironment> = _selectedEnvironment.asStateFlow()

    fun selectEnvironment(envKey: String) {
        val env = gcpEnvironments.find { it.key == envKey } ?: return
        _selectedEnvironment.value = env
        prefs.edit().putString("selected_gcp_env_key", envKey).apply()
        Log.d("AspekEnv", "Berhasil beralih ke lingkungan GCP: ${env.name} -> ${env.url}")
    }

    // Expose all available session IDs in the database dynamically
    val parentChatSessions: StateFlow<List<String>> = repository.chatMessages
        .map { messages ->
            val sessions = messages.map { it.sessionId }.distinct().reversed()
            if (!sessions.contains("default_session")) {
                listOf("default_session") + sessions
            } else {
                sessions
            }
        }
        .catch { emit(listOf("default_session")) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = listOf("default_session")
        )

    val chatMessagesFiltered: StateFlow<List<ChatMessage>> = combine(
        repository.chatMessages,
        currentSessionId
    ) { messages, sessionId ->
        val filtered = messages.filter { it.sessionId == sessionId }
        if (filtered.isEmpty()) {
            val profile = repository.getProfileSync()
            val name = profile?.name ?: "Indra"
            val crop = profile?.cropType ?: "Kakao"
            val location = profile?.location ?: "KOLAKA, SULTRA"
            
            val greeting = when {
                location.contains("TIMUR", true) -> "Halo kk saya asisten petani cerdas kita orang."
                location.contains("SULAWESI", true) || location.contains("SULTRA", true) -> "Halo sodaraku tabe bagaimana kabar ta hari ini?"
                location.contains("JAWA", true) -> "Punten bapak/ibu apa yang bisa saya bantu?"
                location.contains("PADANG", true) || location.contains("MINANG", true) -> "Assalamualaikum amak/apak."
                location.contains("KALIMANTAN", true) -> "Assalamualaikum julak apa yang ulun bisa bantu?"
                else -> "Halo Bapak/Bu $name! Selamat datang di Asisten Petani Kita."
            }

            listOf(
                ChatMessage(
                    sender = "ai",
                    text = "$greeting Selamat datang di Asisten Petani Kita (ASPEK).\n\nSaya telah menganalisis lahan Anda seluas ${profile?.landArea ?: "1.0"} Hektar untuk tanaman $crop di $location.\n\nAda yang bisa asisten bantu kelola hari ini? Misal: saran pupuk subsidi, perkiraan cuaca lokal, atau panen.",
                    sessionId = sessionId
                )
            )
        } else {
            filtered
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    fun selectSession(sessionId: String) {
        currentSessionId.value = sessionId
        prefs.edit().putString("last_session_id", sessionId).apply()
    }

    fun startNewSession() {
        val newSessionId = "session_" + System.currentTimeMillis()
        selectSession(newSessionId)
    }

    fun deleteSession(sessionId: String) {
        viewModelScope.launch {
            repository.deleteChatMessageBySessionId(sessionId)
            if (currentSessionId.value == sessionId) {
                selectSession("default_session")
            }
        }
    }

    private val _isAILoading = MutableStateFlow(false)
    val isAILoading: StateFlow<Boolean> = _isAILoading.asStateFlow()

    private val _imageUploadStatus = MutableStateFlow<String>("IDLE")
    val imageUploadStatus: StateFlow<String> = _imageUploadStatus.asStateFlow()

    fun resetImageUploadStatus() {
        _imageUploadStatus.value = "IDLE"
    }

    // Status kuota harian petani (Mulai dengan 5 kuota gratis)
    private val _userQuota = MutableStateFlow(5)
    val userQuota: StateFlow<Int> = _userQuota.asStateFlow()

    private val _imageQuota = MutableStateFlow(1)
    val imageQuota: StateFlow<Int> = _imageQuota.asStateFlow()

    // Status Premium (Default: Free)
    private val _isPremium = MutableStateFlow(false)
    val isPremium: StateFlow<Boolean> = _isPremium.asStateFlow()

    private val _showInterstitialAdEvent = MutableStateFlow<Boolean>(false)
    val showInterstitialAdEvent: StateFlow<Boolean> = _showInterstitialAdEvent.asStateFlow()

    fun consumeInterstitialAdEvent() {
        _showInterstitialAdEvent.value = false
    }

    private fun handleSystemTriggers(text: String): String {
        var processedText = text
        
        if (processedText.contains("[ADMOB_TRIGGER: INTERSTITIAL]")) {
            _showInterstitialAdEvent.value = true
            processedText = processedText.replace("[ADMOB_TRIGGER: INTERSTITIAL]", "")
        }
        
        val regex = Regex("""\[PANEN_TRIGGER:\s*(\d+)\s*:\s*(.*?)\]""")
        val matchResult = regex.find(processedText)
        if (matchResult != null) {
            val daysStr = matchResult.groupValues[1]
            val taskName = matchResult.groupValues[2]
            try {
                val days = daysStr.toLong()
                com.asistenpetani.service.notification.HarvestScheduler(getApplication()).scheduleHarvestNotification(taskName, days)
                Log.i("AspekScheduler", "Berhasil menjadwalkan $taskName dalam $days hari dari respon AI.")
            } catch (e: Exception) {
                Log.e("AspekScheduler", "Gagal mem-parsing trigger panen: ${e.message}")
            }
            processedText = regex.replace(processedText, "")
        }
        
        return processedText.trim()
    }

    fun useQuota(): Boolean {
        if (_isPremium.value) return true
        return if (_userQuota.value > 0) {
            _userQuota.value -= 1
            true
        } else {
            false
        }
    }

    fun useImageQuota(): Boolean {
        if (_isPremium.value) return true
        return if (_imageQuota.value > 0) {
            _imageQuota.value -= 1
            true
        } else {
            false
        }
    }

    fun rewardUserWithQuota() {
        _userQuota.value = 100 // Kuota teks terbatas 100 kali
        _imageQuota.value = 2  // Analisa gambar terbatas 2 kali
    }

    fun togglePremiumStatus() {
        _isPremium.value = !_isPremium.value
    }

    fun setPremiumStatus(value: Boolean) {
        _isPremium.value = value
    }

    private val _isLocationServiceGranted = MutableStateFlow(false)
    val isLocationServiceGranted = _isLocationServiceGranted.asStateFlow()

    // Real weather/lahan stats
    val tempState = MutableStateFlow("30.5°C")
    val humidityState = MutableStateFlow("22%")

    private val _weatherAlert = MutableStateFlow<com.asistenpetani.domain.model.WeatherAlert?>(null)
    val weatherAlert = _weatherAlert.asStateFlow()

    private val _isLoadingWeather = MutableStateFlow(false)
    val isLoadingWeather = _isLoadingWeather.asStateFlow()

    @android.annotation.SuppressLint("MissingPermission")
    fun fetchLocalWeather() {
        viewModelScope.launch {
            _isLoadingWeather.value = true
            try {
                // 1. Get GPS relative coordinate
                val context = getApplication<Application>()
                val hasGps = com.asistenpetani.util.PermissionManager.isPermissionGranted(context, android.Manifest.permission.ACCESS_FINE_LOCATION) || 
                             com.asistenpetani.util.PermissionManager.isPermissionGranted(context, android.Manifest.permission.ACCESS_COARSE_LOCATION)
                
                var lat = -4.013    // Default Kolaka, Sultra
                var lng = 121.605   // Default Kolaka, Sultra
                
                if (hasGps) {
                    val attrContext = context
                    val locationManager = attrContext.getSystemService(android.content.Context.LOCATION_SERVICE) as? android.location.LocationManager
                    if (locationManager != null) {
                        val providers = locationManager.getProviders(true)
                        var bestLocation: android.location.Location? = null
                        for (provider in providers) {
                            val loc = locationManager.getLastKnownLocation(provider) ?: continue
                            if (bestLocation == null || loc.accuracy < bestLocation.accuracy) {
                                bestLocation = loc
                            }
                        }
                        if (bestLocation != null) {
                            lat = bestLocation.latitude
                            lng = bestLocation.longitude
                        }
                    }
                }
                
                // 2. Fetch using our secure backend (Cloud Run)
                val baseUrl = selectedEnvironment.value.url
                val cropType = "Kakao/Padi" // default atau bisa diambil dari profile
                val request = com.asistenpetani.data.remote.WeatherAnalysisRequest(
                    latitude = lat,
                    longitude = lng,
                    cropType = cropType
                )
                
                val response = com.asistenpetani.data.remote.CloudRunRetrofitClient.getService(baseUrl).getWeatherAdvice(request)
                
                tempState.value = "${response.temperature}°C"
                humidityState.value = "${response.humidity}%"
                
                // Generate alerts based on weather_code and backend advice
                val titleText: String
                val warning: Boolean
                val icon: String
                
                when (response.weatherCode) {
                    0, 1, 2, 3 -> {
                        titleText = "Cuaca Terang & Berawan"
                        warning = false
                        icon = "WbSunny"
                    }
                    45, 48 -> {
                        titleText = "Peringatan Kabut"
                        warning = true
                        icon = "FilterDrawer"
                    }
                    51, 53, 55, 61, 63, 65, 80, 81, 82 -> {
                        titleText = "Hujan Terpantau"
                        warning = true
                        icon = "WaterDrop"
                    }
                    95, 96, 99 -> {
                        titleText = "Waspada Badai"
                        warning = true
                        icon = "Thunderstorm"
                    }
                    else -> {
                        titleText = "Cuaca Saat Ini"
                        warning = false
                        icon = "Cloud"
                    }
                }
                
                _weatherAlert.value = com.asistenpetani.domain.model.WeatherAlert(
                    title = titleText,
                    description = response.advice, // Menggunakan nasihat dari Gemini di backend!
                    isWarning = warning,
                    iconName = icon,
                    temp = "${response.temperature}°C",
                    humidity = "${response.humidity}%",
                    advice = response.advice
                )
                
                // Trigger notification
                showWeatherNotification(titleText, "Cuaca lokal saat ini. Suhu: ${response.temperature}°C. " + response.advice)
                
            } catch (e: retrofit2.HttpException) {
                Log.w("AspekWeather", "Endpoint backend cuaca belum tersedia (HTTP ${e.code()}), beralih ke data cuaca lokal.")
                _weatherAlert.value = com.asistenpetani.domain.model.WeatherAlert(
                    title = "Cuaca Cerah (Lokal)",
                    description = "Cuaca sangat ideal untuk pemupukan urea hari ini. Kelembapan pas dan tidak ada indikasi hujan. (Lokal)",
                    isWarning = false,
                    iconName = "WbSunny",
                    temp = "28.0°C",
                    humidity = "65.0%",
                    advice = "Cuaca sangat ideal untuk pemupukan urea hari ini. Kelembapan pas dan tidak ada indikasi hujan."
                )
            } catch (e: Exception) {
                Log.w("AspekWeather", "Gagal memuat perkiraan cuaca dari backend: ${e.message}, beralih ke data cuaca lokal.")
                _weatherAlert.value = com.asistenpetani.domain.model.WeatherAlert(
                    title = "Cuaca Cerah (Lokal)",
                    description = "Cuaca sangat ideal untuk pemupukan urea hari ini. Kelembapan pas dan tidak ada indikasi hujan. (Lokal)",
                    isWarning = false,
                    iconName = "WbSunny",
                    temp = "28.0°C",
                    humidity = "65.0%",
                    advice = "Cuaca sangat ideal untuk pemupukan urea hari ini. Kelembapan pas dan tidak ada indikasi hujan."
                )
            } finally {
                _isLoadingWeather.value = false
            }
        }
    }

    private fun showWeatherNotification(title: String, text: String) {
        val context = getApplication<Application>()
        val attrContext = context
        val notificationManager = attrContext.getSystemService(android.content.Context.NOTIFICATION_SERVICE) as? android.app.NotificationManager
        if (notificationManager != null) {
            val channelId = "weather_notifications"
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                val channel = android.app.NotificationChannel(
                    channelId,
                    "Pendamping Cuaca Tani ASPEK",
                    android.app.NotificationManager.IMPORTANCE_HIGH
                )
                notificationManager.createNotificationChannel(channel)
            }
            val notification = androidx.core.app.NotificationCompat.Builder(attrContext, channelId)
                .setContentTitle(title)
                .setContentText(text)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setPriority(androidx.core.app.NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .setStyle(androidx.core.app.NotificationCompat.BigTextStyle().bigText(text))
                .build()
            notificationManager.notify(42, notification)
        }
    }

    init {
        val lastSessionId = prefs.getString("last_session_id", "default_session") ?: "default_session"
        currentSessionId.value = lastSessionId
        
        // Panggil fetch cuaca di awal
        fetchLocalWeather()

        // Prepopulate introductory AI welcome message exactly once if the database is empty
        viewModelScope.launch {
            try {
                val list = repository.chatMessages.first()
                if (list.isEmpty()) {
                    val profile = repository.getProfileSync()
                    val nama = profile?.name ?: "Indra"
                    val crop = profile?.cropType ?: "Kakao"
                    repository.saveMessage(
                        sender = "ai",
                        text = "Halo $nama! Selamat datang di Asisten Petani Kita. Lokasi Anda di Kolaka terdeteksi subur hari ini.\n\nSaran saya: Lakukan pengairan berkala untuk tanaman $crop Anda guna menjaga stabilitas kelembapan tanah.",
                        sessionId = "default_session"
                    )
                }
            } catch (e: Exception) {
                Log.e("AspekAuth", "Kesalahan pada inisialisasi basis data / pesan pembuka: ${e.message}")
            }
        }
    }

    fun setLocationGranted(granted: Boolean) {
        _isLocationServiceGranted.value = granted
        if (granted) {
            fetchLocalWeather()
        }
    }

    private fun detectBackgroundLocation(): String {
        val context = getApplication<Application>()
        
        val hasGps = com.asistenpetani.util.PermissionManager.hasAllEssentialPermissions(context)
        
        // 1. Check from GPS/LocationManager if granted & available
        if (hasGps) {
            try {
                val attrContext = context
                val locationManager = attrContext.getSystemService(android.content.Context.LOCATION_SERVICE) as? android.location.LocationManager
                if (locationManager != null) {
                    val providers = locationManager.getProviders(true)
                    var bestLocation: android.location.Location? = null
                    for (provider in providers) {
                        val loc = locationManager.getLastKnownLocation(provider) ?: continue
                        if (bestLocation == null || loc.accuracy < bestLocation.accuracy) {
                            bestLocation = loc
                        }
                    }
                    if (bestLocation != null) {
                        val lat = bestLocation.latitude
                        val lng = bestLocation.longitude
                        return when {
                            lat in -10.0..-2.0 && lng in 118.0..126.0 -> "SULAWESI (Sultra/Sulsel)"
                            lat in -9.0..-5.0 && lng in 105.0..115.0 -> "JAWA (Sunda/Jawa Tengah/Jawa Timur)"
                            lat in -6.0..2.0 && lng in 95.0..105.0 -> "PADANG (Sumatra Barat)"
                            lat in -5.0..4.0 && lng in 108.0..119.0 -> "KALIMANTAN (Banjar)"
                            lat in -9.0..2.0 && lng in 127.0..142.0 -> "TIMUR (Papua/Maluku)"
                            else -> "SULAWESI (Sultra/Sulsel)"
                        }
                    }
                }
            } catch (e: SecurityException) {
                Log.e("AspekLoc", "Izin lokasi ditolak saat sinkronisasi: ${e.message}")
            } catch (e: Exception) {
                Log.e("AspekLoc", "Gagal sinkronisasi data GPS: ${e.message}")
            }
        }

        // 2. Fallback to TimeZone if GPS not ready
        val tzId = java.util.TimeZone.getDefault().id.uppercase()
        return when {
            tzId.contains("MAKASSAR") -> "SULAWESI (Sultra/Sulsel)"
            tzId.contains("JAYAPURA") -> "TIMUR (Papua/Maluku)"
            tzId.contains("PONTIANAK") -> "KALIMANTAN (Banjar)"
            tzId.contains("JAKARTA") -> {
                val isSumatra = java.util.Locale.getDefault().country.contains("ID") && 
                                (java.util.Locale.getDefault().displayName.contains("Padang", true) || 
                                 java.util.Locale.getDefault().displayName.contains("Minang", true))
                if (isSumatra) "PADANG (Sumatra Barat)" else "JAWA (Sunda/Jawa Tengah/Jawa Timur)"
            }
            else -> "SULAWESI (Sultra/Sulsel)"
        }
    }

    fun getDeviceRecognitionInfo(): com.asistenpetani.domain.model.DeviceInfo {
        return customAuthRepo.getDeviceRecognitionInfo()
    }

    fun loginAndSync(name: String, cropType: String, landArea: String, lahanId: String) {
        viewModelScope.launch {
            val location = detectBackgroundLocation()
            // Use domain use case directly to store profile with realistic Land ID and location
            val finalLahanId = if (lahanId.isBlank()) "LHN-KOLAKA-001" else lahanId
            loginFarmerUseCase(name, cropType, landArea, finalLahanId, location)
            // Once saved, append a personalized greeting message as the start of the chat
            repository.clearChat()
            selectSession("default_session")

            val greeting = when {
                location.contains("TIMUR", true) -> "Halo kk saya asisten petani cerdas kita orang."
                location.contains("SULAWESI", true) || location.contains("SULTRA", true) -> "Halo sodaraku tabe bagaimana kabar ta hari ini?"
                location.contains("JAWA", true) -> "Punten bapak/ibu apa yang bisa saya bantu?"
                location.contains("PADANG", true) || location.contains("MINANG", true) -> "Assalamualaikum amak/apak."
                location.contains("KALIMANTAN", true) -> "Assalamualaikum julak apa yang ulun bisa bantu?"
                else -> "Halo Bapak/Bu $name! Selamat datang di Asisten Petani Kita."
            }

            repository.saveMessage(
                sender = "ai",
                text = "$greeting Selamat datang di Asisten Petani Kita (ASPEK).\n\nSaya telah menganalisis lahan Anda seluas $landArea Hektar untuk tanaman $cropType di $location.\n\nAda yang bisa asisten bantu kelola hari ini? Misal: saran pupuk subsidi, perkiraan cuaca lokal, atau panen.",
                sessionId = "default_session"
            )
        }
    }

    fun queryAgriculturalAdvice(query: String) {
        if (query.isBlank()) return
        viewModelScope.launch {
            _adviceState.value = com.asistenpetani.util.UiState.Loading
            repository.saveMessage(sender = "user", text = query)
            _isAILoading.value = true
            try {
                val result = aspekRepository.getAgriculturalData(query)
                val cleanResponse = handleSystemTriggers(result.aiResponse)
                _adviceState.value = com.asistenpetani.util.UiState.Success(result.copy(aiResponse = cleanResponse))
                repository.saveMessage(sender = "ai", text = cleanResponse)
            } catch (e: Exception) {
                _adviceState.value = com.asistenpetani.util.UiState.Error(e.localizedMessage ?: "Gagal terhubung.")
                repository.saveMessage(sender = "ai", text = "Tabe' pak, asisten sedang mengalami kesulitan koneksi eksternal.")
            } finally {
                _isAILoading.value = false
            }
        }
    }

    fun sendMessage(text: String) {
        if (text.isBlank()) return
        if (!useQuota()) return
        viewModelScope.launch {
            // 1. Save user text locally
            repository.saveMessage(sender = "user", text = text, sessionId = currentSessionId.value)
            _isAILoading.value = true

            try {
                // 2. Fetch response from Gemini via Master Brain Use Case orchestrator!
                val profile = repository.getProfileSync()
                val location = profile?.location ?: "KOLAKA, SULTRA"
                
                val weatherContext = "\\n\\n[INFO SISTEM UNTUK ASISTEN]: Cuaca petani saat ini: Suhu ${tempState.value}, Kelembapan ${humidityState.value}. Peringatan: ${weatherAlert.value?.title ?: "Aman"}. "
                val enrichedText = text + weatherContext
                
                
                var aiResponse: String
                try {
                    val chatHistory = repository.getChatMessagesSync().filter { it.sessionId == currentSessionId.value }
                    aiResponse = customGenerativeAiRepo.generateResponse(
                        systemPrompt = getSystemPrompt(location, profile), 
                        userInput = enrichedText, 
                        imageBase64 = null, 
                        mimeType = null, 
                        thinkingLevel = if (_isPremium.value) com.asistenpetani.data.remote.ThinkingLevel.HIGH else null,
                        historyList = chatHistory
                    )
                } catch (e: Exception) {
                    aiResponse = "Gagal menghubungi asisten: ${e.message}"
                }

                // 3. Save AI response locally
                
                val cleanResponse = handleSystemTriggers(aiResponse)
                if (cleanResponse == "QUOTA_EXCEEDED") {
                    repository.saveMessage(sender = "ai", text = "Batas percakapan harian Anda (15/15) telah tercapai. Tingkatkan ke ASPEK Premium untuk akses tanpa batas demi kesuksesan panen Anda!", sessionId = currentSessionId.value)
                } else {
                    repository.saveMessage(sender = "ai", text = cleanResponse, sessionId = currentSessionId.value)
                }

            } catch (e: Exception) {
                Log.e("AspekChat", "Koneksi Bermasalah saat kirim teks: ${e.message}")
                repository.saveMessage(
                    sender = "ai",
                    text = "Mohon maaf pak, koneksi internet asisten sedang terganggu. Silakan periksa jaringan internet Anda di Kolaka dan coba kirim ulang pesan ini.",
                    sessionId = currentSessionId.value
                )
            } finally {
                _isAILoading.value = false
            }
        }
    }

    fun sendMessageWithImage(text: String, bitmap: android.graphics.Bitmap) {
        if (!useImageQuota()) return
        viewModelScope.launch(Dispatchers.IO) {
            _imageUploadStatus.value = "LOADING"
            _isAILoading.value = true
            
            try {
                val profile = repository.getProfileSync()
                val location = profile?.location ?: "KOLAKA, SULTRA"
                val weatherContext = "\n\n[INFO SISTEM UNTUK ASISTEN]: Cuaca petani saat ini: Suhu ${tempState.value}, Kelembapan ${humidityState.value}. Peringatan: ${weatherAlert.value?.title ?: "Aman"}. "
                val enrichedText = text + weatherContext
                
                // === ALUR BARU: KOMPRESI DAN MULTIPART ===
                // 1. Kompres gambar di bawah 200 KB untuk hemat kuota dan RAM
                val compressedFile = com.asistenpetani.util.ImageCompressor.kompresGambarKeBawah200KB(bitmap)
                val requestFile = compressedFile.asRequestBody("image/jpeg".toMediaTypeOrNull())
                val body = okhttp3.MultipartBody.Part.createFormData("file", compressedFile.name, requestFile)
                
                // 2. Siapkan prompt sistem dan teks
                val systemPrompt = getSystemPrompt(location, profile)
                val finalPrompt = "$systemPrompt\n\n$enrichedText"
                val pesanBody = finalPrompt.toRequestBody("text/plain".toMediaTypeOrNull())
                
                // 3. Panggil endpoint analisa-gambar-aman di FastAPI
                val baseUrl = selectedEnvironment.value.url
                val apiResponse = com.asistenpetani.data.remote.CloudRunRetrofitClient.getService(baseUrl).analisaGambarAman(pesanBody, body)
                
                val aiResponseText = if (apiResponse.isSuccessful) {
                    apiResponse.body()?.jawaban ?: "Maaf, tidak ada balasan dari sistem AI."
                } else {
                    "Maaf, server mengalami gangguan (Kode: ${apiResponse.code()})"
                }
                
                // 4. Bersihkan file temporary
                if (compressedFile.exists()) {
                    compressedFile.delete()
                }
                
                // ===========================================
                
                repository.saveMessage(sender = "user", text = "$text [Analisa Gambar]", sessionId = currentSessionId.value)
                
                val cleanResponse = handleSystemTriggers(aiResponseText)
                if (cleanResponse == "QUOTA_EXCEEDED") {
                    repository.saveMessage(sender = "ai", text = "Batas percakapan harian Anda (15/15) telah tercapai. Tingkatkan ke ASPEK Premium untuk akses tanpa batas demi kesuksesan panen Anda!", sessionId = currentSessionId.value)
                } else {
                    repository.saveMessage(sender = "ai", text = cleanResponse, sessionId = currentSessionId.value)
                }
                
                _imageUploadStatus.value = "SUCCESS"
            } catch (e: Exception) {
                Log.e("AspekUpload", "Gagal: ${e.message}")
                _imageUploadStatus.value = "FAILED"
                repository.saveMessage(sender = "ai", text = "Maaf, gagal menganalisa gambar: ${e.message}", sessionId = currentSessionId.value)
            } finally {
                _isAILoading.value = false
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            repository.clearProfile()
        }
    }

    fun deleteMessageHistory() {
        viewModelScope.launch {
            repository.clearChat()
        }
    }

    fun deleteChatMessageByText(text: String) {
        viewModelScope.launch {
            repository.deleteChatMessageByText(text)
        }
    }
}
