package com.asistenpetani.domain.usecase.auth

import com.asistenpetani.data.local.room.FarmerProfile
import com.asistenpetani.domain.repository.AuthRepository

class GetCurrentUserUseCase(private val authRepo: AuthRepository) {
    suspend operator fun invoke(): FarmerProfile? {
        return authRepo.getCurrentUser()
    }
}

class LoginFarmerUseCase(private val authRepo: AuthRepository) {
    suspend operator fun invoke(name: String, cropType: String, landArea: String, lahanId: String, location: String) {
        authRepo.saveProfile(name, cropType, landArea, lahanId, location)
    }
}
