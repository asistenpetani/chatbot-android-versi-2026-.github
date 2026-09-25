# Mengunci penyedia layanan Google Cloud
provider "google" {
  project = "asistenpetani-ai-app"
  region  = "asia-southeast2" # Regional terdekat (Jakarta) untuk efisiensi data & zero-cost egress
}

# Mengambil data akun billing yang aktif secara otomatis
data "google_billing_account" "account" {
  billing_account = "012345-6789AB-CDEF01" # SESUAIKAN: Ganti dengan ID Billing Anda
}

# Kode pembuat Alarm Batasan Anggaran
resource "google_billing_budget" "budget_aspek" {
  billing_account = data.google_billing_account.account.id
  display_name    = "Alarm Anggaran Asisten Petani"

  # Menentukan target jumlah mata uang (Contoh: USD 5 atau setara Rp 75.000)
  budget_filter {
    projects = ["projects/asistenpetani-ai-app"]
  }

  amount {
    specified_amount {
      currency_code = "USD"
      units         = "5" # Batasan maksimal $5 sebelum alarm berbunyi keras
    }
  }

  # Ambang batas pemicu email notifikasi
  threshold_rules {
    threshold_percent = 0.5 # Email dikirim saat tagihan mencapai 50% ($2.5)
  }
  threshold_rules {
    threshold_percent = 1.0 # Email dikirim saat tagihan mencapai 100% ($5.0)
  }
}

# ==============================================================================
# Cloud Storage Bucket: asistenpetani-rag-knowledge (Region: asia-southeast2 Jakarta)
# ==============================================================================
resource "google_storage_bucket" "rag_knowledge_bucket" {
  name                        = "asistenpetani-rag-knowledge"
  location                    = "asia-southeast2"
  storage_class               = "STANDARD"
  uniform_bucket_level_access = true

  versioning {
    enabled = true
  }

  lifecycle_rule {
    condition {
      num_newer_versions = 3
    }
    action {
      type = "Delete"
    }
  }

  cors {
    origin          = ["https://ais-dev-orbmbooub6hjfofqfik3f7-812982832201.asia-southeast1.run.app", "https://ais-pre-orbmbooub6hjfofqfik3f7-812982832201.asia-southeast1.run.app"]
    method          = ["GET", "HEAD"]
    response_header = ["*"]
    max_age_seconds = 3600
  }
}
