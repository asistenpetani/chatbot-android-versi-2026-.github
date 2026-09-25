package com.asistenpetani.util

import android.graphics.Bitmap
import android.os.Build
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import kotlin.math.min

object ImageCompressor {
    /**
     * Memampatkan bitmap ke format WebP yang sangat ringan (di bawah 100-150 KB)
     * untuk mencegah lonjakan Network Data Transfer Egress ke Cloud Run/Gemini.
     */
    fun kompresGambarKeBawah200KB(bitmap: Bitmap): File {
        // 1. Resize proporsional jika dimensi melebihi 1024 piksel
        val maxDimension = 1024f
        val originalWidth = bitmap.width
        val originalHeight = bitmap.height
        val ratio = if (originalWidth > maxDimension || originalHeight > maxDimension) {
            min(maxDimension / originalWidth, maxDimension / originalHeight)
        } else {
            1.0f
        }
        val targetWidth = (originalWidth * ratio).toInt().coerceAtLeast(1)
        val targetHeight = (originalHeight * ratio).toInt().coerceAtLeast(1)

        val scaledBitmap = if (ratio < 1.0f) {
            Bitmap.createScaledBitmap(bitmap, targetWidth, targetHeight, true)
        } else {
            bitmap
        }

        val compressFormat = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            Bitmap.CompressFormat.WEBP_LOSSY
        } else {
            @Suppress("DEPRECATION")
            Bitmap.CompressFormat.WEBP
        }

        val stream = ByteArrayOutputStream()
        var kualitas = 85
        scaledBitmap.compress(compressFormat, kualitas, stream)
        var byteArray = stream.toByteArray()

        // Loop adaptif untuk memastikan ukuran sangat ringan (< 150 KB)
        while (byteArray.size > 150 * 1024 && kualitas > 20) {
            stream.reset()
            kualitas -= 10
            scaledBitmap.compress(compressFormat, kualitas, stream)
            byteArray = stream.toByteArray()
        }

        val fileTemp = File.createTempFile("aspek_plant_webp_", ".webp")
        val fos = FileOutputStream(fileTemp)
        fos.write(byteArray)
        fos.flush()
        fos.close()

        return fileTemp
    }
}
