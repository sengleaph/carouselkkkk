package com.sifu.mysub.data.repository

import com.sifu.mysub.core.util.AppError
import com.sifu.mysub.core.util.AppResult
import com.sifu.mysub.core.util.DispatcherProvider
import com.sifu.mysub.data.dto.UpgradePlanOfferDto
import com.sifu.mysub.data.mapper.UpgradePlanMapper
import com.sifu.mysub.data.source.MalformedSubscriptionException
import com.sifu.mysub.data.source.UpgradePlanLocalDataSource
import com.sifu.mysub.domain.model.UpgradePlanOffer
import com.sifu.mysub.domain.repository.UpgradePlanRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.withContext

class UpgradePlanRepositoryImpl(
    private val localDataSource: UpgradePlanLocalDataSource,
    private val dispatchers: DispatcherProvider
) : UpgradePlanRepository {

    override suspend fun getUpgradePlans(): AppResult<UpgradePlanOffer> =
        withContext(dispatchers.io) {
            try {
                val dto = localDataSource.readUpgradePlans()

                if (dto.code != UpgradePlanOfferDto.CODE_SUCCESS) {
                    return@withContext AppResult.Failure(
                        AppError.Business(code = dto.code, message = dto.msg)
                    )
                }

                val offer = UpgradePlanMapper.toDomain(dto)
                if (offer.plans.isEmpty()) {
                    return@withContext AppResult.Failure(AppError.NotFound())
                }

                AppResult.Success(offer)
            } catch (e: CancellationException) {
                throw e
            } catch (e: MalformedSubscriptionException) {
                AppResult.Failure(AppError.Parsing(e.message))
            } catch (e: Throwable) {
                AppResult.Failure(AppError.Unknown(e.message))
            }
        }
}
