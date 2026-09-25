package com.asistenpetani.domain.model

/**
 * Data classes holding the complete "basisPengetahuanPupuk" (Fertilizer & Nutrient Deficiency Knowledge Base)
 * to support image analysis and precise diagnostic/deficiency recommendation inside the crop assistant application.
 */

data class Fertiliser(
    val pupuk: String,
    val sumberHara: String,
    val kandungan: Map<String, Double>
)

data class NutrientUnsurHara(
    val nama: String,
    val simbol: String,
    val fungsi: List<String>,
    val dampakDefisiensi: List<String>,
    val gejalaDefisiensi: List<String>,
    val toksisitas: List<String>,
    val mobilitas: String = "Cukup aktif"
)

object FertilizerNutrientDb {
    val daftarPupuk = listOf(
        UtilFertiliser("Urea", "NITROGEN", mapOf("N" to 46.0)),
        UtilFertiliser("ZA", "Nitrogen dan sulfur", mapOf("N" to 21.0, "S" to 24.0)),
        UtilFertiliser("Kieserite", "Sulfur", mapOf("S" to 23.0, "MgO" to 29.0)),
        UtilFertiliser("SP 36", "P205", mapOf("P2O5" to 36.0)),
        UtilFertiliser("Superphos", "P205", mapOf("P2O5" to 18.0)),
        UtilFertiliser("TSP", "Transelemen Sulfur dan pospat", mapOf("P2O5" to 46.0, "CaO" to 48.0)),
        UtilFertiliser("Rock Phospate", "Rock pospat", mapOf("P2O5" to 28.0)),
        UtilFertiliser("KCl", "Kalistrate", mapOf("N" to 13.0, "K2O" to 60.0)),
        UtilFertiliser("KNO3", "Nitrogen dan K20", mapOf("K2O" to 46.0)),
        UtilFertiliser("ZK", "Sulfur dan k205", mapOf("S" to 18.0, "K2O" to 50.0)),
        UtilFertiliser("Dolomite", "MGO DAN COA", mapOf("MgO" to 18.0, "CaO" to 30.0))
    )

