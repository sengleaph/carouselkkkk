package com.sifu.mysub.domain.repository

import com.sifu.mysub.core.util.AppResult
import com.sifu.mysub.domain.model.SubscriptionAuth

interface SubscriptionAuthRepository {
    suspend fun getSubscriptionAuth(): AppResult<SubscriptionAuth>
}
