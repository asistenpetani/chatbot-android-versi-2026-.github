#!/bin/bash
echo "==========================================================="
echo " SETUP GOOGLE CLOUD WORKLOAD IDENTITY FEDERATION"
echo "==========================================================="
PROJECT_ID="asistenpetani-ai-app"
GITHUB_REPO="indraoktawan/asistenpetani2026"

gcloud config set project $PROJECT_ID

echo "1. Mengaktifkan IAM Credentials API..."
gcloud services enable iamcredentials.googleapis.com

echo "2. Membuat Workload Identity Pool..."
gcloud iam workload-identity-pools create github-pool \
  --project="${PROJECT_ID}" \
  --location="global" \
  --display-name="GitHub Actions Pool"

echo "3. Membuat Workload Identity Provider..."
gcloud iam workload-identity-pools providers create-oidc github-provider \
  --project="${PROJECT_ID}" \
  --location="global" \
  --workload-identity-pool="github-pool" \
  --display-name="GitHub Actions Provider" \
  --attribute-mapping="google.subject=assertion.sub,attribute.actor=assertion.actor,attribute.repository=assertion.repository" \
  --issuer-uri="https://token.actions.githubusercontent.com"

echo "4. Membuat Service Account untuk GitHub Actions..."
gcloud iam service-accounts create github-actions-sa \
  --project="${PROJECT_ID}" \
  --display-name="GitHub Actions Service Account"

echo "5. Memberikan akses Workload Identity ke Service Account..."
gcloud iam service-accounts add-iam-policy-binding "github-actions-sa@${PROJECT_ID}.iam.gserviceaccount.com" \
  --project="${PROJECT_ID}" \
  --role="roles/iam.workloadIdentityUser" \
  --member="principalSet://iam.googleapis.com/projects/$(gcloud projects describe ${PROJECT_ID} --format='value(projectNumber)')/locations/global/workloadIdentityPools/github-pool/attribute.repository/${GITHUB_REPO}"

echo "==========================================================="
echo "PENGATURAN SELESAI!"
echo "Gunakan nilai berikut di GitHub Actions rahasia (secrets) Anda:"
echo "WORKLOAD_IDENTITY_PROVIDER: projects/$(gcloud projects describe ${PROJECT_ID} --format='value(projectNumber)')/locations/global/workloadIdentityPools/github-pool/providers/github-provider"
echo "SERVICE_ACCOUNT: github-actions-sa@${PROJECT_ID}.iam.gserviceaccount.com"
echo "==========================================================="
