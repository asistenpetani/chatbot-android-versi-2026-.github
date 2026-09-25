package com.asistenpetani.domain.usecase.database

import com.asistenpetani.domain.model.LahanStatus
import com.asistenpetani.domain.repository.LahanRepository

class VerifyLahanLocationUseCase(private val lahanRepo: LahanRepository) {
    suspend operator fun invoke(lahanId: String): LahanStatus {
        return lahanRepo.checkLahanStatus(lahanId)
    }
}
