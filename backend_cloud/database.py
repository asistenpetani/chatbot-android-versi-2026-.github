import os
import logging
from sqlalchemy import create_engine
from sqlalchemy.ext.declarative import declarative_base
from sqlalchemy.orm import sessionmaker

logger = logging.getLogger("ASPEK-Database")

# Ambil URL koneksi PostgreSQL dari environment variable
# Format: postgresql://username:password@host:port/database
DATABASE_URL = os.getenv("DATABASE_URL")

if not DATABASE_URL:
    # Membangun URL koneksi secara manual jika parameter individu diberikan
    pg_user = os.getenv("POSTGRES_USER", "aspek_user")
    pg_pass = os.getenv("POSTGRES_PASSWORD", "secure_password_123")
    pg_host = os.getenv("POSTGRES_HOST", "localhost")
    pg_port = os.getenv("POSTGRES_PORT", "5432")
    pg_db = os.getenv("POSTGRES_DB", "aspek_db")
    DATABASE_URL = f"postgresql://{pg_user}:{pg_pass}@{pg_host}:{pg_port}/{pg_db}"

logger.info(f"Menginisialisasi koneksi basis data PostgreSQL...")

try:
    # Konfigurasi Koneksi Engine PostgreSQL yang sangat skalabel
    # - pool_size: jumlah koneksi aktif yang dijaga di dalam pool
    # - max_overflow: jumlah koneksi tambahan maksimum jika pool_size penuh
    # - pool_recycle: masa aktif koneksi dalam detik sebelum di-refresh (mencegah timeout koneksi mati)
    # - pool_pre_ping: otomatis menguji koneksi sebelum digunakan (menghindari error 'connection lost')
    engine = create_engine(
        DATABASE_URL,
        pool_size=10,
        max_overflow=20,
        pool_recycle=1800,
        pool_pre_ping=True
    )
    
    # SessionLocal untuk berinteraksi dengan database
    SessionLocal = sessionmaker(autocommit=False, autoflush=False, bind=engine)
    
    # Base class untuk ORM Model
    Base = declarative_base()
    
    logger.info("Koneksi Engine PostgreSQL berhasil diinisialisasi!")
except Exception as e:
    logger.error(f"Gagal membuat engine koneksi basis data PostgreSQL: {e}")
    engine = None
    SessionLocal = None
    Base = None

# Dependency untuk mendapatkan DB Session di FastAPI
def get_db():
    if SessionLocal is None:
        raise RuntimeError("Engine basis data PostgreSQL tidak terkonfigurasi atau tidak aktif.")
    db = SessionLocal()
    try:
        yield db
    finally:
        db.close()
