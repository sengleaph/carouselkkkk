package com.sifu.mysub.core.util

import android.util.Log

/**
 * Runs [block], swallowing anything it throws.
 *
 * Reserved for view-layer setup that is decorative rather than functional --
 * the carousel transform, for instance, reaches into ViewPager2's internal
 * RecyclerView, and a layout that changes shape under it should degrade to a
 * plain pager rather than take the sheet down with it. Returns null when the
 * block failed, so callers can tell the two cases apart.
 */
inline fun <T> tryThis(tag: String = "tryThis", block: () -> T): T? = try {
    block()
} catch (e: Exception) {
    Log.w(tag, "Swallowed while running block", e)
    null
}
