package com.sifu.mysub.presentation.upgrade

import com.sifu.mysub.domain.model.UpgradePlan
import com.sifu.mysub.domain.model.UpgradePlanOffer

data class UpgradePlanUiState(
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
    val title: String = "",
    val brandCode: String = "",
    val plans: List<UpgradePlan> = emptyList(),
    val selectedIndex: Int = 0
) {
    val isContentVisible: Boolean get() = !isLoading && errorMessage == null && plans.isNotEmpty()

    val selectedPlan: UpgradePlan? get() = plans.getOrNull(selectedIndex)

    companion object {
        fun content(offer: UpgradePlanOffer) = UpgradePlanUiState(
            isLoading = false,
            errorMessage = null,
            title = offer.title,
            brandCode = offer.brandCode,
            plans = offer.plans,
            selectedIndex = offer.recommendedIndex
        )

        fun failure(message: String) =
            UpgradePlanUiState(isLoading = false, errorMessage = message)
    }
}

sealed interface UpgradePlanUiEvent {
    data class Confirmed(val plan: UpgradePlan) : UpgradePlanUiEvent

    data object Dismiss : UpgradePlanUiEvent
}

sealed interface UpgradePlanIntent {
    data object Load : UpgradePlanIntent
    data class PageSelected(val index: Int) : UpgradePlanIntent
    data object Confirm : UpgradePlanIntent
    data object Close : UpgradePlanIntent
}
