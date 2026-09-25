package com.asistenpetani.domain.model

/**
 * Data class representing concise management info for different soil/land types in Indonesia,
 * providing the required structure for farmer reference charts, in-app land selection calculators,
 * and AI prompt injection.
 */
data class LandManagement(
    val tipeLahan: String,
    val tantangan: List<String>,
    val pengelolaan: List<String>
) {
    companion object {
        val listLahan = listOf(
            LandManagement(
                tipeLahan = "Lahan Kering",
                tantangan = listOf(
                    "Bahan organik menurun 30-60% dalam 10 tahun pemupukan kimia murni tanpa penyeimbang organik.",
                    "Daya dukung penahan air rendah sehingga rentan cekaman kekeringan."
                ),
                pengelolaan = listOf(
                    "Ameliorasi tanah dengan menambahkan bahan organik seperti pupuk kandang matang dan kompos minimal setahun sekali.",
                    "Penanaman tanaman penutup tanah (cover crops) dan memilih varietas komoditas toleran/tahan kekeringan.",
                    "Pembangunan embung tani (on-farm reservoir) yang dipadukan dengan efisiensi irigasi teknologi budidaya."
                )
            ),
            LandManagement(
                tipeLahan = "Lahan Basah",
                tantangan = listOf(
                    "Genangan air terlalu tinggi atau rendah tidak menentu.",
                    "Tingginya potensi serangan hama dan patogen penyakit tular air.",
                    "Salinisasi sekunder pada wilayah pasang surut dekat pesisir."
                ),
                pengelolaan = listOf(
                    "Pengelolaan air dan lahan dengan memadukan pemanfaatan bahan-bahan mekanis organik.",
                    "Pengaplikasian bahan amelioran organik maupun anorganik penyetabil reaktif tanah."
                )
            ),
            LandManagement(
                tipeLahan = "Lahan Rawa",
                tantangan = listOf(
                    "Pengendalian pasang surut air masih rendah.",
                    "Hanya bisa ditanami jenis tanaman tertentu yang toleran genangan periodik."
                ),
                pengelolaan = listOf(
                    "Pemanfaatan pedoman kalender tanam secara tertib.",
                    "Optimalisasi pemanfaatan lahan melalui sistem tata tanam Surjan (bedengan jajar).",
                    "Pengaturan tata mikro jaringan saluran air.",
                    "Penggunaan varietas unggul spesifik lahan rawa berdaya adaptasi tinggi.",
                    "Ameliorasi terarah dan pemupukan berkadar kalium-fosfor tinggi."
                )
            ),
            LandManagement(
                tipeLahan = "Lahan Gambut",
                tantangan = listOf(
                    "Emisi GRK (Gas Rumah Kaca) yang sangat besar saat terbuka.",
                    "Tanah mengalami subsiden (penyusutan volume) ekstrem serta bahaya kebakaran saat kekeringan."
                ),
                pengelolaan = listOf(
                    "Restorasi gambut terdegradasi secara dinamis.",
                    "Pemulihan fungsi hidrologis ekosistem gambut (rewetting saluran primer)."
                )
            ),
            LandManagement(
                tipeLahan = "Lahan Sulfat Masam",
                tantangan = listOf(
                    "Oksidasi mineral pirit (FeS2) menyebabkan pH tanah turun drastis hingga di bawah 3.5.",
                    "Menyebabkan efek keracunan hebat besi (Fe) dan aluminium (Al) pada akar tanaman."
                ),
                pengelolaan = listOf(
                    "Penambahan berbagai jenis kapur pertanian (dolomit, batu kapur giling, fosfat alam, kaptan).",
                    "Aplikasi tanah kelat mineral, lumpur sungai melimpah, pupuk kompos matang, pupuk kandang, dan abu sisa pembakaran."
                )
            ),
            LandManagement(
                tipeLahan = "Lahan Salin",
                tantangan = listOf(
                    "Stress osmotik tinggi pada tanaman menghambat penyerapan hara dan air.",
                    "Keracunan langsung ion Na+ dan Cl-.",
                    "Drainase buruk dengan aerasi tanah yang sangat rendah."
                ),
                pengelolaan = listOf(
                    "Ameliorasi menggunakan kapur pertanian aktif, pupuk kandang masif, dan bahan pembenah organik lainnya.",
                    "Penggunaan bahan pembenah tanah (soil conditioner) seperti mulsa organik selektif, gipsum, SP-50, fosfat, dan abu sekam padi.",
                    "Penggunaan varietas tanaman unggul yang terbukti toleran kondisi salin garam.",
                    "Pengaturan ketat jadwal waktu tanam menjauhi puncak musim kemarau kering."
                )
            ),
            LandManagement(
                tipeLahan = "Lahan Aluvial",
                tantangan = listOf(
                    "Sangat rentan terhadap resiko terendam luapan banjir sungai.",
                    "Bahaya tingkat erosi tinggi saat debit aliran air sungai membesar."
                ),
                pengelolaan = listOf(
                    "Tata kelola tanggul air makro dan penataan zonasi pematang lahan.",
                    "Aplikasi teknologi ameliorasi fisik tanah dan sistem pemupukan berkelanjutan.",
                    "Penggunaan varietas adaptif genangan genetik.",
                    "Teknologi pengendalian terpadu hama dan penyakit tumpangan air."
                )
            ),
            LandManagement(
                tipeLahan = "Lahan Vulkanik",
                tantangan = listOf(
                    "Potensi lereng yang curam mengakibatkan resiko erosi / longsor tanah yang sangat tinggi.",
                    "Tinggi retensi hara P (Fosfat terikat senyawa Al/Fe/allofane) sehingga kebutuhan pupuk P menjadi sangat tinggi."
                ),
                pengelolaan = listOf(
                    "Rehabilitasi lahan secara vegetatif terpadu dengan penanaman tanaman legum sebagai penutup tanah pelindung.",
                    "Penambahan bahan pembenah pengikat tanah seperti zeolit kapur penurun retensi P.",
                    "Optimalisasi pelarutan pupuk fosfat menggunakan kombinasi pupuk hayati (mikoriba)."
                )
            ),
            LandManagement(
                tipeLahan = "Lahan Terjal/Berbukit",
                tantangan = listOf(
                    "Erosi aliran permukaan (run-off) yang sangat ekstrim saat terguyur hujan deras.",
                    "Sangat sulit diolah secara mekanis.",
                    "Penyiraman air atau retensi kelembapan tanah tidak merata."
                ),
                pengelolaan = listOf(
                    "Pembuatan sabuk terasering (sengkedan) miring berpatokan garis kontur.",
                    "Penggunaan mulsa plastik atau jerami penutup area permukaan.",
                    "Penanaman vegetasi penutup tanah (rumput vetiver penggenggam lereng)."
                )
            )
        )

        /**
         * Converts the entire Land Management knowledge database to a compact summarized format
         * specifically formatted to keep tokens small but with 100% precision.
         */
        fun getFormattedLandKnowledgeBase(): String {
            return listLahan.joinToString("\n\n") { lahan ->
                """
                🔷 ${lahan.tipeLahan}
                Tantangan:
                ${lahan.tantangan.joinToString("\n") { "• $it" }}
                Pengelolaan:
                ${lahan.pengelolaan.joinToString("\n") { "• $it" }}
                """.trimIndent()
            }
        }
    }
}
