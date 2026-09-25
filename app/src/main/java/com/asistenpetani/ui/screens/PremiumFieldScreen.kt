package com.asistenpetani.ui.screens

import android.graphics.Bitmap
import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*
import com.asistenpetani.util.MapUtils
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import com.google.android.gms.tasks.Tasks
import java.util.concurrent.TimeUnit

import java.io.ByteArrayOutputStream
import java.util.UUID

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PremiumFieldScreen(onBackClick: () -> Unit = {}) {
    var polygonPoints by remember { mutableStateOf(listOf<LatLng>()) }
    var triggerSnapshot by remember { mutableStateOf(false) }
    var isUploading by remember { mutableStateOf(false) }

    // Konfigurasi awal peta (misal: fokus ke koordinat Kolaka, Sultra)
    val kolakaPosition = LatLng(-4.0487, 121.5975)
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(kolakaPosition, 15f)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Peta Lahan") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Kembali")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
        // Kontainer Peta
        Box(modifier = Modifier.weight(1f)) {
            GoogleMap(
                modifier = Modifier.fillMaxSize(),
                cameraPositionState = cameraPositionState,
                properties = MapProperties(mapType = MapType.SATELLITE),
                onMapClick = { latLng -> polygonPoints = polygonPoints + latLng }
            ) {
                // Gambar titik-titik sudut (Marker Kuning)
                polygonPoints.forEach { point ->
                    Marker(
                        state = MarkerState(position = point),
                        title = "Titik Sudut"
                    )
                }

                // Gambar Poligon jika titik >= 3
                if (polygonPoints.size >= 3) {
                    Polygon(
                        points = polygonPoints,
                        fillColor = Color(0x66FF0000),
                        strokeColor = Color.Yellow,
                        strokeWidth = 5f
                    )
                }

                // MapEffect untuk menangkap perintah Snapshot
                MapEffect(triggerSnapshot) { map ->
                    if (triggerSnapshot) {
                        map.snapshot { bitmap ->
                            if (bitmap != null) {
                                // Eksekusi proses upload setelah bitmap didapat
                                isUploading = true
                                uploadSnapshotToGCP(bitmap, polygonPoints) {
                                    isUploading = false
                                }
                                triggerSnapshot = false
                            } else {
                                isUploading = false
                                triggerSnapshot = false
                            }
                        }
                    }
                }
            }
        }

        // Tombol Aksi di Bawah Peta
        Button(
            onClick = { 
                triggerSnapshot = true
                isUploading = true
            },
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            enabled = polygonPoints.size >= 3 && !isUploading
        ) {
            Text(text = if (isUploading) "Menyimpan Data..." else "Selesai & Simpan Lahan")
        }
    }
}
}

fun uploadSnapshotToGCP(bitmap: Bitmap, points: List<LatLng>, onComplete: () -> Unit) {
    // 1. Konversi Bitmap ke format JPEG (Kompresi 80% agar ringan)
    val baos = ByteArrayOutputStream()
    bitmap.compress(Bitmap.CompressFormat.JPEG, 80, baos)
    val data = baos.toByteArray()
    
    // 2. Buat nama file unik
    val uniqueFileName = "premium_field_${UUID.randomUUID()}.jpg"
    val filePath = "field_snapshots/$uniqueFileName"
    
    CoroutineScope(Dispatchers.IO).launch {
        try {
            val user = FirebaseAuth.getInstance().currentUser
            if (user == null) {
                Log.e("ASPEK_LOG", "Gagal: User belum login.")
                CoroutineScope(Dispatchers.Main).launch { onComplete() }
                return@launch
            }
            
            val idTokenTask = user.getIdToken(false)
            Tasks.await(idTokenTask)
            val idToken = idTokenTask.result?.token ?: throw Exception("Token kosong")

            val client = OkHttpClient.Builder()
                .connectTimeout(60, TimeUnit.SECONDS)
                .writeTimeout(60, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .build()
            
            // Minta Signed URL
            val jsonRequest = JSONObject().apply {
                put("action", "write")
                put("file_path", filePath)
                put("content_type", "image/jpeg")
            }
            
            val backendUrl = "https://asistenpetani-ai-app-backend-service-956692315956.asia-southeast1.run.app/api/storage/signed-url"
            val reqBody = jsonRequest.toString().toRequestBody("application/json".toMediaTypeOrNull())
            val urlRequest = Request.Builder()
                .url(backendUrl)
                .header("Authorization", "Bearer $idToken")
                .post(reqBody)
                .build()
                
            val signedUrlStr = client.newCall(urlRequest).execute().use { response ->
                if (!response.isSuccessful) {
                    Log.e("ASPEK_LOG", "Gagal mendapatkan Signed URL: ${response.code}")
                    return@use null
                }
                val jsonResponse = JSONObject(response.body!!.string())
                if (jsonResponse.optBoolean("success", false)) {
                    jsonResponse.getString("url")
                } else null
            }
            
            if (signedUrlStr != null) {
                // Gunakan Signed URL untuk upload
                val fileBody = data.toRequestBody("image/jpeg".toMediaTypeOrNull())
                val uploadRequest = Request.Builder()
                    .url(signedUrlStr)
                    .put(fileBody) // GCS Signed URL menggunakan PUT
                    .build()
                    
                client.newCall(uploadRequest).execute().use { response ->
                    if (response.isSuccessful) {
                        Log.i("ASPEK_LOG", "HMAC Signed URL: Unggah Berhasil!")
                        // Download URL adalah URL publik GCS jika dikonfigurasi, atau URL aslinya tanpa signature (sebagai ID)
                        val finalUrl = "https://storage.googleapis.com/asistenpetani-ai-app.appspot.com/$filePath"
                        
                        val estimatedArea = MapUtils.calculateArea(points)
                        saveMetadataToDatabase(
                            imageUrl = finalUrl,
                            coordinates = points,
                            areaSize = estimatedArea
                        )
                    } else {
                        Log.e("ASPEK_LOG", "Gagal upload via Signed URL: ${response.code}")
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("ASPEK_LOG", "Error upload via Signed URL: ${e.message}")
        } finally {
            CoroutineScope(Dispatchers.Main).launch { onComplete() }
        }
    }
}

fun saveMetadataToDatabase(imageUrl: String, coordinates: List<LatLng>, areaSize: Double) {
    // Menyimpan Payload lengkap ke Firestore
    Log.d("ASPEK_LOG", "Menyimpan metadata ke server: Luas $areaSize m2")
    
    val auth = FirebaseAuth.getInstance()
    val uid = auth.currentUser?.uid ?: return
    val db = FirebaseFirestore.getInstance()
    
    val fieldData = mapOf(
        "userId" to uid,
        "imageUrl" to imageUrl,
        "coordinates" to coordinates.map { mapOf("lat" to it.latitude, "lng" to it.longitude) },
        "areaSize" to areaSize,
        "timestamp" to com.google.firebase.Timestamp.now()
    )
    
    db.collection("fields").add(fieldData)
        .addOnSuccessListener {
            Log.d("ASPEK_LOG", "Metadata berhasil disimpan")
        }
        .addOnFailureListener {
            Log.e("ASPEK_LOG", "Gagal menyimpan metadata: ${it.message}")
        }
}
