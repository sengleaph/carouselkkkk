package com.sifu.mysub.data.repository

import com.sifu.mysub.core.util.AppError
import com.sifu.mysub.core.util.AppResult
import com.sifu.mysub.core.util.DispatcherProvider
import com.sifu.mysub.data.dto.SubscribeMenuResponseDto
import com.sifu.mysub.data.mapper.SubscribeMenuMapper
import com.sifu.mysub.data.source.MalformedRawJsonException
import com.sifu.mysub.data.source.SubscribeMenuLocalDataSource
import com.sifu.mysub.domain.model.SubscribeService
import com.sifu.mysub.domain.repository.SubscribeMenuRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.withContext

class SubscribeMenuRepositoryImpl(
    private val localDataSource: SubscribeMenuLocalDataSource,
    private val dispatchers: DispatcherProvider
) : SubscribeMenuRepository {

    override suspend fun getSubscribeMenu(): AppResult<List<SubscribeService>> =
        withContext(dispatchers.io) {
            try {
                val dto = localDataSource.readSubscribeMenu()

                if (dto.code != SubscribeMenuResponseDto.CODE_SUCCESS) {
                    return@withContext AppResult.Failure(
                        AppError.Business(code = dto.code, message = dto.msg)
                    )
                }

                val services = SubscribeMenuMapper.toDomain(dto)
                if (services.isEmpty()) {
                    return@withContext AppResult.Failure(AppError.NotFound())
                }

                AppResult.Success(services)
            } catch (e: CancellationException) {
                throw e
            } catch (e: MalformedRawJsonException) {
                AppResult.Failure(AppError.Parsing(e.message))
            } catch (e: Throwable) {
                AppResult.Failure(AppError.Unknown(e.message))
            }
        }
}
