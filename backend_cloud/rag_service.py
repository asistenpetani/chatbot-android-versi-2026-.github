import os
import json
import logging
from typing import List, Dict, Any, Optional

logger = logging.getLogger("ASPEK-RAG")

# Konfigurasi Bucket Cloud Storage untuk RAG
RAG_BUCKET_NAME = os.environ.get("RAG_KNOWLEDGE_BUCKET", "asistenpetani-rag-knowledge")
REGION = os.environ.get("GCP_REGION", "asia-southeast2")
LOCAL_KNOWLEDGE_FALLBACK = "DATA pengetahuan_analisa_gambar.json"

class AgriculturalRAGService:
    """
    Layanan Retrieval-Augmented Generation (RAG) untuk Asisten Petani ASPEK.
    Mengambil dokumen dan basis pengetahuan agrikultur dari Google Cloud Storage Bucket
    'asistenpetani-rag-knowledge' (asia-southeast2) dan menyajikannya secara dinamis ke Prompt AI.
    """

    def __init__(self, bucket_name: str = RAG_BUCKET_NAME):
        self.bucket_name = bucket_name
        self.knowledge_base: List[Dict[str, Any]] = []
        self.fertilizer_data: Dict[str, Any] = {}
        self.is_initialized = False
        self.load_knowledge()

    def load_knowledge(self) -> bool:
        """Memuat data pengetahuan dari Cloud Storage bucket, fallback ke file lokal jika belum tersinkronisasi."""
        try:
            # 1. Mencoba mengambil dari Google Cloud Storage
            from google.cloud import storage
            client = storage.Client()
            bucket = client.bucket(self.bucket_name)
            
            # Cek apakah bucket ada dan memiliki berkas pengetahuan
            blobs = list(client.list_blobs(self.bucket_name, max_results=10))
            if blobs:
                logger.info(f"Berhasil terhubung ke Cloud Storage bucket '{self.bucket_name}' ({REGION}). Memuat dokumen...")
                loaded_items = []
                for blob in blobs:
                    if blob.name.endswith(".json"):
                        content = blob.download_as_text()
                        data = json.loads(content)
                        if isinstance(data, dict):
                            if "penyakit_tanaman" in data:
                                loaded_items.extend(data["penyakit_tanaman"])
                            if "pengetahuanPupuk" in data:
                                self.fertilizer_data = data["pengetahuanPupuk"]
                        elif isinstance(data, list):
                            loaded_items.extend(data)
                
                if loaded_items:
                    self.knowledge_base = loaded_items
                    self.is_initialized = True
                    logger.info(f"RAG: Memuat {len(self.knowledge_base)} entitas pengetahuan dari Cloud Storage!")
                    return True
        except Exception as e:
            logger.warning(f"GCS bucket '{self.bucket_name}' belum dapat diakses via SDK ({e}). Menggunakan cache lokal...")

        # 2. Fallback ke basis pengetahuan lokal yang tersedia
        try:
            fallback_path = os.path.join(os.path.dirname(__file__), LOCAL_KNOWLEDGE_FALLBACK)
            if not os.path.exists(fallback_path):
                fallback_path = LOCAL_KNOWLEDGE_FALLBACK

            if os.path.exists(fallback_path):
                with open(fallback_path, "r", encoding="utf-8") as f:
                    data = json.load(f)
                    if isinstance(data, dict):
                        self.knowledge_base = data.get("penyakit_tanaman", [])
                        self.fertilizer_data = data.get("pengetahuanPupuk", {})
                    elif isinstance(data, list):
                        self.knowledge_base = data
                self.is_initialized = True
                logger.info(f"RAG: Berhasil memuat {len(self.knowledge_base)} entitas dari fallback lokal.")
                return True
        except Exception as ex:
            logger.error(f"Gagal memuat pengetahuan lokal: {ex}")

        return False

    def retrieve_relevant_knowledge(self, query: str, top_k: int = 3) -> str:
        """
        Mencari dokumen pengetahuan pertanian yang paling relevan dengan kueri pengguna
        menggunakan pencocokan semantik/kata kunci berbobot (RAG context builder).
        """
        if not self.knowledge_base:
            self.load_knowledge()

        if not self.knowledge_base:
            return ""

        query_lower = query.lower()
        scored_docs = []

        for item in self.knowledge_base:
            score = 0
            tanaman = item.get("tanaman", "").lower()
            penyakit = item.get("nama_penyakit", "").lower()
            gejala = " ".join(item.get("gejala", [])).lower()
            solusi = " ".join(item.get("solusi", [])).lower()

            # Skor kecocokan tanaman (Bobot 5)
            if tanaman and tanaman in query_lower:
                score += 5

            # Skor kecocokan nama penyakit / hama (Bobot 10)
            for word in penyakit.split():
                if len(word) > 3 and word in query_lower:
                    score += 10

            # Skor kecocokan gejala (Bobot 3)
            query_words = [w for w in query_lower.split() if len(w) > 3]
            for qw in query_words:
                if qw in gejala:
                    score += 3
                if qw in solusi:
                    score += 2

            if score > 0:
                scored_docs.append((score, item))

        # Urutkan berdasarkan skor tertinggi
        scored_docs.sort(key=lambda x: x[0], reverse=True)
        top_results = [doc for score, doc in scored_docs[:top_k]]

        if not top_results:
            # Jika tidak ada pencocokan spesifik, ambil 2 panduan umum
            top_results = self.knowledge_base[:2]

        # Format output sebagai konteks RAG bersih untuk LLM
        rag_context = "=== DOKUMEN PENGETAHUAN PERTANIAN RESMI (RAG ASPEK) ===\n"
        for i, doc in enumerate(top_results, 1):
            tanaman = doc.get("tanaman", "Umum")
            nama_penyakit = doc.get("nama_penyakit", "-")
            gejala_list = "\n   - ".join(doc.get("gejala", ["Tidak ada data gejala"]))
            solusi_list = "\n   - ".join(doc.get("solusi", ["Tidak ada data solusi"]))
            
            rag_context += f"\n[Referensi {i}]: Tanaman {tanaman} - {nama_penyakit}\n"
            rag_context += f"Gejala Utama:\n   - {gejala_list}\n"
            rag_context += f"Solusi & Rekomendasi Teknis:\n   - {solusi_list}\n"

        rag_context += "\n======================================================\n"
        return rag_context

# Singleton RAG instance
rag_service = AgriculturalRAGService()
