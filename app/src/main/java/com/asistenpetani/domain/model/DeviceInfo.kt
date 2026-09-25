package com.asistenpetani.domain.model

/**
 * Model data sederhana untuk mendeteksi perangkat pengguna (Device Recognition)
 * guna menunjang keamanan, audit trail, serta optimasi beban komputasi AI
 * adaptif berdasar level API perangkat Android (misal API 24).
 */
data class DeviceInfo(
    val model: String,
    val manufacturer: String,
    val osVersion: String,
    val apiLevel: Int,
    val lastLogin: Long = System.currentTimeMillis()
)
