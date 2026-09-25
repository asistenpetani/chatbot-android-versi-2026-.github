package com.asistenpetani.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import kotlin.math.min

class ImageHelper(private val context: Context) {

    /**
     * Memampatkan Uri gambar ke dalam file sementara (.webp) dengan batas dimensi maksimum 1024.
     * Melakukan kompresi kualitas adaptif cerdas (WebP) hingga di bawah 150 KB untuk hemat bandwidth.
     */
    suspend fun compressToTempFile(uri: Uri): File? = withContext(Dispatchers.IO) {
        try {
            val inputStream = context.contentResolver.openInputStream(uri)
            val originalBitmap = BitmapFactory.decodeStream(inputStream)
            inputStream?.close()

            if (originalBitmap == null) return@withContext null

            compressBitmapToTempFile(originalBitmap)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Memampatkan Bitmap gambar langsung ke dalam file sementara (.webp) dengan batas dimensi maksimum 1024.
     * Menggunakan format WebP modern hemat egress data.
     */
    suspend fun compressBitmapToTempFile(bitmap: Bitmap): File? = withContext(Dispatchers.IO) {
        try {
            // 1. Perkecil dimensi gambar (maksimal 1024 piksel) secara ergonomis
            val maxDimension = 1024f
            val lebarAsli = bitmap.width
            val tinggiAsli = bitmap.height
            
            val ratio = if (lebarAsli > maxDimension || tinggiAsli > maxDimension) {
                min(maxDimension / lebarAsli, maxDimension / tinggiAsli)
            } else {
                1.0f
            }

            val width = (lebarAsli * ratio).toInt().coerceAtLeast(1)
            val height = (tinggiAsli * ratio).toInt().coerceAtLeast(1)

            val resizedBitmap = if (ratio < 1.0f) {
                Bitmap.createScaledBitmap(bitmap, width, height, true)
            } else {
                bitmap
            }

            // 2. Buat file sementara WebP di folder cache aplikasi
            val tempFile = File(context.cacheDir, "temp_leaf_image.webp")
            if (tempFile.exists()) {
                tempFile.delete()
            }
            tempFile.createNewFile()

            val compressFormat = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                Bitmap.CompressFormat.WEBP_LOSSY
            } else {
                @Suppress("DEPRECATION")
                Bitmap.CompressFormat.WEBP
            }

            // 3. Kompresi kualitas adaptif cerdas agar ukuran file di bawah 150 KB
            var kualitas = 85
            var outputStream = FileOutputStream(tempFile)
            resizedBitmap.compress(compressFormat, kualitas, outputStream)
            outputStream.flush()
            outputStream.close()

            while (tempFile.length() / 1024 > 150 && kualitas > 20) {
                kualitas -= 10
                outputStream = FileOutputStream(tempFile)
                resizedBitmap.compress(compressFormat, kualitas, outputStream)
                outputStream.flush()
                outputStream.close()
            }

            tempFile
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
