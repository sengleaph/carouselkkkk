package com.sifu.mysub.data.dto


import com.google.gson.annotations.SerializedName
import androidx.annotation.Keep

@Keep
data class SuccessAuthDto(
    @SerializedName("accessLink")
    var accessLink: String?,
    @SerializedName("code")
    var code: String?,
    @SerializedName("isSavedFav")
    var isSavedFav: String?,
    @SerializedName("key")
    var key: String?,
    @SerializedName("msg")
    var msg: String?,
    @SerializedName("msgDev")
    var msgDev: Any?,
    @SerializedName("pin")
    var pin: String?,
    @SerializedName("status")
    var status: String?,
    @SerializedName("svRRN")
    var svRRN: String?
)