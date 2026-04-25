package com.ibnalsham.app.feature.home.presentation.viewModel

import com.ibnalsham.app.data.network.error.DomainError
import com.ibnalsham.app.feature.domain.BannerDomain
import com.ibnalsham.app.feature.domain.CategoryDomain
import com.ibnalsham.app.feature.domain.ProductDomain
import com.ibnalsham.app.presentation.mvi.UiEvent
import com.ibnalsham.app.presentation.mvi.UiSideEffect
import com.ibnalsham.app.presentation.mvi.UiState
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

interface HomeViewModelContract {
    sealed class Action : UiEvent() {
        data object ViewReady : Action()
        data object Refresh : Action()
        data class ProductClicked(val productId: Int) : Action()
        data class CategoryClicked(val categoryId: Int) : Action()
        data class BannerClicked(val bannerId: Int) : Action()
        data class CategoryTabSelected(val categoryId: Int) : Action()
    }

    data class MyUiState(
        val isLoading: Boolean = false,
        val error: DomainError? = null,
        val banners: ImmutableList<BannerDomain> = persistentListOf(),
        val categories: ImmutableList<CategoryDomain> = persistentListOf(),
        val featuredProducts: ImmutableList<ProductDomain> = persistentListOf(),
        val specialOffers: ImmutableList<ProductDomain> = persistentListOf(),
        val mostOrdered: ImmutableList<ProductDomain> = persistentListOf(),
        val selectedTabCategoryId: Int? = null,
        val tabProducts: ImmutableList<ProductDomain> = persistentListOf(),
        val isTabLoading: Boolean = false,
    ) : UiState {
        companion object {
            fun defaultInstance() = MyUiState()
        }
    }

    sealed class SideEffect : UiSideEffect {
        data class NavigateToProduct(val productId: Int) : SideEffect()
        data class NavigateToCategory(val categoryId: Int) : SideEffect()
    }
}
