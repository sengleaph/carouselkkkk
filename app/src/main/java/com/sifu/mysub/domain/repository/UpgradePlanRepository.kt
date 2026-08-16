package com.sifu.mysub.domain.repository

import com.sifu.mysub.core.util.AppResult
import com.sifu.mysub.domain.model.UpgradePlanOffer

interface UpgradePlanRepository {
    suspend fun getUpgradePlans(): AppResult<UpgradePlanOffer>
}
