import re

with open('backend_cloud/main.py', 'r') as f:
    content = f.read()

import_req = "import requests\n"
if "import requests" not in content:
    content = content.replace("import os\n", "import os\n" + import_req)

models = """
class WeatherAnalysisRequest(BaseModel):
    latitude: float
    longitude: float
    crop_type: str = "Padi"

class WeatherAnalysisResponse(BaseModel):
    temperature: float
    humidity: float
    weather_code: int
    advice: str
"""

if "class WeatherAnalysisRequest" not in content:
    content = content.replace("class ChatRequest(BaseModel):", models + "\nclass ChatRequest(BaseModel):")

endpoint = """
@app.post("/api/weather-advice", response_model=WeatherAnalysisResponse)
async def get_weather_advice(req: WeatherAnalysisRequest):
    try:
        # Fetch weather from Open-Meteo
        om_url = f"https://api.open-meteo.com/v1/forecast?latitude={req.latitude}&longitude={req.longitude}&current_weather=true"
        response = requests.get(om_url)
        response.raise_for_status()
        data = response.json()
        
        current = data.get("current_weather", {})
        temp = current.get("temperature", 0.0)
        code = current.get("weathercode", 0)
        # Approximate humidity if not provided
        humidity = 80.0
        
        if code in [0, 1, 2, 3]:
            cuaca_desc = "Cerah hingga berawan"
        elif code in [45, 48]:
            cuaca_desc = "Berkabut"
        elif code in [51, 53, 55, 61, 63, 65, 80, 81, 82]:
            cuaca_desc = "Hujan"
        elif code in [71, 73, 75, 85, 86]:
            cuaca_desc = "Salju"
        elif code in [95, 96, 99]:
            cuaca_desc = "Badai petir"
        else:
            cuaca_desc = "Tidak diketahui"
        
        # Analyze with Gemini
        prompt = (f"Saya menanam {req.crop_type}. Saat ini cuaca di lokasi saya adalah {cuaca_desc} dengan "
                  f"suhu {temp}°C (kode cuaca WMO {code}, perkiraan kelembapan {humidity}%). "
                  "Berikan 1-2 kalimat saran pertanian proaktif yang sangat singkat, padat, dan praktis untuk menjaga tanaman saya dari kondisi cuaca ini.")
        
        model = genai.GenerativeModel("gemini-1.5-flash")
        result = model.generate_content(prompt)
        advice = result.text.strip()
        
        return WeatherAnalysisResponse(
            temperature=temp,
            humidity=humidity,
            weather_code=code,
            advice=advice
        )
    except Exception as e:
        logger.error(f"Error in weather advice: {e}")
        raise HTTPException(status_code=500, detail=str(e))
"""

if "@app.post(\"/api/weather-advice\")" not in content:
    content = content + "\n" + endpoint

with open('backend_cloud/main.py', 'w') as f:
    f.write(content)
