package com.sifu.mysub.data.repository

import com.sifu.mysub.core.util.AppError
import com.sifu.mysub.core.util.AppResult
import com.sifu.mysub.core.util.DispatcherProvider
import com.sifu.mysub.data.mapper.SubscriptionMapper
import com.sifu.mysub.data.source.MalformedSuccessAuthException
import com.sifu.mysub.data.source.SuccessAuthLocalDataSource
import com.sifu.mysub.domain.model.SuccessAuthModel
import com.sifu.mysub.domain.repository.SuccessAuthRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.withContext

class SuccessAuthRepositoryImpl(
    private val localDataSource: SuccessAuthLocalDataSource,
    private val dispatchers: DispatcherProvider
) : SuccessAuthRepository {

    override suspend fun getSuccessAuth(): AppResult<SuccessAuthModel> =
        withContext(dispatchers.io) {
            try {
                val dto = localDataSource.readSuccessAuth()

                if (dto.code != CODE_SUCCESS) {
                    return@withContext AppResult.Failure(
                        AppError.Business(code = dto.code, message = dto.msg)
                    )
                }

                AppResult.Success(SubscriptionMapper.toDomain(dto))
            } catch (e: CancellationException) {
                throw e
            } catch (e: MalformedSuccessAuthException) {
                AppResult.Failure(AppError.Parsing(e.message))
            } catch (e: Throwable) {
                AppResult.Failure(AppError.Unknown(e.message))
            }
        }

    private companion object {
        const val CODE_SUCCESS = "0"
    }
}