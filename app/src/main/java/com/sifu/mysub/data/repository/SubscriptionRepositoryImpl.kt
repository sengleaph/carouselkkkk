package com.sifu.mysub.data.repository

import com.sifu.mysub.core.util.AppError
import com.sifu.mysub.core.util.AppResult
import com.sifu.mysub.core.util.DispatcherProvider
import com.sifu.mysub.data.dto.SubscriptionDto
import com.sifu.mysub.data.mapper.SubscriptionMapper
import com.sifu.mysub.data.source.MalformedSubscriptionException
import com.sifu.mysub.data.source.SubscriptionLocalDataSource
import com.sifu.mysub.domain.model.Subscription
import com.sifu.mysub.domain.repository.SubscriptionRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.withContext

/**
 * Implements the domain contract. Owns three responsibilities:
 * threading, DTO -> entity mapping, and turning exceptions into [AppError].
 */
class SubscriptionRepositoryImpl(
    private val localDataSource: SubscriptionLocalDataSource,
    private val dispatchers: DispatcherProvider
) : SubscriptionRepository {

    override suspend fun getSubscription(): AppResult<Subscription> =
        withContext(dispatchers.io) {
            try {
                val dto = localDataSource.readSubscription()

                if (dto.code != SubscriptionDto.CODE_SUCCESS) {
                    return@withContext AppResult.Failure(
                        AppError.Business(code = dto.code, message = dto.msg)
                    )
                }

                AppResult.Success(SubscriptionMapper.toDomain(dto))
            } catch (e: CancellationException) {
                throw e
            } catch (e: MalformedSubscriptionException) {
                AppResult.Failure(AppError.Parsing(e.message))
            } catch (e: Throwable) {
                AppResult.Failure(AppError.Unknown(e.message))
            }
        }
}
