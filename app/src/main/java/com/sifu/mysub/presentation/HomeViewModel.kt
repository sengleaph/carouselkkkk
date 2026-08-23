package com.sifu.mysub.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.sifu.mysub.core.util.onFailure
import com.sifu.mysub.core.util.onSuccess
import com.sifu.mysub.domain.usecase.GetSubscriptionUseCase
import com.sifu.mysub.presentation.subscription.ErrorMessageMapper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * State-only ViewModel: one [StateFlow] out, plain functions in.
 *
 * Reuses [GetSubscriptionUseCase] — Home needs the same entity as the detail
 * screen, just a much smaller slice of it. No second repository, no duplicated
 * parsing.
 */
class HomeViewModel(
    private val getSubscription: GetSubscriptionUseCase,
    private val errorMessages: ErrorMessageMapper
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        load()
    }

    /** Called on first composition and by the retry button. */
    fun load() {
        _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
        viewModelScope.launch {
            getSubscription()
                .onSuccess { _uiState.value = HomeUiState.content(it) }
                .onFailure { _uiState.value = HomeUiState.failure(errorMessages.map(it)) }
        }
    }

    /**
     * "May the detail screen be opened?" is a rule, not a View concern, so the
     * View asks rather than deciding. Guards against a tap queued just before
     * the state flipped to no-subscription.
     */
    fun canOpenSubscription(): Boolean = _uiState.value.hasSubscription

    class Factory(
        private val getSubscription: GetSubscriptionUseCase,
        private val errorMessages: ErrorMessageMapper
    ) : ViewModelProvider.Factory {

        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            require(modelClass.isAssignableFrom(HomeViewModel::class.java)) {
                "Unknown ViewModel class: ${modelClass.name}"
            }
            return HomeViewModel(getSubscription, errorMessages) as T
        }
    }
}
