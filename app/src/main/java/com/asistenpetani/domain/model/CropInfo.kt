package com.asistenpetani.domain.model

/**
 * Model Data untuk referensi budidaya dan nilai jual.
 */
data class CropInfo(
    val nama: String,
    val syaratTumbuh: String, // Ketinggian, Suhu, Curah Hujan
    val tanahPh: String,
    val pemupukan: String,
    val tipsTambahan: String,
    // Tambahan baru untuk menjaga nilai jual hasil panen:
    val standarKualitasPanen: String, // Misal: "Kadar air kakao < 7%"
    val estimasiHargaPasar: String    // Misal: "Harga premium jika biji kering rata"
) {
    companion object {
        /**
         * List of standardized concise expert crop database records for 10 key crops,
         * satisfying the requirements of quick dosage calculations and minimal token footprint.
         */
        val standardCrops = listOf(
            CropInfo(
                nama = "Gandum (Triticum aestivum L.)",
                syaratTumbuh = "Suhu 20-25°C, Curah Hujan 350-1250mm, Ketinggian >1000m dpl atau dataran rendah kering.",
                tanahPh = "Netral sampai agak lembap, berdrainase baik.",
                pemupukan = "Urea 150kg/ha, SP-36 100kg/ha, KCl 75kg/ha.",
                tipsTambahan = "Optimal untuk lahan kering, membutuhkan kontrol pemompaan air pada fase awal perkecambahan.",
                standarKualitasPanen = "Kadar air biji < 13%, bebas jamur, warna kuning jerami seragam.",
                estimasiHargaPasar = "Rp 11.500 - Rp 13.000 per Kg (Stabil tinggi)"
            ),
            CropInfo(
                nama = "Sagu (Metroxylon sagu)",
                syaratTumbuh = "Curah Hujan 2500-3500mm, Suka lahan rawa / gambut basah.",
                tanahPh = "pH 3.5 - 6.5 (Agak masam).",
                pemupukan = "Tinggi Kalium (K) untuk pati & kekuatan akar serta Kalsium (Ca) untuk pembentukan dinding sel.",
                tipsTambahan = "Dapat beradaptasi dengan sangat baik pada area tanah rawa bereaksi masam tinggi.",
                standarKualitasPanen = "Kekeringan tepung sagu tinggi, kadar air tepung < 14%, warna putih bersih alami.",
                estimasiHargaPasar = "Rp 9.000 - Rp 11.000 per Kg olahan kasar"
            ),
            CropInfo(
                nama = "Porang (Amorphophallus muelleri)",
                syaratTumbuh = "Ketinggian 100-600m dpl, Suhu 22-35°C (Jika >35°C rawan terbakar daunnya).",
                tanahPh = "pH 6.0 - 7.0 (Netral), kondisi tanah gembur dengan kandungan bahan organik tinggi.",
                pemupukan = "Pupuk organik 6 bulan sekali + Urea 100kg/ha + NPK 200kg/ha.",
                tipsTambahan = "Ideal ditanam di bawah naungan pohon penunjang sekitar 30%-40% intensitas sinar matahari.",
                standarKualitasPanen = "Keripik (chip) kering tipis, kadar glukomanan > 65%, kadar air cip < 12%.",
                estimasiHargaPasar = "Rp 55.000 - Rp 75.000 per Kg kering ekspor"
            ),
            CropInfo(
                nama = "Sorgum (Sorghum bicolor L.)",
                syaratTumbuh = "Tahan kekeringan ekstrim, Curah Hujan ideal 50-100mm/bulan (terutama pada 2 bulan pertama).",
                tanahPh = "pH 6.0 - 7.5 (Tidak cocok untuk tanah Ultisol atau tanah yang sangat masam).",
                pemupukan = "Wajib pupuk organik 500kg/ha tiap selesai panen. Jika tanah masam: Kapur pertanian (diamkan 2-3 hari) -> Pupuk Organik (diamkan 7-10 hari) -> Urea 100kg/ha + NPK 250kg/ha.",
                tipsTambahan = "Bisa diratun (panen berulang kali tanpa menanam bibit baru dari awal).",
                standarKualitasPanen = "Butir sorgum kupas bersih bulat seragam, kadar air < 12.5%.",
                estimasiHargaPasar = "Rp 6.500 - Rp 8.000 per Kg (Industri bahan pangan)"
            ),
            CropInfo(
                nama = "Talas (Colocasia esculenta L.)",
                syaratTumbuh = "Ketinggian 250-1100m dpl, Suhu 21-27°C, Curah Hujan rata-rata 1750mm.",
                tanahPh = "pH 5.5 - 6.5 (Jika pH < 5.0, wajib diberi kapur dolomit 1 ton/ha).",
                pemupukan = "Pupuk kompos 2-3 ton/ha sebagai pupuk dasar + Urea 150kg/ha pada umur 4 minggu.",
                tipsTambahan = "Memerlukan penyinaran matahari penuh minimal 11 jam per hari agar umbi berkembang maksimal.",
                standarKualitasPanen = "Umbi utuh tanpa luka garpu, serat lembut, bebas pembusukan pangkalan.",
                estimasiHargaPasar = "Rp 12.000 - Rp 15.000 per Kg ubi segar"
            ),
            CropInfo(
                nama = "Kacang Hijau (Phaseolus radiatus)",
                syaratTumbuh = "Ketinggian optimal 500m dpl, Suhu udara hangat hingga panas hangat.",
                tanahPh = "pH 5.8 (Jika tanah terlampau asam pH < 5.0, membutuhkan kapur dolomit).",
                pemupukan = "NPK 100kg/ha + pupuk hayati pengikat nitrogen.",
                tipsTambahan = "Sangat peka terhadap genangan air, drainase parit di sekeliling bedengan harus prima.",
                standarKualitasPanen = "Kulit kacang hijau tidak keriput, biji tumpul mengkilap, kadar air < 13%.",
                estimasiHargaPasar = "Rp 16.000 - Rp 18.500 per Kg"
            ),
            CropInfo(
                nama = "Kacang Tunggak (Vigna unguiculata L.Walp)",
                syaratTumbuh = "Ketinggian optimal <500m dpl, Curah Hujan rendah <1500mm.",
                tanahPh = "Adaptasi sangat luas pada berbagai jenis tanah marginal.",
                pemupukan = "NPK dosis rendah (50kg/ha) karena tanaman mampu mengikat nitrogen udara dengan simbiosis bintil akar.",
                tipsTambahan = "Sangat serbaguna, baik untuk pakan ternak maupun pemulihan hara nitrogen tanah pasca padi.",
                standarKualitasPanen = "Biji kering tua sempurna, warna merah hati cerah/putih bintik hitam, tidak berlubang.",
                estimasiHargaPasar = "Rp 10.000 - Rp 12.000 per Kg kering"
            ),
            CropInfo(
                nama = "Kacang Tanah (Arachis hypogaea)",
                syaratTumbuh = "Suhu optimal 25-35°C, Membutuhkan paparan sinar matahari penuh (wajib).",
                tanahPh = "Suhu tanah ideal perkecambahan >18°C dengan tekstur gembur berpasir agar ginofor mudah masuk tanah.",
                pemupukan = "Fosfor (SP-36) 150kg/ha, KCl 100kg/ha, dan Kalsium untuk mencegah polong kosong.",
                tipsTambahan = "Lakukan penyiangan dan pembubunan tanah secara hati-hati saat pembentukan ginofor.",
                standarKualitasPanen = "Polong keras berserat jelas, isi penuh 2-3 biji, terhindar dari racun aflatoksin.",
                estimasiHargaPasar = "Rp 14.500 - Rp 16.000 per Kg polong basah"
            ),
            CropInfo(
                nama = "Gadung (Dioscorea hispida)",
                syaratTumbuh = "Ketinggian 845-1500m dpl, Curah Hujan 760-1050mm, Sinar matahari >10 jam/hari.",
                tanahPh = "Tanah liat berpasir kaya humus dengan aerasi yang sangat lancar.",
                pemupukan = "Pupuk kandang matang 1.5 - 2 ton/ha ditambah KCl 50kg/ha untuk kualitas umbi.",
                tipsTambahan = "Umbi segar aman dikonsumsi asalkan diproses khusus dengan melarutkan kadar senyawa racun dioscorin dalam air mengalir / abu.",
                standarKualitasPanen = "Chips gadung kering steril, bebas aroma/rasa pahit racun, warna putih gading murni.",
                estimasiHargaPasar = "Rp 25.000 - Rp 30.000 per Kg keripik siap saji"
            ),
            CropInfo(
                nama = "Ubi Jalar (Ipomoea batatas L.)",
                syaratTumbuh = "Tumbuh subur di dataran rendah hingga tinggi, memerlukan suhu hangat cerah.",
                tanahPh = "pH 5.5 - 6.5, gembur berpasir.",
                pemupukan = "Pupuk organik dasar + NPK 200kg/ha dosis terbagi.",
                tipsTambahan = "Merupakan komoditas unggul berproduktivitas tinggi (>30 ton/ha). Disarankan memakai varietas unggul nasional seperti Daya, Prambanan, Borobudur, Mendut, atau Kalasan.",
                standarKualitasPanen = "Umbi berkulit mulus bebas dari lubang penggerek Boleng, ukuran 150-300g per umbi.",
                estimasiHargaPasar = "Rp 8.000 - Rp 10.000 per Kg segar"
            )
        )

        /**
         * Returns a beautifully structured context string representing the entire
         * ringkas crop knowledge, designed to be injected into the Gemini System Prompt context.
         */
        fun getFormattedCropKnowledgeBase(): String {
            return """
DATA KOMODITAS (Format Ringkas)
🌾 GANDUM (Triticum aestivum L.)
Syarat: Suhu 20-25°C, Hujan 350-1250mm, Tinggi >1000m dpl atau dataran rendah kering.
Tanah: -
Catatan: Optimal untuk lahan kering.

🌴 SAGU (Metroxylon sagu)
Syarat: pH 3.5-6.5, Hujan 2500-3500mm, Suka lahan rawa/gambut.
Tanah: Agak masam.
Pemupukan: Tinggi Kalium (untuk pati & akar) & Kalsium (dinding sel).

🥔 PORANG (Amorphophallus muelleri)
Syarat: Tinggi 100-600m dpl, Suhu 22-35°C (Jika >35°C rawan terbakar).
Tanah: pH 6-7 (Netral), gembur, organik tinggi.
Pemupukan: Organik (6 bln sekali) + Urea 100kg/ha + NPK 200kg/ha.

🌽 SORGUM (Sorghum bicolor L.)
Syarat: Tahan kekeringan, Hujan 50-100mm/bln (2 bln pertama).
Tanah: pH 6.0-7.5 (Tidak cocok tanah Ultisol/Masam).
Pemupukan: Wajib organik 500kg/ha tiap panen. Jika tanah masam: Kapur (diamkan 2-3 hari) -> Organik (diamkan 7-10 hari) -> Urea 100kg/ha + NPK 250kg/ha.

🍠 TALAS (Colocasia esculenta L.)
Syarat: Tinggi 250-1100m dpl, Suhu 21-27°C, Hujan 1750mm.
Tanah: pH 5.5-6.5 (Jika <5, beri kapur 1 ton/ha).
Catatan: Butuh matahari penuh (minimal 11 jam/hari).

🫘 KACANG HIJAU (Phaseolus radiatus)
Syarat: Tinggi 500m dpl, Suhu panas.
Tanah: pH 5.8 (Jika <5, butuh kapur).

🫘 KACANG TUNGGAK (Vigna unguiculata L.Walp)
Syarat: Tinggi optimal 500m dpl, Hujan <1500mm.
Tanah: Adaptasi luas.

🥜 KACANG TANAH (Arachis hypogaea)
Syarat: Suhu 25-35°C, Matahari penuh (wajib).
Tanah: Suhu tanah penentu perkecambahan (ideal >18°C).

🍠 GADUNG (Dioscorea hispida)
Syarat: Tinggi 845-1500m dpl, Hujan 760-1050mm, Sinar matahari >10 jam/hari.
Catatan: Mengandung racun (diperlukan pengolahan khusus).

🍠 UBI JALAR (Ipomoea batatas L.)
Catatan: Komoditas unggulan dengan produktivitas >30 ton/ha. Varietas unggul: Daya, Prambanan, Borobudur, Mendut, Kalasan.
            """.trimIndent()
        }
    }
}
