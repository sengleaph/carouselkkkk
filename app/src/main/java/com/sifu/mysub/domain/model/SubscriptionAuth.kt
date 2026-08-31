package com.sifu.mysub.domain.model

/**
 * The per-user secrets for a subscription.
 *
 * They live in their own feed because they are not part of the subscription
 * record: subscription.json ships "X" placeholders for both, and dataauth.json
 * is the authority that fills them in.
 */
data class SubscriptionAuth(
    val pin: String,
    val accessLink: String
) {
    companion object {
        /** Used when the auth feed cannot be read, leaving placeholders in place. */
        val EMPTY = SubscriptionAuth(pin = "", accessLink = "")
    }
}
