package com.asistenpetani.service.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.Worker
import androidx.work.WorkerParameters

class HarvestNotificationWorker(
    context: Context,
    workerParams: WorkerParameters
) : Worker(context, workerParams) {

    override fun doWork(): Result {
        // 1. Ambil data nama tanaman yang dikirim saat penjadwalan
        val namaTanaman = inputData.getString("NAMA_TANAMAN") ?: "Tanaman Anda"

        // 2. Tampilkan notifikasi ke sistem Android
        showNotification(namaTanaman)

        return Result.success()
    }

    private fun showNotification(namaTanaman: String) {
        val attrContext = applicationContext
        val notificationManager = attrContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "harvest_notifications"

        // Android 8.0 ke atas wajib menggunakan Notification Channel
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Notifikasi Panen ASPEK",
                NotificationManager.IMPORTANCE_HIGH
            )
            notificationManager.createNotificationChannel(channel)
        }

        // Membangun isi dan tampilan notifikasi
        val notification = NotificationCompat.Builder(attrContext, channelId)
            .setContentTitle("Waktu Panen Tiba! 🌾")
            .setContentText("Halo sodaraku, berdasarkan analisa AI, hari ini adalah waktu terbaik untuk memanen $namaTanaman Anda.")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        // Munculkan notifikasi dengan ID unik (misal 1)
        notificationManager.notify(1, notification)
    }
}
