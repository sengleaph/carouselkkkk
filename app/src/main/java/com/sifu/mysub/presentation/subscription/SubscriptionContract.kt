package com.sifu.mysub.presentation.subscription

import com.sifu.mysub.domain.model.DetailRow
import com.sifu.mysub.domain.model.Subscription

/**
 * The screen contract: one immutable state object, one event stream,
 * one set of user intents.
 */
data class SubscriptionUiState(
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
    /** Toolbar title; blank means "fall back to the default string resource". */
    val categoryName: String = "",
    /** Text inside the dark pill on the logo, e.g. "GEMEZ". */
    val brandCode: String = "",
    /** Caption under the logo, e.g. "Gemez". */
    val brandName: String = "",
    val planName: String = "",
    val amount: String = "",
    val renew: String = "",
    val hasSubscription: Boolean = false,
    val rows: List<DetailRow> = emptyList()
) {
    val isContentVisible: Boolean get() = !isLoading && errorMessage == null

    val isCancelVisible: Boolean get() = isContentVisible && hasSubscription

    val isEmpty: Boolean get() = isContentVisible && rows.isEmpty()

    companion object {
        fun content(subscription: Subscription) = SubscriptionUiState(
            isLoading = false,
            errorMessage = null,
            categoryName = subscription.categoryName,
            brandCode = subscription.subCode.uppercase(),
            brandName = subscription.subCode
                .replaceFirstChar { it.uppercaseChar() },
            planName = subscription.planName,
            amount = subscription.amount,
            renew = subscription.renew,
            hasSubscription = subscription.hasSubscription,
            rows = subscription.rows
        )

        fun failure(message: String) =
            SubscriptionUiState(isLoading = false, errorMessage = message)
    }
}

/** Fire-once effects — must not replay on rotation, so they are not part of state. */
sealed interface SubscriptionUiEvent {
    data class CopyToClipboard(val label: String, val value: String) : SubscriptionUiEvent
    data class OpenUrl(val url: String) : SubscriptionUiEvent

    /** Overflow-menu destinations. */
    data object NavigateToEditAccount : SubscriptionUiEvent
    data object NavigateToUpgradePlan : SubscriptionUiEvent
}

/** Everything the View can ask the ViewModel to do. */
sealed interface SubscriptionIntent {
    data object Load : SubscriptionIntent
    data object Retry : SubscriptionIntent
    data class RowClicked(val row: DetailRow) : SubscriptionIntent

    /** Overflow-menu selections. */
    data object EditAccountClicked : SubscriptionIntent
    data object UpgradePlanClicked : SubscriptionIntent
}
