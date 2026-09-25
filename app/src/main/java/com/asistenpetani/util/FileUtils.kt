package com.asistenpetani.util

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import java.io.File
import java.io.FileOutputStream

object FileUtils {
    fun bitmapToFileSementara(context: Context, bitmap: Bitmap): File? {
        return try {
            val fileSementara = File.createTempFile("gambar_chat_temp", ".jpg", context.cacheDir)
            val outputStream = FileOutputStream(fileSementara)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 85, outputStream)
            outputStream.close()
            fileSementara
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
    
    fun uriKeFileSementara(context: Context, uri: Uri): File? {
        return try {
            val inputStream = context.contentResolver.openInputStream(uri) ?: return null
            val fileSementara = File.createTempFile("gambar_chat_temp", ".jpg", context.cacheDir)
            val outputStream = FileOutputStream(fileSementara)
            inputStream.copyTo(outputStream)
            inputStream.close()
            outputStream.close()
            fileSementara
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
