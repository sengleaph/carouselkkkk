package com.sifu.mysub.core.util

import kotlin.coroutines.cancellation.CancellationException

/**
 * Layer-agnostic result wrapper. Lives in `core` so domain can return it
 * without depending on data or presentation.
 */
sealed interface AppResult<out T> {

    data class Success<out T>(val data: T) : AppResult<T>

    data class Failure(val error: AppError) : AppResult<Nothing>
}

fun <T> AppResult<T>.getOrNull(): T? = (this as? AppResult.Success)?.data

fun <T> AppResult<T>.errorOrNull(): AppError? = (this as? AppResult.Failure)?.error

val AppResult<*>.isSuccess: Boolean get() = this is AppResult.Success

inline fun <T, R> AppResult<T>.map(transform: (T) -> R): AppResult<R> = when (this) {
    is AppResult.Success -> AppResult.Success(transform(data))
    is AppResult.Failure -> this
}

inline fun <T> AppResult<T>.onSuccess(action: (T) -> Unit): AppResult<T> = apply {
    if (this is AppResult.Success) action(data)
}

inline fun <T> AppResult<T>.onFailure(action: (AppError) -> Unit): AppResult<T> = apply {
    if (this is AppResult.Failure) action(error)
}

/** Domain-level error taxonomy — no exceptions leak past the data layer. */
sealed class AppError(open val message: String?) {

    data class NotFound(override val message: String? = null) : AppError(message)

    data class Parsing(override val message: String? = null) : AppError(message)

    data class Business(val code: String?, override val message: String?) : AppError(message)

    data class Unknown(override val message: String? = null) : AppError(message)
}

inline fun <T> runResult(block: () -> T): AppResult<T> = try {
    AppResult.Success(block())
} catch (e: CancellationException) {
    throw e
} catch (t: Throwable) {
    AppResult.Failure(AppError.Unknown(t.message))
}
