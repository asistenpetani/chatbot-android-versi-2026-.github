#!/bin/bash
# ==============================================================================
# ASPEK AI - Script Proteksi Egress & Nol-Biaya Transfer Data GCP
# Proyek: asistenpetani-ai-app
# Akun Pemilik: indra.oktawan@gmail.com
# ==============================================================================

set -e

PROJECT_ID="asistenpetani-ai-app"
REGION="asia-southeast2" # Jakarta (Colocation)

echo "======================================================================="
echo "Mengaktifkan Proteksi Egress & Penghematan Biaya Jaringan untuk: $PROJECT_ID"
echo "======================================================================="

# 1. Konfigurasi Proyek Aktif
gcloud config set project "$PROJECT_ID"

# 2. Turunkan Network Service Tiers ke STANDARD
echo "Mengatur Network Service Tier proyek menjadi STANDARD..."
gcloud compute project-info set-default-network-tier --default-network-tier=STANDARD || true

# 3. Verifikasi Billing Account ID
BILLING_ACCOUNT_ID=$(gcloud beta billing projects describe "$PROJECT_ID" --format="value(billingAccountName)" 2>/dev/null | sed 's/billingAccounts\///' || true)

if [ -n "$BILLING_ACCOUNT_ID" ]; then
    echo "Billing Account terdeteksi: $BILLING_ACCOUNT_ID"
    echo "Membuat Budget Alert Rp 100.000 (Peringatan Dini Kebocoran Egress ke indra.oktawan@gmail.com)..."
    gcloud billing budgets create \
        --billing-account="$BILLING_ACCOUNT_ID" \
        --display-name="Budget Alert Proteksi Egress ASPEK Rp 100k" \
        --budget-amount=100000IDR \
        --threshold-rule=percent=0.5,basis=current-spend \
        --threshold-rule=percent=0.9,basis=current-spend \
        --threshold-rule=percent=1.0,basis=current-spend \
        --all-updates-rule-pubsub-topic="" \
        2>/dev/null || echo "Budget alert dapat disetel di Konsol GCP -> Billing -> Budgets & Alerts."
fi

echo "======================================================================="
echo "Petunjuk Verifikasi di GCP Console:"
echo "1. Colocation (asia-southeast2 Jakarta):"
echo "   - Cloud Run, Firestore, dan Bucket Storage berada di region yang sama."
echo "2. Firebase Hosting CDN:"
echo "   - Seluruh aset statis dilayani melalui CDN Global Google (gratis cache)."
echo "3. Network Service Tier:"
echo "   - Default Project Network Tier: STANDARD."
echo "4. Client Optimization:"
echo "   - Kompresi gambar WebP (< 150KB) & Firestore Query limit(10) aktif di APK."
echo "======================================================================="
