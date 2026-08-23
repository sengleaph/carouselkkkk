package com.sifu.mysub.domain.repository

import com.sifu.mysub.core.util.AppResult
import com.sifu.mysub.domain.model.SuccessAuthModel

interface SuccessAuthRepository {
    suspend fun getSuccessAuth(): AppResult<SuccessAuthModel>
}