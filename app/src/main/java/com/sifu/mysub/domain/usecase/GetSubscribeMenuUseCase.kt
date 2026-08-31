package com.sifu.mysub.domain.usecase

import com.sifu.mysub.core.util.AppResult
import com.sifu.mysub.domain.model.SubscribeService
import com.sifu.mysub.domain.repository.SubscribeMenuRepository

class GetSubscribeMenuUseCase(
    private val repository: SubscribeMenuRepository
) {
    suspend operator fun invoke(): AppResult<List<SubscribeService>> =
        repository.getSubscribeMenu()
}
