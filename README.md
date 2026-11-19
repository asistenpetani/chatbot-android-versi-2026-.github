www
```markdown
# 🌾 ASPEK — Asisten Petani Kita

[![Android](https://img.shields.io/badge/Platform-Android-green)]
[![Status](https://img.shields.io/badge/Status-MVP-orange)]
[![License](https://img.shields.io/badge/License-Proprietary-red)]

Asisten digital berbasis AI untuk membantu petani memperoleh informasi awal
tentang tanaman, hama, penyakit, cuaca, dan budidaya.

> ASPEK adalah alat bantu. Hasil AI bukan pengganti penyuluh atau ahli pertanian.
```

Tambahkan gambar atau banner sendiri di folder:

```text
assets/
├── banner.png
├── screenshot-home.png
├── screenshot-diagnosis.png
└── architecture.svg
```

struktur:

```markdown
## Daftar Isi

- [Tentang ASPEK](#tentang-aspek)
- [Fitur](#fitur)
- [Demo](#demo)
- [Status Proyek](#status-proyek)
- [Cara Menjalankan](#cara-menjalankan)
- [Arsitektur](#arsitektur)
- [Roadmap](#roadmap)
- [Kontribusi](#kontribusi)
- [Dukungan](#dukungan)
- [Lisensi](#lisensi)
- [Batasan AI](#batasan-ai)
```
 3. Tampilkan fitur dalam bentuk ringkas

Contoh:

```markdown
 Fitur

- 🤖 Konsultasi pertanian berbasis AI
- 🌱 Identifikasi awal gejala hama dan penyakit tanaman
- 🌦️ Informasi cuaca dan peringatan kondisi ekstrem
- 📊 Informasi harga komoditas
- 📚 Basis pengetahuan pertanian
- 📱 Aplikasi Android
```

| Fitur | Status |
|---|---|
| Login Google | Tersedia |
| Chat pertanian | Prototipe |
| Diagnosa gambar | Dalam pengujian |
| Cuaca | Direncanakan |
| Harga pasar | Direncanakan |

- GIF atau video singkat;
- screenshot aplikasi;
- tautan APK atau halaman demo, jika aman;
- contoh pertanyaan;
- contoh hasil AI;
- batasan hasil diagnosis.

Contoh:

```markdown
## Demo

1. Buka aplikasi.
2. Masuk menggunakan akun Google.
3. Pilih menu Konsultasi.
4. Masukkan komoditas dan kondisi tanaman.
5. Tinjau rekomendasi awal.

Demo: [Lihat video demo](YouTube https://youtube.com/@nama-kita?si=UCy6z0JXfJd5DE5O)
```

 struktur:

```text
docs/
├── architecture.md
├── android-development.md
├── backend-development.md
├── deployment.md
├── security.md
└── testing.md
```

README arsitektur:

```markdown
## Arsitektur

ASPEK terdiri dari:

- Android client berbasis Kotlin;
- backend API;
- Firebase Authentication;
- Firestore;
- layanan AI;
- penyimpanan gambar dan dokumen.

Detail penerapan tersedia di [docs/architecture.md](docs/architecture.md).
```

- project number;
- account ID Play Console;
- nama bucket produksi;
- detail IAM;
- perintah deployment internal;
`CONTRIBUTING.md`:

```markdown
# Panduan Kontribusi

1. Buat fork repository.
2. Buat branch baru.
3. Jelaskan perubahan yang dibuat.
4. Jalankan test dan lint.
5. Buat Pull Request.

Mohon jangan mengirim:
- API key;
- password;
- service-account JSON;
- signing key;
- data pribadi pengguna.
```

Tambahkan template:

```text
.github/
├── ISSUE_TEMPLATE/
│   ├── bug_report.md
│   └── feature_request.md
├── pull_request_template.md
├── FUNDING.yml
└── workflows/
```
sponsor
GitHub Sponsors sudah aktif:

```yaml
github: indraoktawan
custom:
  - https://alamat-resmi-dukungan 
```
roadmap

Contoh:

```markdown
## Roadmap

### Tahap 1 — Prototipe
- [x] Struktur aplikasi Android
- [x] Dokumentasi arsitektur
- [ ] Demo alur login

### Tahap 2 — MVP
- [ ] Chat pertanian
- [ ] Penyimpanan riwayat
- [ ] Pengujian dengan penyuluh

### Tahap 3 — Pilot
- [ ] Uji coba pada kelompok tani
- [ ] Evaluasi kualitas jawaban
- [ ] Perbaikan berdasarkan umpan balik
```

Gunakan checklist berdasarkan kondisi nyata, bukan rencana yang belum dikerjakan.

## 9. Tambahkan topik repository

Untuk meningkatkan

```text
agriculture
agritech
android
kotlin
artificial-intelligence
farmer
indonesia
firebase
```

lisensi

Saat ini proyek Anda menggunakan lisensi proprietary. Itu boleh, tetapi harus konsisten:

- jika ingin mencari **sponsor open source**, gunakan lisensi open source yang sesuai;
- jika ingin melindungi kode sebagai produk komersial, pertahankan proprietary dan tawarkan sponsor/pilot/lisensi komersial;
- jika ingin kompromi, pisahkan komponen:
  - dokumentasi atau SDK dengan lisensi terbuka;
  - backend, prompt, dan basis pengetahuan dengan lisensi komersial.

## Struktur

```text
chatbot-android-versi-2026/
├── README.md
├── LICENSE
├── EULA.md
├── PRIVACY_POLICY.md
├── SECURITY.md
├── CONTRIBUTING.md
├── CHANGELOG.md
├── docs/
├── assets/
├── app/
├── backend_cloud/
├── chatbot-knowledge-base/
└── .github/
    ├── FUNDING.yml
    ├── ISSUE_TEMPLATE/
    └── workflows/
```

> Dukungan sukarela untuk pengembangan ASPEK dapat diberikan melalui PayPal:  
> https://paypal.me/indraoktawan
> proxy payment via gmail: indra.oktawan@gmail.com
> Dana yang diterima akan digunakan untuk pengujian aplikasi, pengembangan fitur, dokumentasi, dan operasional proyek sesuai transparansi yang diumumkan. Dukungan bersifat sukarela dan tidak menjamin kepemilikan saham, lisensi, atau hasil investasi.

Untuk `.github/FUNDING.yml`, PayPal tidak tersedia sebagai platform pendanaan bawaan GitHub. Gunakan tautan di bagian `custom`:

```yaml
custom:
  - https://paypal.me/indraoktawan
```

Jika GitHub Sponsors Anda sudah aktif dan disetujui, tambahkan juga:

```yaml
github: indraoktawan
```