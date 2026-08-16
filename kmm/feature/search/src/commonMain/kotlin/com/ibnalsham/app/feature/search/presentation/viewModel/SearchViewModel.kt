package com.ibnalsham.app.feature.search.presentation.viewModel

import com.ibnalsham.app.core.common.onFailure
import com.ibnalsham.app.core.common.onSuccess
import com.ibnalsham.app.data.search.SearchRepository
import com.ibnalsham.app.feature.domain.ProductFilterDomain
import com.ibnalsham.app.presentation.mvi.KMMViewModel
import com.ibnalsham.app.presentation.mvi.MVI
import com.ibnalsham.app.presentation.mvi.mvi
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class SearchViewModel(
    private val searchRepository: SearchRepository,
) : KMMViewModel(),
    MVI<SearchViewModelContract.MyUiState, SearchViewModelContract.Action, SearchViewModelContract.SideEffect>
    by mvi(SearchViewModelContract.MyUiState.defaultInstance()) {

    private var searchJob: Job? = null
    private var suggestionJob: Job? = null

    override fun onAction(action: SearchViewModelContract.Action) {
        when (action) {
            SearchViewModelContract.Action.ViewReady -> loadFilterOptions()

            is SearchViewModelContract.Action.QueryChanged -> {
                updateState { copy(query = action.query) }
                debounceSearch(action.query)
            }

            SearchViewModelContract.Action.SubmitSearch -> performSearch(currentState.query, currentState.filter)

            SearchViewModelContract.Action.ClearQuery -> {
                searchJob?.cancel()
                updateState {
                    copy(query = "", results = persistentListOf(), suggestions = persistentListOf(), hasSearched = false)
                }
            }

            is SearchViewModelContract.Action.ProductClicked -> viewModelScope.launch {
                emitEffect { SearchViewModelContract.SideEffect.NavigateToProduct(action.productId) }
            }

            is SearchViewModelContract.Action.SuggestionClicked -> {
                updateState { copy(query = action.suggestion, suggestions = persistentListOf()) }
                performSearch(action.suggestion, currentState.filter)
            }

            SearchViewModelContract.Action.OpenFilters ->
                updateState { copy(isFilterSheetOpen = true, draftFilter = filter) }

            SearchViewModelContract.Action.CloseFilters ->
                updateState { copy(isFilterSheetOpen = false, draftFilter = filter) }

            is SearchViewModelContract.Action.SetCategory ->
                updateState { copy(draftFilter = draftFilter.copy(categoryId = action.categoryId)) }

            is SearchViewModelContract.Action.SetPriceRange ->
                updateState { copy(draftFilter = draftFilter.copy(minPrice = action.min, maxPrice = action.max)) }

            is SearchViewModelContract.Action.SetOnlyOffers ->
                updateState { copy(draftFilter = draftFilter.copy(onlyOffers = action.value)) }

            is SearchViewModelContract.Action.SetOnlyFeatured ->
                updateState { copy(draftFilter = draftFilter.copy(onlyFeatured = action.value)) }

            is SearchViewModelContract.Action.SetSort ->
                updateState { copy(draftFilter = draftFilter.copy(sort = action.sort)) }

            SearchViewModelContract.Action.ApplyFilters -> {
                val applied = currentState.draftFilter
                updateState { copy(filter = applied, isFilterSheetOpen = false) }
                performSearch(currentState.query, applied)
            }

            SearchViewModelContract.Action.ClearFilters -> {
                val cleared = ProductFilterDomain()
                updateState { copy(filter = cleared, draftFilter = cleared, isFilterSheetOpen = false) }
                performSearch(currentState.query, cleared)
            }
        }
    }

    private fun loadFilterOptions() {
        viewModelScope.launch {
            searchRepository.getFilterOptions().onSuccess { options ->
                updateState {
                    copy(
                        filterCategories = options.categories.toImmutableList(),
                        priceBoundMin = options.minPrice,
                        priceBoundMax = options.maxPrice,
                    )
                }
            }
        }
    }

    private fun debounceSearch(query: String) {
        searchJob?.cancel()
        suggestionJob?.cancel()

        if (query.isBlank()) {
            // With filters on, an empty query still yields a browsable list.
            if (currentState.filter.isActive) {
                searchJob = viewModelScope.launch {
                    delay(DEBOUNCE_MILLIS)
                    performSearch(null, currentState.filter)
                }
            } else {
                updateState { copy(results = persistentListOf(), suggestions = persistentListOf(), hasSearched = false) }
            }
            return
        }

        searchJob = viewModelScope.launch {
            delay(DEBOUNCE_MILLIS)
            performSearch(query, currentState.filter)
        }

        suggestionJob = viewModelScope.launch {
            delay(SUGGESTION_DEBOUNCE_MILLIS)
            searchRepository.getSuggestions(query).onSuccess { suggestions ->
                updateState { copy(suggestions = suggestions) }
            }
        }
    }

    private fun performSearch(query: String?, filter: ProductFilterDomain) {
        if (query.isNullOrBlank() && !filter.isActive) return

        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            updateState { copy(isLoading = true, error = null, hasSearched = true) }

            searchRepository.search(query, filter).onSuccess { products ->
                updateState { copy(isLoading = false, results = products, suggestions = persistentListOf()) }
            }.onFailure { error ->
                updateState { copy(isLoading = false, error = error) }
            }
        }
    }

    private companion object {
        const val DEBOUNCE_MILLIS = 400L
        const val SUGGESTION_DEBOUNCE_MILLIS = 250L
    }
}
