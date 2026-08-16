package com.sifu.mysub.domain.usecase

import com.sifu.mysub.domain.model.DetailRow
import com.sifu.mysub.domain.model.RowAction
import com.sifu.mysub.domain.model.RowStyle

/**
 * "What happens when a row is tapped" is a business rule, not a View concern,
 * so it lives here and is unit-testable without Android.
 */
class ResolveRowActionUseCase {

    operator fun invoke(row: DetailRow): RowAction = when {
        row.value.isBlank() -> RowAction.None

        row.style == RowStyle.COPYABLE -> RowAction.Copy(row.title, row.value)

        row.style == RowStyle.LINK && isSupportedUrl(row.value) ->
            RowAction.OpenLink(row.value)

        else -> RowAction.None
    }

    private fun isSupportedUrl(value: String): Boolean =
        value.startsWith("http://", ignoreCase = true) ||
            value.startsWith("https://", ignoreCase = true)
}
