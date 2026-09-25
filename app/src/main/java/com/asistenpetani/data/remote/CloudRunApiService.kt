package com.asistenpetani.data.remote

import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Body

interface CloudRunApiService {

    @Multipart
    @POST("api/analisa-gambar-aman")
    suspend fun analisaGambarAman(
        @Part("pesan") pesan: RequestBody,
        @Part file: MultipartBody.Part
    ): Response<AnalisaGambarResponse>

    @POST("api/chat")
    suspend fun sendChatMessage(
        @Body request: ChatRequest
    ): ChatResponse

    @POST("api/weather")
    suspend fun getWeatherAdvice(
        @Body request: WeatherAnalysisRequest
    ): WeatherAnalysisResponse
}

data class AnalisaGambarResponse(val jawaban: String)
data class ChatRequest(val message: String)
data class ChatResponse(val response: String)
data class WeatherAnalysisRequest(val latitude: Double, val longitude: Double, val cropType: String)
data class WeatherAnalysisResponse(val temperature: Double, val humidity: Double, val weatherCode: Int, val advice: String)

data class GcpEnvironment(
    val key: String,
    val name: String,
    val description: String,
    val url: String
)
