package com.asistenpetani.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.asistenpetani.R

/**
 * Komponen Compose untuk memvisualisasikan Ikon Asisten Petani Anda 
 * sebagai Splash Screen atau Logo Pintar di dalam Dashboard Aplikasi Kotlin.
 */
@Composable
fun LogoAsistenPetaniSplash() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color(0xFF042f2e),
                        Color(0xFF022c22)
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        // Kontainer Pembungkus Bulat Ikon
        Box(
            modifier = Modifier
                .size(160.dp)
                .clip(CircleShape),
            contentAlignment = Alignment.Center
        ) {
            // Memasang Lapisan Depan (Foreground) dari Drawable
            Image(
                painter = painterResource(id = R.drawable.aspek_mascot_icon_1782037281779),
                contentDescription = "Logo Asisten Petani",
                modifier = Modifier.fillMaxSize().padding(12.dp)
            )
        }
    }
}
