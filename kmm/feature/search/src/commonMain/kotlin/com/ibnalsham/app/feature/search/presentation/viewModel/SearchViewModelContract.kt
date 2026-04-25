package com.ibnalsham.app.feature.search.presentation.viewModel

import com.ibnalsham.app.data.network.error.DomainError
import com.ibnalsham.app.feature.domain.ProductDomain
import com.ibnalsham.app.presentation.mvi.UiEvent
import com.ibnalsham.app.presentation.mvi.UiSideEffect
import com.ibnalsham.app.presentation.mvi.UiState
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

interface SearchViewModelContract {
    sealed class Action : UiEvent() {
        data class QueryChanged(val query: String) : Action()
        data object SubmitSearch : Action()
        data class ProductClicked(val productId: Int) : Action()
    }

    data class MyUiState(
        val isLoading: Boolean = false,
        val error: DomainError? = null,
        val query: String = "",
        val results: ImmutableList<ProductDomain> = persistentListOf(),
        val hasSearched: Boolean = false
    ) : UiState {
        companion object {
            fun defaultInstance() = MyUiState()
        }
    }

    sealed class SideEffect : UiSideEffect {
        data class NavigateToProduct(val productId: Int) : SideEffect()
        data object NavigateBack : SideEffect()
    }
}
