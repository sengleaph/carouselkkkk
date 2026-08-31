package com.sifu.mysub.data.source

import android.content.Context
import androidx.annotation.RawRes
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import com.sifu.mysub.R
import com.sifu.mysub.data.dto.DataAuthDto

interface DataAuthLocalDataSource {
    fun readDataAuth(): DataAuthDto
}

class RawResDataAuthDataSource(
    context: Context,
    private val gson: Gson = Gson(),
    @RawRes private val rawResId: Int = R.raw.dataauth
) : DataAuthLocalDataSource {

    private val appContext = context.applicationContext

    override fun readDataAuth(): DataAuthDto {
        val json = appContext.resources.openRawResource(rawResId)
            .bufferedReader()
            .use { it.readText() }

        return try {
            gson.fromJson(json, DataAuthDto::class.java)
                ?: throw JsonSyntaxException("dataauth.json parsed to null")
        } catch (e: JsonSyntaxException) {
            throw MalformedRawJsonException("dataauth.json", e)
        }
    }
}
