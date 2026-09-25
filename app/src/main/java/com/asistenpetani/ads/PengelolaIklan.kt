package com.asistenpetani.ads

import android.content.Context
import android.util.Log
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback

class PengelolaIklan(private val context: Context) {
    var mInterstitialAd: InterstitialAd? = null

    fun muatIklanVideo() {
        val adRequest = AdRequest.Builder().build()
        
        // Diambil dari environment variable (BuildConfig) sehingga aman
        val adUnitId = com.asistenpetani.BuildConfig.ADMOB_INTERSTITIAL_ID
        
        InterstitialAd.load(context, adUnitId, adRequest,
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(ad: InterstitialAd) {
                    mInterstitialAd = ad
                    Log.d("AdMob", "Iklan berhasil dimuat dan siap ditayangkan")
                }

                override fun onAdFailedToLoad(adError: LoadAdError) {
                    mInterstitialAd = null
                    Log.d("AdMob", "Iklan gagal dimuat: ${adError.message}")
                }
            })
    }
}
