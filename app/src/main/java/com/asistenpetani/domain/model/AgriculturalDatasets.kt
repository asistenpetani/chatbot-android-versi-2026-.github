package com.asistenpetani.domain.model

data class CropBudidaya(
    val komoditas: String,
    val syaratTumbuh: String,
    val caraTanam: String,
    val pemeliharaan: String,
    val panen: String
)

data class PerikananBudidaya(
    val jenis: String,
    val persiapanKolam: String,
    val penebaranBenih: String,
    val manajemenPakan: String,
    val pascaPanen: String
)

data class RegulasiPupuk(
    val nomorAturan: String,
    val tentang: String,
    val poinPenting: List<String>,
    val rincianHet: Map<String, String>
)

data class DiagnosaTanaman(
    val gejala: String,
    val kemungkinanPenyebab: String,
    val penanganan: String
)

object AgriculturalDatasets {

    val cropBudidayaList = listOf(
        CropBudidaya(
            komoditas = "Kakao (Cokelat) Sultra",
            syaratTumbuh = "Dataran rendah hingga sedang, curah hujan 1100-3000 mm/tahun, pH 6.0-7.5.",
            caraTanam = "Jarak tanam ideal 3x3 m, gunakan pohon pelindung (Gamal/Sengon) sejak dini.",
            pemeliharaan = "Pemangkasan tunas air (wiwilan) rutin, pemupukan NPK Kakao berkala.",
            panen = "Petik buah yang berwarna kuning/oranye, belah buah hati-hati tanpa merusak biji."
        ),
        CropBudidaya(
            komoditas = "Padi Sawah Sultra",
            syaratTumbuh = "Tumbuh baik di dataran rendah beriklim tropis, air melimpah, tanah lempung liat.",
            caraTanam = "Sistem Jajar Legowo 2:1 atau 4:1 guna menaikkan produksi gabah per hektar.",
            pemeliharaan = "Pengairan berselang (intermittent), pemupukan dasar Urea & NPK.",
            panen = "Potong malai ketika 90% butir padi telah kuning cerah keemasan."
        ),
        CropBudidaya(
            komoditas = "Jagung Hibrida",
            syaratTumbuh = "Sinar matahari langsung penuh, tanah porous/drainase baik, pH lurus 5.6-7.5.",
            caraTanam = "Tanam sedalam 3-5 cm dengan jarak optimal 75x20 cm (satu benih per lubang).",
            pemeliharaan = "Pembumbunan tanah penopang akar pada 15 HST, herbisida selektif prabentang.",
            panen = "Klobot jagung mengering cokelat penuh, biji mengeras dengan titik hitam di pangkal."
        )
    )

    val perikananBudidayaList = listOf(
        PerikananBudidaya(
            jenis = "Udang Vaname Sultra",
            persiapanKolam = "Pengeringan menyeluruh, pengangkatan lumpur hitam, netralisasi tanah dasar dengan Kapur Dolomit.",
            penebaranBenih = "Aklimatisasi wadah benur 15-30 menit supaya terhindar dari stres salinitas mendadak.",
            manajemenPakan = "Beri pakan protein tinggi (30-40%) bertahap 3-4 kali sehari sesuai estimasi biomasa.",
            pascaPanen = "Panen cepat malam/subuh menggunakan jala tarik, langsung celup wadah air es kristal."
        ),
        PerikananBudidaya(
            jenis = "Ikan Nila Super",
            persiapanKolam = "Pengolahan dasar tanah, fermentasi pupuk kandang guna menumbuhkan fitoplankton pakan alami dasar.",
            penebaranBenih = "Tebar benih ukuran 3-5 cm kepadatan 15-20 ekor/m² saat suhu air sejuk pagi/sore hari.",
            manajemenPakan = "Pelet apung protein 28-30% f-rate berkelanjutan 3-5% dari bobot total ikan harian.",
            pascaPanen = "Sortir ukuran sesuai kemauan pasar, angkut hidup dengan air aerator beroksigen konstan."
        )
    )

