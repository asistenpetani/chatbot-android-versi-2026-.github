#!/bin/bash
# Skrip POC untuk setup Service Account dengan prinsip Least Privilege

PROJECT_ID="asistenpetani-ai-app"

echo "=== Memulai Setup IAM (Proof of Concept) ==="

# 1. SA Backend AI (Hanya bisa akses Vertex AI Genkit & Secret Manager)
gcloud iam service-accounts create sa-compute-ai --display-name="SA Compute and AI"
gcloud projects add-iam-policy-binding $PROJECT_ID \
    --member="serviceAccount:sa-compute-ai@$PROJECT_ID.iam.gserviceaccount.com" \
    --role="roles/aiplatform.user"

# 2. SA Karantina & Scanner (Hanya bisa atur Storage untuk memindai Malware/Trojan)
gcloud iam service-accounts create sa-security-scanner --display-name="SA Security Scanner"
gcloud projects add-iam-policy-binding $PROJECT_ID \
    --member="serviceAccount:sa-security-scanner@$PROJECT_ID.iam.gserviceaccount.com" \
    --role="roles/storage.objectAdmin"

echo "=== Setup IAM Selesai ==="
