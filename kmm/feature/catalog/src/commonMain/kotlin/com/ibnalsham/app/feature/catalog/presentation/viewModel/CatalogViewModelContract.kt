package com.ibnalsham.app.feature.catalog.presentation.viewModel

import com.ibnalsham.app.data.network.error.DomainError
import com.ibnalsham.app.feature.domain.CategoryDomain
import com.ibnalsham.app.presentation.mvi.UiEvent
import com.ibnalsham.app.presentation.mvi.UiSideEffect
import com.ibnalsham.app.presentation.mvi.UiState
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

interface CatalogViewModelContract {
    sealed class Action : UiEvent() {
        data object ViewReady : Action()
        data class CategoryClicked(val categoryId: Int) : Action()
    }

    data class MyUiState(
        val isLoading: Boolean = true,
        val error: DomainError? = null,
        val categories: ImmutableList<CategoryDomain> = persistentListOf()
    ) : UiState {
        companion object {
            fun defaultInstance() = MyUiState()
        }
    }

    sealed class SideEffect : UiSideEffect {
        data class NavigateToCategory(val categoryId: Int) : SideEffect()
    }
}
