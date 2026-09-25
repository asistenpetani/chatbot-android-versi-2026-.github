package com.asistenpetani.domain.model

data class PrinsipInti(
    val nomor: Int,
    val prinsip: String,
    val penjelasan: String
)

data class ProductKatalog(
    val kategori: String,
    val nama: String,
    val tipe: String,
    val komposisi_atau_bahanAktif: String,
    val indikasi_atau_target: String
)

data class PraktikBudidaya(
    val komoditas: String,
    val rekomendasiDosis: String,
    val notesTambahan: String = ""
)

object AgriculturalKnowledgeBase {

    val prinsipIntiList = listOf(
        PrinsipInti(1, "Kesehatan Tanah adalah Segalanya", "Fondasi dari semua saran adalah tanah yang hidup dan seimbang."),
        PrinsipInti(2, "Mulai dari Awal yang Benar", "Keberhasilan dimulai dari benih berkualitas dan persiapan lahan yang matang."),
        PrinsipInti(3, "Jadilah praktisi yang profesional", "Identifikasi masalah secara spesifik sebelum memberikan solusi. Tekankan dosis yang tepat."),
        PrinsipInti(4, "Berpikir Global, Berbicara Lokal", "Terjemahkan data teknis menjadi bahasa yang mudah dipahami dan relevan."),
        PrinsipInti(5, "Tanaman adalah makhluk hidup", "Benamkan pupuk di area perakaran aktif. Membawa manfaat untuk makhluk hidup lain."),
        PrinsipInti(6, "Fokus untuk Optimal, Diversifikasi untuk Stabilitas", "Kuasai satu komoditas dulu untuk pemula, lalu diversifikasi."),
        PrinsipInti(7, "Jadilah Pebisnis, Bukan Hanya Petani", "Selalu pikirkan biaya, keuntungan, dan ROI. Gunakan data.")
    )

    val produkList = listOf(
        ProductKatalog("PUPUK", "Urea (Subsidi & Non)", "Pupuk Tunggal Nitrogen", "N 46%", "Pertumbuhan vegetatif (daun)"),
        ProductKatalog("PUPUK", "NPK Phonska (Subsidi)", "Pupuk Majemuk", "N 15%, P₂O₅ 10%, K₂O 12%", "Tanaman pangan"),
        ProductKatalog("PUPUK", "NPK Khusus Kakao", "Pupuk Majemuk", "N 14%, P₂O₅ 12%, K₂O 16%, MgO 4%", "Kebutuhan nutrisi kakao"),
        ProductKatalog("PUPUK", "NPK Phonska Plus", "Pupuk Majemuk", "N 15%, P₂O₅ 15%, K₂O 15%, S 9%, Zinc", "Kualitas panen, diperkaya zinc"),
        ProductKatalog("PUPUK", "NPK Pelangi", "Pupuk Majemuk", "Formula khusus", "Tanaman perkebunan penghasil buah"),
        ProductKatalog("PUPUK", "SP-36 Petro", "Pupuk Tunggal Fosfor", "P₂O₅ 36%, S 5%", "Memacu pertumbuhan akar dan pembungaan"),
        ProductKatalog("PUPUK", "ZK Petro", "Pupuk Tunggal Kalium", "K₂O 50%, S 17%", "Meningkatkan kualitas hasil (hortikultura)"),
        ProductKatalog("PUPUK HAYATI", "Ecofert", "Pupuk Hayati Organik", "Mikrob fiksasi N", "Menghemat NPK hingga 25%"),
        ProductKatalog("PUPUK HAYATI", "Biotara", "Pupuk Hayati", "Mikrob Tricoderma, Bacillus", "Lahan rawa atau masam"),
        ProductKatalog("PESTISIDA", "Sidabas 500 EC", "Insektisida Kontak", "Klorpirifos", "Walang Sangit, Wereng, Ulat"),
        ProductKatalog("PESTISIDA", "Sidazeb 80 WP", "Fungisida", "Mankozeb", "Penyakit jamur (Busuk daun, dll)"),
        ProductKatalog("PESTISIDA", "Sidafos 480 SL", "Herbisida Sistemik", "IPA Glifosat", "Gulma umum (bunuh sampai akar)"),
        ProductKatalog("PESTISIDA", "Sidamec 20 EC", "Insektisida", "Abamektin", "Kutu daun, Trips, Pengorok daun"),
        ProductKatalog("PESTISIDA", "Fipros 55 SC", "Insektisida", "Fipronil", "Wereng Coklat, Rayap, Penggerek Batang"),
        ProductKatalog("PESTISIDA", "Sidafur 3 GR", "Insektisida Butiran", "Karbofuran", "Nematoda, Penggerek batang"),
        ProductKatalog("PESTISIDA", "Fenosida 255 EC", "Fungisida Sistemik", "Difenokonazol", "Hawar, Bercak Coklat")
    )

    val budidayaList = listOf(
        PraktikBudidaya("PADI", "Urea 225kg/ha, Phonska 300kg/ha, Petroganik 500kg/ha", "Bisa ningkat hasil 17%"),
        PraktikBudidaya("JAGUNG", "Urea 250kg/ha, Phonska 350kg/ha, Petroganik 1000kg/ha", "Pemupukan pertama 7-10 HST (Urea)"),
        PraktikBudidaya("TEBU", "ZA 300kg, Petrocane/Pelangi 300-500kg, Petroganik 500-1000kg", "Awas penggerek batang pucuk"),
        PraktikBudidaya("CABAI", "Phonska 400kg/ha, Petroganik 1000kg/ha", "Bisa ningkat hasil +4 ton/ha"),
        PraktikBudidaya("KAKAO", "Phonska Khusus 625kg/ha, Petroganik 1000kg, Urea 25kg/ha", "Diformulasikan untuk batang dan buah")
    )

    fun getFormattedKnowledgeBase(): String {
        val prinsip = prinsipIntiList.joinToString("\n") { "${it.nomor}. ${it.prinsip}: ${it.penjelasan}" }
        val produk = produkList.joinToString("\n") { "- [${it.kategori}] ${it.nama}: ${it.indikasi_atau_target} (${it.komposisi_atau_bahanAktif})" }
        val budi = budidayaList.joinToString("\n") { "- ${it.komoditas}: Dosis ${it.rekomendasiDosis}. ${it.notesTambahan}" }

        return """
        # Prinsip Inti Asisten Petani (Aspek)
        $prinsip

        # Katalog Produk Tersedia
        $produk

        # Panduan Dosis Dasar
        $budi
        
        # Info Harga Subsidi 2025:
        - Urea: Rp 90.000/zak
        - NPK Phonska: Rp 92.000/zak
        - Petroganik: Rp 25.600/zak (40kg)

        - Pastikan petani punya nama di e-RDKK untuk mencairkan kuota di pengecer.
        """.trimIndent()
    }
}
