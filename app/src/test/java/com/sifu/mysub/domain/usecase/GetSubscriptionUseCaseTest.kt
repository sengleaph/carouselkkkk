package com.sifu.mysub.domain.usecase

import com.sifu.mysub.core.util.AppResult
import com.sifu.mysub.core.util.getOrNull
import com.sifu.mysub.domain.model.DetailRow
import com.sifu.mysub.domain.model.Subscription
import com.sifu.mysub.domain.repository.SubscriptionRepository
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class GetSubscriptionUseCaseTest {

    private fun useCase(subscription: Subscription) = GetSubscriptionUseCase(
        object : SubscriptionRepository {
            override suspend fun getSubscription() = AppResult.Success(subscription)
        }
    )

    @Test
    fun `inserts Description row right after Account No`() = runTest {
        val source = Subscription.EMPTY.copy(
            planName = "Gemezz Daily Plan",
            rows = listOf(
                DetailRow("Receiver", "Vemeanreach"),
                DetailRow("Account No.", "0001***822 (USD)"),
                DetailRow("Debit Amount", "12.00 USD")
            )
        )

        val rows = useCase(source)().getOrNull()!!.rows

        assertEquals(
            listOf("Receiver", "Account No.", "Description", "Debit Amount"),
            rows.map { it.title }
        )
        assertEquals("Gemezz Daily Plan", rows[2].value)
    }

    @Test
    fun `does not duplicate a Description row the server already sent`() = runTest {
        val source = Subscription.EMPTY.copy(
            planName = "Gemezz Daily Plan",
            rows = listOf(
                DetailRow("Account No.", "0001***822 (USD)"),
                DetailRow("Description", "Something else")
            )
        )

        val rows = useCase(source)().getOrNull()!!.rows

        assertEquals(2, rows.size)
        assertEquals("Something else", rows.single { it.title == "Description" }.value)
    }

    @Test
    fun `adds nothing when planName is blank`() = runTest {
        val source = Subscription.EMPTY.copy(
            planName = "",
            rows = listOf(DetailRow("Receiver", "Vemeanreach"))
        )

        assertEquals(1, useCase(source)().getOrNull()!!.rows.size)
    }
}
