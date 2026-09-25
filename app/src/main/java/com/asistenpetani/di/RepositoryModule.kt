package com.asistenpetani.di

import android.content.Context
import com.asistenpetani.data.local.room.AppDatabase
import com.asistenpetani.data.repository.*
import com.asistenpetani.domain.repository.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

    @Provides
    @Singleton
    fun provideAppRepository(@dagger.hilt.android.qualifiers.ApplicationContext context: Context): AiAnalysisRepositoryImpl {
        val database = AppDatabase.getDatabase(context)
        return AiAnalysisRepositoryImpl(database.appDao())
    }

    @Provides
    @Singleton
    fun provideAuthRepository(@dagger.hilt.android.qualifiers.ApplicationContext context: Context): AuthRepository {
        val database = AppDatabase.getDatabase(context)
        return AuthRepositoryImpl(database.appDao())
    }

    @Provides
    @Singleton
    fun provideLahanRepository(): LahanRepository {
        return LahanRepositoryImpl()
    }

    @Provides
    @Singleton
    fun provideMarketRepository(): MarketRepository {
        return MarketRepositoryImpl()
    }

    @Provides
    @Singleton
    fun provideFarmingRepository(): FarmingRepository {
        return FarmingRepositoryImpl()
    }

    @Provides
    @Singleton
    fun provideExpertRepository(): ExpertRepository {
        return ExpertRepositoryImpl()
    }

    @Provides
    @Singleton
    fun provideAspekRepository(
        appRepository: AiAnalysisRepositoryImpl,
        workloadCredentials: WorkloadCredentials? = null
    ): AspekRepository {
        return AspekRepositoryImpl(appRepository, workloadCredentials)
    }

    @Provides
    @Singleton
    fun provideGenerativeAiRepository(@dagger.hilt.android.qualifiers.ApplicationContext context: Context? = null): GenerativeAiRepository {
        if (context == null) return GenerativeAiRepositoryImpl(null)
        return GenerativeAiRepositoryImpl(AppDatabase.getDatabase(context).appDao())
    }
}
