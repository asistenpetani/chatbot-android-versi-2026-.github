package com.asistenpetani.domain.repository

interface BigDataRepository {
    suspend fun fetchData(query: String): String
}
