package com.asistenpetani.service.notification

import android.content.Context
import androidx.work.Data
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

class HarvestScheduler(private val context: Context) {

    /**
     * Fungsi untuk mendaftarkan jadwal notifikasi panen ke sistem Android
     * @param namaTanaman Nama tanaman (contoh: Padi, Jagung)
     * @param delayHari Jumlah hari tunggu sampai panen tiba
     */
    fun scheduleHarvestNotification(namaTanaman: String, delayHari: Long) {
        
        // 1. Bungkus data pelengkap yang ingin dikirim ke Worker
        val inputData = Data.Builder()
            .putString("NAMA_TANAMAN", namaTanaman)
            .build()

        // 2. Buat permintaan kerja satu kali (OneTimeWorkRequest) dengan DELAY waktu
        val harvestWorkRequest = OneTimeWorkRequestBuilder<HarvestNotificationWorker>()
            .setInputData(inputData)
            .setInitialDelay(delayHari, TimeUnit.DAYS) // Set delay berdasarkan hari dari AI
            .build()

        // 3. Daftarkan ke sistem WorkManager Android
        WorkManager.getInstance(context).enqueue(harvestWorkRequest)
    }
}
