package com.sifu.mysub.presentation.main

/**
 * State only — no event channel, no effects.
 *
 * Which of the two feeds the main screen shows is a *value in the state*, not a
 * navigation: `haveSub` is answered asynchronously, and parking an async
 * "go here" in a replayable holder is what relaunches a screen every time the
 * user comes back. Modelled this way, rendering the same state twice is a no-op.
 *
 * Nothing here decides anything; the rules live in [MainViewModel].
 */
data class MainUiState(
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
    /** Toolbar caption — the category, the same on both modes. */
    val title: String = "",
    /** What sits on the blue header, which differs per mode. */
    val header: Header = Header.None,
    /**
     * Search belongs to the menu only: with a single subscription card there is
     * nothing to search, so the field and its white panel come and go together.
     */
    val isSearchVisible: Boolean = false,
    /** What the user typed. Held in state so it survives recreation. */
    val searchQuery: String = "",
    /** Already filtered by [searchQuery]; the screen renders these verbatim. */
    val rows: List<ScreenRow> = emptyList(),
    /** True when a query matched nothing, as opposed to nothing being loaded. */
    val isEmptyResult: Boolean = false
) {
    val isContentVisible: Boolean get() = !isLoading && errorMessage == null && rows.isNotEmpty()
}

/**
 * The mark on the blue header.
 *
 * The two modes show different things — a category tile when browsing what is
 * on offer, the brand itself once the user has a subscription — so which one is
 * a value in the state rather than a flag the screen interprets.
 */
sealed interface Header {
    data object None : Header

    /** Browsing: the teal category tile. */
    data object Category : Header

    /** Subscribed: the brand circle and its name. */
    data class Brand(val logoText: String, val name: String) : Header
}

/**
 * Every row any screen can show, in one type.
 *
 * The main screen is a list of [Card]s whichever feed it came from — the
 * subscription the user already has, or the services they could subscribe to.
 * One shape means one layout and one ViewHolder, and the screens stay
 * consistent by construction rather than by two layouts being kept in step.
 */
sealed interface ScreenRow {

    data class Card(
        val title: String,
        val description: String,
        val isDescriptionVisible: Boolean,
        /** Remote artwork. Empty falls back to [logoText] on the dark circle. */
        val imageUrl: String,
        val logoText: String,
        /** Trailing value, e.g. the subscription's amount. Empty hides it. */
        val trailing: String,
        /** Where a tap goes. Null makes the card inert. */
        val target: CardTarget?
    ) : ScreenRow

    /** One row of subscription.json's dataList. */
    data class Detail(
        val title: String,
        val value: String,
        val emphasis: ValueEmphasis,
        val isBold: Boolean,
        val hasDivider: Boolean
    ) : ScreenRow

    /** One plan under a service. */
    data class Plan(
        val code: String,
        val name: String,
        /** Already formatted, e.g. "1.53 USD". */
        val price: String
    ) : ScreenRow
}

/**
 * What a card opens, named by intent rather than by Activity so the routing rule
 * stays out of the ViewModel's Android-free world. The screen maps each case to
 * a class.
 */
sealed interface CardTarget {
    /** One service: its subscription if there is one, otherwise its plans. */
    data class Service(val serviceCode: String) : CardTarget

    /** The detail rows of the subscription the user already has. */
    data object SubscriptionDetail : CardTarget
}

/**
 * How a detail value should read. Named by meaning, not by colour — the adapter
 * turns each case into a resource, which is a lookup rather than a decision.
 */
enum class ValueEmphasis { PLAIN, NEGATIVE, COPYABLE, LINK }
