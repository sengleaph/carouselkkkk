package com.sifu.mysub.domain.model

/**
 * Domain entity — already typed and validated.
 *
 * No `"true"` or `"NONE"` strings survive here: the transport quirks are
 * normalised by the data layer's mapper, so nothing above it knows the wire
 * format.
 */
data class Subscription(
    val subCode: String,
    val planName: String,
    val amount: String,
    val renew: String,
    /** `haveSub` — the flag that decides which screen the user gets. */
    val hasSubscription: Boolean,
    val rows: List<DetailRow>
)

data class DetailRow(
    val title: String,
    val value: String,
    val emphasis: RowEmphasis,
    val style: RowStyle,
    val isBold: Boolean,
    /** `isLine` — draw a divider under this row. */
    val hasDivider: Boolean
)

/** Colour intent. The domain names the meaning; presentation picks the colour. */
enum class RowEmphasis { NONE, NEGATIVE }

/** What the value *is*, which is what makes it copyable or tappable. */
enum class RowStyle { PLAIN, COPYABLE, LINK }
