package com.sifu.mysub.domain.usecase

import com.sifu.mysub.core.util.AppResult
import com.sifu.mysub.domain.model.Subscription
import com.sifu.mysub.domain.repository.SubscriptionRepository

class GetSubscriptionUseCase(
    private val repository: SubscriptionRepository
) {
    suspend operator fun invoke(): AppResult<Subscription> = repository.getSubscription()
}