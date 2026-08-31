package com.sifu.mysub.presentation.main

import com.sifu.mysub.core.util.AppError

/** Maps a domain error to a user-facing string; the container supplies the resources. */
fun interface ErrorMessageMapper {
    fun map(error: AppError): String
}

/**
 * Screen captions, injected rather than read from `R` inside the ViewModel, so
 * the ViewModel stays free of Android resources and unit-testable.
 */
interface TitleProvider {
    /** The toolbar caption, e.g. "Entertainment" — the same in both modes. */
    fun category(): String
}
