from fastapi import FastAPI, HTTPException, File, UploadFile, Form
from pydantic import BaseModel
import os
import io
import asyncio
from PIL import Image
import google.generativeai as genai
from google.cloud import secretmanager
import google.auth
from tenacity import retry, stop_after_attempt, wait_exponential

# Inisialisasi Aplikasi Backend
app = FastAPI(
    title="Asisten Petani AI API",
    description="Backend Python untuk asistenpetani-ai-app di Google Cloud Run",
    version="1.0.0"
)

# Fungsi untuk mengambil secret dari Google Secret Manager
def get_secret(secret_id, version_id="latest"):
    env_key = os.environ.get(secret_id)
    try:
        # Dapatkan project_id bawaan dari environment (Cloud Run)
        _, project_id = google.auth.default()
        if not project_id:
            return env_key
            
        client = secretmanager.SecretManagerServiceClient()
        name = f"projects/{project_id}/secrets/{secret_id}/versions/{version_id}"
        response = client.access_secret_version(request={"name": name})
        return response.payload.data.decode("UTF-8")
    except Exception as e:
        print(f"Gagal mengambil secret {secret_id}: {e}")
        return env_key

# Konfigurasi Gemini API
GEMINI_API_KEY = get_secret("GEMINI_API_KEY")
if GEMINI_API_KEY:
    genai.configure(api_key=GEMINI_API_KEY)
    # Gunakan model gemini-3.5-flash untuk teks dan gambar
    model = genai.GenerativeModel('gemini-3.5-flash')
else:
    model = None

# Cetakan data (Model) yang akan diterima dari aplikasi Android
class ChatRequest(BaseModel):
    message: str
    location: str = "user" # Data tambahan opsional
    luas_lahan: str = "0 Ha"

# Fungsi retry untuk Gemini API
@retry(stop=stop_after_attempt(3), wait=wait_exponential(multiplier=1, min=2, max=10))
async def call_gemini_api(prompt, image=None):
    if image:
        # Menjalankan pemanggilan sinkron API di thread terpisah agar tidak nge-block FastAPI
        return await asyncio.to_thread(model.generate_content, [prompt, image])
    else:
        return await asyncio.to_thread(model.generate_content, prompt)

# Rute ini akan dipanggil oleh Firebase via /api/chat
@app.post("/api/chat")
async def get_ai_response(request: ChatRequest):
    try:
        user_msg = request.message
        
        # Jika Gemini API Key disetel, kita pakai Gemini
        if model:
            response = await call_gemini_api(user_msg)
            ai_reply = response.text
        else:
            # Sesuai data lokal (Fallback)
            ai_reply = f"tabe,horas,ampun! Saya disini untuk membantu Anda: '{user_msg}'. " \
                       f"Analisis untuk lahan seluas {request.luas_lahan} di {request.location} sedang diproses."
                   
        return {"status": "success", "response": ai_reply}
        
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))

# Rute Baru untuk Analisa Gambar dari Petani
@app.post("/api/analyze-multipart")
async def analyze_plant_image(
    # Menerima file gambar dari Android
    image: UploadFile = File(...), 
    # Menerima pesan teks tambahan
    prompt: str = Form("Tolong analisa penyakit pada tanaman di gambar ini dan berikan solusinya.")
):
    if not GEMINI_API_KEY or not model:
         raise HTTPException(status_code=500, detail="Mesin AI belum siap (API Key hilang).")
         
    try:
        # 1. Membaca isi file gambar yang dikirim Petani
        image_bytes = await image.read()
        
        # 2. Mengubah data mentah menjadi bentuk Gambar yang bisa dilihat AI
        img = Image.open(io.BytesIO(image_bytes))
        
        # 3. Mengirim TEKS dan GAMBAR secara bersamaan ke Gemini
        response = await call_gemini_api(prompt, img)
        
        # 4. Mengembalikan hasil analisa ke HP Petani
        return {
            "success": True, 
            "reply": response.text
        }
        
    except Exception as e:
        return {"success": False, "error": str(e)}

# Titik masuk untuk Google Cloud Run
if __name__ == "__main__":
    import uvicorn
    # Cloud Run secara otomatis memberikan port lewat environment variable 'PORT'
    port = int(os.environ.get("PORT", 8080))
    # Jalankan server
    uvicorn.run(app, host="0.0.0.0", port=port)
