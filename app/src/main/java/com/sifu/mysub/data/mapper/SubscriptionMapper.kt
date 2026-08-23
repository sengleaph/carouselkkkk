package com.sifu.mysub.data.mapper

import com.sifu.mysub.data.dto.DataItemDto
import com.sifu.mysub.data.dto.SubscriptionDto
import com.sifu.mysub.data.dto.SuccessAuthDto
import com.sifu.mysub.domain.model.DetailRow
import com.sifu.mysub.domain.model.RowEmphasis
import com.sifu.mysub.domain.model.RowStyle
import com.sifu.mysub.domain.model.Subscription
import com.sifu.mysub.domain.model.SuccessAuthModel

/**
 * The anti-corruption layer: every transport quirk is normalised here exactly
 * once, so a backend rename never ripples past this file.
 */
object SubscriptionMapper {

    private const val NONE = "NONE"
    private const val COLOR_RED = "RED"
    private const val STYLE_COPY_GOLD = "copyGold"
    private const val STYLE_LINK = "link"

    fun toDomain(dto: SubscriptionDto): Subscription = Subscription(
        subCode = dto.subCode.orEmpty(),
        categoryName = dto.category.normalised().orEmpty(),
        planName = dto.planName.orEmpty(),
        regId = dto.regId.orEmpty(),
        accountId = dto.accountId.orEmpty(),
        subscribeToken = dto.subscribeToken.orEmpty(),
        amount = dto.amount.orEmpty(),
        renew = dto.renew.orEmpty(),
        hasSubscription = dto.haveSub == true,
        rows = dto.dataList.orEmpty().mapNotNull(::toDomain)
    )

    /** Drops rows that carry no title *and* no value — they render as blank noise. */
    private fun toDomain(dto: DataItemDto): DetailRow? {
        val title = dto.title.normalised().orEmpty()
        val value = dto.value.normalised().orEmpty()
        if (title.isEmpty() && value.isEmpty()) return null

        return DetailRow(
            title = title,
            value = value,
            emphasis = dto.color.toEmphasis(),
            style = dto.style.toRowStyle(),
            bold = dto.isBold.toBooleanFlag(),
            showDivider = dto.isLine.toBooleanFlag(),
            remark = dto.remark.normalised()
        )
    }

    /** The API sends the literal string "NONE" where it means null. */
    private fun String?.normalised(): String? =
        this?.trim()?.takeIf { it.isNotEmpty() && !it.equals(NONE, ignoreCase = true) }

    /** The API sends booleans as "true"/"false" strings. */
    private fun String?.toBooleanFlag(): Boolean = this?.trim().equals("true", ignoreCase = true)

    private fun String?.toEmphasis(): RowEmphasis =
        if (this?.trim().equals(COLOR_RED, ignoreCase = true)) {
            RowEmphasis.NEGATIVE
        } else {
            RowEmphasis.NORMAL
        }

    private fun String?.toRowStyle(): RowStyle = when {
        this?.trim().equals(STYLE_COPY_GOLD, ignoreCase = true) -> RowStyle.COPYABLE
        this?.trim().equals(STYLE_LINK, ignoreCase = true) -> RowStyle.LINK
        else -> RowStyle.PLAIN
    }
    fun toDomain(dto: SuccessAuthDto) = SuccessAuthModel(
        accessLink = dto.accessLink.orEmpty(),
        code = dto.code.orEmpty(),
        isSavedFav = dto.isSavedFav.orEmpty(),
        key = dto.key.orEmpty(),
        msg = dto.msg.orEmpty(),
        msgDev = dto.msgDev,
        pin = dto.pin.orEmpty(),
        status = dto.status.orEmpty(),
        svRRN = dto.svRRN.orEmpty()
    )
}
