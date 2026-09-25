package com.asistenpetani.domain.util

fun getTimeBasedGreeting(): String {
    val hour = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY)
    return when (hour) {
        in 0..10 -> "Pagi"
        in 11..14 -> "Siang"
        in 15..18 -> "Sore"
        else -> "Malam"
    }
}
