package com.asistenpetani.service.sync

import android.content.Context
import android.util.Log
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

object DatabaseSyncScheduler {

    /**
     * Menjadwalkan backup secara berkala (setiap 24 jam) saat terhubung ke internet.
     */
    fun schedulePeriodicSync(context: Context) {
        Log.i("DatabaseSyncScheduler", "Menjadwalkan sinkronisasi berkala database Room...")
        
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val syncRequest = PeriodicWorkRequestBuilder<DatabaseSyncWorker>(24, TimeUnit.HOURS)
            .setConstraints(constraints)
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            "database_sync_work",
            ExistingPeriodicWorkPolicy.KEEP,
            syncRequest
        )
    }

    /**
     * Menjadwalkan sinkronisasi satu kali secara instan saat aplikasi pertama kali dijalankan.
     */
    fun scheduleImmediateSync(context: Context) {
        Log.i("DatabaseSyncScheduler", "Menjadwalkan sinkronisasi instan database Room...")

        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val syncRequest = OneTimeWorkRequestBuilder<DatabaseSyncWorker>()
            .setConstraints(constraints)
            .build()

        WorkManager.getInstance(context).enqueueUniqueWork(
            "immediate_database_sync",
            androidx.work.ExistingWorkPolicy.REPLACE,
            syncRequest
        )
    }
}
