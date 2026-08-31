package com.sifu.mysub.domain.model

/**
 * One service in the subscribe menu — Gemez, Youtube.
 *
 * A service with no plans is not represented: the mapper drops it, because a
 * menu entry the user cannot buy anything from is not a menu entry.
 */
data class SubscribeService(
    val code: String,
    val name: String,
    val imageUrl: String,
    val description: String,
    val plans: List<SubscribePlan>
)

/**
 * One purchasable plan under a service.
 *
 * The JSON repeats the parent's name in a `service` field on every plan; it is
 * dropped here rather than carried, since the plan already lives inside the
 * service it belongs to.
 */
data class SubscribePlan(
    val code: String,
    val name: String,
    val price: Double,
    val currency: String
)
