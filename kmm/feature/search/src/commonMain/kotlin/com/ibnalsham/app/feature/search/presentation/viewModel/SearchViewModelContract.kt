package com.ibnalsham.app.feature.search.presentation.viewModel

import com.ibnalsham.app.data.network.error.DomainError
import com.ibnalsham.app.feature.domain.CategoryDomain
import com.ibnalsham.app.feature.domain.ProductDomain
import com.ibnalsham.app.feature.domain.ProductFilterDomain
import com.ibnalsham.app.feature.domain.ProductSort
import com.ibnalsham.app.presentation.mvi.UiEvent
import com.ibnalsham.app.presentation.mvi.UiSideEffect
import com.ibnalsham.app.presentation.mvi.UiState
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

interface SearchViewModelContract {
    sealed class Action : UiEvent() {
        data object ViewReady : Action()
        data class QueryChanged(val query: String) : Action()
        data object SubmitSearch : Action()
        data object ClearQuery : Action()
        data class ProductClicked(val productId: Int) : Action()
        data class SuggestionClicked(val suggestion: String) : Action()

        // Advanced filters
        data object OpenFilters : Action()
        data object CloseFilters : Action()
        data class SetCategory(val categoryId: Int?) : Action()
        data class SetPriceRange(val min: Double?, val max: Double?) : Action()
        data class SetOnlyOffers(val value: Boolean) : Action()
        data class SetOnlyFeatured(val value: Boolean) : Action()
        data class SetSort(val sort: ProductSort) : Action()
        data object ApplyFilters : Action()
        data object ClearFilters : Action()
    }

    data class MyUiState(
        val isLoading: Boolean = false,
        val error: DomainError? = null,
        val query: String = "",
        val results: ImmutableList<ProductDomain> = persistentListOf(),
        val suggestions: ImmutableList<String> = persistentListOf(),
        val hasSearched: Boolean = false,
        // Filters
        val isFilterSheetOpen: Boolean = false,
        /** Committed filter — the one the results reflect. */
        val filter: ProductFilterDomain = ProductFilterDomain(),
        /** Filter being edited in the sheet, applied on confirm. */
        val draftFilter: ProductFilterDomain = ProductFilterDomain(),
        val filterCategories: ImmutableList<CategoryDomain> = persistentListOf(),
        val priceBoundMin: Double = 0.0,
        val priceBoundMax: Double = 0.0,
    ) : UiState {
        val isEmptyResult: Boolean get() = hasSearched && !isLoading && results.isEmpty()
        val activeFilterCount: Int get() = filter.activeCount
        val hasActiveFilters: Boolean get() = filter.isActive

        companion object {
            fun defaultInstance() = MyUiState()
        }
    }

    sealed class SideEffect : UiSideEffect {
        data class NavigateToProduct(val productId: Int) : SideEffect()
        data object NavigateBack : SideEffect()
    }
}
