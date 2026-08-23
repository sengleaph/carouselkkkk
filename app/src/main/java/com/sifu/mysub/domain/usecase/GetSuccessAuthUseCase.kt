package com.sifu.mysub.domain.usecase

import com.sifu.mysub.core.util.AppResult
import com.sifu.mysub.domain.model.SuccessAuthModel
import com.sifu.mysub.domain.repository.SuccessAuthRepository

class GetSuccessAuthUseCase(
    private val repository: SuccessAuthRepository
) {
    suspend operator fun invoke(): AppResult<SuccessAuthModel> = repository.getSuccessAuth()
}