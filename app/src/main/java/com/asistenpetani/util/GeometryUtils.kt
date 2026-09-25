package com.asistenpetani.util

/**
 * Representasi latitude dan longitude sederhana tanpa bergantung pada dependency Maps SDK eksternal.
 */
data class SimpleLatLng(val latitude: Double, val longitude: Double)

/**
 * Helper utilitas untuk perhitungan geometri geosferis (Spherical Geometry)
 * yang dioptimalkan untuk Android tanpa memerlukan overhead library eksternal.
 */
object GeometryUtils {
    private const val EARTH_RADIUS = 6378137.0 // Radius Bumi dalam meter

    /**
     * Menghitung luas lahan berdasarkan daftar beberapa titik koordinat (Polygon).
     * Sangat hemat baterai, memori, dan nol overhead biner APK karena murni memanfaatkan fungsi built-id Kotlin Math.
     * @param points Daftar titik sudut polygon lahan
     * @return Luas lahan dalam satuan Hektar (Ha)
     */
    fun calculateLandArea(points: List<SimpleLatLng>): Double {
        if (points.size < 3) return 0.0
        
        var area = 0.0
        for (i in points.indices) {
            val p1 = points[i]
            val p2 = points[(i + 1) % points.size]
            
            // Konversi ke Radian untuk hasil kalkulasi kelurusan lengkung bumi presisi
            val x1 = Math.toRadians(p1.longitude) * Math.cos(Math.toRadians(p1.latitude))
            val y1 = Math.toRadians(p1.latitude)
            val x2 = Math.toRadians(p2.longitude) * Math.cos(Math.toRadians(p2.latitude))
            val y2 = Math.toRadians(p2.latitude)
            
            area += (x1 * y2 - x2 * y1)
        }
        
        val areaInSquareMeters = Math.abs(area * EARTH_RADIUS * EARTH_RADIUS / 2.0)
        return areaInSquareMeters / 10000.0 // Mengonversi Meter Persegi ke Hektar
    }

    /**
     * Menghitung jangkauan luas lingkaran berdasarkan satu titik pusat (Latitude/Longitude).
     * @param radiusInMeters Radius lingkaran jangkauan dalam meter
     * @return Luas cakupan dalam satuan Hektar (Ha)
     */
    fun calculateCoverageArea(radiusInMeters: Double): Double {
        val areaInSquareMeters = Math.PI * (radiusInMeters * radiusInMeters)
        return areaInSquareMeters / 10000.0 // Mengonversi Meter Persegi ke Hektar
    }

    /**
     * Menghitung jarak antara dua titik koordinat menggunakan rumus Haversine.
     * @return Jarak dalam kilometer (KM)
     */
    fun calculateDistance(start: SimpleLatLng, end: SimpleLatLng): Double {
        val lat1 = Math.toRadians(start.latitude)
        val lon1 = Math.toRadians(start.longitude)
        val lat2 = Math.toRadians(end.latitude)
        val lon2 = Math.toRadians(end.longitude)

        val dLat = lat2 - lat1
        val dLon = lon2 - lon1

        val a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(lat1) * Math.cos(lat2) *
                Math.sin(dLon / 2) * Math.sin(dLon / 2)
        val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))

        return (EARTH_RADIUS * c) / 1000.0 // Dalam Kilometer
    }

    /**
     * Mengurai string lokasi (misalnya "-4.0125,121.4988") secara aman.
     * Jika tidak valid atau kosong, menggunakan titik koordinat pusat default (Kolaka: -4.0588, 121.5403).
     */
    fun parseLocation(location: String?): SimpleLatLng {
        if (location.isNullOrBlank()) {
            return SimpleLatLng(-4.0588, 121.5403) // Default Kolaka Kakao
        }
        return try {
            val parts = location.split(",")
            if (parts.size >= 2) {
                val lat = parts[0].trim().toDouble()
                val lon = parts[1].trim().toDouble()
                SimpleLatLng(lat, lon)
            } else {
                SimpleLatLng(-4.0588, 121.5403)
            }
        } catch (e: Exception) {
            SimpleLatLng(-4.0588, 121.5403)
        }
    }
}