    val daftarUnsurHara = listOf(
        NutrientUnsurHara(
            nama = "NITROGEN",
            simbol = "(N)-NO3,NH4+",
            fungsi = listOf(
                "Menyumbang 1-5% dari total bahan kering",
                "Penyusun asam nukleat, protein, fitohormon, koenzim dan metabolit sekunder",
                "Penting dalam pembentukan molekul klorofil",
                "Metabolisme asimilasi C",
                "Sifat unsur hara ini sangat aktif pada tanaman."
            ),
            dampakDefisiensi = listOf(
                "Laju pertumbuhan menurun",
                "Luas daun berkurang",
                "Berkurangnya jumlah anakan dan biji",
                "Tunas berkurang: Proporsi akar dominan, proliferasi akar lateral"
            ),
            gejalaDefisiensi = listOf(
                "Warna hijau pucat",
                "Tanaman kerdil",
                "Daun yang lebih tua mengalami klorosis karena N dipindahkan ke jaringan yang lebih muda",
                "Daun bagian atas berwarna hijau muda dimana daun bagian bawahnya berwarna kuning.",
                "Bagian bawah atau daun lebih tua berwarna kuning dan keriput"
            ),
            toksisitas = listOf(
                "Daun menjadi hijau pekat, produksi daun banyak dan batang muda patah, rentan serangan organisme pengganggu tanaman (OPT)",
                "Penambahan N berlebih pada tanaman menyebabkan tanaman berpotensi mengalami defisiensi K dan Cu karena N antagonis terhadap K dan Cu.",
                "Selain itu menyebabkan jaringan menjadi berair (sukulen) dan rentan kerusakan akibat serangan hama/penyakit"
            ),
            mobilitas = "Sangat aktif / Bergerak bebas"
        ),
        NutrientUnsurHara(
            nama = "CALCIUM (KALSIUM)",
            simbol = "(Ca)-Ca2+",
            fungsi = listOf(
                "Komponen penyusun dinding sel",
                "Menstabilkan membran sel dengan mengatur fosfolipid dan protein",
                "Mengatur pemanjangan rambut akar dan tabung polen",
                "Komponen sekresi lendir di tudung akar",
                "Pembentukan callose saat terjadi paparan ion beracun dan terjadi luka",
                "Respon adaptif terhadap pembekuan, suhu rendah, dan anaerobiosis",
                "Keseimbangan ion dalam vakuola sel",
                "Penyusun kalsium pektat di lamela tengah"
            ),
            dampakDefisiensi = listOf(
                "Disintegrasi dinding sel yang lebih cepat oleh poligalakturonase",
                "Kerentanan yang lebih tinggi terhadap patogen jamur dan bakteri",
                "Mengurangi kepadatan dan umur buah serta sayuran",
                "Gangguan pemanjangan akar karena pembelahan sel terbatas"
            ),
            gejalaDefisiensi = listOf(
                "Tingkat pertumbuhan berkurang",
                "Jaringan muda dapat terlipat ke atas dan nekrosis pada defisiensi yang parah",
                "Laju transpirasi organ-organ tanaman rendah sehingga memicu nekrosis dan busuk pangkal buah",
                "Mempercepat penuaan buah dan sayuran",
                "Daun baru cacat atau terhambat pada jaringan muda",
                "Timbul bagian memutih pada daun.",
                "Pertumbuhan terhambat dan tanaman mati dari ujung (akar mundur)"
            ),
            toksisitas = listOf(
                "Kelebihan kalsium (Ca) menyebabkan potensi defisiensi K, Mg, P, Mn, Zn, B, dan Fe"
            ),
            mobilitas = "Tidak bergerak"
        ),
        NutrientUnsurHara(
            nama = "POTASSIUM (KALIUM)",
            simbol = "(K)-K*",
            fungsi = listOf(
                "Zat terlarut anorganik utama dalam pengaturan osmotik pada sel dan jaringan",
                "Pembukaan/penutupan stomata dan niktinasti",
                "Meningkatkan toleransi/resistensi cekaman biotik dan abiotik tanaman",
                "Mengatur proses distribusi dalam floem dan laju alirannya",
                "Penyeimbangan pH dan ion dalam sitoplasma dan kloroplas",
                "Komponen sintesis protein yaitu RuBiP karboksilase dan nitrat reductase"
            ),
            dampakDefisiensi = listOf(
                "Penurunan laju fotosintesis dan peningkatan laju respirasi",
                "Luas daun dan transportasi asimilat berkurang",
                "Lambatnya pembukaan/penutupan stomata",
                "Dehidrasi lebih cepat",
                "Akumulasi gula dalam daun tua",
                "Gangguan pematangan buah seperti tomat"
            ),
            gejalaDefisiensi = listOf(
                "Daun dapat menjadi dehidrasi dan/atau berkerut sepanjang hari",
                "Ruas yang lebih pendek",
                "Tepi daun klorosis dimulai pada daun tua",
                "Nekrosis jaringan pada kasus defisiensi parah",
                "Pada daun muda biasanya menguning di ujung dan tepi.",
                "Pada daun tua, timbul bercak-bercak kuning dan bercak-bercak kering/mati"
            ),
            toksisitas = listOf(
                "Kelebihan unsur Kalium dalam tanah menghambat penyerapan hara kalsium (Ca) dan magnesium (Mg) serta menghambat serapan Boron (B)"
            ),
            mobilitas = "Sangat mobile"
        ),
        NutrientUnsurHara(
            nama = "MANGANESE",
            simbol = "(Mn)-Mn2+",
            fungsi = listOf(
                "Berperan penting dalam reaksi redoks",
                "Komponen enzim seperti superoksida dismutase, oksalat oksidase",
                "Kofaktor enzim",
                "Penting dalam siklus asam trikarboksilat (TCA)",
                "Penting untuk tanaman C, dimana PEP karboksikinase berfungsi dalam dekarboksilasi oksaloasetat dan asimilasi CO2",
                "Biosintesis lignin dan lipid",
                "Kofaktor dari sebuah enzim yang mengendalikan proses fotolisa air di fotosistem 2 yang menyebabkan terjadinya sintesis O2"
            ),
            dampakDefisiensi = listOf(
                "Penurunan kandungan karbohidrat non struktural",
                "Menghambat pertumbuhan akar",
                "Penurunan resistensi pembekuan dan patogen yang menginfeksi akar",
                "Berkurangnya jumlah butir dan kesuburan serbuk sari",
                "Peningkatan degradasi IAA (kehilangan dominasi akar)"
            ),
            gejalaDefisiensi = listOf(
                "Klorosis interveinal pada daun yang baru berkembang.",
                "Mengurangi tingkat pertumbuhan",
                "Perubahan warna biji legume dan deformasi",
                "Timbul bintik-bintik kuning dan atau lubang memanjang antara urat-urat daun"
            ),
            toksisitas = listOf(
                "Muncul ditandai bintik-bintik coklat pada daun dewasa atau daun muda yang berkerut",
                "Kelebihan Mn terjadi pada pH rendah dan menyebabkan potensi defisiensi Fe"
            ),
            mobilitas = "Kurang bergerak"
        ),
        NutrientUnsurHara(
            nama = "ZINC",
            simbol = "(Zn)-Zn2+",
            fungsi = listOf(
                "Berperan sebagai struktur dan katalitik dalam reaksi enzimatik",
                "Replikasi dan ekspresi genetik yaitu RNA polimerase dan protein Zn-metallo",
                "Pengikatan protein",
                "Komponen alkohol dehydrogenase dalam respon tanaman yang tergenang air",
                "Terlibat dalam pengikatan CO2 melalui karbonat anhydrase",
                "Komponen dari Cu-Zn superoksida dismutase",
                "Dibutuhkan untuk menyusun ribosom, pertumbuhan organ polen, membran, serta melindungi lipid dari kerusakan oksidatif"
            ),
            dampakDefisiensi = listOf(
                "Peningkatan aktivitas RNA",
                "Akumulasi karbohidrat",
                "Metabolisme IAA yang terganggu",
                "Peningkatan proses oksidasi serta eksudasi zat terlarut pada akar",
                "Mengurangi kesuburan serbuk sari"
            ),
            gejalaDefisiensi = listOf(
                "Klorosis interveinal pada daun dewasa yang mengembang penuh",
                "Lesi nekrotik pada daun tua",
                "Warna hijau pucat antara vena timbal sentral dan sekunder ('daun belang')",
                "Tanaman kerdil dengan daun kecil",
                "Ruas yang lebih pendek (rosetting)"
            ),
            toksisitas = listOf(
                "Kelebihan Zn menyebabkan potensi defisiensi Besi (Fe)"
            ),
            mobilitas = "Kurang aktif"
        ),
        NutrientUnsurHara(
            nama = "IRON (BESI)",
            simbol = "(Fe)-Fe2+",
            fungsi = listOf(
                "Terlibat dalam reaksi reduksi-oksidasi, sintesis klorofil",
                "Komponen heme protein seperti sitokrom, katalase, peroksidase leghemoglobin",
                "Terlibat dalam detoksifikasi ROS, polimerisasi",
                "Komponen protein fe-S yaitu ferredoxin, superoxide, dismutasem aconitase",
                "Komponen lipoxygenase, yang mendukung pembentukan asam lemak membran sel"
            ),
            dampakDefisiensi = listOf(
                "Penurunan tingkat fotosintesis",
                "Konsentrasi klorofil rendah",
                "Menurunkan jumlah ribosom dan kandungan protein (yaitu rubisco)",
                "Pengurangan ukuran kloroplas dan membrane tilakoid",
                "Risiko fotoinhibisi dan kerusakan foto oksidatif yang lebih tinggi",
                "Menghambat pemanjangan akar"
            ),
            gejalaDefisiensi = listOf(
                "Tingkat pertumbuhan berkurang",
                "Klorosis interveinal dimulai pada daun yang baru terbentuk",
                "Daun muda tumbuh berwarna kuning dan putih dengan urat hijau."
            ),
            toksisitas = listOf(
                "Toksisitas besi pada lahan basah dengan munculnya 'bronzing' daun.",
                "Terjadi terutama pada tanah bereaksi masam (pH rendah) dan menyebabkan defisiensi P"
            ),
            mobilitas = "Sangat lambat"
        ),
        NutrientUnsurHara(
            nama = "MAGNESIUM",
            simbol = "(Mg)-Mg2+",
            fungsi = listOf(
                "Komponen atom inti dalam molekul klorofil",
                "Elemen penghubung dalam agregasi ribosom",
                "Meningkatkan efisiensi enzimatik dalam mengikat air sebagai faktor katalitik",
                "Vacuolar Mg2+ berperan dalam keseimbangan ion dan pengaturan turgor sel",
                "Diperlukan untuk aktivitas polimerase asam nukleat dan nuclease",
                "Berperan dalam fotofosforilasi ATP",
                "Menyeimbangkan pH di stroma selama aktivitas kloroplas yang meningkatkan aktivitas RuBP karboksilase"
            ),
            dampakDefisiensi = listOf(
                "Berpengaruh terhadap ukuran, struktur, dan fungsi kloroplas",
                "Penurunan tingkat fotosintesis",
                "Akumulasi zat pati di daun karena gangguan ekspor fotosintesis",
                "Kecepatan pertumbuhan berkurang",
                "Mengurangi penyerapan dan distribusi sukrosa dalam floem"
            ),
            gejalaDefisiensi = listOf(
                "Klorosis interveinal pada daun dewasa yang mengembang penuh",
                "Daun berubah keputihan dan mengalami absisi karena kekurangan hara",
                "Menyebabkan kandungan protein N daun yang lebih rendah",
                "Pertumbuhan akar berkurang",
                "Daun yang lebih rendah menjadi menguning dari luar ke dalam urat-urat tetap hijau"
            ),
            toksisitas = emptyList(),
            mobilitas = "Cukup lambat"
        ),
        NutrientUnsurHara(
            nama = "SULPHUR",
            simbol = "(S)-SO2, SO2",
            fungsi = listOf(
                "Komponen asam amino esensial yaitu metionin dan sistein",
                "Bagian dari glutathione peptide yang dapat mengurangi stres oksidatif dan toleransi logam berat",
                "Struktur penyusun koenzim dan gugus prostetik dan gugus -SH",
                "Bagian dari koenzim biotin yang membantu dalam sintesis asam lemak",
                "Komponen metabolit sekunder sebagai alliins dan glucosinolates berperan sebagai pengendali tingkat kepedasan, rasa, dan aroma",
                "Pembentukan bintil pada legume",
                "Pengendali metabolisme karbohidrat terutama sukrosa, misal penentu rendemen gula pada tebu"
            ),
            dampakDefisiensi = listOf(
                "Tunas berkurang: proporsi akar dominan, proliferasi akar lateral",
                "Penurunan konduktivitas akar",
                "Fotosintesis dan konduktivitas stomata berkurang",
                "Luas daun berkurang"
            ),
            gejalaDefisiensi = listOf(
                "Hijau pucat hingga warna klorosis yang terdistribusi secara merata di tanaman",
                "Pada kacang-kacangan, bintilnya lebih kecil dan jumlahnya berkurang"
            ),
            toksisitas = listOf(
                "Daun mudah gugur saat kelebihan hara sulfur"
            ),
            mobilitas = "Cukup bervariasi"
        ),
        NutrientUnsurHara(
            nama = "COPPER",
            simbol = "(Cu)-Cu2+",
            fungsi = listOf(
                "Mengkatalisis reaksi redoks",
                "Komponen dari 100 enzim seperti askorbat, polifenol dan diamina oksidase",
                "Komponen plastosianin dan Cu-Zn superoksida dismutase",
                "Komponen Sitokrom C oksidase, terlibat dalam transpot elektron mitokondria",
                "Biosintesis lignin, alkaloid, dan zat melatonik (reaksi terhadap luka dan infeksi) serta penting dalam respon etilen"
            ),
            dampakDefisiensi = listOf(
                "Pembungaan dan pematangan tertunda",
                "Pembentukan serbuk sari terganggu",
                "Tingkat fotosintesis dan sintesis karbohidrat yang lebih rendah",
                "Mengurangi fiksasi symbiosis N2 dan lignifikasi",
                "Menghambat pembentukan akar"
            ),
            gejalaDefisiensi = listOf(
                "Daun muda klorosis (pemutihan lebih lanjut) dan nekrosis",
                "Pertumbuhan terhambat akibat ruas batang abnormal (rosetting)",
                "Pertumbuhan terhambat",
                "Hilangnya dominasi spesial"
            ),
            toksisitas = listOf(
                "Toksisitas Cu berpotensi menyebabkan defisiensi Mn"
            ),
            mobilitas = "Tidak bergerak"
        ),
        NutrientUnsurHara(
            nama = "BORON",
            simbol = "(B)-B(OH)",
            fungsi = listOf(
                "Biosintesis dan penyusun dinding sel serta pembentukan jaringan pektik",
                "Penyusun dan mendukung fungsi membran",
                "Metabolisme IAA",
                "Diperlukan dalam asosiasi rhizobium dan aktivitas meristematik",
                "Metabolisme fenol dan biosintesis lignin",
                "Diperlukan untuk pertumbuhan dan perkembangan tabung polen"
            ),
            dampakDefisiensi = listOf(
                "Akumulasi fenol dan pembentukan ROS",
                "Penghentian pemanjangan akar dan pucuk",
                "Penghambatan dominasi apikal",
                "Mengurangi ekspansi daun, serta produksi dan kesuburan serbuk sari",
                "Gagalnya pembungaan atau pembentukan buah",
                "Kerentanan yang lebih tinggi terhadap keracunan aluminum (Al)",
                "Defisiensi B dapat terjadi jika kelebihan pasokan N, K, dan Ca"
            ),
            gejalaDefisiensi = listOf(
                "Gagalnya pertumbuhan apikal",
                "Ruas lebih pendek (tampilan lebat atau roset)",
                "Daun baru terbentuk cacat",
                "Batang retak atau berlubang",
                "Tepi daun yang terbakar atau hangus"
            ),
            toksisitas = emptyList(),
            mobilitas = "Hanya bergerak pada tanaman pengangkut karbohidrat poliol"
        ),
        NutrientUnsurHara(
            nama = "MOLYBDENUM",
            simbol = "(Mo)-MoO2",
            fungsi = listOf(
                "Kofaktor dari enzim nitrat reductase yang mengendalikan reduksi nitrat menjadi ammonium",
                "Terlibat dalam biosintesis vitamin",
                "Katalisator dalam reaksi transfer elektron dalam enzim seperti xantin dehidrogenase, aldehida oksigenase dan sulfit reduktase",
                "Penting untuk pengurangan dan penyerapan nitrat",
                "Kofaktor dari enzim nitrogenase yang terlibat dalam proses fiksasi N2 dari udara menjadi ammonium"
            ),
            dampakDefisiensi = listOf(
                "Hambatan pertumbuhan tanaman akibat kegagalan reduksi nitrat menjadi ammonium",
                "Konsentrasi klorofil rendah",
                "Konsentrasi ABA yang lebih rendah, menyebabkan rendahnya toleransi terhadap pembekuan, genangan air salin dan kekeringan",
                "Keterlambatan munculnya mulai dan mekarnya bunga"
            ),
            gejalaDefisiensi = listOf(
                "Klorosis pada daun muda",
                "Deformitas daun muda yaitu whiptail",
                "Ruas yang lebih pendek",
                "Klorosi lokal dan nekrosi di sepanjang tulang utama daun dewasa"
            ),
            toksisitas = emptyList(),
            mobilitas = "Tinggi / Menyeluruh"
        ),
        NutrientUnsurHara(
            nama = "Fosfat",
            simbol = "(P)",
            fungsi = listOf(
                "Unsur penting pembentuk ATP untuk penyaluran energi tumbuhan",
                "Mempercepat pembentukan akar akar lateral baru",
                "Mendukung kokohnya batang padi dan sereal"
            ),
            dampakDefisiensi = listOf(
                "Laju fotosintesis terhambat karena minim pasokan ATP energi",
                "Permukaan daun mengecil"
            ),
            gejalaDefisiensi = listOf(
                "Daun-daun cenderung gelap dibandingkan biasanya dan berguguran"
            ),
            toksisitas = emptyList(),
            mobilitas = "Mudah dialihkan"
        )
    )

    /**
     * Diagnose based on symptom notes with smart match
     */
    fun diagnoseDeficiency(inputText: String): NutrientUnsurHara? {
        val lowerText = inputText.lowercase()
        return daftarUnsurHara.firstOrNull { hara ->
            lowerText.contains(hara.nama.lowercase()) ||
                lowerText.contains(hara.simbol.lowercase()) ||
                hara.gejalaDefisiensi.any { lowerText.contains(it.lowercase()) } ||
                hara.dampakDefisiensi.any { lowerText.contains(it.lowercase()) }
        }
    }
}

// Quick type alias helper to prevent moshi conflict
typealias UtilFertiliser = Fertiliser
