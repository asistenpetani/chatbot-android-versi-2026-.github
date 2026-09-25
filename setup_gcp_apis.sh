#!/bin/bash
# Skrip untuk mengaktifkan layanan Google Cloud yang dibutuhkan Asisten Petani
# Pemilik: indra.oktawa@gmail.com (atau indra.oktawan@gmail.com)
# Project ID: asistenpetani-ai-app

PROJECT_ID="asistenpetani-ai-app"

echo "Mempersiapkan infrastruktur Cloud untuk aplikasi Asisten Petani..."
echo "Project ID: $PROJECT_ID"

# Mengaktifkan seluruh API dalam satu perintah (tanpa apphosting yang menyebabkan error)
gcloud services enable \
  run.googleapis.com \
  cloudfunctions.googleapis.com \
  speech.googleapis.com \
  vision.googleapis.com \
  firebase.googleapis.com \
  firestore.googleapis.com \
  firebasehosting.googleapis.com \
  maps-android-backend.googleapis.com \
  storage.googleapis.com \
  storage-api.googleapis.com \
  billingbudgets.googleapis.com \
  playintegrity.googleapis.com \
  generativelanguage.googleapis.com \
  --project="$PROJECT_ID"

echo "Infrastruktur layanan Google Cloud berhasil diaktifkan!"
