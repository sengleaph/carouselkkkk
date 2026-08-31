package com.sifu.mysub.data.dto

import com.google.gson.annotations.SerializedName

/** Transport model for res/raw/upgrade_plans.json. */
data class SubscribeMenuResponseDto(
    @SerializedName("code") val code: String? = null,
    @SerializedName("msg") val msg: String? = null,
    @SerializedName("msgDev") val msgDev: String? = null,
    @SerializedName("status") val status: String? = null,
    @SerializedName("subscribeMenu") val subscribeMenu: List<SubscribeServiceDto>? = null
) {
    companion object {
        const val CODE_SUCCESS = "0"
    }
}

data class SubscribeServiceDto(
    @SerializedName("code") val code: String? = null,
    @SerializedName("name") val name: String? = null,
    @SerializedName("image") val image: String? = null,
    @SerializedName("description") val description: String? = null,
    @SerializedName("plans") val plans: List<SubscribePlanDto>? = null
)

/** `price` is a real JSON number here, not the quoted string the older feeds used. */
data class SubscribePlanDto(
    @SerializedName("code") val code: String? = null,
    @SerializedName("name") val name: String? = null,
    @SerializedName("price") val price: Double? = null,
    @SerializedName("ccy") val ccy: String? = null,
    @SerializedName("service") val service: String? = null
)
