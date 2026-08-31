package com.sifu.mysub.data.source

import android.content.Context
import androidx.annotation.RawRes
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import com.sifu.mysub.R
import com.sifu.mysub.data.dto.SubscribeMenuResponseDto

interface SubscribeMenuLocalDataSource {
    fun readSubscribeMenu(): SubscribeMenuResponseDto
}

class RawResSubscribeMenuDataSource(
    context: Context,
    private val gson: Gson = Gson(),
    @RawRes private val rawResId: Int = R.raw.upgrade_plans
) : SubscribeMenuLocalDataSource {

    private val appContext = context.applicationContext

    override fun readSubscribeMenu(): SubscribeMenuResponseDto {
        val json = appContext.resources.openRawResource(rawResId)
            .bufferedReader()
            .use { it.readText() }

        return try {
            gson.fromJson(json, SubscribeMenuResponseDto::class.java)
                ?: throw JsonSyntaxException("upgrade_plans.json parsed to null")
        } catch (e: JsonSyntaxException) {
            throw MalformedRawJsonException("upgrade_plans.json", e)
        }
    }
}
