import logging
from typing import Optional, Dict, Any
from rag_service import rag_service

logger = logging.getLogger("ASPEK-PromptBuilder")

class PromptBuilder:
    """
    Layanan terpusat (Centralized PromptBuilder) untuk menyusun System Prompt
    dan User Prompt yang menyuntikkan (dynamically injects) konteks pengetahuan agrikultur
    dari Cloud RAG (Retrieval-Augmented Generation) sebelum dikirim ke GenAI / Gemini API.
    """

    SYSTEM_BASE_IDENTITY = (
        "Anda adalah ASPEK (Asisten Petani Kita), asisten dan konsultan ahli agrikultur digital "
        "yang ramah, santun, objektif, dan solutif bagi para petani Indonesia. "
        "Selalu berikan rekomendasi berbasis bukti ilmiah agrikultur yang terstruktur, aman, "
        "dan dapat diterapkan secara praktis di lapangan."
    )

    @classmethod
    def build_chat_prompt(
        cls, 
        user_message: str, 
        crop_type: str = "Umum",
        farmer_profile: Optional[Dict[str, Any]] = None,
        top_k: int = 3
    ) -> str:
        """
        Menyusun prompt percakapan dengan injeksi konteks profil petani dan pengetahuan Cloud RAG.
        """
        # 1. Ambil pengetahuan RAG dari Cloud Storage
        rag_query = f"{crop_type} {user_message}"
        rag_context = rag_service.retrieve_relevant_knowledge(rag_query, top_k=top_k)

        # 2. Injeksi Profil Petani jika tersedia
        profile_context = ""
        if farmer_profile:
            nama = farmer_profile.get("nama", "Petani")
            lokasi = farmer_profile.get("lokasi", "Indonesia")
            komoditas = farmer_profile.get("komoditasUtama", crop_type)
            luas = farmer_profile.get("luasLahanHa", "-")
            umur = farmer_profile.get("umurTanamanHari", "-")
            profile_context = (
                f"\n[Konteks Profil Petani]\n"
                f"- Nama: {nama}\n"
                f"- Wilayah: {lokasi}\n"
                f"- Komoditas: {komoditas}\n"
                f"- Luas Lahan: {luas} Ha | Umur Tanaman: {umur} Hari\n"
            )

        # 3. Rakit Prompt Terstruktur
        prompt = (
            f"{cls.SYSTEM_BASE_IDENTITY}\n"
            f"{profile_context}\n"
            f"{rag_context}\n"
            f"Pertanyaan Petani: \"{user_message}\"\n\n"
            "Panduan Respon:\n"
            "- Gunakan istilah pertanian yang mudah dipahami petani lokal.\n"
            "- Sertakan panduan takaran atau dosis obat/pupuk yang presisi jika relevan.\n"
            "- Jika pertanyaan di luar konteks agrikultur, arahkan kembali dengan sopan ke topik pertanian."
        )
        return prompt

    @classmethod
    def build_vision_diagnosis_prompt(
        cls, 
        user_notes: str, 
        crop_type: str = "Umum",
        top_k: int = 3
    ) -> str:
        """
        Menyusun prompt visi komputer untuk diagnosis penyakit/hama tanaman dengan injeksi Cloud RAG.
        """
        rag_context = rag_service.retrieve_relevant_knowledge(f"{crop_type} {user_notes}", top_k=top_k)

        prompt = (
            f"{cls.SYSTEM_BASE_IDENTITY}\n\n"
            "TUGAS DIAGNOSIS VISUAL:\n"
            "Analisis gambar tanaman yang dilampirkan secara seksama. Identifikasi gejala kelainan, "
            "defisiensi hara, infeksi jamur/bakteri, atau serangan hama.\n\n"
            f"{rag_context}\n"
            f"Catatan/Keluhan Petani: \"{user_notes}\"\n\n"
            "SUSUN JAWABAN DENGAN STRUKTUR BERIKUT:\n"
            "1. 🌾 **Hasil Identifikasi Penyakit / Hama** (dan tingkat keparahan)\n"
            "2. 🔍 **Gejala Utama yang Teramati pada Visual**\n"
            "3. 💊 **Tindakan Penanganan Cepat (Organik / Kimiawi & Dosis)**\n"
            "4. 🛡️ **Pencegahan Jangka Panjang & Sanitasi Lahan**"
        )
        return prompt

    @classmethod
    def build_weather_advice_prompt(
        cls, 
        crop_type: str, 
        temp: float, 
        humidity: float, 
        condition: str = "Normal"
    ) -> str:
        """
        Menyusun prompt rekomendasi cuaca harian dan jadwal pemupukan/penyemprotan.
        """
        return (
            f"{cls.SYSTEM_BASE_IDENTITY}\n\n"
            f"Data Cuaca Saat Ini: Suhu {temp}°C, Kelembaban {humidity}%, Kondisi: {condition}.\n"
            f"Komoditas Tanaman: {crop_type}.\n"
            "Berikan 1-2 kalimat saran teknis yang sangat ringkas mengenai kelayakan pemupukan, "
            "penyiraman, atau penyemprotan pestisida hari ini."
        )

prompt_builder = PromptBuilder()
