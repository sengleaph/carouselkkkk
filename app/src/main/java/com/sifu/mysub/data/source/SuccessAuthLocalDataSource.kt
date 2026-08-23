package com.sifu.mysub.data.source

import android.content.Context
import androidx.annotation.RawRes
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import com.sifu.mysub.R
import com.sifu.mysub.data.dto.SuccessAuthDto

interface SuccessAuthLocalDataSource {
    fun readSuccessAuth(): SuccessAuthDto
}

class RawResSuccessAuthDataSource(
    context: Context,
    private val gson: Gson = Gson(),
    @RawRes private val rawResId: Int = R.raw.dataauth
) : SuccessAuthLocalDataSource {

    private val appContext = context.applicationContext

    override fun readSuccessAuth(): SuccessAuthDto {
        val json = appContext.resources.openRawResource(rawResId)
            .bufferedReader()
            .use { it.readText() }

        return try {
            gson.fromJson(json, SuccessAuthDto::class.java)
                ?: throw JsonSyntaxException("data_auth.json parsed to null")
        } catch (e: JsonSyntaxException) {
            throw MalformedSuccessAuthException(e)
        }
    }
}

class MalformedSuccessAuthException(cause: Throwable) :
    RuntimeException("data_auth.json is not valid JSON", cause)