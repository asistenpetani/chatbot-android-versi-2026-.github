# Panduan Deploy ke Google Play Console via Google Cloud Shell

Panduan ini menjelaskan langkah-langkah lengkap dan aman untuk melakukan kompilasi (*build*), penandatanganan rilis (*release signing*), dan pengunduhan berkas **Android App Bundle (.aab)** aplikasi **Asisten Petani** (`com.asistenpetani`) menggunakan **Google Cloud Shell** untuk diunggah ke Google Play Console.

---

## 🚀 Langkah 1: Persiapan Environment di Google Cloud Shell

1. Buka [Google Cloud Console](https://console.cloud.google.com/) dan aktifkan **Cloud Shell** dengan mengklik ikon terminal di bar kanan atas.
2. Clone repositori proyek Anda ke dalam workspace Cloud Shell:
   ```bash
   git clone https://github.com/indraoktawan/asistenpetani2026.git
   cd asistenpetani2026
   ```
3. Pastikan versi Java yang aktif adalah Java 17 (diperlukan oleh Gradle modern):
   ```bash
   # Cek versi Java saat ini
   java -version

   # Jika versi Java belum versi 17, atur ke JDK 17 di Cloud Shell:
   sudo update-alternatives --config java
   # Pilih nomor yang mengarah ke OpenJDK 17 atau templat Java 17
   ```

---

## 🔑 Langkah 2: Membuat Keystore Rilis (Upload Key)

Google Play Store mewajibkan setiap aplikasi ditandatangani dengan kunci rilis resmi. Jika Anda belum memiliki file Keystore rilis, Anda dapat membuatnya langsung di Cloud Shell dengan perintah `keytool`:

```bash
keytool -genkey -v -keystore my-upload-key.jks \
  -keyalg RSA -keysize 2048 -validity 10000 \
  -alias upload
```

> ⚠️ **PENTING**:
> * Gunakan alias `upload` agar sesuai dengan konfigurasi bawaan rilis di `build.gradle.kts` secara otomatis.
> * Simpan file `my-upload-key.jks` dan catat password yang Anda masukkan dengan baik. Jika file Keystore rilis ini hilang, Anda tidak akan bisa melakukan update aplikasi di masa mendatang.

---

## 🛡️ Langkah 3: Konfigurasi Penandatanganan Kunci Rilis (Signing Config)

Proyek ini menggunakan sistem signing dinamis yang membaca konfigurasi dari Environment Variables untuk mencegah kebocoran password ke dalam Git. Anda memiliki dua opsi praktis di Cloud Shell:

### Opsi A: Menggunakan Environment Variables (Sangat Direkomendasikan)
Atur variabel lingkungan berikut sebelum menjalankan perintah build:
```bash
export STORE_PASSWORD="PASSWORD_KEYSTORE_ANDA"
export KEY_ALIAS="upload"
export KEY_PASSWORD="PASSWORD_KEY_ALIAS_ANDA"
export KEYSTORE_PATH="$(pwd)/my-upload-key.jks"
```

### Opsi B: Meletakkan Keystore di Root Proyek
Salin file `my-upload-key.jks` yang telah dibuat ke direktori utama proyek (`/asistenpetani2026/my-upload-key.jks`). Sistem build di `build.gradle.kts` dikonfigurasi untuk otomatis mencari file dengan nama tersebut di root proyek:
```bash
# Pindahkan keystore jika berada di luar direktori proyek
mv release-key.jks my-upload-key.jks
```

---

## 📦 Langkah 4: Kompilasi Android App Bundle (AAB)

Kompilasi aplikasi ke dalam format **Android App Bundle (.aab)** yang diwajibkan oleh Google Play Store:

1. Pastikan skrip `gradlew` memiliki izin eksekusi:
   ```bash
   chmod +x gradlew
   ```
2. Jalankan perintah kompilasi rilis:
   ```bash
   ./gradlew :app:bundleRelease
   ```
3. Tunggu proses hingga muncul pesan `BUILD SUCCESSFUL`. File bundle rilis yang siap diunggah akan berada di:
   ```
   app/build/outputs/bundle/release/app-release.aab
   ```

---

## 📥 Langkah 5: Unduh File AAB ke Perangkat Lokal

Unduh berkas `.aab` hasil build dari Cloud Shell ke komputer Anda:

1. Di pojok kanan atas panel terminal Cloud Shell, klik ikon titik tiga (Opsi).
2. Pilih **Download**.
3. Masukkan path lengkap dari file `.aab`:
   ```
   asistenpetani2026/app/build/outputs/bundle/release/app-release.aab
   ```
4. Klik **Download**. Simpan file tersebut di komputer lokal Anda.

---

## 🌐 Langkah 6: Unggah ke Google Play Console

1. Buka [Google Play Console](https://play.google.com/console/).
2. Pilih aplikasi Anda (**Asisten Petani**) dari daftar aplikasi.
3. Pada panel navigasi kiri, gulir ke bagian **Rilis** dan pilih **Produksi** (*Production*) atau **Pengujian Terbuka/Tertutup** (*Testing*).
4. Klik tombol **Buat Rilis Baru** (*Create new release*) di pojok kanan atas.
5. Pada bagian **App Bundle**, seret (*drag and drop*) atau unggah file `app-release.aab` yang telah Anda unduh.
6. Isi kolom **Catatan Rilis** (*Release Notes*) dengan rangkuman pembaruan versi Anda.
7. Klik **Simpan** dan pilih **Tinjau Rilis**.
8. Klik **Mulai Peluncuran ke Produksi** untuk mengajukan proses tinjauan (*review*) dari tim Google Play.

---

## 🔒 Praktik Keamanan Terbaik
* **Jangan komit file Keystore**: Pastikan file `my-upload-key.jks` tidak terunggah ke repositori Git publik. File `.jks` sudah dimasukkan ke dalam `.gitignore` bawaan proyek untuk mencegah kesalahan komit.
* **Google Play App Signing**: Pastikan Anda mengaktifkan fitur *Google Play App Signing* pada Google Play Console. Hal ini memungkinkan Google mengelola kunci penandatanganan utama Anda secara aman, sedangkan kunci yang Anda buat di Cloud Shell hanya bertindak sebagai kunci unggah (*upload key*).

### 🛡️ 3 Langkah Mitigasi & Pengamanan Akun Layanan (IAM) & Kredensial
Untuk menjaga aplikasi Anda (**Asisten Petani AI**) tetap aman, stabil, dan terhindar dari risiko penyalahgunaan akun layanan, kami telah menerapkan dan menyusun **3 Langkah Mitigasi Utama** berikut:

#### 1. Isolasi Total Kredensial Sensitif (Anti-Kebocoran Git)
* Kami telah memperbarui berkas `/.gitignore` untuk memastikan semua file kredensial lokal sensitif tidak akan pernah terunggah ke repositori Git publik Anda:
  * `client_secret.json` (Kunci otentikasi login OAuth terminal)
  * `service-account.json` (Kunci akses Firebase Admin SDK)
  * `backend_cloud/service-account.json` (Salinan kredensial backend lokal)
* **Keamanan Klien (APK/AAB)**: Perlu dipahami bahwa file kunci akun layanan (`.json`) **TIDAK PERNAH dimasukkan ke dalam file instalasi Android (APK atau AAB)**. Pengguna akhir atau pembajak yang mendekompilasi APK Anda **tidak akan pernah bisa mengakses** kunci Admin Cloud tersebut. Aplikasi mobile hanya menggunakan string `default_web_client_id` publik yang aman hanya untuk verifikasi Google Sign-In.

#### 2. Mekanisme Fallback Kredensial yang Aman (Anti-Crash di Produksi)
* File inisialisasi backend (`backend_cloud/firebase_init.py`) telah diperbarui secara cerdas:
  * Di lingkungan lokal / Cloud Shell: Backend akan mendeteksi file `service-account.json` secara otomatis untuk mempermudah pengujian.
  * Di lingkungan produksi Google Cloud Run: Backend diprogram untuk **mengutamakan Application Default Credentials (ADC)** secara langsung dari server metadata GCP. 
  * Ini berarti Anda **tidak wajib mengunggah file JSON fisik** ke container Docker Cloud Run Anda. Cukup hubungkan akun layanan dengan izin yang tepat langsung ke instansi Cloud Run Anda, yang merupakan metode paling aman menurut standar industri Google Cloud.

#### 3. Penerapan Prinsip Hak Akses Minimum (Principle of Least Privilege / PoLP)
Untuk membatasi akun layanan `firebase-adminsdk-fbsvc@asistenpetani-ai-app.iam.gserviceaccount.com` agar tidak memiliki akses berlebih yang tidak perlu ke seluruh proyek GCP Anda, pastikan Anda mengonfigurasi peran (roles) di Konsol IAM Google Cloud Anda secara presisi:
* **Batasi Akses Hanya pada Layanan yang Digunakan**:
  1. **Secret Manager**: Berikan peran `Secret Manager Secret Accessor` (untuk membaca kunci API Gemini secara aman dari Secret Manager, **hindari** memberikan `Secret Manager Admin` penuh jika tidak perlu membuat secret baru secara dinamis).
  2. **Cloud Storage**: Berikan peran `Storage Object Admin` (untuk mengunggah dan menampilkan foto hara daun petani).
  3. **Cloud Datastore & Firestore**: Berikan peran `Cloud Datastore User` (untuk melakukan sinkronisasi database lokal petani dengan Firestore).
  4. **Firebase Authentication**: Berikan peran `Firebase Authentication Admin` (untuk memvalidasi token login akun petani secara aman).
* **Hindari Peran Berlebih**: Jangan pernah memberikan peran seperti `Owner` atau `Editor` global kepada akun layanan backend untuk mencegah kerentanan keamanan jika container mengalami kendala di masa depan.

