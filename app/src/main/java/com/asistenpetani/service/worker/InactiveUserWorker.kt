package com.asistenpetani.service.worker

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.asistenpetani.data.remote.CloudRunRetrofitClient
import com.asistenpetani.data.remote.WeatherAnalysisRequest

class InactiveUserWorker(
    private val appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        Log.d("InactiveUserWorker", "Mengecek aktivitas pengguna...")
        val prefs = appContext.getSharedPreferences("aspek_ads_prefs", Context.MODE_PRIVATE)
        val lastOpenTime = prefs.getLong("last_app_open_time", 0L)
        val currentTime = System.currentTimeMillis()

        // 24 hours = 24 * 60 * 60 * 1000 = 86400000 ms
        if (currentTime - lastOpenTime > 24 * 60 * 60 * 1000) {
            Log.d("InactiveUserWorker", "Pengguna tidak aktif > 24 jam. Mengambil rekomendasi cuaca...")
            
            // Lokasi default Kolaka, atau dari profil. Untuk worker, kita pakai default/lokasi terakhir yang tersimpan.
            val lat = -4.0487
            val lng = 121.6080
            val cropType = "Kakao/Padi" // default

            try {
                // Gunakan URL default (atau yang tersimpan di pref jika ada).
                val baseUrl = "https://asistenpetani-ai-app-backend-service-956692315956.asia-southeast1.run.app/"
                val request = WeatherAnalysisRequest(
                    latitude = lat,
                    longitude = lng,
                    cropType = cropType
                )
                
                val response = CloudRunRetrofitClient.getService(baseUrl).getWeatherAdvice(request)
                
                val title = "Rekomendasi Cuaca Tani"
                val text = "Suhu: ${response.temperature}°C, Kelembapan: ${response.humidity}%. ${response.advice}"
                
                showWeatherNotification(title, text)
                
            } catch (e: Exception) {
                Log.w("InactiveUserWorker", "Informasi cuaca backend belum tersedia (${e.message}), menggunakan data fallback lokal.")
            }
        } else {
            Log.d("InactiveUserWorker", "Pengguna masih aktif baru-baru ini. Tidak mengirim notifikasi.")
        }
        
        return Result.success()
    }

    private fun showWeatherNotification(title: String, text: String) {
        val notificationManager = appContext.getSystemService(Context.NOTIFICATION_SERVICE) as? android.app.NotificationManager
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
            val notification = androidx.core.app.NotificationCompat.Builder(appContext, channelId)
                .setContentTitle(title)
                .setContentText(text)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setPriority(androidx.core.app.NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .setStyle(androidx.core.app.NotificationCompat.BigTextStyle().bigText(text))
                .build()

            notificationManager.notify(1001, notification)
        }
    }
}
