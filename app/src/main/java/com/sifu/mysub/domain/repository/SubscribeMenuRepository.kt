package com.sifu.mysub.domain.repository

import com.sifu.mysub.core.util.AppResult
import com.sifu.mysub.domain.model.SubscribeService

interface SubscribeMenuRepository {
    suspend fun getSubscribeMenu(): AppResult<List<SubscribeService>>
}
