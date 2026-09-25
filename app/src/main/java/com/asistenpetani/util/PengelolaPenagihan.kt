package com.asistenpetani.util

import android.content.Context
import android.util.Log
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.PurchasesUpdatedListener

// Kelas ini bertugas sebagai jembatan teknis antara aplikasi Asisten Petani dan Google Play
class PengelolaPenagihan(context: Context) {

    // 1. Membuat Listener: Bertugas mendengarkan jika ada transaksi yang berhasil atau gagal
    private val pendengarTransaksi = PurchasesUpdatedListener { billingResult, purchases ->
        if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && purchases != null) {
            // Logika ketika petani berhasil membayar langganan
            Log.d("Penagihan", "Transaksi berhasil!")
        } else {
            // Logika ketika transaksi batal atau error
            Log.e("Penagihan", "Transaksi gagal: ${billingResult.debugMessage}")
        }
    }

    // 2. Membangun Klien Penagihan: Mempersiapkan mesin penagihan Google Play
    private val klienPenagihan = BillingClient.newBuilder(context)
        .setListener(pendengarTransaksi)
        .enablePendingPurchases() // WAJIB diaktifkan untuk mendukung pembayaran tertunda (misal: bayar di minimarket)
        .build()

    // 3. Fungsi untuk memulai koneksi ke Google Play saat aplikasi dibuka
    fun mulaiKoneksi() {
        klienPenagihan.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(billingResult: BillingResult) {
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    // Sistem siap! Aplikasi sudah terhubung dengan server Google Play
                    Log.d("Penagihan", "Google Play Billing siap digunakan.")
                }
            }

            override fun onBillingServiceDisconnected() {
                // Logika jika koneksi terputus (misalnya sinyal hilang), panggil ulang mulaiKoneksi()
                Log.w("Penagihan", "Koneksi Google Play terputus.")
            }
        })
    }
}
