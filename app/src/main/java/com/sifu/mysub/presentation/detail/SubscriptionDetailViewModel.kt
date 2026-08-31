package com.sifu.mysub.presentation.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.sifu.mysub.core.util.onFailure
import com.sifu.mysub.core.util.onSuccess
import com.sifu.mysub.domain.model.DetailRow
import com.sifu.mysub.domain.model.RowEmphasis
import com.sifu.mysub.domain.model.RowStyle
import com.sifu.mysub.domain.model.Subscription
import com.sifu.mysub.domain.model.SubscriptionAuth
import com.sifu.mysub.core.util.getOrNull
import com.sifu.mysub.domain.usecase.GetSubscriptionAuthUseCase
import com.sifu.mysub.domain.usecase.GetSubscriptionUseCase
import com.sifu.mysub.presentation.main.ErrorMessageMapper
import com.sifu.mysub.presentation.main.ScreenRow
import com.sifu.mysub.presentation.main.ValueEmphasis
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * The subscription's detail rows, reached by tapping its card.
 *
 * Reloads from the same use case the card came from rather than accepting a
 * serialized row: the card is a display model that may be stale by the time it
 * is tapped.
 */
class SubscriptionDetailViewModel(
    private val getSubscription: GetSubscriptionUseCase,
    private val getSubscriptionAuth: GetSubscriptionAuthUseCase,
    private val errorMessages: ErrorMessageMapper
) : ViewModel() {

    private val _uiState = MutableStateFlow(SubscriptionDetailUiState())
    val uiState: StateFlow<SubscriptionDetailUiState> = _uiState.asStateFlow()

    /** Triggered by the screen once it is set up, and by the retry button. */
    fun load() {
        _uiState.update { it.copy(isLoading = true, errorMessage = null) }
        viewModelScope.launch {
            getSubscription()
                .onSuccess { subscription ->
                    // Secondary: a failed auth read leaves the placeholders in
                    // place rather than blanking a screen that otherwise loaded.
                    val auth = getSubscriptionAuth().getOrNull() ?: SubscriptionAuth.EMPTY
                    _uiState.value = SubscriptionDetailUiState(
                        isLoading = false,
                        errorMessage = null,
                        title = subscription.planName,
                        rows = subscription.toRows(auth)
                    )
                }
                .onFailure { error ->
                    _uiState.value = SubscriptionDetailUiState(
                        isLoading = false,
                        errorMessage = errorMessages.map(error),
                        rows = emptyList()
                    )
                }
        }
    }

    /** The same card the user tapped, then the detail rows beneath it. */
    private fun Subscription.toRows(auth: SubscriptionAuth): List<ScreenRow> = buildList {
        add(
            ScreenRow.Card(
                title = planName,
                description = renew,
                isDescriptionVisible = renew.isNotEmpty(),
                imageUrl = "",
                logoText = subCode.uppercase(),
                trailing = amount,
                // Already here: no chevron, no ripple, no tap.
                target = null
            )
        )
        rows.forEach { row -> add(row.toScreenRow(auth)) }
    }

    private fun DetailRow.toScreenRow(auth: SubscriptionAuth) = ScreenRow.Detail(
        title = title,
        value = auth.valueFor(this) ?: value,
        emphasis = resolveEmphasis(),
        isBold = isBold,
        hasDivider = hasDivider
    )

    /**
     * dataauth.json is the authority for the pin and the access link, both of
     * which subscription.json ships as an "X" placeholder.
     *
     * Matched on [RowStyle], not on the title: "Pin" and "Access Link" are
     * display text that a translated feed would change, while the style is what
     * already marks the value as copyable or tappable. Returns null when there
     * is nothing to substitute, so the caller keeps the original value.
     */
    private fun SubscriptionAuth.valueFor(row: DetailRow): String? = when (row.style) {
        RowStyle.COPYABLE -> pin.ifEmpty { null }
        RowStyle.LINK -> accessLink.ifEmpty { null }
        RowStyle.PLAIN -> null
    }

    /**
     * A negative amount reads as negative first; the copy/link affordances only
     * apply to values that are not already flagged red.
     */
    private fun DetailRow.resolveEmphasis(): ValueEmphasis = when {
        emphasis == RowEmphasis.NEGATIVE -> ValueEmphasis.NEGATIVE
        style == RowStyle.COPYABLE -> ValueEmphasis.COPYABLE
        style == RowStyle.LINK -> ValueEmphasis.LINK
        else -> ValueEmphasis.PLAIN
    }

    class Factory(
        private val getSubscription: GetSubscriptionUseCase,
        private val getSubscriptionAuth: GetSubscriptionAuthUseCase,
        private val errorMessages: ErrorMessageMapper
    ) : ViewModelProvider.Factory {

        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            require(modelClass.isAssignableFrom(SubscriptionDetailViewModel::class.java)) {
                "Unknown ViewModel class: ${modelClass.name}"
            }
            return SubscriptionDetailViewModel(
                getSubscription, getSubscriptionAuth, errorMessages
            ) as T
        }
    }
}
