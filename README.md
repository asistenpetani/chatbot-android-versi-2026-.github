# 🚀 ASPEK - Asisten Petani Kita (Universal Architecture & Blueprint)

Dokumen ini merupakan **Spesifikasi Arsitektur Sistem (System Architecture Specification)** resmi untuk **ASPEK - Asisten Petani Kita** (`indraoktawan/asistenpetani2026`). Dokumen ini disusun secara modular untuk memisahkan domain bisnis, arsitektur sistem, skema data, alur kecerdasan buatan, keamanan berbasis peran, dan otomasi deployment produksi di Google Play Store.

---

## 📌 1. Identitas & Kepemilikan Sistem

- **Pemilik Aplikasi (Owner):** `indra.oktawan@gmail.com` / Indra Oktawan
- **Badan Hukum:** PT. BERTANI TEKNOLOGI INTELEGENSI
- **Google Cloud Project ID:** `asistenpetani-ai-app`
- **Google Cloud Project Number:** `956692315956`
- **Google Play Console Account ID:** `5106747258949665772`
- **Google Developer Profile:** [https://g.dev/indraoktawan](https://g.dev/indraoktawan)
- **GitHub Repository:** [indraoktawan/asistenpetani2026](https://github.com/indraoktawan/asistenpetani2026)
- **Primary Cloud Region (Colocation Zero-Cost Egress):** `asia-southeast2` (Jakarta)
- **Cloud RAG Knowledge Bucket:** `gs://asistenpetani-rag-knowledge` (`asia-southeast2`)

---

## 🎯 2. Tujuan & Lingkup Aplikasi (Business Scope)

ASPEK dirancang sebagai platform pendamping digital agrikultur modern berbasis kecerdasan buatan (*GenAI*) yang berfokus pada:
1. **Diagnosis Cepat Penyakit & Hama Tanaman:** Identifikasi berbasis visi komputer melalui foto daun/tanaman.
2. **Konsultasi Budidaya & Cuaca Cerdas:** Rekomendasi pemupukan adaptif, peringatan cuaca ekstrem, dan jadwal tanam.
3. **Pemberdayaan Ekonomi Petani:** Pemantauan harga pasar komoditas dan pemetaan kiosk saprotan/tani terdekat.
4. **Efisiensi Biaya Operasional (Zero-Cost Architecture):** Berjalan optimal di atas infrastruktur Google Cloud *Always Free Tier* dengan proteksi kebocoran egress.

---

## 🏛️ 3. Diagram Arsitektur Keseluruhan (System Architecture)

```text
┌────────────────────────────────────────────────────────────────────────┐
│                        📱 ANDROID CLIENT (KOTLIN)                       │
│  - Jetpack Compose M3 UI               - Room Database (Local Cache)    │
│  - WebP Image Compressor (<150KB)      - Credential Manager (OAuth)     │
└───────────────────┬────────────────────────────────┬───────────────────┘
                    │ 1. Google OAuth ID Token       │ 2. HTTPS Request + Bearer Token
                    ▼                                ▼
┌──────────────────────────────┐       ┌─────────────────────────────────┐
│     🔐 GOOGLE IDENTITY       │       │   ☁️ BACKEND API (CLOUD RUN)    │
│      & FIREBASE AUTH         │       │        (asia-southeast2)        │
│  - Verify Token & Claims     │◄──────┤  - Rate Limiter (Token Bucket)  │
│  - UID-based Role Validation │       │  - Response & Knowledge Cache   │
└──────────────────────────────┘       │  - Centralized Prompt Builder   │
                                       │  - Cloud RAG Knowledge Engine   │
                                       └──────────────┬──────────────────┘
                                                      │
         ┌───────────────────┬────────────────────────┼───────────────────┐
         ▼                   ▼                        ▼                   ▼
┌──────────────────┐┌──────────────────┐    ┌──────────────────┐┌──────────────────┐
│   🗄️ FIRESTORE   ││ 🪣 CLOUD STORAGE │    │ 🧠 GENAI SERVICE ││  🗺️ MAPS & OPEN  │
│ (asia-southeast2)││ (asia-southeast2)│    │   (Fast & Pro)   ││   WEATHER API    │
│ - users/profiles ││ - rag-knowledge  │    │ - MODEL_FAST     ││ - Geolocation    │
│ - chats/history  ││ - plant_images   │    │ - MODEL_PRO      ││ - Weather Alert  │
└──────────────────┘└──────────────────┘    └──────────────────┘└──────────────────┘
```

---

## 🔄 4. Alur Pengguna & Autentikasi (Auth Sequence)

```text
[Petani / User]
       │ 1. Buka Aplikasi & Setujui Terms of Service & Privacy Policy
       ▼
[Google Credential Manager]
       │ 2. Pilih Akun Google -> Dapatkan ID Token
       ▼
[Cloud Run Backend]
       │ 3. Verifikasi ID Token via Firebase Admin SDK
       │ 4. Periksa apakah UID terdaftar di collection 'users/{uid}'
       ├── [Belum Terdaftar] ──> Buat dokumen 'users/{uid}' & redirect ke Form Profil Petani
       └── [Sudah Terdaftar] ──> Ambil profil 'profiles/{uid}' & verifikasi custom claims role
       ▼
[Dashboard Utama ASPEK]
       │ 5. Masuk ke halaman utama (Siap chat, diagnosa daun, peta kiosk, cuaca)
```

---

## 👥 5. Role-Based Access Control (RBAC Berbasis UID)

Validasi hak akses **tidak mengandalkan perbandingan string email** pada kode client, melainkan menggunakan **Firebase UID & Custom Claims**:

| Role Module | Identifikasi & Otorisasi | Hak Akses Fitur |
| :--- | :--- | :--- |
| **Owner** | UID terdaftar di collection `admins/{uid}` (`role="owner"`) | Akses penuh dashboard analitik, konfigurasi prompt backend, audit log, bypass limitasi kuota testing. |
| **Admin Service** | Service Account GCP (`roles/iam.serviceAccountUser`) | Eksekusi pipeline CI/CD, backup database otomatis, broadcast notifikasi FCM. |
| **Petani Premium** | Dokumen `users/{uid}` field `role="premium"` (Play Billing aktif) | Akses `MODEL_PRO` (Deep Thinking), riwayat diagnosa tanpa batas, konsultasi pasar prioritas. |
| **Petani Gratis** | Dokumen `users/{uid}` field `role="free"` (Default) | Akses `MODEL_FAST`, diagnosa harian standar, informasi cuaca dan kiosk publik. |

---

## 🗄️ 6. Skema & Model Database (Firestore Schema)

```text
firestore/
├── users/
│   └── {uid}/
│       ├── email: string
│       ├── role: "owner" | "admin" | "premium" | "free"
│       ├── status: "active" | "suspended"
│       └── createdAt: timestamp
│
├── profiles/
│   └── {uid}/
│       ├── nama: string
│       ├── desa: string
│       ├── kecamatan: string
│       ├── kabupaten: string
│       ├── provinsi: string
│       ├── komoditasUtama: string
│       ├── luasLahanHa: number
│       ├── umurTanamanHari: number
│       └── updatedAt: timestamp
│
├── chat_history/
│   └── {chatId}/
│       ├── uid: string
│       ├── sender: "user" | "asisten"
│       ├── message: string
│       ├── sessionId: string
│       └── timestamp: timestamp
│
├── diagnosis/
│   └── {diagId}/
│       ├── uid: string
│       ├── imageUrl: string
│       ├── diseaseName: string
│       ├── confidence: number
│       ├── recommendations: array<string>
│       └── timestamp: timestamp
│
├── weather_cache/
│   └── {locationHash}/
│       ├── dataJson: string
│       └── expiresAt: timestamp
│
├── market_prices/
│   └── {commodityId}/
│       ├── namaKomoditas: string
│       ├── hargaPerKg: number
│       ├── pasarWilayah: string
│       └── updatedAt: timestamp
│
└── notifications/
    └── {notifId}/
        ├── targetUid: string | "all"
        ├── title: string
        ├── body: string
        ├── type: "weather_alert" | "pest_warning" | "market_update"
        └── isRead: boolean
```

---

## 🧠 7. Arsitektur Cloud RAG & Pengorganisasian Basis Pengetahuan Agrikultur

### 7.1 Struktur Folder Cloud Storage Bucket (`gs://asistenpetani-rag-knowledge` di `asia-southeast2`)
Untuk memastikan sistem Retrieval-Augmented Generation (RAG) bekerja dengan akurasi tinggi, seluruh berkas pengetahuan ilmiah agrikultur diorganisasikan dalam struktur hirarkis berikut:

```text
gs://asistenpetani-rag-knowledge/
├── commodities/
│   ├── kakao/
│   │   ├── sop_budidaya_kakao_sulawesi.json
│   │   ├── hama_penggerek_buah_kakao_pbk.json
│   │   ├── penyakit_busuk_buah_phytophthora.json
│   │   └── standar_fermentasi_dan_kadar_air.json
│   ├── padi/
│   │   ├── sop_pemupukan_berimbang_padi.json
│   │   ├── pengendalian_wereng_coklat_dan_blast.json
│   │   └── kalender_tanam_iklim_tropis.json
│   ├── jagung/
│   │   ├── varietas_unggul_dan_jarak_tanam.json
│   │   └── ulat_grayak_frugiperda_solusi.json
│   └── kelapa_sawit/
│       └── manajemen_kastrasi_dan_pemupukan.json
│
├── nutrients_and_fertilizers/
│   ├── tabel_defisiensi_unsur_hara_npk_mikro.json
│   ├── panduan_dosis_pupuk_organik_hayati.json
│   └── kalkulator_kebutuhan_dolomit_asam_tanah.json
│
├── pest_and_disease_atlas/
│   ├── visual_symptom_dictionary.json
│   ├── rekomendasi_insektisida_nabati_organik.json
│   └── ambang_ekonomi_pengendalian_hama_terpadu.json
│
├── regulations_and_market/
│   ├── het_pupuk_bersubsidi_pemerintah.json
│   └── standar_mutu_ekspor_sni_komoditas.json
│
└── master_index/
    └── rag_knowledge_catalog.json
```

### 7.2 Standar Format Berkas Dokumen RAG (JSON Schema)
Setiap dokumen pengetahuan agrikultur wajib mengikuti format terstruktur JSON berikut agar dapat diparsing dan diindeks secara otomatis oleh Cloud Run Backend:

```json
{
  "document_id": "KAKAO-PBK-001",
  "commodity": "Kakao",
  "category": "Penyakit & Hama",
  "title": "Pengendalian Penggerek Buah Kakao (PBK - Conopomorpha cramerella)",
  "region_applicability": ["Sulawesi Tenggara", "Sulawesi Selatan", "Nasional"],
  "symptoms": [
    "Buah masak sebelum waktunya dengan warna kuning tidak merata",
    "Biji kakao saling melekat dan sulit dikeluarkan dari kulit buah",
    "Terdapat lubang gerek bekas keluarnya larva"
  ],
  "organic_solutions": [
    "Penyarungan buah (kondomisasi) menggunakan plastik tipis saat buah berukuran 8-10 cm",
    "Aplikasi jamur entomopatogen Beauveria bassiana semprot pada sore hari",
    "Pemangkasan sanitasi tajuk pohon untuk mengurangi kelembaban"
  ],
  "chemical_solutions": [
    "Penyemprotan insektisida berbahan aktif deltametrin sesuai ambang kendali ekonomi"
  ],
  "preventive_sop": [
    "Panen sering (rotasi panen tiap 7 hari)",
    "Kondomisasi buah muda",
    "Sanitasi kebun dan kubur kulit buah terinfeksi minimal 30 cm"
  ],
  "last_updated": "2026-06-12"
}
```

### 7.3 Prosedur Unggah & Sinkronisasi Dokumen Pengetahuan
1. **Membuat / Memperbarui Berkas Pengetahuan:**
   - Simpan dokumen dalam format `.json` yang valid di direktori lokal `./chatbot-knowledge-base/` atau `./backend_cloud/`.
2. **Sinkronisasi Otomatis via Cloud Shell / Google Cloud CLI:**
   ```bash
   # Sinkronisasi seluruh direktori ke bucket RAG di region Jakarta
   gcloud storage rsync -r ./chatbot-knowledge-base/ gs://asistenpetani-rag-knowledge/ \
       --project=asistenpetani-ai-app
   ```
3. **Validasi Indeks RAG di Cloud Run:**
   - Saat backend Cloud Run memulai *instance* baru, service `AgriculturalRAGService` secara otomatis memuat metadata dari `gs://asistenpetani-rag-knowledge` ke dalam *in-memory semantic index* untuk pencarian cepat *Top-K Context* (< 20ms).

---

## 🔒 8. Protokol Keamanan & Perlindungan Cabang Utama (`main`) di GitHub

Untuk melindungi hak kekayaan intelektual (Proprietary Software) dan mencegah ancaman penimpaan kode liar (*unauthorized force push*), cabang utama repositori **`indraoktawan/asistenpetani2026`** dikunci dengan kebijakan berikut:

### 8.1 Aturan Perlindungan Cabang (*GitHub Branch Protection Ruleset*)
1. **Require a Pull Request before merging:**
   - Centang opsi: **Require approvals** (Minimal 1 persetujuan dari `@indraoktawan`).
   - Centang opsi: **Dismiss stale pull request approvals when new commits are pushed**.
   - Centang opsi: **Require review from Code Owners** (Merujuk berkas `.github/CODEOWNERS`).
2. **Require status checks to pass before merging:**
   - Centang opsi: **Require branches to be up to date before merging**.
   - Status check wajib: `Android Test & Build Lint`, `Backend Cloud Run Validation`.
3. **Require signed commits:**
   - Mengharuskan setiap *commit* diverifikasi dengan GPG Key pemilik sah.
4. **Block Force Pushes & Deletions:**
   - Centang: **Do not allow force pushes** (Mencegah `git push --force`).
   - Centang: **Do not allow deletions** (Mencegah penghapusan cabang `main`).
5. **Enforce on Administrators:**
   - Centang: **Include administrators** untuk memastikan aturan berlaku tanpa celah bypass.

### 8.2 Perintah Konfigurasi Otomatis via GitHub CLI (`gh`)
Jalankan perintah berikut di terminal untuk mengaktifkan proteksi cabang secara instan:
```bash
gh api \
  --method PUT \
  -H "Accept: application/vnd.github+json" \
  /repos/indraoktawan/asistenpetani2026/branches/main/protection \
  -f required_status_checks.strict=true \
  -f required_status_checks.contexts[]="compile_applet" \
  -f enforce_admins=true \
  -f required_pull_request_reviews.dismiss_stale_reviews=true \
  -f required_pull_request_reviews.require_code_owner_reviews=true \
  -f required_pull_request_reviews.required_approving_review_count=1 \
  -f restrictions=null \
  -f allow_force_pushes=false \
  -f allow_deletions=false
```

---

## ⚡ 9. Optimasi Performa, Cache, & Rate Limiting

1. **Abstraksi Model (Environment Variables):**  
   Menggunakan variabel lingkungan fleksibel `MODEL_FAST` dan `MODEL_PRO` (contoh: `gemini-2.0-flash` dan `gemini-1.5-pro`) agar pembaruan model tidak memerlukan kompilasi ulang kode.
2. **Rate Limiter Terpasang di Backend:**  
   Membatasi maksimal 15 request per menit per UID guna mencegah lonjakan pemanggilan tak terkontrol (*API flooding*).
3. **Response & Knowledge Caching:**  
   Menyimpan hasil pertanyaan umum (misal: rekomendasi NPK untuk padi umur 30 hari) dalam cache memori backend untuk menghemat kuota token.

---

## 🛡️ 10. Proteksi Biaya Jaringan (Zero-Cost Network Egress)

1. **Regional Colocation (`asia-southeast2` Jakarta):**  
   Cloud Run, Firestore, dan Cloud Storage Bucket `asistenpetani-rag-knowledge` berada dalam satu region yang sama (Rp 0 Egress antar-layanan).
2. **Firebase Hosting CDN:**  
   Aset statis (dokumen privasi, gambar icon) dilayani langsung oleh Google Edge CDN dengan `Cache-Control: max-age=31536000, public, immutable`.
3. **Network Service Tier STANDARD:**  
   Perutean proyek diatur ke rute publik standar lokal hemat biaya.
4. **Kompresi Data Klien:**  
   Setiap gambar dikompresi ke WebP (< 150 KB) dan kueri Firestore dibatasi dengan `limit(10)`.
5. **Budget Alert Rp 1.000.000:**  
   Peringatan dini terhubung langsung ke email `indra.oktawan@gmail.com`.

---

## 📊 11. Monitoring, Logging, Backup, & Notifikasi

1. **Observabilitas Sistem:**  
   - **Google Cloud Logging:** Pelacakan request, latensi, dan eksepsi di Cloud Run.
   - **Firebase Crashlytics & Performance:** Telemetri stabilitas aplikasi Android di tangan petani.
   - **Google Cloud Monitoring:** Alarm otomatis saat terjadi lonjakan error 5xx atau latensi tinggi.
2. **Backup Otomatis Firestore:**  
   Jadwal export harian koleksi Firestore ke Cloud Storage bucket terisolasi.
3. **Notifikasi Cerdas (Firebase Cloud Messaging):**  
   Kirim broadcast peringatan cuaca dini, fluktuasi harga komoditas pasar, dan pengingat pemupukan terjadwal.

---

## 🚀 12. Otomasi Pipeline CI/CD (GitHub Actions)

Pipeline didefinisikan secara deklaratif di dalam `.github/workflows/release.yml`:
1. **Tahap 1: 🧪 Android Lint & Test:** Validasi sintaks dan unit test lokal di runner Ubuntu.
2. **Tahap 2: ☁️ Deploy Backend (`asia-southeast2`):** Build Docker image ke Google Artifact Registry Jakarta dan deploy ke Cloud Run dengan integrasi `asistenpetani-rag-knowledge`.
3. **Tahap 3: 📱 Build AAB & Publish Play Store:** Menghasilkan Android App Bundle (`.aab`) rilis dan mendistribusikan ke Google Play Console via Workload Identity Federation (WIF).

---

*Copyright © 2026 Indra Oktawan / PT. BERTANI TEKNOLOGI INTELEGENSI. All Rights Reserved.*
