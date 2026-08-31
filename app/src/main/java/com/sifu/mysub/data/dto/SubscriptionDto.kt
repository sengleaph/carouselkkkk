package com.sifu.mysub.data.dto

import com.google.gson.annotations.SerializedName

/**
 * Transport model for res/raw/subscription.json.
 *
 * Note the quirks this feed carries and the domain must not: `isLine` and
 * `isBold` arrive as the strings "true"/"false" rather than JSON booleans, and
 * absent values are spelled "NONE" rather than omitted. Only `haveSub` is a
 * real boolean.
 */
data class SubscriptionDto(
    @SerializedName("code") val code: String? = null,
    @SerializedName("msg") val msg: String? = null,
    @SerializedName("subCode") val subCode: String? = null,
    @SerializedName("planName") val planName: String? = null,
    @SerializedName("regId") val regId: String? = null,
    @SerializedName("subscribeToken") val subscribeToken: String? = null,
    @SerializedName("accountId") val accountId: String? = null,
    @SerializedName("amount") val amount: String? = null,
    @SerializedName("renew") val renew: String? = null,
    @SerializedName("dataList") val dataList: List<DetailRowDto>? = null,
    @SerializedName("haveSub") val haveSub: Boolean? = null
) {
    companion object {
        const val CODE_SUCCESS = "0"
    }
}

data class DetailRowDto(
    @SerializedName("title") val title: String? = null,
    @SerializedName("val") val value: String? = null,
    @SerializedName("color") val color: String? = null,
    @SerializedName("isLine") val isLine: String? = null,
    @SerializedName("isBold") val isBold: String? = null,
    @SerializedName("remark") val remark: String? = null,
    @SerializedName("style") val style: String? = null
)
