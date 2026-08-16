package com.sifu.mysub.domain.model

/** A single card in the upgrade carousel. */
data class UpgradePlan(
    val code: String,
    val title: String,
    val price: String,
    val theme: PlanTheme,
    /** The card the carousel opens on. */
    val isRecommended: Boolean
)

/**
 * Artwork variant. The domain names the *intent*; the presentation layer decides
 * which drawable that maps to — same split as [RowEmphasis].
 */
enum class PlanTheme { PURPLE, BLUE, PINK }

data class UpgradePlanOffer(
    val title: String,
    val brandCode: String,
    val plans: List<UpgradePlan>
) {
    /** Index the carousel should settle on, or 0 when nothing is flagged. */
    val recommendedIndex: Int
        get() = plans.indexOfFirst { it.isRecommended }.coerceAtLeast(0)

    companion object {
        val EMPTY = UpgradePlanOffer(title = "", brandCode = "", plans = emptyList())
    }
}
