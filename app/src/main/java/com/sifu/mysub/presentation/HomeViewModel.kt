package com.sifu.mysub.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.sifu.mysub.core.util.onFailure
import com.sifu.mysub.core.util.onSuccess
import com.sifu.mysub.domain.usecase.GetSubscriptionUseCase
import com.sifu.mysub.domain.usecase.GetUpgradePlansUseCase
import com.sifu.mysub.presentation.subscription.ErrorMessageMapper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * State-only ViewModel: one [StateFlow] out, plain functions in.
 *
 * Reuses both existing use cases rather than adding repositories:
 * [GetUpgradePlansUseCase] is the one behind the upgrade sheet, and
 * [GetSubscriptionUseCase] is the detail screen's — Home wants a single boolean
 * out of it, `haveSub`, to decide where a plan tap goes.
 */
class HomeViewModel(
    private val getUpgradePlans: GetUpgradePlansUseCase,
    private val getSubscription: GetSubscriptionUseCase,
    private val errorMessages: ErrorMessageMapper
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        load()
    }

    /**
     * Called on first composition and by the retry button.
     *
     * The two reads run in their own coroutines: the offer is what the screen
     * draws, while `haveSub` only has to be settled by the time something is
     * tapped. A failed subscription read therefore leaves the flag false and
     * routes to the no-subscription screen, which is the safe way to be wrong.
     */
    fun load() {
        _uiState.update { it.loading() }

        viewModelScope.launch {
            getUpgradePlans()
                .onSuccess { offer -> _uiState.update { it.withPlans(offer) } }
                .onFailure { error ->
                    _uiState.update { it.withFailure(errorMessages.map(error)) }
                }
        }

        viewModelScope.launch {
            getSubscription().onSuccess { subscription ->
                _uiState.update { it.withSubscriptionFlag(subscription) }
            }
        }
    }

    /**
     * "Where does this tap go?" is a rule, not a View concern, so the View asks
     * rather than reading the flag and deciding for itself.
     */
    fun destinationForPlanTap(): HomeDestination = _uiState.value.planDestination

    class Factory(
        private val getUpgradePlans: GetUpgradePlansUseCase,
        private val getSubscription: GetSubscriptionUseCase,
        private val errorMessages: ErrorMessageMapper
    ) : ViewModelProvider.Factory {

        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            require(modelClass.isAssignableFrom(HomeViewModel::class.java)) {
                "Unknown ViewModel class: ${modelClass.name}"
            }
            return HomeViewModel(getUpgradePlans, getSubscription, errorMessages) as T
        }
    }
}
