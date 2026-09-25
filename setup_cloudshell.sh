#!/bin/bash
echo "==========================================================="
echo " PENGATURAN GOOGLE CLOUD SHELL - ASISTEN PETANI AI APP"
echo "==========================================================="

PROJECT_ID="asistenpetani-ai-app"

echo "1. Mengatur Default Project ke: $PROJECT_ID"
gcloud config set project $PROJECT_ID

echo "2. Mengaktifkan Google Cloud APIs yang dibutuhkan..."
echo "(Maps SDK, Cloud Run, Cloud Build, IAM, Secret Manager)"
gcloud services enable \
  maps-android-backend.googleapis.com \
  run.googleapis.com \
  cloudbuild.googleapis.com \
  iam.googleapis.com \
  secretmanager.googleapis.com

echo ""
echo "3. Membuat file .env untuk menyimpan kunci API..."
if [ ! -f .env ]; then
    echo "MAPS_API_KEY=ISI_DENGAN_MAPS_API_KEY_ANDA" > .env
    echo "GEMINI_API_KEY=ISI_DENGAN_GEMINI_API_KEY_ANDA" >> .env
    echo "File .env berhasil dibuat!"
else
    echo "File .env sudah tersedia."
fi

echo ""
echo "==========================================================="
echo "PENGATURAN SELESAI!"
echo "==========================================================="
echo "Instruksi selanjutnya untuk Anda jalankan di Cloud Shell:"
echo "1. Edit kunci API Anda dengan perintah:"
echo "   nano .env"
echo ""
echo "2. Berikan izin eksekusi pada Gradle wrapper:"
echo "   chmod +x gradlew"
echo ""
echo "3. Untuk membuat file APK (Uji Coba):"
echo "   ./gradlew assembleDebug"
echo ""
echo "4. Untuk membuat file AAB (Rilis Play Store):"
echo "   ./gradlew :app:bundleRelease"
echo "==========================================================="
