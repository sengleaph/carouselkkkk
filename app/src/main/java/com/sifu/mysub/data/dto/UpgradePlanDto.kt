package com.sifu.mysub.data.dto

import com.google.gson.annotations.SerializedName

/**
 * Transport model for res/raw/upgrade_plans.json.
 *
 * Unlike [SubscriptionDto] this one uses real JSON booleans — it is a new
 * contract, so there are no legacy string-boolean quirks to carry.
 */
data class UpgradePlanOfferDto(
    @SerializedName("code") val code: String? = null,
    @SerializedName("msg") val msg: String? = null,
    @SerializedName("title") val title: String? = null,
    @SerializedName("brandCode") val brandCode: String? = null,
    @SerializedName("plans") val plans: List<UpgradePlanDto>? = null
) {
    companion object {
        const val CODE_SUCCESS = "0"
    }
}

data class UpgradePlanDto(
    @SerializedName("planCode") val planCode: String? = null,
    @SerializedName("title") val title: String? = null,
    @SerializedName("price") val price: String? = null,
    @SerializedName("theme") val theme: String? = null,
    @SerializedName("recommended") val recommended: Boolean? = null
)
