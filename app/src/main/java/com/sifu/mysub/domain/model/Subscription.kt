package com.sifu.mysub.domain.model

/**
 * Domain entity — already typed and validated.
 *
 * Note there are no `"true"`/`"NONE"` strings here: the raw JSON quirks are
 * normalised by the data layer's mapper, so nothing above it has to know
 * the transport format.
 */
data class Subscription(
    val subCode: String,
    val categoryName: String,
    val planName: String,
    val regId: String,
    val accountId: String,
    val subscribeToken: String,
    val amount: String,
    val renew: String,
    val hasSubscription: Boolean,
    val rows: List<DetailRow>
) {
    val pin: String?
        get() = rows.firstOrNull { it.style == RowStyle.COPYABLE }?.value

    val accessLink: String?
        get() = rows.firstOrNull { it.style == RowStyle.LINK }?.value

    companion object {
        val EMPTY = Subscription(
            subCode = "",
            categoryName = "",
            planName = "",
            regId = "",
            accountId = "",
            subscribeToken = "",
            amount = "",
            renew = "",
            hasSubscription = false,
            rows = emptyList()
        )
    }
}

data class DetailRow(
    val title: String,
    val value: String,
    val emphasis: RowEmphasis = RowEmphasis.NORMAL,
    val style: RowStyle = RowStyle.PLAIN,
    val bold: Boolean = false,
    val showDivider: Boolean = false,
    val remark: String? = null
)

/** Maps the transport's `color` field. */
enum class RowEmphasis { NORMAL, NEGATIVE }

/** Maps the transport's `style` field — drives what a row tap does. */
enum class RowStyle { PLAIN, COPYABLE, LINK }

/** Business outcome of tapping a row. */
sealed interface RowAction {
    data class Copy(val label: String, val value: String) : RowAction
    data class OpenLink(val url: String) : RowAction
    data object None : RowAction
}
