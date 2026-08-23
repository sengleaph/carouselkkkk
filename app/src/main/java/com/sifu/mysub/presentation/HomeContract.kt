package com.sifu.mysub.presentation

import com.sifu.mysub.domain.model.Subscription

/**
 * Home's whole contract is one immutable state object. There is no event
 * channel and no intent type: the only side effect on this screen is
 * navigation, which fires synchronously from a tap rather than from an
 * async result, so it cannot be replayed on rotation.
 */
data class HomeUiState(
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
    val hasSubscription: Boolean = false,
    /** Text inside the dark pill on the logo, e.g. "GEMEZ". */
    val brandCode: String = "",
    /** Caption beside the logo, e.g. "Gemez". */
    val brandName: String = "",
    val planName: String = "",
    val amount: String = "",
    val renew: String = ""
) {
    val isContentVisible: Boolean get() = !isLoading && errorMessage == null

    /** The card exists only when the user actually has a subscription. */
    val isCardVisible: Boolean get() = isContentVisible && hasSubscription

    val isEmptyVisible: Boolean get() = isContentVisible && !hasSubscription

    companion object {
        fun content(subscription: Subscription) = HomeUiState(
            isLoading = false,
            errorMessage = null,
            hasSubscription = subscription.hasSubscription,
            brandCode = subscription.subCode.uppercase(),
            brandName = subscription.subCode.replaceFirstChar { it.uppercaseChar() },
            planName = subscription.planName,
            amount = subscription.amount,
            renew = subscription.renew
        )

        fun failure(message: String) =
            HomeUiState(isLoading = false, errorMessage = message)
    }
}
