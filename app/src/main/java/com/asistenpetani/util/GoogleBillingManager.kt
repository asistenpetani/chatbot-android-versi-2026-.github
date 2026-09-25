package com.asistenpetani.util

import android.app.Activity
import android.content.Context
import android.util.Log
import com.android.billingclient.api.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class GoogleBillingManager(private val context: Context) {
    private val billingClient: BillingClient
    
    // ID Produk yang nanti Anda daftarkan di Google Play Console (Harga Rp 25.000)
    private val productId = "premium_monthly_25k"

    // Callback untuk memberi tahu ViewModel apakah sukses atau gagal
    var onPurchaseSuccess: (() -> Unit)? = null
    var onPurchaseError: ((String) -> Unit)? = null

    // 1. Mendengarkan Hasil Pembayaran dari Pop-up Google
    private val purchasesUpdatedListener = PurchasesUpdatedListener { billingResult, purchases ->
        if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && purchases != null) {
            for (purchase in purchases) {
                handlePurchase(purchase)
            }
        } else if (billingResult.responseCode == BillingClient.BillingResponseCode.USER_CANCELED) {
            onPurchaseError?.invoke("Pembayaran dibatalkan oleh pengguna.")
        } else {
            onPurchaseError?.invoke("Terjadi kesalahan sistem: ${billingResult.debugMessage}")
        }
    }

    init {
        // Inisialisasi koneksi ke aplikasi Google Play Store di HP
        billingClient = BillingClient.newBuilder(context)
            .setListener(purchasesUpdatedListener)
            .enablePendingPurchases()
            .build()
        
        connectToGooglePlay()
    }

    private fun connectToGooglePlay() {
        billingClient.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(billingResult: BillingResult) {
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    Log.d("Billing", "Berhasil terhubung ke Google Play")
                }
            }
            override fun onBillingServiceDisconnected() {
                // Coba hubungkan ulang jika terputus (Mencegah aplikasi crash)
                connectToGooglePlay()
            }
        })
    }

    // 2. Fungsi Utama untuk Membuka Pop-up Pembayaran (Setara window.snap.pay)
    fun launchBillingFlow(activity: Activity) {
        val queryProductDetailsParams = QueryProductDetailsParams.newBuilder()
            .setProductList(
                listOf(
                    QueryProductDetailsParams.Product.newBuilder()
                        .setProductId(productId)
                        .setProductType(BillingClient.ProductType.SUBS) // SUBS = Langganan bulanan
                        .build()
                )
            ).build()

        billingClient.queryProductDetailsAsync(queryProductDetailsParams) { billingResult, productDetailsList ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && !productDetailsList.isNullOrEmpty()) {
                val productDetails = productDetailsList[0]
                
                // Menyiapkan paket berlangganan
                val offerToken = productDetails.subscriptionOfferDetails?.get(0)?.offerToken ?: ""
                val productDetailsParamsList = listOf(
                    BillingFlowParams.ProductDetailsParams.newBuilder()
                        .setProductDetails(productDetails)
                        .setOfferToken(offerToken)
                        .build()
                )

                val billingFlowParams = BillingFlowParams.newBuilder()
                    .setProductDetailsParamsList(productDetailsParamsList)
                    .build()

                // Meluncurkan Pop-Up Pembayaran
                billingClient.launchBillingFlow(activity, billingFlowParams)
            } else {
                onPurchaseError?.invoke("Produk tidak ditemukan. Pastikan internet stabil.")
            }
        }
    }

    // 3. Wajib: Mengkonfirmasi Pembayaran agar Google tidak me-refund uangnya
    private fun handlePurchase(purchase: Purchase) {
        if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
            if (!purchase.isAcknowledged) {
                val acknowledgePurchaseParams = AcknowledgePurchaseParams.newBuilder()
                    .setPurchaseToken(purchase.purchaseToken)
                    .build()
                billingClient.acknowledgePurchase(acknowledgePurchaseParams) { billingResult ->
                    if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                        // PEMBAYARAN SUKSES! Beritahu ViewModel
                        CoroutineScope(Dispatchers.Main).launch {
                            onPurchaseSuccess?.invoke()
                        }
                    }
                }
            } else {
                CoroutineScope(Dispatchers.Main).launch { onPurchaseSuccess?.invoke() }
            }
        }
    }
}
