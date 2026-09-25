package com.asistenpetani.di

import android.content.Context
import com.asistenpetani.R
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

/**
 * WorkloadCredentials data model containing OIDC external credentials.
 */
data class WorkloadCredentials(
    val type: String,
    val audience: String,
    val subject_token_type: String,
    val token_url: String,
    val service_account_impersonation_url: String
)

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    /**
     * Reads credentials.json securely from resources raw folder
     * as the Single Source of Truth, preventing hardcoded keys.
     */
    @Provides
    fun provideWorkloadCredentials(@dagger.hilt.android.qualifiers.ApplicationContext context: Context): WorkloadCredentials? {
        return try {
            val inputStream = context.resources.openRawResource(R.raw.credentials)
            val reader = BufferedReader(InputStreamReader(inputStream))
            val jsonString = reader.use { it.readText() }
            val json = JSONObject(jsonString)
            WorkloadCredentials(
                type = json.optString("type"),
                audience = json.optString("audience"),
                subject_token_type = json.optString("subject_token_type"),
                token_url = json.optString("token_url"),
                service_account_impersonation_url = json.optString("service_account_impersonation_url")
            )
        } catch (e: Exception) {
            android.util.Log.e("AspekDI", "Gagal membaca credentials dari raw res: ${e.message}")
            null
        }
    }

    @Provides
    @javax.inject.Named("webClientId")
    fun provideWebClientId(@dagger.hilt.android.qualifiers.ApplicationContext context: Context): String {
        return context.getString(R.string.default_web_client_id)
    }
}