    val regulasiPupukList = listOf(
        RegulasiPupuk(
            nomorAturan = "Permentan No. 01 Tahun 2024",
            tentang = "Perbaikan Regulasi Subsidi Pupuk Nasional menggantikan Permentan Nomor 10 Tahun 2022.",
            poinPenting = listOf(
                "Fokus alokasi subsidi hanya ditujukan bagi 9 komoditas pokok nasional (Padi, Jagung, Kedelai, Cabai, Bawang Merah, Bawang Putih, Tebu Rakyat, Kakao, & Kopi).",
                "Hak kuota diberikan eksklusif kepada petani terdaftar simluhtan dengan maksimal penguasaan lahan 2 Hektar.",
                "Penebusan resmi dipermudah menggunakan KTP asli langsung ke kios penyalur melalui Aplikasi i-Pubers."
            ),
            rincianHet = mapOf(
                "Urea Subsidi" to "Rp 2.250 / kg (Rp 112.500 / sak 50kg)",
                "NPK Phonska" to "Rp 2.300 / kg (Rp 115.000 / sak 50kg)",
                "NPK Khusus Kakao" to "Rp 3.300 / kg (Rp 165.000 / sak 50kg)",
                "Petroganik (Organik)" to "Rp 800 / kg (Rp 32.000 / sak 40kg)"
            )
        )
    )

    val diagnosaTanamanList = listOf(
        DiagnosaTanaman(
            gejala = "Daun bawah menguning membentuk huruf V dari ujung ke tengah, batang kurus.",
            kemungkinanPenyebab = "Defisiensi Unsur Nitrogen (N).",
            penanganan = "Segera beri pupuk Urea atau semprot pupuk cair daun berkandungan N tinggi."
        ),
        DiagnosaTanaman(
            gejala = "Ujung daun terbakar cokelat hitam, melengkung ke atas, bunga buah gampang rontok.",
            kemungkinanPenyebab = "Defisiensi Unsur Kalium (K).",
            penanganan = "Aplikasi segera pupuk ZK, KCL, atau abu janjang sawit di sekeliling piringan tanaman."
        ),
        DiagnosaTanaman(
            gejala = "Daun hijau pekat kusam keunguan di sisi bawah, akar kerdil merana.",
            kemungkinanPenyebab = "Defisiensi Unsur Fosfor (P).",
            penanganan = "Berikan pupuk SP-36 atau TSP sedini mungkin pada saat olah tanah dasar tanaman."
        ),
        DiagnosaTanaman(
            gejala = "Bercak cokelat bulat konsentris pada daun tua yang merembet cepat.",
            kemungkinanPenyebab = "Penyakit Hawar Daun (Jamur Alternaria).",
            penanganan = "Pangkas bagian terdampak untuk kurangi kelembapan, lalu semprot fungisida Mankozeb sesuai dosis."
        )
    )

    val kioskList = listOf(
        StoreInfo(
            name = "Kios Sandang Murah Penyalur Resmi 'Sultra'",
            address = "Jl. Mekongga No. 45, Kel. Laloeha, Kolaka",
            distance = "1.2 KM (Dekat Puskesmas)"
        ),
        StoreInfo(
            name = "Kios Pengecer Sinar Tani",
            address = "Jl. Mekongga Indah Bypass No. 1, Kel. Lamokato, Kolaka",
            distance = "3.8 KM"
        ),
        StoreInfo(
            name = "Kios Penyalur Resmi 'Sultra Sudirman Tani'",
            address = "Jl. Wundulako, Kolaka",
            distance = "5.5 KM"
        ),
        StoreInfo(
            name = "Sahabat Tani",
            address = "Jl. DR. Sutomo No. 16, Pomalaa, Kolaka",
            distance = "15.2 KM",
            phone = "0811-XXXX-XXXX"
        ),
        StoreInfo(
            name = "Kios Pupuk 'Sumber Tani' Baula",
            address = "Jl. Pasar Lamekongga, Kelurahan Ngapa",
            distance = "22.0 KM"
        )
    )

