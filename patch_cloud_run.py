import re

with open('app/src/main/java/com/asistenpetani/data/remote/CloudRunApiService.kt', 'r') as f:
    content = f.read()

models = """
@JsonClass(generateAdapter = true)
data class WeatherAnalysisRequest(
    val latitude: Double,
    val longitude: Double,
    @Json(name = "crop_type") val cropType: String = "Padi"
)

@JsonClass(generateAdapter = true)
data class WeatherAnalysisResponse(
    val temperature: Double,
    val humidity: Double,
    @Json(name = "weather_code") val weatherCode: Int,
    val advice: String
)
"""

if "data class WeatherAnalysisRequest" not in content:
    content = content.replace("interface CloudRunApiService {", models + "\ninterface CloudRunApiService {")
    
endpoint = """
    @POST("api/weather-advice")
    suspend fun getWeatherAdvice(
        @Body request: WeatherAnalysisRequest
    ): WeatherAnalysisResponse
"""

if "getWeatherAdvice" not in content:
    content = content.replace("interface CloudRunApiService {", "interface CloudRunApiService {" + endpoint)

with open('app/src/main/java/com/asistenpetani/data/remote/CloudRunApiService.kt', 'w') as f:
    f.write(content)
