package com.sifu.mysub.data.source

import android.content.Context
import androidx.annotation.RawRes
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import com.sifu.mysub.R
import com.sifu.mysub.data.dto.UpgradePlanOfferDto

interface UpgradePlanLocalDataSource {
    fun readUpgradePlans(): UpgradePlanOfferDto
}

class RawResUpgradePlanDataSource(
    context: Context,
    private val gson: Gson = Gson(),
    @RawRes private val rawResId: Int = R.raw.upgrade_plans
) : UpgradePlanLocalDataSource {

    private val appContext = context.applicationContext

    override fun readUpgradePlans(): UpgradePlanOfferDto {
        val json = appContext.resources.openRawResource(rawResId)
            .bufferedReader()
            .use { it.readText() }

        return try {
            gson.fromJson(json, UpgradePlanOfferDto::class.java)
                ?: throw JsonSyntaxException("upgrade_plans.json parsed to null")
        } catch (e: JsonSyntaxException) {
            throw MalformedSubscriptionException(e)
        }
    }
}