    val buyerList = listOf(
        BuyerInfo(
            name = "UD Kakao Berkah Sultra (Pengepul Utama)",
            address = "Kawasan Pergudangan Balandete, Kolaka",
            contact = "Haji Ambo",
            priceRange = "Rp 122.000 - Rp 135.000 / Kg (Coklat Kering Premium)"
        ),
        BuyerInfo(
            name = "UD Perikanan Tambak & Laut Sultra (Sentra Komoditas Air)",
            address = "Kawasan Pesisir Pelabuhan Pomalaa, Kolaka",
            contact = "Pak Syarifudin (Humas Hasil Tambak)",
            priceRange = "Rp 68.000 - Rp 85.000 / Kg (Udang Vaname & Bandeng Super)"
        ),
        BuyerInfo(
            name = "CV Sultra Nilam Abadi (Eksportir Atsiri)",
            address = "Zona Industri Laloeha, Kolaka",
            contact = "Pak Bayu Roisman",
            priceRange = "Rp 850.000 - Rp 1.100.000 / Kg (Minyak Nilam Atsiri)"
        ),
        BuyerInfo(
            name = "Koperasi Serba Usaha Tani Kelapa & Cengkeh Sultra",
            address = "Jl. Diponegoro No. 88, Wundulako, Kolaka",
            contact = "Ibu Haji Fira",
            priceRange = "Rp 115.000 - Rp 125.000 / Kg (Cengkeh Kering Kupas)"
        ),
        BuyerInfo(
            name = "UD Beras Padi Mekar Sultra (Sentra Giling Modern)",
            address = "Sentra Produksi Samaturu, Kolaka",
            contact = "Pak Risaldi Toni",
            priceRange = "Rp 12.500 - Rp 13.800 / Kg (Beras Kepala Super)"
        ),
        BuyerInfo(
            name = "Koperasi Tebu Rakyat Sejahtera",
            address = "Poros Bombana-Kolaka KM 10, Sultra",
            contact = "Pak Ardiyansyah",
            priceRange = "Rp 850.000 - Rp 950.000 / Ton Tebu Bersih"
        )
    )

    fun formattedDataSummary(currentUser: UserMember? = null, formatHint: String = "CSV"): String {
        val userSummary = if (currentUser != null) {
            "- Nama Akun: ${currentUser.nama}\n" +
            "- Fokus Tanaman: ${currentUser.tanaman}\n" +
            "- Luas Lahan Mandiri: ${currentUser.luasLahan} Ha\n" +
            "- Lokasi: Desa/Kel. ${currentUser.village}, Kec. ${currentUser.subDistrict}\n" +
            "- Komoditas Utama: ${currentUser.commodity}"
        } else {
            "- Status Akun: Pengguna Baru / Belum Mengisi Profil Lengkap"
        }

        val kioskSummary = kioskList.joinToString("\n") { kiosk ->
            "- ${kiosk.name} di ${kiosk.address}. (Jarak: ${kiosk.distance})"
        }

        val buyerSummary = buyerList.joinToString("\n") { buyer ->
            "- ${buyer.name} di ${buyer.address}. Pembelian: ${buyer.priceRange}. Kontak: ${buyer.contact}"
        }

        return """
=== SNAPSHOT INTEGRASI DATASET USER AKUN ASPEK SULTRA (FORMAT: $formatHint) ===
1. INFORMASI PROFIL USER AKUN AKTIF:
$userSummary

2. KIOS MITRA DAN PENYALUR RESMI TERDEKAT:
$kioskSummary

3. JALUR/LOKASI PEMBELI HASIL BUMI & PENGEPUL (OFFICIAL BUYERS):
$buyerSummary
        """.trimIndent()
    }
}
