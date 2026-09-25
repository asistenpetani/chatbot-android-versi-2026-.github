#!/bin/bash
echo "==========================================================="
echo "ASISTEN PETANI - GCP & FIREBASE SHA DEPLOYMENT SETUP"
echo "==========================================================="
echo ""
echo "Mengambil SHA-1 dan SHA-256 dari Debug Keystore..."

# Create debug keystore if it doesn't exist
if [ ! -f ~/.android/debug.keystore ]; then
    echo "Membuat debug.keystore karena belum ada..."
    mkdir -p ~/.android
    keytool -genkey -v -keystore ~/.android/debug.keystore -alias androiddebugkey -storepass android -keypass android -keyalg RSA -keysize 2048 -validity 10000 -dname "C=US, O=Android, CN=Android Debug" > /dev/null 2>&1
fi

keytool -list -v -keystore ~/.android/debug.keystore -alias androiddebugkey -storepass android -keypass android | grep -E "SHA1:|SHA256:"

echo ""
echo "Langkah selanjutnya di GCP / Firebase Console:"
echo "1. Buka console.firebase.google.com -> Pengaturan Project (Project Settings)"
echo "2. Tambahkan Sidik Jari (Fingerprint) SHA-1 dan SHA-256 di atas ke dalam aplikasi Android (com.asistenpetani)."
echo "3. Buka console.cloud.google.com -> APIs & Services -> Credentials"
echo "4. Daftarkan API Key untuk 'Maps SDK for Android' dan batasi menggunakan SHA-1 di atas serta package: com.asistenpetani"
echo "5. Unduh file google-services.json yang baru dan letakkan di folder app/"
echo "6. Jalankan perintah berikut di Cloud Shell untuk menyimpan API Key ke dalam sistem aplikasi:"
echo "   echo \"MAPS_API_KEY=KUNCI_API_ANDA\" >> .env"
echo "   echo \"GEMINI_API_KEY=KUNCI_API_ANDA\" >> .env"
echo ""
echo "Untuk Deployment ke Google Play Console (Lewat GCP Terminal):"
echo "1. Build Android App Bundle (.aab) dengan menjalankan:"
echo "   ./gradlew :app:bundleRelease"
echo "2. Upload file AAB (berada di app/build/outputs/bundle/release/) ke Play Console."
echo "3. Setelah Play Console membuatkan kunci rilis (App Signing by Google Play),"
echo "   salin SHA-1 rilis tersebut dan daftarkan juga ke Firebase dan Google Cloud Console (Maps API)."
echo "==========================================================="
