package com.sifu.mysub.presentation.service

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.sifu.mysub.core.util.AppError
import com.sifu.mysub.core.util.AppResult
import com.sifu.mysub.core.util.getOrNull
import com.sifu.mysub.domain.model.SubscribeService
import com.sifu.mysub.domain.model.Subscription
import com.sifu.mysub.domain.usecase.GetSubscribeMenuUseCase
import com.sifu.mysub.domain.usecase.GetSubscriptionUseCase
import com.sifu.mysub.presentation.main.CardTarget
import com.sifu.mysub.presentation.main.ErrorMessageMapper
import com.sifu.mysub.presentation.main.Header
import com.sifu.mysub.presentation.main.ScreenRow
import com.sifu.mysub.presentation.main.TitleProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Locale

/**
 * One service, and what the user can do with it.
 *
 * This is where haveSub is answered, and it is answered *for this service*:
 * subscription.json names its service in subCode, so a subscription to Gemez
 * must not make Youtube look subscribed. Matched, the screen shows the
 * subscription card; unmatched, it shows the plans on offer.
 */
class ServiceViewModel(
    private val serviceCode: String,
    private val getSubscribeMenu: GetSubscribeMenuUseCase,
    private val getSubscription: GetSubscriptionUseCase,
    private val errorMessages: ErrorMessageMapper,
    private val titles: TitleProvider
) : ViewModel() {

    private val _uiState = MutableStateFlow(ServiceUiState())
    val uiState: StateFlow<ServiceUiState> = _uiState.asStateFlow()

    /** Triggered by the screen once it is set up, and by the retry button. */
    fun load() {
        _uiState.update { it.copy(isLoading = true, errorMessage = null) }
        viewModelScope.launch {
            val service = when (val menu = getSubscribeMenu()) {
                is AppResult.Failure -> {
                    showError(errorMessages.map(menu.error))
                    return@launch
                }

                is AppResult.Success -> menu.data.firstOrNull { it.code == serviceCode }
            }

            if (service == null) {
                // The menu changed under the tap; better an honest message than
                // a titled screen with nothing beneath it.
                showError(errorMessages.map(AppError.NotFound()))
                return@launch
            }

            // A failed subscription read leaves this null, which shows the plans
            // — the safe way to be wrong, since it offers rather than claims.
            val subscription = getSubscription().getOrNull()
            if (subscription != null && subscription.covers(service)) {
                showSubscription(service, subscription)
            } else {
                showPlans(service)
            }
        }
    }

    /** subCode names the service the subscription belongs to. */
    private fun Subscription.covers(service: SubscribeService): Boolean =
        hasSubscription && subCode.equals(service.code, ignoreCase = true)

    private fun showSubscription(service: SubscribeService, subscription: Subscription) {
        _uiState.value = ServiceUiState(
            isLoading = false,
            errorMessage = null,
            title = titles.category(),
            header = service.toHeader(),
            rows = listOf(
                ScreenRow.Card(
                    title = service.name,
                    // Two lines, as in the design: the plan, then when it renews.
                    description = listOf(subscription.planName, subscription.renew)
                        .filter { it.isNotEmpty() }
                        .joinToString(LINE_BREAK),
                    isDescriptionVisible = subscription.planName.isNotEmpty() ||
                        subscription.renew.isNotEmpty(),
                    imageUrl = service.imageUrl,
                    logoText = service.code.uppercase(),
                    trailing = subscription.amount,
                    target = CardTarget.SubscriptionDetail
                )
            )
        )
    }

    private fun showPlans(service: SubscribeService) {
        _uiState.value = ServiceUiState(
            isLoading = false,
            errorMessage = null,
            title = titles.category(),
            header = service.toHeader(),
            rows = service.plans.map { plan ->
                ScreenRow.Plan(
                    code = plan.code,
                    name = plan.name,
                    price = formatPrice(plan.price, plan.currency)
                )
            }
        )
    }

    private fun showError(message: String) {
        _uiState.value = ServiceUiState(
            isLoading = false,
            errorMessage = message,
            title = titles.category(),
            rows = emptyList()
        )
    }

    private fun SubscribeService.toHeader() = Header.Brand(
        logoText = code.uppercase(),
        name = name
    )

    /** 0.28 + "USD" -> "0.28 USD". Locale.US so the separator is a dot, not a comma. */
    private fun formatPrice(price: Double, currency: String): String {
        val amount = String.format(Locale.US, "%.2f", price)
        return if (currency.isEmpty()) amount else amount + " " + currency
    }

    private companion object {
        const val LINE_BREAK = "\n"
    }

    class Factory(
        private val serviceCode: String,
        private val getSubscribeMenu: GetSubscribeMenuUseCase,
        private val getSubscription: GetSubscriptionUseCase,
        private val errorMessages: ErrorMessageMapper,
        private val titles: TitleProvider
    ) : ViewModelProvider.Factory {

        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            require(modelClass.isAssignableFrom(ServiceViewModel::class.java)) {
                "Unknown ViewModel class: " + modelClass.name
            }
            return ServiceViewModel(
                serviceCode, getSubscribeMenu, getSubscription, errorMessages, titles
            ) as T
        }
    }
}
