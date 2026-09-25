package com.asistenpetani.data.remote

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Header
import retrofit2.http.Path
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

@JsonClass(generateAdapter = true)
data class GenerateContentRequest(
    val contents: List<Content>,
    val systemInstruction: Content? = null,
    val generationConfig: GenerationConfig? = null
)

@JsonClass(generateAdapter = true)
data class GenerationConfig(
    val thinkingConfig: ThinkingConfig? = null
)

@JsonClass(generateAdapter = true)
data class ThinkingConfig(
    val thinkingLevel: String? = null
)

enum class ThinkingLevel {
    HIGH,
    LOW
}

@JsonClass(generateAdapter = true)
data class Content(
    val parts: List<Part>
)

@JsonClass(generateAdapter = true)
data class Part(
    val text: String? = null,
    val inlineData: InlineData? = null
)

@JsonClass(generateAdapter = true)
data class InlineData(
    val mimeType: String,
    val data: String
)

@JsonClass(generateAdapter = true)
data class GenerateContentResponse(
    val candidates: List<Candidate>?
)

@JsonClass(generateAdapter = true)
data class Candidate(
    val content: Content?
)

@JsonClass(generateAdapter = true)
data class GeminiRequest(
    val systemInstruction: SystemInstruction? = null,
    val contents: List<Content>
)

@JsonClass(generateAdapter = true)
data class SystemInstruction(
    val parts: List<Part>
)

@JsonClass(generateAdapter = true)
data class GeminiResponse(
    val candidates: List<Candidate>? = null
)



@JsonClass(generateAdapter = true)
data class ChatBackendRequest(
    val message: String,
    val history: List<Map<String, Any>> = emptyList()
)

@JsonClass(generateAdapter = true)
data class BackendResponse(
    val reply: String? = null,
    val success: Boolean? = null
)


@JsonClass(generateAdapter = true)
data class AnalyzeBackendRequest(
    val image: String,
    val mimeType: String = "image/jpeg",
    val prompt: String
)
interface GeminiApiService {

    @retrofit2.http.GET("api/check-quota")
    suspend fun checkQuota(
        @retrofit2.http.Header("Authorization") authHeader: String
    ): BackendResponse


    @POST("api/analyze")
    suspend fun analyzeWithBackend(
        @retrofit2.http.Header("Authorization") authHeader: String,
        @Body request: AnalyzeBackendRequest
    ): BackendResponse


    @POST("api/chat")
    suspend fun chatWithBackend(
        @retrofit2.http.Header("Authorization") authHeader: String,
        @Body request: ChatBackendRequest
    ): BackendResponse

    @POST("v1beta/models/{model}:generateContent")
    suspend fun generateContent(
        @Path("model") model: String,
        @Query("key") apiKey: String,
        @Body request: GenerateContentRequest
    ): GenerateContentResponse

    @POST("v1beta/models/{model}:generateContent")
    suspend fun generateContent(
        @Path("model") model: String,
        @Query("key") apiKey: String,
        @Body request: GeminiRequest
    ): retrofit2.Response<GeminiResponse>
}

object RetrofitClient {
    private const val BASE_URL = "https://generativelanguage.googleapis.com/" // Endpoint Langsung Google Gemini API (Free Tier)

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    val service: GeminiApiService by lazy {
        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create())
            .build()
        retrofit.create(GeminiApiService::class.java)
    }
}
