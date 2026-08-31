package com.sifu.mysub.data.dto

import com.google.gson.annotations.SerializedName

/**
 * Transport model for res/raw/dataauth.json.
 *
 * Only pin and accessLink are consumed. The rest of the payload is declared so
 * the envelope check has a code to read and so the shape stays documented.
 */
data class DataAuthDto(
    @SerializedName("code") val code: String? = null,
    @SerializedName("status") val status: String? = null,
    @SerializedName("msg") val msg: String? = null,
    @SerializedName("msgDev") val msgDev: String? = null,
    @SerializedName("key") val key: String? = null,
    @SerializedName("svRRN") val svRRN: String? = null,
    @SerializedName("isSavedFav") val isSavedFav: String? = null,
    @SerializedName("accessLink") val accessLink: String? = null,
    @SerializedName("pin") val pin: String? = null
) {
    companion object {
        const val CODE_SUCCESS = "0"
    }
}
