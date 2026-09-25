#!/bin/bash

# ==============================================================================
# Konfigurasi Utama & Regional Colocation (asia-southeast2 Jakarta)
# ==============================================================================
MASUKKAN_GEMINI_KEY_ASLI_DISINI="AIzaSyDkXVke6HbyzFbCLnLBuKqm21GAGGjV_Gc"

PROJECT_ID="asistenpetani-ai-app"
REGION="asia-southeast2" # Kunci ke Jakarta (asia-southeast2) untuk zero-cost colocation
SERVICE_NAME="asistenpetani-ai-app-backend-service"
ARTIFACT_REGISTRY_REPO="aspek-docker-repo"
RAG_BUCKET="asistenpetani-rag-knowledge"

# Nama rahasia di Secret Manager
SECRET_NAME_GEMINI="GEMINI_API_KEY" 
SECRET_NAME_FIREBASE="FIREBASE_API_KEY"

echo "======================================================================="
echo "Memulai Setup GCP & Cloud RAG untuk $PROJECT_ID ($REGION)..."
echo "======================================================================="

# 1. Set Project
echo "1. Mengatur project aktif..."
gcloud config set project $PROJECT_ID

# 2. Mengaktifkan API Layanan GCP yang Dibutuhkan
echo "2. Mengaktifkan API Layanan GCP..."
gcloud services enable \
   run.googleapis.com \
   secretmanager.googleapis.com \
   artifactregistry.googleapis.com \
   cloudbuild.googleapis.com \
   firestore.googleapis.com \
   storage.googleapis.com \
   iam.googleapis.com

# 3. Pengaturan Akses & IAM Roles
echo "3. Menyiapkan Peran IAM untuk Service Account..."
PROJECT_NUMBER=$(gcloud projects describe $PROJECT_ID --format="value(projectNumber)")
COMPUTE_SA="${PROJECT_NUMBER}-compute@developer.gserviceaccount.com"
FIREBASE_APP_HOSTING_SA="firebase-app-hosting-compute@$PROJECT_ID.iam.gserviceaccount.com"
GOOGLE_AUTH_MANAGER_SA="googleauthmanager@$PROJECT_ID.iam.gserviceaccount.com"
ASPEK_RUNTIME_SA="aspek-runtime-cloudrun@$PROJECT_ID.iam.gserviceaccount.com"

# Membuat Service Account Kustom
gcloud iam service-accounts create googleauthmanager \
    --display-name="GoogleAuthManager" --quiet 2>/dev/null || true

gcloud iam service-accounts create firebase-app-hosting-compute \
    --display-name="Firebase App Hosting Compute Runner" --quiet 2>/dev/null || true

gcloud iam service-accounts create aspek-runtime-cloudrun \
    --display-name="ASPEK Runtime Cloud Run SA" --quiet 2>/dev/null || true

# Akses Membaca Rahasia di Secret Manager
gcloud projects add-iam-policy-binding $PROJECT_ID \
    --member="serviceAccount:${COMPUTE_SA}" \
    --role="roles/secretmanager.secretAccessor" \
    --condition=None --quiet

gcloud projects add-iam-policy-binding $PROJECT_ID \
    --member="serviceAccount:${ASPEK_RUNTIME_SA}" \
    --role="roles/secretmanager.secretAccessor" \
    --condition=None --quiet

# Akses Cloud Storage (Bucket RAG Knowledge & Object Viewer)
gcloud projects add-iam-policy-binding $PROJECT_ID \
    --member="serviceAccount:${COMPUTE_SA}" \
    --role="roles/storage.objectAdmin" \
    --condition=None --quiet

gcloud projects add-iam-policy-binding $PROJECT_ID \
    --member="serviceAccount:${ASPEK_RUNTIME_SA}" \
    --role="roles/storage.objectViewer" \
    --condition=None --quiet

# Akses Artifact Registry, Logging, Firestore
gcloud projects add-iam-policy-binding $PROJECT_ID \
    --member="serviceAccount:${COMPUTE_SA}" \
    --role="roles/artifactregistry.writer" \
    --condition=None --quiet

gcloud projects add-iam-policy-binding $PROJECT_ID \
    --member="serviceAccount:${COMPUTE_SA}" \
    --role="roles/logging.logWriter" \
    --condition=None --quiet

gcloud projects add-iam-policy-binding $PROJECT_ID \
    --member="serviceAccount:${ASPEK_RUNTIME_SA}" \
    --role="roles/datastore.user" \
    --condition=None --quiet

