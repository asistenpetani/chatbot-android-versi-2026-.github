package com.asistenpetani.domain.util

object DialectHelper {
    /**
     * Menyisipkan salam sapaan lokal berdasarkan nama provinsi/lokasi.
     */
    fun getLocalGreeting(location: String): String {
        val loc = location.lowercase()
        return when {
            loc.contains("jawa barat") || loc.contains("sunda") -> "Sampurasun! Kumaha damang?"
            loc.contains("jawa tengah") || loc.contains("jawa timur") -> "Sugeng rawuh. Pripun kabare?"
            loc.contains("sulawesi") || loc.contains("makassar") || loc.contains("bugis") || loc.contains("kolaka") -> "Tabe', Apa kareba? Baji-baji ji?"
            loc.contains("kalimantan") -> "Salamat baisukan. Narai kabar? Tabe."
            loc.contains("sumatera utara") || loc.contains("batak") -> "Horas! Boha kabar?"
            loc.contains("sumatera barat") || loc.contains("minang") -> "Ba'a kaba? Sehat sajo?"
            loc.contains("bali") -> "Om Swastiastu. Punapi gatra?"
            else -> "Halo Bapak/Ibu Petani!"
        }
    }

    /**
     * Memformat respon AI jika gagal atau kuota habis berdasarkan lokasi.
     */
    fun getFallbackQuotaMessage(location: String): String {
        val loc = location.lowercase()
        return when {
            loc.contains("jawa barat") || loc.contains("sunda") -> "Punten, kuota asisten AI nuju limit."
            loc.contains("jawa") -> "Ngapunten, kuota asisten telas."
            loc.contains("sulawesi") || loc.contains("makassar") || loc.contains("bugis") || loc.contains("kolaka") -> "Tabe', maaf kuota habis."
            else -> "Maaf, kuota asisten habis."
        }
    }
}
