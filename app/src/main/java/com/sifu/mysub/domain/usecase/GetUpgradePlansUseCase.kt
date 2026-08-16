package com.sifu.mysub.domain.usecase

import com.sifu.mysub.core.util.AppResult
import com.sifu.mysub.domain.model.UpgradePlanOffer
import com.sifu.mysub.domain.repository.UpgradePlanRepository

class GetUpgradePlansUseCase(
    private val repository: UpgradePlanRepository
) {
    suspend operator fun invoke(): AppResult<UpgradePlanOffer> = repository.getUpgradePlans()
}
