package com.sifu.mysub.data.dto

import com.google.gson.annotations.SerializedName

/**
 * Transport model — mirrors res/raw/subscription.json exactly, warts and all
 * (booleans as strings, `"NONE"` as null, `val` as a field name).
 *
 * Nothing outside the data layer ever sees this type.
 */
data class SubscriptionDto(
    @SerializedName("code") val code: String? = null,
    @SerializedName("msg") val msg: String? = null,
    @SerializedName("subCode") val subCode: String? = null,
    /** Not in the current payload; the design's toolbar title maps here once the API sends it. */
    @SerializedName("category") val category: String? = null,
    @SerializedName("planName") val planName: String? = null,
    @SerializedName("regId") val regId: String? = null,
    @SerializedName("subscribeToken") val subscribeToken: String? = null,
    @SerializedName("accountId") val accountId: String? = null,
    @SerializedName("amount") val amount: String? = null,
    @SerializedName("renew") val renew: String? = null,
    @SerializedName("dataList") val dataList: List<DataItemDto>? = null,
    @SerializedName("haveSub") val haveSub: Boolean? = null
) {
    companion object {
        const val CODE_SUCCESS = "0"
    }
}

data class DataItemDto(
    @SerializedName("title") val title: String? = null,
    @SerializedName("val") val value: String? = null,
    @SerializedName("color") val color: String? = null,
    @SerializedName("isLine") val isLine: String? = null,
    @SerializedName("isBold") val isBold: String? = null,
    @SerializedName("remark") val remark: String? = null,
    @SerializedName("style") val style: String? = null
)
