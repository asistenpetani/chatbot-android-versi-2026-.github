package com.asistenpetani.di

import android.content.Context
import com.asistenpetani.data.remote.GoogleAuthManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    @Provides
    @Singleton
    fun provideFirebaseAuth(): FirebaseAuth = FirebaseAuth.getInstance()

    // Tambahkan ini agar Hilt bisa menyuntikkan database ke Repository
    @Provides
    @Singleton
    fun provideFirebaseFirestore(): FirebaseFirestore = FirebaseFirestore.getInstance()

    /**
     * Mesin Google Auth
     * Fungsi ini akan dipanggil otomatis oleh Hilt saat aplikasi pertama kali dibuka.
     * @Singleton memastikan mesin ini hanya dibuat satu kali (hemat memori).
     * @ApplicationContext memberikan akses sistem Android ke mesin tanpa menyentuh UI.
     */
    @Provides
    @Singleton
    fun provideGoogleAuthManager(
        @dagger.hilt.android.qualifiers.ApplicationContext context: Context
    ): GoogleAuthManager {
        return GoogleAuthManager(context)
    }
}
