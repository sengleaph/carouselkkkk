package com.sifu.mysub.domain.repository

import com.sifu.mysub.core.util.AppResult
import com.sifu.mysub.domain.model.Subscription

/**
 * The abstraction lives in domain; the implementation lives in data.
 * This inversion is what lets domain stay independent of Gson, res/raw,
 * Retrofit, Room, or anything else.
 */
interface SubscriptionRepository {
    suspend fun getSubscription(): AppResult<Subscription>
}
