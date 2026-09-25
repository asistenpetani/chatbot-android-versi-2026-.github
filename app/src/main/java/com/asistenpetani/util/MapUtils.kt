package com.asistenpetani.util

import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.SphericalUtil

object MapUtils {
    /**
     * Menghitung luas area poligon dalam meter persegi (m2)
     */
    fun calculateArea(points: List<LatLng>): Double {
        if (points.size < 3) return 0.0
        // SphericalUtil menghitung luas dengan memperhitungkan lengkungan bumi
        return SphericalUtil.computeArea(points) 
    }
}
