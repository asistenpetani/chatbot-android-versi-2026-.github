package com.asistenpetani.plugins

import android.content.Context
import android.telephony.TelephonyManager
import android.util.Log

/**
 * Helper Jaringan Native ASPEK.
 * 
 * Karena aplikasi ASPEK dibangun menggunakan Jetpack Compose Native (bukan Hybrid Webview Capacitor),
 * kita dapat langsung memanggil sensor informasi kartu SIM secara efisien lewat Kotlin JVM
 * tanpa memerlukan overhead jembatan (bridge) Capacitor.
 */
object NetworkOperatorPlugin {

    data class SimInfo(
        val mcc: String,
        val mnc: String,
        val operatorName: String
    )

    /**
     * Membaca informasi kartu SIM secara native untuk deteksi area dan kekuatan jaringan Sultra/Kolaka.
     */
    fun getSimInfo(context: Context): SimInfo? {
        return try {
            val attrContext = context
            val telephonyManager = attrContext.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
            val operatorCode = telephonyManager.networkOperator ?: telephonyManager.simOperator

            if (!operatorCode.isNullOrEmpty() && operatorCode.length >= 5) {
                val mcc = operatorCode.substring(0, 3)
                val mnc = operatorCode.substring(3)
                val operatorName = telephonyManager.networkOperatorName ?: "Sebut Operator"
                
                SimInfo(
                    mcc = mcc,
                    mnc = mnc,
                    operatorName = operatorName
                )
            } else {
                null
            }
        } catch (e: Exception) {
            Log.e("AspekNetwork", "Gagal membaca sensor jaringan SIM native: ${e.message}")
            null
        }
    }
}

/*
========================================================================================
LAMPIRAN REFERENSI: BILA ANDA INGIN MENGGUNAKAN CAPACITOR DI MASA DEPAN (HYBRID WEB)
========================================================================================
Jika proyek ini dimigrasikan ke hybrid Ionic/Capacitor, ganti kode di atas dengan file asli ini:

package com.asistenpetani.plugins

import android.Manifest
import android.content.Context
import android.telephony.TelephonyManager
import com.getcapacitor.JSObject
import com.getcapacitor.Plugin
import com.getcapacitor.PluginCall
import com.getcapacitor.PluginMethod
import com.getcapacitor.annotation.CapacitorPlugin
import com.getcapacitor.annotation.Permission

@CapacitorPlugin(
    name = "NetworkOperator",
    permissions = [
        Permission(
            alias = "phone_state",
            strings = [Manifest.permission.READ_PHONE_STATE]
        )
    ]
)
class NetworkOperatorPlugin : Plugin() {

    @PluginMethod
    fun getSimInfo(call: PluginCall) {
        if (getPermissionState("phone_state") != com.getcapacitor.PermissionState.GRANTED) {
            requestPermissionForAlias("phone_state", call, "permissionCallback")
            return
        }
        fetchSimData(call)
    }

    @PluginMethod
    fun permissionCallback(call: PluginCall) {
        if (getPermissionState("phone_state") == com.getcapacitor.PermissionState.GRANTED) {
            fetchSimData(call)
        } else {
            call.reject("Izin membaca status telepon ditolak. Fitur optimasi jaringan dimatikan.")
        }
    }

    private fun fetchSimData(call: PluginCall) {
        try {
            val attrContext = context
            val telephonyManager = attrContext.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
            val operatorCode = telephonyManager.networkOperator ?: telephonyManager.simOperator
            val resultData = JSObject()

            if (!operatorCode.isNullOrEmpty() && operatorCode.length >= 5) {
                val mcc = operatorCode.substring(0, 3)
                val mnc = operatorCode.substring(3)
                
                resultData.put("mcc", mcc)
                resultData.put("mnc", mnc)
                resultData.put("operatorName", telephonyManager.networkOperatorName)
                
                call.resolve(resultData)
            } else {
                call.reject("Kartu SIM tidak terdeteksi atau sedang di luar jangkauan sinyal.")
            }
        } catch (e: Exception) {
            call.reject("Gagal membaca sensor jaringan: ${e.message}")
        }
    }
}
*/
