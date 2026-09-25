package com.asistenpetani.ui.components

import android.view.HapticFeedbackConstants
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Tombol khusus yang didesain untuk penggunaan di luar ruangan (sawah/kebun).
 * Memiliki warna kontras tinggi, ukuran target sentuh lebih besar, dan umpan balik getar (haptic).
 */
@Composable
fun OutdoorAiButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val view = LocalView.current

    // Menggunakan warna kontras tinggi khusus untuk di luar ruangan
    // Latar belakang cerah (Kuning) dengan teks gelap (Hitam) untuk mengalahkan silau matahari
    val outdoorContainerColor = Color(0xFFFFEB3B) 
    val outdoorContentColor = Color(0xFF000000)   

    Button(
        onClick = {
            // Haptic feedback agar petani tahu tombol berhasil ditekan tanpa harus melihat layar dengan teliti
            view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
            onClick()
        },
        modifier = modifier.size(height = 72.dp, width = 240.dp), // Ukuran tombol ekstra besar
        shape = CircleShape,
        colors = ButtonDefaults.buttonColors(
            containerColor = outdoorContainerColor,
            contentColor = outdoorContentColor
        ),
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation = 8.dp,
            pressedElevation = 2.dp
        ),
        contentPadding = PaddingValues(horizontal = 24.dp, vertical = 16.dp)
    ) {
        Icon(
            imageVector = Icons.AutoMirrored.Filled.VolumeUp,
            contentDescription = "Dengarkan Instruksi AI",
            modifier = Modifier.size(32.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = text,
            fontWeight = FontWeight.ExtraBold,
            fontSize = 18.sp
        )
    }
}
