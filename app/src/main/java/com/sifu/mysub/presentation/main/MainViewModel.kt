package com.sifu.mysub.presentation.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.sifu.mysub.core.util.onFailure
import com.sifu.mysub.core.util.onSuccess
import com.sifu.mysub.domain.model.SubscribeService
import com.sifu.mysub.domain.usecase.GetSubscribeMenuUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * State-only ViewModel: one [StateFlow] out, plain functions in.
 *
 * The first screen is always the category browse: the services from
 * `upgrade_plans.json`. Whether the user already subscribes to one of them is
 * not asked here — that question belongs to the service the user taps, so
 * subscription.json is read one screen later rather than on every launch.
 */
class MainViewModel(
    private val getSubscribeMenu: GetSubscribeMenuUseCase,
    private val errorMessages: ErrorMessageMapper,
    private val titles: TitleProvider
) : ViewModel() {

    private val _uiState = MutableStateFlow(MainUiState())
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()

    /**
     * Every card the feed produced, before the query narrows it.
     *
     * Kept here rather than in the state: it is the ViewModel's working set, not
     * something the screen renders, and holding it means a keystroke filters an
     * in-memory list instead of re-reading and re-parsing the JSON.
     */
    private var allCards: List<ScreenRow.Card> = emptyList()

    /** Triggered by the screen once it is set up, and by the retry button. */
    fun load() {
        _uiState.update { it.copy(isLoading = true, errorMessage = null) }
        viewModelScope.launch {
            getSubscribeMenu()
                .onSuccess { services -> showCards(services.map { it.toCard() }) }
                .onFailure { error -> showError(errorMessages.map(error)) }
        }
    }

    private fun showCards(cards: List<ScreenRow.Card>) {
        allCards = cards
        // A reload keeps whatever the user had typed, so the visible list stays
        // consistent with the search field rather than silently widening.
        val query = _uiState.value.searchQuery
        val matches = cards.matching(query)
        _uiState.value = MainUiState(
            isLoading = false,
            errorMessage = null,
            title = titles.category(),
            header = Header.Category,
            isSearchVisible = true,
            searchQuery = query,
            rows = matches,
            isEmptyResult = cards.isNotEmpty() && matches.isEmpty()
        )
    }

    private fun showError(message: String) {
        allCards = emptyList()
        _uiState.value = MainUiState(
            isLoading = false,
            errorMessage = message,
            title = titles.category(),
            rows = emptyList()
        )
    }

    /** Called on every keystroke; filters the cards already in memory. */
    fun onSearchQueryChanged(query: String) {
        _uiState.update { state ->
            val matches = allCards.matching(query)
            state.copy(
                searchQuery = query,
                rows = matches,
                isEmptyResult = allCards.isNotEmpty() && matches.isEmpty()
            )
        }
    }

    /** Matches on title and description, so a search can find either. */
    private fun List<ScreenRow.Card>.matching(query: String): List<ScreenRow.Card> {
        val trimmed = query.trim()
        if (trimmed.isEmpty()) return this
        return filter { card ->
            card.title.contains(trimmed, ignoreCase = true) ||
                card.description.contains(trimmed, ignoreCase = true)
        }
    }

    private fun SubscribeService.toCard() = ScreenRow.Card(
        title = name,
        description = description,
        isDescriptionVisible = hasRealDescription(),
        imageUrl = imageUrl,
        logoText = code.uppercase(),
        trailing = "",
        target = CardTarget.Service(code)
    )

    /** A description that just repeats the name is noise, not a description. */
    private fun SubscribeService.hasRealDescription(): Boolean =
        description.isNotEmpty() && !description.equals(name, ignoreCase = true)

    class Factory(
        private val getSubscribeMenu: GetSubscribeMenuUseCase,
        private val errorMessages: ErrorMessageMapper,
        private val titles: TitleProvider
    ) : ViewModelProvider.Factory {

        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            require(modelClass.isAssignableFrom(MainViewModel::class.java)) {
                "Unknown ViewModel class: ${modelClass.name}"
            }
            return MainViewModel(getSubscribeMenu, errorMessages, titles) as T
        }
    }
}
