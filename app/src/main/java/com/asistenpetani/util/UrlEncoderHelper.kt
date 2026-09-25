package com.asistenpetani.util

import java.net.URLEncoder
import java.nio.charset.StandardCharsets

/**
 * Utility helper to ensure all Google services & maps query strings, parameters,
 * and custom API URLs are built and encoded correctly using UTF-8 character encoding,
 * satisfying Google and Android API requirements.
 */
object UrlEncoderHelper {
    /**
     * Encodes a query or parameter string securely using UTF-8 character set.
     */
    fun encode(value: String): String {
        return try {
            URLEncoder.encode(value, StandardCharsets.UTF_8.name())
        } catch (e: Exception) {
            // Fallback safe encoding
            try {
                URLEncoder.encode(value, "UTF-8")
            } catch (ex: Exception) {
                value
            }
        }
    }

    /**
     * Builds a safe Google Maps intent query or web URL using UTF-8 encoding.
     */
    fun buildGoogleMapsUrl(query: String): String {
        val encodedQuery = encode(query)
        return "https://www.google.com/maps/search/?api=1&query=$encodedQuery"
    }

    /**
     * Builds a safe Google Maps Directions search URL using UTF-8 encoding.
     */
    fun buildGoogleMapsDirectionsUrl(destination: String, origin: String? = null): String {
        val encodedDest = encode(destination)
        return if (origin != null) {
            val encodedOrigin = encode(origin)
            "https://www.google.com/maps/dir/?api=1&destination=$encodedDest&origin=$encodedOrigin"
        } else {
            "https://www.google.com/maps/dir/?api=1&destination=$encodedDest"
        }
    }

    /**
     * Builds a safe, publicly readable Firebase Storage download link using UTF-8 encoding.
     */
    fun buildFirebaseStorageUrl(bucket: String, path: String): String {
        val sanitizedBucket = bucket.removePrefix("gs://").trim()
        val encodedPath = encode(path).replace("+", "%20")
        return "https://firebasestorage.googleapis.com/v0/b/$sanitizedBucket/o/$encodedPath?alt=media"
    }
}
