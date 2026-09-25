package com.asistenpetani.core.ai

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import java.io.ByteArrayOutputStream
import java.io.InputStream
import kotlin.math.min

object ImageCompressor {

    private const val TAG = "ImageCompressor"

    /**
     * Decode Uri secara aman dengan inSampleSize untuk mencegah OutOfMemory (OOM)
     * jika gambar dari galeri HP sangat besar.
     */
    fun decodeUriSangatAman(context: Context, uri: Uri, maxDimensi: Int = 1024): Bitmap? {
        var inputStream: InputStream? = null
        try {
            // Langkah 1: Membaca dimensi gambar saja (tanpa memuat pixelnya ke memori)
            val options = BitmapFactory.Options().apply {
                inJustDecodeBounds = true
            }
            inputStream = context.contentResolver.openInputStream(uri)
            BitmapFactory.decodeStream(inputStream, null, options)
            inputStream?.close()

            val lebarAsli = options.outWidth
            val tinggiAsli = options.outHeight

            if (lebarAsli <= 0 || tinggiAsli <= 0) {
                return null
            }

            // Langkah 2: Hitung input-sample-size yang pas
            var sampleSize = 1
            while ((lebarAsli / sampleSize) > maxDimensi || (tinggiAsli / sampleSize) > maxDimensi) {
                sampleSize *= 2
            }

            // Langkah 3: Decode gambar yang sesungguhnya dengan sampleSize terpilih
            val decodeOptions = BitmapFactory.Options().apply {
                inSampleSize = sampleSize
                inPreferredConfig = Bitmap.Config.ARGB_8888
            }
            inputStream = context.contentResolver.openInputStream(uri)
            val bitmap = BitmapFactory.decodeStream(inputStream, null, decodeOptions)
            inputStream?.close()

            return bitmap
        } catch (e: Exception) {
            Log.e(TAG, "Gagal melalukan decodeUriSangatAman: ${e.message}")
            return null
        } finally {
            try {
                inputStream?.close()
            } catch (e: Exception) { /* ignore */ }
        }
    }

    /**
     * TAHAP 1: Menyesuaikan Dimensi (Max Lebar 1078, Max Tinggi 708)
     */
    fun kompresDimensi(bitmapAsli: Bitmap?): Bitmap {
        // Filter Keamanan: Null check & Recycled check
        if (bitmapAsli == null || bitmapAsli.isRecycled) {
            Log.e(TAG, "Gagal kompres dimensi: Bitmap null atau sudah di-recycled")
            // Kembalikan bitmap kosong minimalis agar aplikasi tidak crash
            return Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888)
        }

        val maxLebar = 1078.0f
        val maxTinggi = 708.0f

        val lebarAsli = bitmapAsli.width
        val tinggiAsli = bitmapAsli.height

        if (lebarAsli <= 0 || tinggiAsli <= 0) {
            return bitmapAsli
        }

        // Cari rasio pengecilan untuk lebar dan tinggi
        val rasioLebar = maxLebar / lebarAsli
        val rasioTinggi = maxTinggi / tinggiAsli

        // Gunakan rasio terkecil agar gambar tidak gepeng (menjaga aspek rasio)
        val rasio = min(rasioLebar, rasioTinggi)

        // Jika gambar bawaan HP sudah lebih kecil dari 1078x708, jangan diubah
        if (rasio >= 1.0f) return bitmapAsli

        val lebarBaru = (lebarAsli * rasio).toInt().coerceAtLeast(1)
        val tinggiBaru = (tinggiAsli * rasio).toInt().coerceAtLeast(1)

        return try {
            Bitmap.createScaledBitmap(bitmapAsli, lebarBaru, tinggiBaru, true)
        } catch (e: OutOfMemoryError) {
            Log.e(TAG, "OOM ketika scaling gambar, menggunakan fallback pemangkasan sample size")
            bitmapAsli
        }
    }

    /**
     * TAHAP 2: Menekan ukuran file agar mendekati target (misal: 208 KB)
     */
    fun kompresKeJPG(bitmap: Bitmap?, targetKb: Int = 208): ByteArray {
        // Filter Keamanan: Null check & Recycled check
        if (bitmap == null || bitmap.isRecycled) {
            Log.e(TAG, "Gagal kompres JPG: Bitmap null atau sudah di-recycled")
            return ByteArray(0)
        }

        var kualitas = 100 // Mulai dari kualitas 100%
        val stream = ByteArrayOutputStream()

        try {
            // Lakukan kompresi awal ke format JPG
            bitmap.compress(Bitmap.CompressFormat.JPEG, kualitas, stream)

            // Looping pintar: Turunkan kualitas 5% terus menerus SAMPAI ukuran di bawah target
            // (Selama kualitas masih di atas 10% agar gambar tidak hancur total)
            while (stream.toByteArray().size / 1024 > targetKb && kualitas > 10) {
                stream.reset() // Bersihkan tanpa merusak buffer internal
                kualitas -= 5  // Turunkan kualitas
                bitmap.compress(Bitmap.CompressFormat.JPEG, kualitas, stream)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Terjadi kesalahan saat kompresKeJPG: ${e.message}")
        }

        return stream.toByteArray()
    }
}
