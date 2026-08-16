package com.sifu.mysub.presentation.subscription

import app.cash.turbine.test
import com.sifu.mysub.core.util.AppError
import com.sifu.mysub.core.util.AppResult
import com.sifu.mysub.domain.model.DetailRow
import com.sifu.mysub.domain.model.RowStyle
import com.sifu.mysub.domain.model.Subscription
import com.sifu.mysub.domain.repository.SubscriptionRepository
import com.sifu.mysub.domain.usecase.GetSubscriptionUseCase
import com.sifu.mysub.domain.usecase.ResolveRowActionUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SubscriptionViewModelTest {

    private val dispatcher = StandardTestDispatcher()

    private val subscription = Subscription.EMPTY.copy(
        subCode = "gemez",
        planName = "Gemez Daily Plan",
        amount = "0.28 $",
        renew = "Tomorrow",
        hasSubscription = true,
        rows = listOf(
            DetailRow("Receiver", "Sengleap Seang"),
            DetailRow("Pin", "8466d4", style = RowStyle.COPYABLE)
        )
    )

    @Before
    fun setUp() = Dispatchers.setMain(dispatcher)

    @After
    fun tearDown() = Dispatchers.resetMain()

    private fun viewModel(result: AppResult<Subscription>) = SubscriptionViewModel(
        getSubscription = GetSubscriptionUseCase(
            object : SubscriptionRepository {
                override suspend fun getSubscription() = result
            }
        ),
        resolveRowAction = ResolveRowActionUseCase(),
        errorMessages = { it.message ?: "generic" }
    )

    @Test
    fun `success populates state`() = runTest(dispatcher) {
        val vm = viewModel(AppResult.Success(subscription))
        testScheduler.advanceUntilIdle()

        val state = vm.uiState.value
        assertEquals(false, state.isLoading)
        assertNull(state.errorMessage)
        assertEquals("Gemez Daily Plan", state.planName)
        // 2 seeded rows + the synthesised "Description" row
        assertEquals(3, state.rows.size)
        assertTrue(state.isCancelVisible)
    }

    @Test
    fun `derives header branding from subCode`() = runTest(dispatcher) {
        val vm = viewModel(AppResult.Success(subscription))
        testScheduler.advanceUntilIdle()

        assertEquals("GEMEZ", vm.uiState.value.brandCode)
        assertEquals("Gemez", vm.uiState.value.brandName)
    }

    @Test
    fun `business failure surfaces mapped message`() = runTest(dispatcher) {
        val vm = viewModel(AppResult.Failure(AppError.Business("500", "no active plan")))
        testScheduler.advanceUntilIdle()

        val state = vm.uiState.value
        assertEquals("no active plan", state.errorMessage)
        assertEquals(false, state.isContentVisible)
        assertEquals(false, state.isCancelVisible)
    }

    @Test
    fun `tapping a copyable row emits CopyToClipboard`() = runTest(dispatcher) {
        val vm = viewModel(AppResult.Success(subscription))
        testScheduler.advanceUntilIdle()

        vm.events.test {
            vm.onIntent(
                SubscriptionIntent.RowClicked(
                    DetailRow("Pin", "8466d4", style = RowStyle.COPYABLE)
                )
            )
            testScheduler.advanceUntilIdle()

            assertEquals(
                SubscriptionUiEvent.CopyToClipboard("Pin", "8466d4"),
                awaitItem()
            )
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `tapping a plain row emits nothing`() = runTest(dispatcher) {
        val vm = viewModel(AppResult.Success(subscription))
        testScheduler.advanceUntilIdle()

        vm.events.test {
            vm.onIntent(SubscriptionIntent.RowClicked(DetailRow("Receiver", "Sengleap Seang")))
            testScheduler.advanceUntilIdle()

            expectNoEvents()
            cancelAndIgnoreRemainingEvents()
        }
    }
}
