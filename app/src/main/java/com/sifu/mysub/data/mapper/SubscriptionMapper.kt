package com.sifu.mysub.data.mapper

import com.sifu.mysub.data.dto.DetailRowDto
import com.sifu.mysub.data.dto.SubscriptionDto
import com.sifu.mysub.domain.model.DetailRow
import com.sifu.mysub.domain.model.RowEmphasis
import com.sifu.mysub.domain.model.RowStyle
import com.sifu.mysub.domain.model.Subscription

object SubscriptionMapper {

    fun toDomain(dto: SubscriptionDto) = Subscription(
        subCode = dto.subCode?.trim().orEmpty(),
        planName = dto.planName?.trim().orEmpty(),
        amount = dto.amount?.trim().orEmpty(),
        renew = dto.renew?.trim().orEmpty(),
        // A null flag is not a subscription: absent means no.
        hasSubscription = dto.haveSub == true,
        rows = dto.dataList.orEmpty().mapNotNull(::toDomain)
    )

    /** A row with no title and no value has nothing to render, so it is dropped. */
    private fun toDomain(dto: DetailRowDto): DetailRow? {
        val title = dto.title?.trim().orEmpty()
        val value = dto.value?.trim().orEmpty()
        if (title.isEmpty() && value.isEmpty()) return null

        return DetailRow(
            title = title,
            value = value,
            emphasis = dto.color.toEmphasis(),
            style = dto.style.toStyle(),
            isBold = dto.isBold.toBoolean(),
            hasDivider = dto.isLine.toBoolean()
        )
    }

    /** "true"/"false" arrive as strings on this feed; anything else means false. */
    private fun String?.toBoolean(): Boolean = this?.trim()?.equals("true", ignoreCase = true) == true

    private fun String?.toEmphasis(): RowEmphasis = when (this?.trim()?.uppercase()) {
        "RED" -> RowEmphasis.NEGATIVE
        else -> RowEmphasis.NONE
    }

    private fun String?.toStyle(): RowStyle = when (this?.trim()?.lowercase()) {
        "copygold" -> RowStyle.COPYABLE
        "link" -> RowStyle.LINK
        else -> RowStyle.PLAIN
    }
}
