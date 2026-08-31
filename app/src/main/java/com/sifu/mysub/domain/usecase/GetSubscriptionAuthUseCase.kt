package com.sifu.mysub.domain.usecase

import com.sifu.mysub.core.util.AppResult
import com.sifu.mysub.domain.model.SubscriptionAuth
import com.sifu.mysub.domain.repository.SubscriptionAuthRepository

class GetSubscriptionAuthUseCase(
    private val repository: SubscriptionAuthRepository
) {
    suspend operator fun invoke(): AppResult<SubscriptionAuth> =
        repository.getSubscriptionAuth()
}
