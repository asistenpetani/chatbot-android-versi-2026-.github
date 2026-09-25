#!/bin/bash
set -e

PROJECT_ID="asistenpetani-ai-app"
REGION="asia-southeast1"

echo "🔧 Memulai pengaturan Cloud Shell untuk proyek: $PROJECT_ID..."

# Set project
echo "📌 Menyetel project default ke $PROJECT_ID..."
gcloud config set project $PROJECT_ID

# Set region
echo "📌 Menyetel region default ke $REGION..."
gcloud config set compute/region $REGION

# Enable APIs
echo "🚀 Mengaktifkan API yang diperlukan (ini mungkin memakan waktu beberapa menit)..."
gcloud services enable \
  iamcredentials.googleapis.com \
  iam.googleapis.com \
  cloudbuild.googleapis.com \
  run.googleapis.com \
  artifactregistry.googleapis.com \
  --project=$PROJECT_ID

echo "✅ Pengaturan Cloud Shell selesai!"
