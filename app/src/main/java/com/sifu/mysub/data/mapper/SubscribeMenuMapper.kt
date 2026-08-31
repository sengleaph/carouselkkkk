package com.sifu.mysub.data.mapper

import com.sifu.mysub.data.dto.SubscribeMenuResponseDto
import com.sifu.mysub.data.dto.SubscribePlanDto
import com.sifu.mysub.data.dto.SubscribeServiceDto
import com.sifu.mysub.domain.model.SubscribePlan
import com.sifu.mysub.domain.model.SubscribeService

object SubscribeMenuMapper {

    fun toDomain(dto: SubscribeMenuResponseDto): List<SubscribeService> =
        dto.subscribeMenu.orEmpty().mapNotNull(::toDomain)

    private fun toDomain(dto: SubscribeServiceDto): SubscribeService? {
        val name = dto.name?.trim().orEmpty()
        if (name.isEmpty()) return null

        val plans = dto.plans.orEmpty().mapNotNull(::toDomain)
        if (plans.isEmpty()) return null

        return SubscribeService(
            code = dto.code?.trim().orEmpty(),
            name = name,
            imageUrl = dto.image?.trim().orEmpty(),
            description = dto.description?.trim().orEmpty(),
            plans = plans
        )
    }

    /** No price means nothing to charge, so the row is dropped rather than shown blank. */
    private fun toDomain(dto: SubscribePlanDto): SubscribePlan? {
        val price = dto.price ?: return null
        // "Monthly " ships with a trailing space; trimming here keeps it out of the UI.
        val name = dto.name?.trim().orEmpty()
        if (name.isEmpty()) return null

        return SubscribePlan(
            code = dto.code?.trim().orEmpty(),
            name = name,
            price = price,
            currency = dto.ccy?.trim().orEmpty()
        )
    }
}
