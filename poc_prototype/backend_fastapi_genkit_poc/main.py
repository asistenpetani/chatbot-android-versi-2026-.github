from fastapi import FastAPI, Depends, HTTPException
from pydantic import BaseModel
from genkit.core import genkit
from genkit.plugins.gemini import gemini
import firebase_admin
from firebase_admin import auth, credentials

# 1. Inisialisasi Firebase & Genkit
# Di production Cloud Run, ini akan otomatis menggunakan IAM Service Account (sa-compute-ai)
app_firebase = firebase_admin.initialize_app()
ai = genkit(plugins=[gemini()])

app = FastAPI(title="POC API Konsultasi Tani dengan Genkit")

# 2. Definisikan Struktur Data (Kontrak JSON antara Android & Python)
#    Penggunaan skema Pydantic ini adalah Kontrol Preventif terhadap Prompt Injection
class PermintaanKonsultasi(BaseModel):
    pesan_petani: str

class ResponKonsultasi(BaseModel):
    jawaban_ai: str

# 3. Middleware: Verifikasi Token Firebase (Garis Pertahanan Utama Server)
def verifikasi_token_android(authorization: str) -> dict:
    if not authorization or not authorization.startswith("Bearer "):
        raise HTTPException(status_code=401, detail="Token tidak valid/hilang")
    
    token = authorization.split("Bearer ")[1]
    try:
        # Validasi token ini dengan Firebase Auth
        decoded_token = auth.verify_id_token(token)
        return decoded_token # Mengembalikan data user (uid)
    except Exception as e:
        raise HTTPException(status_code=403, detail="Akses ditolak")

# 4. Buat Flow Genkit (Otak AI)
@ai.flow
async def alur_konsultasi_tani(pesan: str) -> str:
    # Model memproses pesan dari Android. Data dipisah dari instruksi sistem secara otomatis.
    respon = await ai.generate_async(
        model="gemini-1.5-flash",
        prompt=f"Kamu adalah pakar pertanian dari aplikasi Asisten Petani Kita. Jawab keluhan petani ini secara singkat dan tepat sasaran: {pesan}",
        # Bisa juga menggunakan system instructions di sini
    )
    return respon.text

# 5. Endpoint FastAPI yang menyambung ke Flow Genkit
@app.post("/api/konsultasi", response_model=ResponKonsultasi)
async def endpoint_konsultasi(
    request: PermintaanKonsultasi,
    # Inject middleware verifikasi token
    user: dict = Depends(verifikasi_token_android)
):
    """
    Android (Retrofit) akan menembak ke endpoint ini.
    Server akan memverifikasi token terlebih dahulu, barulah memproses Genkit.
    """
    # FastAPI menerima JSON dari Android, lalu memberikannya ke Genkit Flow
    hasil = await alur_konsultasi_tani(request.pesan_petani)
    
    # Kembalikan jawaban ke Android dalam format JSON yang bersih
    return ResponKonsultasi(jawaban_ai=hasil)

if __name__ == "__main__":
    import uvicorn
    uvicorn.run(app, host="0.0.0.0", port=8000)
