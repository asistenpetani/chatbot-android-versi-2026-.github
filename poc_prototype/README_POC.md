# Proof of Concept (POC) - Asisten Petani AI

Folder ini berisi rangkuman prototipe (POC) keseluruhan dari sistem **Asisten Petani AI** untuk memudahkan pemahaman konseptual dan arsitektur aplikasi tanpa harus melihat kerumitan kode *production-ready*.

## Daftar Isi:
1. **`android_client_poc/`**: Berisi contoh kode Android (Kotlin & Jetpack Compose) yang mendemonstrasikan bagaimana aplikasi seluler mengirim pesan/foto menggunakan token otentikasi Firebase (bukan kredensial Service Account).
2. **`backend_fastapi_genkit_poc/`**: Berisi contoh server backend menggunakan Python, FastAPI, dan Firebase Genkit. Menunjukkan bagaimana backend menerima token dari Android, memvalidasinya, lalu menggunakan Genkit untuk orkestrasi model Gemini (mencegah *Prompt Injection*).
3. **`security_rules_poc/`**: Berisi aturan keamanan Firebase (Firestore dan Cloud Storage) yang menerapkan lapisan pertahanan untuk aplikasi Android (hanya user yang login yang bisa unggah/baca datanya sendiri).
4. **`iam_setup_poc.sh`**: Skrip bash yang menunjukkan cara membuat Service Account dengan prinsip *Least Privilege* (SA terpisah untuk AI, Storage, dan Scanner Antivirus).

## Arsitektur Keamanan 2 Lapis:
1. **Lapis 1 (Client - Android)**: Aplikasi Android sama sekali tidak memegang Service Account JSON. Aplikasi menggunakan SDK Firebase Client. Setiap akses data dipagari oleh `security_rules_poc`.
2. **Lapis 2 (Server - Cloud Run & GCP)**: Server backend menggunakan Service Account yang diinjeksi via variabel lingkungan GCP, dipisah berdasarkan fungsinya (Komputasi AI, Manajemen Storage, dan Scanner Keamanan).

Folder ini dirancang khusus sebagai materi pembelajaran dan panduan tingkat tinggi bagi Pemilik (indra.oktawan@gmail.com).
