package com.asistenpetani.di

import com.asistenpetani.data.remote.CloudRunApiService
import com.asistenpetani.data.remote.CloudRunRetrofitClient
import com.asistenpetani.data.repository.CloudRunBigDataRepositoryImpl
import com.asistenpetani.data.repository.MockBigDataRepositoryImpl
import com.asistenpetani.domain.repository.BigDataRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DataModule {

    @Provides
    @Singleton
    fun provideCloudRunApiService(): CloudRunApiService {
        // Masukkan URL Cloud Run Anda di sini nanti
        val cloudRunUrl = "https://asistenpetani-ai-app-backend-service-956692315956.asia-southeast1.run.app/" 
        return CloudRunRetrofitClient.getService(cloudRunUrl)
    }

    @Provides
    @Singleton
    fun provideBigDataRepository(
        apiService: CloudRunApiService
    ): BigDataRepository {
        // [MODE PENGEMBANGAN / OFFLINE GCP]
        // Jika tagihan GCP masih OR_BACR2_44, gunakan Mock:
        // return MockBigDataRepositoryImpl()
        
        // [MODE PRODUKSI]
        // Hapus komentar baris di bawah ini jika Cloud Run Python sudah aktif:
        return CloudRunBigDataRepositoryImpl(apiService)
    }
}
