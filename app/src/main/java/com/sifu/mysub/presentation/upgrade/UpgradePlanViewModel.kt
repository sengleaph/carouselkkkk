package com.sifu.mysub.presentation.upgrade

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.sifu.mysub.core.util.onFailure
import com.sifu.mysub.core.util.onSuccess
import com.sifu.mysub.domain.usecase.GetUpgradePlansUseCase
import com.sifu.mysub.presentation.subscription.ErrorMessageMapper
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

class UpgradePlanViewModel(
    private val getUpgradePlans: GetUpgradePlansUseCase,
    private val errorMessages: ErrorMessageMapper
) : ViewModel() {

    private val _uiState = MutableStateFlow(UpgradePlanUiState())
    val uiState: StateFlow<UpgradePlanUiState> = _uiState.asStateFlow()

    private val _events = Channel<UpgradePlanUiEvent>(Channel.BUFFERED)
    val events: Flow<UpgradePlanUiEvent> = _events.receiveAsFlow()

    init {
        onIntent(UpgradePlanIntent.Load)
    }

    fun onIntent(intent: UpgradePlanIntent) {
        when (intent) {
            UpgradePlanIntent.Load -> load()

            is UpgradePlanIntent.PageSelected ->
                _uiState.value = _uiState.value.copy(selectedIndex = intent.index)

            UpgradePlanIntent.Confirm ->
                _uiState.value.selectedPlan?.let { emit(UpgradePlanUiEvent.Confirmed(it)) }

            UpgradePlanIntent.Close -> emit(UpgradePlanUiEvent.Dismiss)
        }
    }

    private fun load() {
        _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
        viewModelScope.launch {
            getUpgradePlans()
                // selectedIndex starts at the recommended card; the View settles
                // the carousel there once the list is actually committed.
                .onSuccess { _uiState.value = UpgradePlanUiState.content(it) }
                .onFailure {
                    _uiState.value = UpgradePlanUiState.failure(errorMessages.map(it))
                }
        }
    }

    private fun emit(event: UpgradePlanUiEvent) {
        viewModelScope.launch { _events.send(event) }
    }

    class Factory(
        private val getUpgradePlans: GetUpgradePlansUseCase,
        private val errorMessages: ErrorMessageMapper
    ) : ViewModelProvider.Factory {

        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            require(modelClass.isAssignableFrom(UpgradePlanViewModel::class.java)) {
                "Unknown ViewModel class: ${modelClass.name}"
            }
            return UpgradePlanViewModel(getUpgradePlans, errorMessages) as T
        }
    }
}
