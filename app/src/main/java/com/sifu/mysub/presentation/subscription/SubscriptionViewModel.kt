package com.sifu.mysub.presentation.subscription

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.sifu.mysub.core.util.AppError
import com.sifu.mysub.core.util.onFailure
import com.sifu.mysub.core.util.onSuccess
import com.sifu.mysub.domain.model.DetailRow
import com.sifu.mysub.domain.model.RowAction
import com.sifu.mysub.domain.usecase.GetSubscriptionUseCase
import com.sifu.mysub.domain.usecase.ResolveRowActionUseCase
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

/**
 * Depends only on use cases and an [ErrorMessageMapper] — no Android framework
 * types, no repository, no Gson. That is what makes it unit-testable on the JVM.
 */
class SubscriptionViewModel(
    private val getSubscription: GetSubscriptionUseCase,
    private val resolveRowAction: ResolveRowActionUseCase,
    private val errorMessages: ErrorMessageMapper
) : ViewModel() {

    private val _uiState = MutableStateFlow(SubscriptionUiState())
    val uiState: StateFlow<SubscriptionUiState> = _uiState.asStateFlow()

    private val _events = Channel<SubscriptionUiEvent>(Channel.BUFFERED)
    val events: Flow<SubscriptionUiEvent> = _events.receiveAsFlow()

    init {
        onIntent(SubscriptionIntent.Load)
    }

    fun onIntent(intent: SubscriptionIntent) {
        when (intent) {
            SubscriptionIntent.Load, SubscriptionIntent.Retry -> load()

            is SubscriptionIntent.RowClicked -> handleRow(intent.row)

            SubscriptionIntent.EditAccountClicked ->
                emit(SubscriptionUiEvent.NavigateToEditAccount)

            SubscriptionIntent.UpgradePlanClicked ->
                emit(SubscriptionUiEvent.NavigateToUpgradePlan)
        }
    }

    private fun load() {
        _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
        viewModelScope.launch {
            getSubscription()
                .onSuccess { _uiState.value = SubscriptionUiState.content(it) }
                .onFailure { _uiState.value = SubscriptionUiState.failure(errorMessages.map(it)) }
        }
    }

    private fun handleRow(row: DetailRow) {
        when (val action = resolveRowAction(row)) {
            is RowAction.Copy -> emit(
                SubscriptionUiEvent.CopyToClipboard(action.label, action.value)
            )

            is RowAction.OpenLink -> emit(SubscriptionUiEvent.OpenUrl(action.url))

            RowAction.None -> Unit
        }
    }

    private fun emit(event: SubscriptionUiEvent) {
        viewModelScope.launch { _events.send(event) }
    }

    class Factory(
        private val getSubscription: GetSubscriptionUseCase,
        private val resolveRowAction: ResolveRowActionUseCase,
        private val errorMessages: ErrorMessageMapper
    ) : ViewModelProvider.Factory {

        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            require(modelClass.isAssignableFrom(SubscriptionViewModel::class.java)) {
                "Unknown ViewModel class: ${modelClass.name}"
            }
            return SubscriptionViewModel(
                getSubscription,
                resolveRowAction,
                errorMessages
            ) as T
        }
    }
}

/**
 * Turns a domain [AppError] into a user-facing string. Implemented in the
 * presentation layer because only it knows about resources and locale.
 */
fun interface ErrorMessageMapper {
    fun map(error: AppError): String
}
