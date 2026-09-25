package com.asistenpetani.data.local.room

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "farmer_profile")
data class FarmerProfile(
    @PrimaryKey val uid: String = "default_user",
    val name: String,
    val cropType: String,
    val landArea: String,
    val isLoggedIn: Boolean = false,
    val location: String = "KOLAKA, SULTRA",
    val lahanId: String = "LHN-KOLAKA-001"
)

@Entity(tableName = "chat_messages")
data class ChatMessage(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val sender: String, // "user" or "ai"
    val text: String,
    val timestamp: Long = System.currentTimeMillis(),
    val sessionId: String = "default_session",
    val category: String = "Umum" // e.g. "Umum", "Pupuk", "Cuaca", "Hama & Penyakit", "Budidaya"
)

@Entity(tableName = "planting_schedules")
data class PlantingSchedule(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val cropName: String,
    val locationName: String,
    val plantingDate: Long,
    val estimatedHarvestDate: Long,
    val currentStage: String, // e.g. "Penyemaian", "Fase Vegetatif", "Fase Generatif / Perawatan", "Masa Panen"
    val notifyMe: Boolean = true,
    val notes: String = ""
)
