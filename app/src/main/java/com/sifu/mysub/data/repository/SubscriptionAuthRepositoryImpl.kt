package com.sifu.mysub.data.repository

import com.sifu.mysub.core.util.AppError
import com.sifu.mysub.core.util.AppResult
import com.sifu.mysub.core.util.DispatcherProvider
import com.sifu.mysub.data.dto.DataAuthDto
import com.sifu.mysub.data.source.DataAuthLocalDataSource
import com.sifu.mysub.data.source.MalformedRawJsonException
import com.sifu.mysub.domain.model.SubscriptionAuth
import com.sifu.mysub.domain.repository.SubscriptionAuthRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.withContext

class SubscriptionAuthRepositoryImpl(
    private val localDataSource: DataAuthLocalDataSource,
    private val dispatchers: DispatcherProvider
) : SubscriptionAuthRepository {

    override suspend fun getSubscriptionAuth(): AppResult<SubscriptionAuth> =
        withContext(dispatchers.io) {
            try {
                val dto = localDataSource.readDataAuth()

                if (dto.code != DataAuthDto.CODE_SUCCESS) {
                    return@withContext AppResult.Failure(
                        AppError.Business(code = dto.code, message = dto.msg)
                    )
                }

                AppResult.Success(
                    SubscriptionAuth(
                        pin = dto.pin?.trim().orEmpty(),
                        accessLink = dto.accessLink?.trim().orEmpty()
                    )
                )
            } catch (e: CancellationException) {
                throw e
            } catch (e: MalformedRawJsonException) {
                AppResult.Failure(AppError.Parsing(e.message))
            } catch (e: Throwable) {
                AppResult.Failure(AppError.Unknown(e.message))
            }
        }
}
