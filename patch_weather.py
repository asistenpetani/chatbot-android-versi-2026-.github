import re

with open('app/src/main/java/com/asistenpetani/ui/viewmodel/AppViewModel.kt', 'r') as f:
    content = f.read()

new_fetch_local_weather = """
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
                    iconRes = icon
                )
                
                // Trigger notification
                showWeatherNotification(titleText, "Cuaca lokal saat ini. Suhu: ${response.temperature}°C. " + response.advice)
                
            } catch (e: Exception) {
                Log.e("AspekWeather", "Gagal memuat perkiraan cuaca lokal dari backend: ${e.message}")
                _weatherAlert.value = com.asistenpetani.domain.model.WeatherAlert(
                    title = "Cuaca Tidak Diketahui",
                    description = "Gagal memuat cuaca dari sistem ASPEK (Cloud Run). Harap periksa koneksi internet.",
                    isWarning = true,
                    iconRes = "CloudOff"
                )
            } finally {
                _isLoadingWeather.value = false
            }
        }
    }
"""

# Now we need to replace the original fetchLocalWeather
# It starts at `fun fetchLocalWeather() {` and ends at the first `private fun showWeatherNotification(`
pattern = re.compile(r'fun fetchLocalWeather\(\)\s*\{.*?(?=private fun showWeatherNotification\()', re.DOTALL)
content = pattern.sub(new_fetch_local_weather.strip() + "\n\n    ", content)

with open('app/src/main/java/com/asistenpetani/ui/viewmodel/AppViewModel.kt', 'w') as f:
    f.write(content)
