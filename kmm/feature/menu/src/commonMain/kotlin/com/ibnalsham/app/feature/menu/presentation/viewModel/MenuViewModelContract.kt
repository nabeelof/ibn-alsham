package com.ibnalsham.app.feature.menu.presentation.viewModel

import com.ibnalsham.app.data.network.error.DomainError
import com.ibnalsham.app.presentation.mvi.UiEvent
import com.ibnalsham.app.presentation.mvi.UiSideEffect
import com.ibnalsham.app.presentation.mvi.UiState
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import com.ibnalsham.app.feature.domain.CategoryDomain
import com.ibnalsham.app.feature.domain.ProductDomain

enum class ViewType { GRID, LIST }

interface MenuViewModelContract {
    sealed class Action : UiEvent() {
        data object ViewReady : Action()
        data class CategorySelected(val categoryId: Int) : Action()
        data class ProductClicked(val productId: Int) : Action()
        data class AddToCart(val productId: Int) : Action()
        data class IncreaseQuantity(val productId: Int) : Action()
        data class DecreaseQuantity(val productId: Int) : Action()
        data class ToggleViewType(val viewType: ViewType) : Action()
    }

    data class MyUiState(
        val isLoading: Boolean = true,
        val error: DomainError? = null,
        val categories: ImmutableList<CategoryDomain> = persistentListOf(),
        val products: ImmutableList<ProductDomain> = persistentListOf(),
        val selectedCategoryId: Int? = null,
        val viewType: ViewType = ViewType.GRID,
        val cartItemCounts: Map<Int, Int> = emptyMap(),
        val cartTotalItems: Int = 0,
        val cartSubtotal: Double = 0.0,
    ) : UiState {
        companion object {
            fun defaultInstance() = MyUiState()
        }
    }

    sealed class SideEffect : UiSideEffect {
        data class NavigateToProduct(val productId: Int) : SideEffect()
        data class ShowCartSnackbar(val message: String) : SideEffect()
    }
}
