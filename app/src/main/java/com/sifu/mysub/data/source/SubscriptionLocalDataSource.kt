package com.sifu.mysub.data.source

import android.content.Context
import androidx.annotation.RawRes
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import com.sifu.mysub.R
import com.sifu.mysub.data.dto.SubscriptionDto

/**
 * Data-source abstraction. Swapping res/raw for Retrofit or Room later means
 * adding a sibling implementation — the repository above does not change.
 */
interface SubscriptionLocalDataSource {
    fun readSubscription(): SubscriptionDto
}

class RawResSubscriptionDataSource(
    context: Context,
    private val gson: Gson = Gson(),
    @RawRes private val rawResId: Int = R.raw.subscription
) : SubscriptionLocalDataSource {

    private val appContext = context.applicationContext

    override fun readSubscription(): SubscriptionDto {
        val json = appContext.resources.openRawResource(rawResId)
            .bufferedReader()
            .use { it.readText() }

        return try {
            gson.fromJson(json, SubscriptionDto::class.java)
                ?: throw JsonSyntaxException("subscription.json parsed to null")
        } catch (e: JsonSyntaxException) {
            throw MalformedSubscriptionException(e)
        }
    }
}

class MalformedSubscriptionException(cause: Throwable) :
    RuntimeException("subscription.json is not valid JSON", cause)
