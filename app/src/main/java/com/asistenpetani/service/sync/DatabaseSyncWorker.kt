package com.asistenpetani.service.sync

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.google.android.gms.tasks.Tasks
import com.google.firebase.storage.FirebaseStorage
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import com.google.firebase.auth.FirebaseAuth
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject

import java.io.BufferedInputStream
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.concurrent.TimeUnit
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

class DatabaseSyncWorker(
    context: Context,
    workerParams: WorkerParameters
) : Worker(context, workerParams) {

    private val token = "118576e9-6322-4fd8-8c09-3b01c8fed86a"
    private val bucketName = "asistenpetani-ai-app.appspot.com"
    private val objectName = "asisten-petani.zip"

    override fun doWork(): Result {
        Log.i("DatabaseSyncWorker", "Memulai pencadangan database Room...")

        val dbFile = applicationContext.getDatabasePath("aspek_database")
        val dbDir = dbFile.parentFile

        if (dbDir == null || !dbDir.exists()) {
            Log.e("DatabaseSyncWorker", "Direktori database tidak ditemukan!")
            return Result.failure()
        }

        val uniqueFileName = "aspek_database_backup_${System.currentTimeMillis()}_${java.util.UUID.randomUUID()}.zip"
        val tempZipFile = File(applicationContext.cacheDir, uniqueFileName)

        // 1. Zip database files
        val zipped = zipDatabaseFiles(dbDir, tempZipFile)
        if (!zipped || !tempZipFile.exists() || tempZipFile.length() == 0L) {
            Log.e("DatabaseSyncWorker", "Gagal mengompresi database ke file ZIP")
            return Result.failure()
        }

        Log.i("DatabaseSyncWorker", "File ZIP berhasil dibuat: ${tempZipFile.absolutePath}, ukuran: ${tempZipFile.length()} byte")

        var success = false

        // 2. Coba upload dengan Firebase Storage SDK
        try {
            val user = FirebaseAuth.getInstance().currentUser
            if (user == null) {
                Log.w("DatabaseSyncWorker", "Batal: User belum login.")
                return Result.success()
            }
            Log.i("DatabaseSyncWorker", "Mencoba mengunggah menggunakan Firebase Storage SDK...")
            val storage = FirebaseStorage.getInstance("gs://$bucketName")
            val storageRef = storage.reference.child(objectName)
            
            // Menggunakan Tasks.await untuk memblokir thread background hingga upload selesai
            val uploadTask = storageRef.putFile(Uri.fromFile(tempZipFile))
            Tasks.await(uploadTask)
            
            Log.i("DatabaseSyncWorker", "Firebase Storage SDK: Unggah Berhasil!")
            success = true
        } catch (e: Exception) {
            Log.w("DatabaseSyncWorker", "Firebase Storage SDK gagal: ${e.message}. Mencoba fallback ke REST API dengan Token...")
        }

        // 3. Fallback / Secure Upload: Upload langsung dengan GCS menggunakan HMAC Signed URL dari FastAPI Server
        if (!success) {
            try {
                Log.i("DatabaseSyncWorker", "Mencoba mengunggah menggunakan HMAC Signed URL dari Server...")
                val user = FirebaseAuth.getInstance().currentUser
                if (user == null) {
                    Log.e("DatabaseSyncWorker", "Gagal: User belum login.")
                    return Result.failure()
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
                    put("file_path", "aspek_database/$objectName")
                    put("content_type", "application/zip")
                }
                
                // Sesuaikan dengan URL Cloud Run Anda
                val backendUrl = "https://asistenpetani-ai-app-backend-service-956692315956.asia-southeast1.run.app/api/storage/signed-url"
                val reqBody = jsonRequest.toString().toRequestBody("application/json".toMediaTypeOrNull())
                val urlRequest = Request.Builder()
                    .url(backendUrl)
                    .header("Authorization", "Bearer $idToken")
                    .post(reqBody)
                    .build()
                    
                val signedUrlStr = client.newCall(urlRequest).execute().use { response ->
                    if (!response.isSuccessful) {
                        Log.e("DatabaseSyncWorker", "Gagal mendapatkan Signed URL: ${response.code} - ${response.body?.string()}")
                        return@use null
                    }
                    val jsonResponse = JSONObject(response.body!!.string())
                    if (jsonResponse.optBoolean("success", false)) {
                        jsonResponse.getString("url")
                    } else null
                }
                
                if (signedUrlStr != null) {
                    // Gunakan Signed URL untuk upload
                    val fileBody = tempZipFile.asRequestBody("application/zip".toMediaTypeOrNull())
                    val uploadRequest = Request.Builder()
                        .url(signedUrlStr)
                        .put(fileBody) // GCS Signed URL menggunakan PUT
                        .build()
                        
                    client.newCall(uploadRequest).execute().use { response ->
                        if (response.isSuccessful) {
                            Log.i("DatabaseSyncWorker", "HMAC Signed URL: Unggah Berhasil!")
                            success = true
                        } else {
                            Log.e("DatabaseSyncWorker", "Gagal upload via Signed URL: ${response.code} - ${response.body?.string()}")
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("DatabaseSyncWorker", "GCS REST API error: ${e.message}")
            }
        }

        // Hapus file sementara setelah unggahan selesai
        try {
            tempZipFile.delete()
        } catch (e: Exception) {
            Log.w("DatabaseSyncWorker", "Gagal menghapus file sementara: ${e.message}")
        }

        return if (success) {
            Result.success()
        } else {
            Result.retry()
        }
    }

    private fun zipDatabaseFiles(dbDir: File, tempZipFile: File): Boolean {
        val dbFiles = dbDir.listFiles { _, name -> name.startsWith("aspek_database") } ?: return false
        if (dbFiles.isEmpty()) return false

        try {
            ZipOutputStream(BufferedOutputStream(FileOutputStream(tempZipFile))).use { zos ->
                val byteBuffer = ByteArray(4096)
                for (file in dbFiles) {
                    if (!file.exists() || file.isDirectory) continue
                    BufferedInputStream(FileInputStream(file)).use { bis ->
                        val entry = ZipEntry(file.name)
                        zos.putNextEntry(entry)
                        var count: Int
                        while (bis.read(byteBuffer).also { count = it } != -1) {
                            zos.write(byteBuffer, 0, count)
                        }
                        zos.closeEntry()
                    }
                }
            }
            return true
        } catch (e: Exception) {
            Log.e("DatabaseSyncWorker", "Gagal melakukan zip file database: ${e.message}", e)
            return false
        }
    }
}
