package com.sifu.mysub.domain.usecase

import com.sifu.mysub.core.util.AppResult
import com.sifu.mysub.core.util.map
import com.sifu.mysub.domain.model.DetailRow
import com.sifu.mysub.domain.model.Subscription
import com.sifu.mysub.domain.repository.SubscriptionRepository

/**
 * One public entry point per user intent. The ViewModel talks to use cases,
 * never to a repository directly.
 */
class GetSubscriptionUseCase(
    private val repository: SubscriptionRepository
) {
    suspend operator fun invoke(): AppResult<Subscription> =
        repository.getSubscription().map { it.withDescriptionRow() }

    /**
     * The design shows a "Description" row carrying the plan name, but the payload
     * only exposes `planName` at the top level. Synthesise the row when the server
     * does not already send one — delete this once `dataList` includes it.
     */
    private fun Subscription.withDescriptionRow(): Subscription {
        if (planName.isBlank()) return this
        if (rows.any { it.title.equals(DESCRIPTION, ignoreCase = true) }) return this

        val anchor = rows.indexOfFirst { it.title.equals(ACCOUNT_NO, ignoreCase = true) }
        val insertAt = if (anchor >= 0) anchor + 1 else rows.size.coerceAtMost(2)

        val updated = rows.toMutableList().apply {
            add(insertAt, DetailRow(title = DESCRIPTION, value = planName))
        }
        return copy(rows = updated)
    }

    private companion object {
        const val DESCRIPTION = "Description"
        const val ACCOUNT_NO = "Account No."
    }
}
