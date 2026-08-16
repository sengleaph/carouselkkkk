package com.sifu.mysub.data.mapper

import com.sifu.mysub.data.dto.UpgradePlanDto
import com.sifu.mysub.data.dto.UpgradePlanOfferDto
import com.sifu.mysub.domain.model.PlanTheme
import com.sifu.mysub.domain.model.UpgradePlan
import com.sifu.mysub.domain.model.UpgradePlanOffer

object UpgradePlanMapper {

    fun toDomain(dto: UpgradePlanOfferDto): UpgradePlanOffer {
        val plans = dto.plans.orEmpty().mapNotNull(::toDomain)

        return UpgradePlanOffer(
            title = dto.title.orEmpty(),
            brandCode = dto.brandCode.orEmpty(),
            // Guarantee exactly one recommended card so the carousel has a
            // single, unambiguous starting page.
            plans = plans.withSingleRecommendation()
        )
    }

    /** A card with no price is not purchasable, so it is dropped rather than rendered blank. */
    private fun toDomain(dto: UpgradePlanDto): UpgradePlan? {
        val price = dto.price?.trim().orEmpty()
        if (price.isEmpty()) return null

        return UpgradePlan(
            code = dto.planCode?.trim().orEmpty(),
            title = dto.title?.trim().orEmpty(),
            price = price,
            theme = dto.theme.toPlanTheme(),
            isRecommended = dto.recommended == true
        )
    }

    private fun List<UpgradePlan>.withSingleRecommendation(): List<UpgradePlan> {
        if (isEmpty()) return this
        val firstFlagged = indexOfFirst { it.isRecommended }
        val chosen = if (firstFlagged >= 0) firstFlagged else size / 2
        return mapIndexed { index, plan -> plan.copy(isRecommended = index == chosen) }
    }

    private fun String?.toPlanTheme(): PlanTheme = when (this?.trim()?.lowercase()) {
        "blue" -> PlanTheme.BLUE
        "pink" -> PlanTheme.PINK
        else -> PlanTheme.PURPLE
    }
}
