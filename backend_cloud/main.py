from fastapi import FastAPI, UploadFile, File, Form, HTTPException
from pydantic import BaseModel
from typing import Optional, Dict, Any
import google.generativeai as genai
from PIL import Image
import io
import os
import logging
from rag_service import rag_service
from prompt_builder import prompt_builder

logging.basicConfig(level=logging.INFO)
logger = logging.getLogger("ASPEK-API")

app = FastAPI(
    title="ASPEK AI Backend API (Cloud RAG & Centralized PromptBuilder)", 
    version="2.1.0",
    description="Backend microservice providing dynamic GenAI RAG context injection and Computer Vision agricultural diagnostics."
)

# Konfigurasi Model & Kunci Gemini dari Environment Variable
api_key = os.environ.get("GEMINI_API_KEY", "")
MODEL_FAST = os.environ.get("MODEL_FAST", "gemini-2.0-flash")
MODEL_PRO = os.environ.get("MODEL_PRO", "gemini-1.5-pro")

if api_key:
    genai.configure(api_key=api_key)

class ChatRequest(BaseModel):
    message: str
    is_deep_thinking: bool = False
    crop_type: str = "Umum"
    farmer_profile: Optional[Dict[str, Any]] = None

class WeatherAnalysisRequest(BaseModel):
    latitude: float
    longitude: float
    cropType: str

@app.get("/")
async def root():
    return {
        "status": "online",
        "service": "ASPEK AI Backend API",
        "architecture": "Cloud RAG + Centralized PromptBuilder",
        "region": "asia-southeast2",
        "rag_bucket": rag_service.bucket_name,
        "rag_items_loaded": len(rag_service.knowledge_base),
        "models": {
            "fast": MODEL_FAST,
            "pro": MODEL_PRO
        }
    }

@app.post("/api/analisa-gambar-aman")
async def analisa_gambar_dengan_reencoding_dan_rag(
    pesan: str = Form(...),
    crop_type: str = Form("Umum"),
    file: UploadFile = File(...)
):
    """
    Analisis gambar tanaman dengan pembersihan metadata (Re-encoding)
    dan penambahan konteks pengetahuan agrikultur dinamis melalui PromptBuilder + Cloud RAG.
    """
    try:
        # 1. Membaca biner file gambar
        input_bytes = await file.read()
        
        # 2. Re-encoding gambar untuk membersihkan EXIF dan Trojan metadata
        image = Image.open(io.BytesIO(input_bytes))
        if image.mode in ("RGBA", "P"):
            image = image.convert("RGB")
            
        clean_buffer = io.BytesIO()
        image.save(clean_buffer, format="JPEG", quality=85)
        clean_image_bytes = clean_buffer.getvalue()

        # 3. Menyiapkan payload part gambar untuk GenAI Vision
        part_gambar = {
            "mime_type": "image/jpeg",
            "data": clean_image_bytes
        }

        # 4. Bangun prompt terpusat via PromptBuilder dengan injeksi Cloud RAG
        prompt_sistem = prompt_builder.build_vision_diagnosis_prompt(
            user_notes=pesan,
            crop_type=crop_type,
            top_k=3
        )

        model_name = MODEL_FAST
        model = genai.GenerativeModel(model_name)
        respon = model.generate_content([prompt_sistem, part_gambar])

        return {
            "jawaban": respon.text,
            "rag_sourced": True,
            "model_used": model_name
        }

    except Exception as e:
        logger.error(f"Error pada /api/analisa-gambar-aman: {e}")
        raise HTTPException(status_code=400, detail=f"Gagal memproses diagnosis gambar: {str(e)}")

@app.post("/api/chat")
async def get_ai_response_with_rag(request: ChatRequest):
    """
    Chatbot agrikultur interaktif dengan prompt dinamis dari PromptBuilder dan Cloud RAG.
    """
    try:
        # 1. Bangun prompt melalui PromptBuilder terpusat
        prompt = prompt_builder.build_chat_prompt(
            user_message=request.message,
            crop_type=request.crop_type,
            farmer_profile=request.farmer_profile,
            top_k=3
        )

        # 2. Tentukan model AI (Fast / Pro)
        target_model = MODEL_PRO if request.is_deep_thinking else MODEL_FAST
        model = genai.GenerativeModel(target_model)
        respon = model.generate_content(prompt)
        
        return {
            "response": respon.text,
            "rag_sourced": True,
            "model_used": target_model
        }
    except Exception as e:
        logger.error(f"Error pada /api/chat: {e}")
        raise HTTPException(status_code=500, detail=str(e))

@app.post("/api/weather")
async def get_weather_advice(request: WeatherAnalysisRequest):
    """
    Menghasilkan saran agronomi harian berdasarkan cuaca dan tanaman petani.
    """
    try:
        advice = "Kondisi cuaca mendukung aktivitas pertanian hari ini."
        if api_key:
            model = genai.GenerativeModel(MODEL_FAST)
            prompt = prompt_builder.build_weather_advice_prompt(
                crop_type=request.cropType,
                temp=28.0,
                humidity=65.0,
                condition="Cerah Berawan"
            )
            try:
                respon = model.generate_content(prompt)
                advice = respon.text.strip()
            except Exception as ex:
                logger.warning(f"Gagal generate weather advice: {ex}")
        
        return {
            "temperature": 28.0,
            "humidity": 65.0,
            "weatherCode": 1,
            "advice": advice
        }
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))

@app.post("/api/rag/sync")
async def sync_rag_knowledge():
    """
    Sinkronisasi ulang basis pengetahuan RAG dari Cloud Storage 'asistenpetani-rag-knowledge'.
    """
    success = rag_service.load_knowledge()
    return {
        "status": "success" if success else "fallback_local",
        "bucket": rag_service.bucket_name,
        "total_items": len(rag_service.knowledge_base),
        "message": "Sinkronisasi pengetahuan RAG berhasil diperbarui dari Cloud Storage." if success else "Memuat data dari cache lokal."
    }
