package com.asistenpetani.ui.features.maps

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Store
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Map
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel

import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapScreen(
    onBackClick: () -> Unit,
    viewModel: MapViewModel = hiltViewModel()
) {
    // 1. Titik koordinat pusat awal (Kolaka, Sulawesi Tenggara)
    val koordinatKolaka = LatLng(-4.4461, 121.6042)
    
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(koordinatKolaka, 12f)
    }

    val aiResult by viewModel.aiAnalysisResult.collectAsState()
    val lokasiList by viewModel.lokasiSekunder.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    var showAnalysisDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Peta Geografi & Rantai Pasok", fontWeight = FontWeight.Bold, fontSize = 18.sp) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Kembali")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF16A34A),
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        },
        bottomBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .padding(16.dp)
            ) {
                Text(
                    text = "Aksi Pintar Peta",
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1D4ED8),
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { 
                            val currentLat = cameraPositionState.position.target.latitude
                            val currentLon = cameraPositionState.position.target.longitude
                            viewModel.analisaKondisiLahan("1.5", "Padi", currentLat, currentLon)
                            showAnalysisDialog = true
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF16A34A))
                    ) {
                        Icon(Icons.Default.Map, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Analisa Lahan", fontSize = 12.sp)
                    }
                    Button(
                        onClick = { 
                            val currentLat = cameraPositionState.position.target.latitude
                            val currentLon = cameraPositionState.position.target.longitude
                            viewModel.muatLokasiDariGcp("Kios", currentLat, currentLon)
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB))
                    ) {
                        Icon(Icons.Default.Store, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Cari Kios", fontSize = 12.sp)
                    }
                    Button(
                        onClick = { 
                            val currentLat = cameraPositionState.position.target.latitude
                            val currentLon = cameraPositionState.position.target.longitude
                            viewModel.muatLokasiDariGcp("Pembeli", currentLat, currentLon)
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEA580C))
                    ) {
                        Icon(Icons.Default.Person, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Cari Pembeli", fontSize = 12.sp)
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Google Maps
            GoogleMap(
                modifier = Modifier.fillMaxSize(),
                cameraPositionState = cameraPositionState,
                properties = MapProperties(isMyLocationEnabled = false),
                uiSettings = MapUiSettings(
                    zoomControlsEnabled = true,
                    compassEnabled = true
                )
            ) {
                // Tampilkan marker
                lokasiList.forEach { lokasi ->
                    val pos = LatLng(lokasi.lat, lokasi.lon)
                    Marker(
                        state = MarkerState(position = pos),
                        title = lokasi.nama,
                        snippet = "Jarak: ${lokasi.jarakKm ?: "-"} km"
                    )
                }
            }

            if (isLoading && !showAnalysisDialog) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center)
                )
            }
        }
    }

    if (showAnalysisDialog) {
        AlertDialog(
            onDismissRequest = { showAnalysisDialog = false },
            title = { Text("Analisis Geografi AI", fontWeight = FontWeight.Bold) },
            text = {
                if (isLoading) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        CircularProgressIndicator()
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("AI sedang menganalisis koordinat...")
                    }
                } else {
                    Text(aiResult)
                }
            },
            confirmButton = {
                TextButton(onClick = { showAnalysisDialog = false }) {
                    Text("Tutup")
                }
            },
            containerColor = Color.White,
            shape = RoundedCornerShape(16.dp)
        )
    }
}
