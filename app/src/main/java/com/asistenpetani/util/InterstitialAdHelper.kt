package com.asistenpetani.util

import android.app.Activity
import android.content.Context
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback

object InterstitialAdHelper {
    private var mInterstitialAd: InterstitialAd? = null

    /**
     * Memuat iklan video di latar belakang agar siap ditampilkan kapan saja.
     */
    fun loadAd(context: Context) {
        val adRequest = AdRequest.Builder().build()
        // PENTING: Untuk "video tayangan penuh durasi 10 detik lalu boleh diskip",
        // pengaturan tersebut harus dilakukan melalui Dasbor AdMob pada pengaturan Ad Unit Interstitial (Pilih Video, setel skip delay).
        // Ini adalah ID Uji Coba Interstitial dari Google.
        // Ganti dengan ID AdMob asli Anda H-1 sebelum rilis Play Store.
        val testAdUnitId = com.asistenpetani.BuildConfig.ADMOB_INTERSTITIAL_ID

        InterstitialAd.load(context, testAdUnitId, adRequest, object : InterstitialAdLoadCallback() {
            override fun onAdFailedToLoad(adError: LoadAdError) {
                mInterstitialAd = null
            }

            override fun onAdLoaded(interstitialAd: InterstitialAd) {
                mInterstitialAd = interstitialAd
                
                mInterstitialAd?.onPaidEventListener = com.google.android.gms.ads.OnPaidEventListener { adValue ->
                    val nilaiUangMicros = adValue.valueMicros
                    val mataUang = adValue.currencyCode
                    catatPendapatanKeFirebase(context, nilaiUangMicros, mataUang)
                }
            }
        })
    }

    private fun catatPendapatanKeFirebase(context: Context, valueMicros: Long, currencyCode: String) {
        val firebaseAnalytics = com.google.firebase.analytics.FirebaseAnalytics.getInstance(context)
        val nilaiAsli = valueMicros / 1000000.0

        val bundle = android.os.Bundle().apply {
            putString(com.google.firebase.analytics.FirebaseAnalytics.Param.CURRENCY, currencyCode)
            putDouble(com.google.firebase.analytics.FirebaseAnalytics.Param.VALUE, nilaiAsli)
            putString(com.google.firebase.analytics.FirebaseAnalytics.Param.AD_FORMAT, "interstitial")
            putString(com.google.firebase.analytics.FirebaseAnalytics.Param.AD_SOURCE, "AdMob")
        }

        firebaseAnalytics.logEvent(com.google.firebase.analytics.FirebaseAnalytics.Event.AD_IMPRESSION, bundle)
        android.util.Log.d("AdRevenue", "Pendapatan dicatat: ${nilaiAsli} ${currencyCode}")
    }

    /**
     * Dapatkan SharedPreferences khusus untuk menyimpan hitungan iklan.
     */
    private fun getSharedPrefs(context: Context): android.content.SharedPreferences {
        return context.getSharedPreferences("aspek_ads_prefs", Context.MODE_PRIVATE)
    }

    private fun getTodayDateString(): String {
        val sdf = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
        return sdf.format(java.util.Date())
    }

    /**
     * Mengecek apakah iklan masih boleh ditampilkan hari ini (maksimal 3 kali).
     */
    fun canShowAdToday(context: Context): Boolean {
        val prefs = getSharedPrefs(context)
        val today = getTodayDateString()
        val lastDate = prefs.getString("last_ad_date", "")
        
        if (lastDate != today) {
            // Reset harian otomatis
            prefs.edit().putString("last_ad_date", today).putInt("ad_count_today", 0).apply()
            return true
        }
        
        val count = prefs.getInt("ad_count_today", 0)
        return count < 3
    }

    /**
     * Menambah hitungan penayangan iklan hari ini.
     */
    fun incrementAdCount(context: Context) {
        val prefs = getSharedPrefs(context)
        val today = getTodayDateString()
        val lastDate = prefs.getString("last_ad_date", "")
        
        val currentCount = if (lastDate == today) prefs.getInt("ad_count_today", 0) else 0
        prefs.edit()
            .putString("last_ad_date", today)
            .putInt("ad_count_today", currentCount + 1)
            .apply()
            
        android.util.Log.d("AspekAdLimit", "Iklan ditampilkan. Total penayangan hari ini: ${currentCount + 1}/3")
    }

    /**
     * Menampilkan iklan layar penuh jika sudah selesai dimuat.
     * @param onAdDismissed fungsi yang dijalankan SETELAH iklan ditutup (di-skip)
     */
    fun showAd(activity: Activity, isPremium: Boolean = false, onAdDismissed: () -> Unit) {
        if (isPremium) {
            onAdDismissed()
            return
        }

        // Batasi penayangan iklan maksimal 3 kali sehari agar tidak mengganggu kenyamanan petani
        if (!canShowAdToday(activity)) {
            android.util.Log.d("AspekAdLimit", "Batas tayangan iklan harian (3 kali) tercapai. Melewati iklan demi kenyamanan petani.")
            onAdDismissed()
            return
        }

        if (mInterstitialAd != null) {
            mInterstitialAd?.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    // Iklan ditutup oleh pengguna, jalankan aksi selanjutnya, lalu muat iklan baru
                    mInterstitialAd = null
                    incrementAdCount(activity)
                    onAdDismissed()
                    if (!isPremium) {
                        loadAd(activity) 
                    }
                }

                override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                    mInterstitialAd = null
                    onAdDismissed()
                }
            }
            mInterstitialAd?.show(activity)
        } else {
            // Jika iklan belum siap, langsung jalankan aksi tanpa hambatan
            onAdDismissed()
        }
    }
}
