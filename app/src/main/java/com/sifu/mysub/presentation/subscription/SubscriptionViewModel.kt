package com.sifu.mysub.presentation.subscription

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.sifu.mysub.core.util.AppError
import com.sifu.mysub.core.util.getOrNull
import com.sifu.mysub.core.util.onFailure
import com.sifu.mysub.core.util.onSuccess
import com.sifu.mysub.domain.model.DetailRow
import com.sifu.mysub.domain.model.RowAction
import com.sifu.mysub.domain.model.RowStyle
import com.sifu.mysub.domain.model.Subscription
import com.sifu.mysub.domain.model.SuccessAuthModel
import com.sifu.mysub.domain.usecase.GetSubscriptionUseCase
import com.sifu.mysub.domain.usecase.GetSuccessAuthUseCase
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
    private val getSuccessAuth: GetSuccessAuthUseCase,
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
            val auth = getSuccessAuth().getOrNull()
            getSubscription()
                .onSuccess {  _uiState.value = SubscriptionUiState.content(
                    it.withDescriptionRow().withAuth(auth)
                ) }
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

    private fun Subscription.withAuth(auth: SuccessAuthModel?): Subscription {
        if (auth == null) return this

        val link = auth.accessLink?.takeIf { it.isNotBlank() }
        val pin = auth.pin?.takeIf { it.isNotBlank() }
        val key = auth.key?.takeIf { it.isNotBlank() }
        if (link == null && pin == null && key == null) return this

        val updated = rows.map { row ->
            when {
                row.style == RowStyle.LINK && link != null -> row.copy(value = link)
                row.style == RowStyle.COPYABLE && pin != null -> row.copy(value = pin)
                row.title.equals(ACCOUNT_NO, ignoreCase = true) && key != null ->
                    row.copy(value = key)
                else -> row
            }
        }
        return copy(rows = updated)
    }


    private fun emit(event: SubscriptionUiEvent) {
        viewModelScope.launch { _events.send(event) }
    }

    class Factory(
        private val getSubscription: GetSubscriptionUseCase,
        private val resolveRowAction: ResolveRowActionUseCase,
        private val getSuccessAuth: GetSuccessAuthUseCase,
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
                getSuccessAuth,
                errorMessages
            ) as T
        }
    }
    private companion object {
        const val DESCRIPTION = "Description"
        const val ACCOUNT_NO = "Account No."
        const val RECEIVER = "Receiver"
    }
}

/**
 * Turns a domain [AppError] into a user-facing string. Implemented in the
 * presentation layer because only it knows about resources and locale.
 */
fun interface ErrorMessageMapper {
    fun map(error: AppError): String
}
