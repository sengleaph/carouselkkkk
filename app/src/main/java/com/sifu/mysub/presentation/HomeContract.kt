package com.sifu.mysub.presentation

import com.sifu.mysub.domain.model.Subscription
import com.sifu.mysub.domain.model.UpgradePlanOffer

/**
 * Home's whole contract is one immutable state object. There is no event
 * channel and no intent type: the screen reads the upgrade offer and draws it,
 * and has no side effects to sequence.
 */
data class HomeUiState(
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
    /**
     * `haveSub` from subscription.json — the only thing Home reads that file
     * for. It decides where a plan tap lands, and nothing on screen shows it,
     * so it never gates loading or the error block.
     */
    val hasSubscription: Boolean = false,
    /** Heading above the upgrade plan cards, e.g. "Gemezz Upgrade Plan". */
    val planOfferTitle: String = "",
    val plans: List<PlanRow> = emptyList()
) {
    val isContentVisible: Boolean get() = !isLoading && errorMessage == null

    val arePlansVisible: Boolean get() = isContentVisible && plans.isNotEmpty()

    /** Where tapping a plan card should go. */
    val planDestination: HomeDestination
        get() = if (hasSubscription) {
            HomeDestination.SUBSCRIPTION
        } else {
            HomeDestination.NO_SUBSCRIPTION
        }

    fun loading() = copy(isLoading = true, errorMessage = null)

    fun withSubscriptionFlag(subscription: Subscription) =
        copy(hasSubscription = subscription.hasSubscription)

    fun withPlans(offer: UpgradePlanOffer) = copy(
        isLoading = false,
        errorMessage = null,
        planOfferTitle = offer.title,
        plans = offer.plans.map { plan ->
            PlanRow(
                code = plan.code,
                title = plan.title,
                price = plan.price,
                brandCode = offer.brandCode,
                isRecommended = plan.isRecommended
            )
        }
    )

    /**
     * The offer is the whole screen now, so a failure surfaces as the error
     * block and its retry button rather than being swallowed.
     */
    fun withFailure(message: String) = copy(
        isLoading = false,
        errorMessage = message,
        planOfferTitle = "",
        plans = emptyList()
    )
}

/**
 * The two screens a plan tap can open.
 *
 * Named by intent rather than by Activity so the routing rule stays a
 * presentation decision and the class names live only in the View.
 */
enum class HomeDestination { SUBSCRIPTION, NO_SUBSCRIPTION }

/**
 * One upgrade plan as Home draws it.
 *
 * Carries the offer's brand code per row because that is what the row's logo
 * pill shows; the domain keeps it on the offer, where it belongs, and the
 * flattening happens here at the edge. The code goes in exactly as the JSON
 * spells it -- the offer already supplies "GEMEZZ".
 */
data class PlanRow(
    val code: String,
    val title: String,
    val price: String,
    val brandCode: String,
    val isRecommended: Boolean
)
