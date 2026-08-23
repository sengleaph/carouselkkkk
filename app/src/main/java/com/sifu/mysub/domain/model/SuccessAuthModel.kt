package com.sifu.mysub.domain.model

import com.google.gson.annotations.SerializedName

class SuccessAuthModel(
    var accessLink: String ?= "",
    var code:String ?= "",
    var isSavedFav:String ?= "",
    var key:String ?= "",
    var msg:String ?= "",
    var msgDev: Any?,
    var pin:String ?= "",
    var status:String ?= "",
    var svRRN:String ?= "",
)