# 🛡️ Kebijakan Keamanan & Perlindungan Repositori (Security Policy)

## 📌 1. Kepemilikan & Hak Cipta Perangkat Lunak
- **Pemilik Resmi:** Indra Oktawan (`indra.oktawan@gmail.com`)
- **Badan Hukum:** PT. BERTANI TEKNOLOGI INTELEGENSI
- **Status Lisensi:** *Proprietary Software* (Hak Cipta Dilindungi Undang-Undang).
- **Ketentuan:** Dilarang keras melakukan redistribusi, pembuatan salinan/kloning tidak sah, atau rekayasa balik (*reverse engineering*) terhadap basis kode dan logika integrasi AI.

---

## 🔒 2. Perlindungan Cabang Utama (`main` Branch Protection)
Untuk mencegah potensi ancaman, penimpaan kode liar (*unauthorized force push*), atau perubahan tanpa izin, cabang `main` di GitHub wajib dilindungi dengan aturan (*Ruleset*) berikut:

1. **Restrict Direct Pushes:**
   - Akses langsung *push* ke cabang `main` dikunci.
   - Semua perubahan wajib melalui mekanisme *Pull Request* (PR) yang ditinjau dan disetujui langsung oleh pemilik (`@indraoktawan`).
2. **Require Status Checks to Pass:**
   - Pipeline CI/CD (GitHub Actions) harus berstatus **GREEN** (sukses linting dan unit test) sebelum kode dapat digabungkan (*merge*).
3. **Block Force Pushes & Branch Deletions:**
   - Fitur *Force Push* (`--force`) dan *Delete Branch* dinonaktifkan secara absolut pada cabang `main`.
4. **Require Signed Commits (GPG):**
   - Setiap *commit* harus ditandatangani dengan kunci GPG terverifikasi milik pemilik akun resmi.
5. **Enforce on Administrators:**
   - Seluruh aturan berlaku tanpa pengecualian (*Do not allow bypassing*).

---

## 🚨 3. Pelaporan Kerentanan Keamanan (Vulnerability Reporting)
Jika Anda menemukan indikasi celah keamanan atau potensi ancaman pada sistem ASPEK:
- **Email Kontak Keamanan:** `indra.oktawan@gmail.com`
- Mohon jangan mempublikasikan laporan kerentanan di *Public Issue Tracker* GitHub sebelum tim pengembang melakukan investigasi dan merilis perbaikan (*patch*).
