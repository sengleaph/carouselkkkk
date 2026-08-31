package com.sifu.mysub.presentation.detail

import com.sifu.mysub.presentation.main.ScreenRow

/**
 * State only — no event channel, no effects.
 *
 * Reuses [ScreenRow] rather than declaring its own row type: this screen shows
 * the same card the main screen does, plus detail rows.
 */
data class SubscriptionDetailUiState(
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
    val title: String = "",
    val rows: List<ScreenRow> = emptyList()
) {
    val isContentVisible: Boolean get() = !isLoading && errorMessage == null && rows.isNotEmpty()
}
