package com.asistenpetani.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.asistenpetani.service.sync.DatabaseSyncScheduler

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            Log.i("BootReceiver", "Perangkat selesai booting. Memulai ulang layanan Asisten Petani 24/7 (AI & Database Sync)...")
            
            // 1. Mulai ulang jadwal sinkronisasi database & analisis AI secara berkala 24/7
            DatabaseSyncScheduler.schedulePeriodicSync(context)
            
            // Catatan: WorkManager secara otomatis akan memulihkan task OneTimeWorkRequest (seperti notifikasi panen), 
            // sehingga kita hanya perlu menjadwalkan ulang periodic worker jika diperlukan.
        }
    }
}