# 4. Membuat Bucket Cloud Storage Khusus RAG Knowledge (asia-southeast2)
echo "4. Membuat Bucket Cloud Storage untuk RAG Knowledge ($RAG_BUCKET di $REGION)..."
if ! gcloud storage buckets describe gs://$RAG_BUCKET &>/dev/null; then
    gcloud storage buckets create gs://$RAG_BUCKET \
        --location=$REGION \
        --default-storage-class=STANDARD \
        --uniform-bucket-level-access \
        --project=$PROJECT_ID
    echo "Bucket gs://$RAG_BUCKET berhasil dibuat di $REGION."
else
    echo "Bucket gs://$RAG_BUCKET sudah ada."
fi

# 5. Mengunggah Data Pengetahuan Agrikultur Awal ke Bucket RAG
echo "5. Mengunggah dokumen basis pengetahuan ke gs://$RAG_BUCKET/..."
if [ -f "./backend_cloud/DATA pengetahuan_analisa_gambar.json" ]; then
    gcloud storage cp "./backend_cloud/DATA pengetahuan_analisa_gambar.json" "gs://$RAG_BUCKET/pengetahuan_analisa_gambar.json" --project=$PROJECT_ID 2>/dev/null || true
    echo "Dokumen pengetahuan berhasil disinkronkan ke Cloud Storage RAG."
fi

# 6. Manajemen Kunci Rahasia
echo "6. Menyimpan Secret Key di Secret Manager..."
gcloud secrets delete $SECRET_NAME_GEMINI --quiet 2>/dev/null || true
gcloud secrets delete $SECRET_NAME_FIREBASE --quiet 2>/dev/null || true

echo -n "$MASUKKAN_GEMINI_KEY_ASLI_DISINI" | gcloud secrets create $SECRET_NAME_GEMINI --replication-policy="automatic" --data-file=-
echo -n "BOvXPl6h5NOoSKxj5_vnTLA5zd3alc-f5TrIOXIBfyMQrJf4-SM-FdiPLtCF9QPY9uvYx0GAomxANkDdShdY6Lc" | gcloud secrets create $SECRET_NAME_FIREBASE --replication-policy="automatic" --data-file=-

# 7. Repositori Artifact Registry
echo "7. Menyiapkan Artifact Registry..."
if ! gcloud artifacts repositories describe $ARTIFACT_REGISTRY_REPO --location=$REGION &>/dev/null; then
    gcloud artifacts repositories create $ARTIFACT_REGISTRY_REPO --repository-format=docker --location=$REGION --description="Repositori Docker untuk Layanan Mikro Backend Python ASPEK"
fi

gcloud auth configure-docker $REGION-docker.pkg.dev --quiet

# 8. Build dan Deploy ke Cloud Run dengan Integrasi Cloud Storage RAG
echo "8. Melakukan Build dan Deploy ke Cloud Run dengan RAG..."
gcloud builds submit --tag $REGION-docker.pkg.dev/$PROJECT_ID/$ARTIFACT_REGISTRY_REPO/$SERVICE_NAME:latest ./backend_cloud/

gcloud run deploy $SERVICE_NAME \
    --image $REGION-docker.pkg.dev/$PROJECT_ID/$ARTIFACT_REGISTRY_REPO/$SERVICE_NAME:latest \
    --platform managed \
    --region $REGION \
    --allow-unauthenticated \
    --service-account $ASPEK_RUNTIME_SA \
    --set-env-vars RAG_KNOWLEDGE_BUCKET=$RAG_BUCKET,GCP_REGION=$REGION,MODEL_FAST=gemini-2.0-flash,MODEL_PRO=gemini-1.5-pro \
    --update-secrets=GEMINI_API_KEY=${SECRET_NAME_GEMINI}:latest,FIREBASE_API_KEY=${SECRET_NAME_FIREBASE}:latest

# 9. Workload Identity Federation
echo "9. Menyiapkan Workload Identity Pool..."
if ! gcloud iam workload-identity-pools describe "aspek-github-pool" --location="global" &>/dev/null; then
    gcloud iam workload-identity-pools create "aspek-github-pool" --location="global" --description="Pool untuk GitHub Actions CI/CD" --display-name="ASPEK GitHub Pool"
fi

# 10. Konfigurasi Network Service Tier ke STANDARD
echo "10. Mengatur Network Service Tier default proyek ke STANDARD..."
gcloud compute project-info set-default-network-tier --default-network-tier=STANDARD --quiet 2>/dev/null || true

echo "======================================================================="
echo "Setup Cloud RAG & Backend ASPEK Berhasil:"
echo "1. Bucket RAG: gs://$RAG_BUCKET (Region: $REGION)"
echo "2. Colocation: Zero-Cost Egress aktif antar Cloud Run & Storage Bucket"
echo "3. Backend Service: Terhubung ke Cloud Storage RAG + Dynamic Prompting"
echo "======================================================================="
