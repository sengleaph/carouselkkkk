package com.sifu.mysub.presentation.service

import com.sifu.mysub.presentation.main.Header
import com.sifu.mysub.presentation.main.ScreenRow

/**
 * State only — no event channel, no effects.
 *
 * Reuses [ScreenRow] and [Header] rather than declaring parallel types: this
 * screen shows the same cards the browse screen does, under the same header.
 */
data class ServiceUiState(
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
    val title: String = "",
    val header: Header = Header.None,
    val rows: List<ScreenRow> = emptyList()
) {
    val isContentVisible: Boolean get() = !isLoading && errorMessage == null && rows.isNotEmpty()
}
