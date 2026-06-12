package com.example.ecowash_washer.feature.auth.domain.usecase

import com.example.ecowash_washer.feature.auth.domain.repository.AuthRepository
import kotlinx.coroutines.flow.Flow

class GetSavedTokenUseCase(private val repository: AuthRepository) {
    operator fun invoke(): Flow<String?> {
        return repository.getSavedToken()
    }
}
