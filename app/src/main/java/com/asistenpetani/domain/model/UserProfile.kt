package com.asistenpetani.domain.model

data class UserProfile(
    val uid: String = "",
    val name: String = "",
    val cropType: String = "",
    val landArea: String = "",
    val location: String = "KOLAKA, SULTRA",
    val tanamanPerikananBudidaya: String = "",
    val email: String = ""
)
