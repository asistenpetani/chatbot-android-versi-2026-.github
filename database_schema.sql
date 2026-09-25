-- ============================================================================
-- SKEMA BASIS DATA POSTGRESQL YANG SKALABEL - APLIKASI ASISTEN PETANI (ASPEK)
-- ============================================================================
-- Dirancang untuk keandalan, integritas data tinggi, kueri cepat, dan skalabilitas
-- menggunakan indeks B-Tree/GIN, foreign keys, serta strategi partisi data historis.
-- ============================================================================

-- Ekstensi PostgreSQL untuk UUID dan data spasial (GIS) jika diperlukan di masa depan
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
CREATE EXTENSION IF NOT EXISTS "btree_gist";

-- 1. TABEL PENGGUNA / PETANI (Farmers)
CREATE TABLE IF NOT EXISTS farmers (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    phone_number VARCHAR(20) UNIQUE NOT NULL,
    full_name VARCHAR(100) NOT NULL,
    email VARCHAR(100) UNIQUE,
    region VARCHAR(100), -- Sulawesi Tenggara, Sunda, dll.
    district VARCHAR(100),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    subscription_status VARCHAR(20) DEFAULT 'FREE', -- FREE, PREMIUM, ADMIN
    subscription_expires_at TIMESTAMP WITH TIME ZONE
);

-- Indeks untuk pencarian cepat berdasarkan nomor telepon dan status langganan
CREATE INDEX IF NOT EXISTS idx_farmers_phone ON farmers(phone_number);
CREATE INDEX IF NOT EXISTS idx_farmers_subscription ON farmers(subscription_status);

-- 2. TABEL LAHAN & PETAK TANAMAN (Farm Plots)
CREATE TABLE IF NOT EXISTS farm_plots (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    farmer_id UUID NOT NULL REFERENCES farmers(id) ON DELETE CASCADE,
    plot_name VARCHAR(100) NOT NULL,
    crop_type VARCHAR(50) NOT NULL, -- Kakao, Padi, Jagung, dll.
    soil_type VARCHAR(50), -- Liat, Pasir, Vulkanik, dll.
    latitude DOUBLE PRECISION,
    longitude DOUBLE PRECISION,
    area_hectares NUMERIC(5, 2) NOT NULL,
    planting_date DATE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Indeks asing untuk kueri relasional cepat
CREATE INDEX IF NOT EXISTS idx_plots_farmer ON farm_plots(farmer_id);
CREATE INDEX IF NOT EXISTS idx_plots_crop ON farm_plots(crop_type);

-- 3. TABEL ANALISA KANDUNGAN HARA TANAH (Soil Analyses)
CREATE TABLE IF NOT EXISTS soil_analyses (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    plot_id UUID NOT NULL REFERENCES farm_plots(id) ON DELETE CASCADE,
    ph_level NUMERIC(3, 1) NOT NULL, -- Derajat Keasaman Tanah
    nitrogen_ppm NUMERIC(6, 2),
    phosphorus_ppm NUMERIC(6, 2),
    potassium_ppm NUMERIC(6, 2),
    organic_matter_pct NUMERIC(4, 2),
    moisture_pct NUMERIC(5, 2),
    analyzed_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Indeks untuk pencarian hara berdasarkan lahan
CREATE INDEX IF NOT EXISTS idx_soil_plot ON soil_analyses(plot_id);

-- 4. TABEL RIWAYAT DIAGNOSA GEJALA DAUN / TANAMAN (Crop Diagnoses)
-- Dibuat partisi opsional berdasarkan waktu pembuatan jika data berukuran terabyte (TB)
CREATE TABLE IF NOT EXISTS crop_diagnoses (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    farmer_id UUID NOT NULL REFERENCES farmers(id) ON DELETE CASCADE,
    plot_id UUID REFERENCES farm_plots(id) ON DELETE SET NULL,
    image_url TEXT,
    visual_symptoms TEXT,
    detected_disease VARCHAR(150),
    ai_recommendation TEXT NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Indeks komposit untuk memuat riwayat diagnosa per petani dengan cepat
CREATE INDEX IF NOT EXISTS idx_diagnoses_farmer_date ON crop_diagnoses(farmer_id, created_at DESC);

-- 5. TABEL TRANSAKSI PEMBAYARAN LANGGANAN & LOG FCM (Subscriptions & Payments)
CREATE TABLE IF NOT EXISTS subscriptions_payments (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    farmer_id UUID NOT NULL REFERENCES farmers(id) ON DELETE CASCADE,
    transaction_id VARCHAR(100) UNIQUE NOT NULL,
    amount NUMERIC(12, 2) NOT NULL, -- Nominal Rupiah
    payment_method VARCHAR(50) NOT NULL, -- Midtrans, Transfer, GooglePlay, dll.
    payment_status VARCHAR(20) NOT NULL, -- PENDING, COMPLETED, FAILED
    duration_days INT DEFAULT 30,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_payments_farmer ON subscriptions_payments(farmer_id);
CREATE INDEX IF NOT EXISTS idx_payments_status ON subscriptions_payments(payment_status);

-- 6. TABEL FCM TOKENS UNTUK PENGIRIMAN NOTIFIKASI DAN KEAMANAN TRANSAKSI
CREATE TABLE IF NOT EXISTS fcm_tokens (
    farmer_id UUID NOT NULL REFERENCES farmers(id) ON DELETE CASCADE,
    token_value TEXT UNIQUE NOT NULL,
    device_model VARCHAR(100),
    last_active TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (farmer_id, token_value)
);

-- ============================================================================
-- STRATEGI PENGOPTIMALAN SKALABILITAS (CONTOH VIEW DAN TRIGGER)
-- ============================================================================

-- Fungsi otomatis untuk memperbarui kolom 'updated_at' secara dinamis
CREATE OR REPLACE FUNCTION update_modified_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Memasang trigger ke tabel farmers dan farm_plots
CREATE TRIGGER update_farmer_modtime
    BEFORE UPDATE ON farmers
    FOR EACH ROW
    EXECUTE FUNCTION update_modified_column();

CREATE TRIGGER update_plot_modtime
    BEFORE UPDATE ON farm_plots
    FOR EACH ROW
    EXECUTE FUNCTION update_modified_column();

-- View untuk dashboard statistik agraris yang teragregasi secara efisien
CREATE OR REPLACE VIEW view_farmer_agricultural_summary AS
SELECT 
    f.id as farmer_id,
    f.full_name,
    f.subscription_status,
    COUNT(p.id) as total_plots,
    COALESCE(SUM(p.area_hectares), 0) as total_area_hectares,
    MAX(sa.analyzed_at) as last_soil_test
FROM farmers f
LEFT JOIN farm_plots p ON f.id = p.farmer_id
LEFT JOIN soil_analyses sa ON p.id = sa.plot_id
GROUP BY f.id, f.full_name, f.subscription_status;
