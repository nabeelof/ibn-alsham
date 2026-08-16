package com.ibnalsham.app.feature.favorites.presentation.viewModel

import com.ibnalsham.app.data.network.error.DomainError
import com.ibnalsham.app.feature.domain.ProductDomain
import com.ibnalsham.app.presentation.mvi.UiEvent
import com.ibnalsham.app.presentation.mvi.UiSideEffect
import com.ibnalsham.app.presentation.mvi.UiState
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

interface FavoritesViewModelContract {
    sealed class Action : UiEvent() {
        data object ViewReady : Action()
        data object Refresh : Action()
        data class ProductClicked(val productId: Int) : Action()
        data class RemoveFavorite(val productId: Int) : Action()
        data class AddToCart(val productId: Int) : Action()
        data object GoToMenu : Action()
        data object SignInClicked : Action()
    }

    data class MyUiState(
        val isLoading: Boolean = true,
        val error: DomainError? = null,
        val products: ImmutableList<ProductDomain> = persistentListOf(),
        val removingProductId: Int? = null,
        val cartItemCounts: Map<Int, Int> = emptyMap(),
        val isGuest: Boolean = false,
    ) : UiState {
        val isEmpty: Boolean get() = !isLoading && !isGuest && error == null && products.isEmpty()

        /** Guests get a sign-in prompt rather than an empty list. */
        val requiresLogin: Boolean get() = isGuest || error == DomainError.Unauthorized

        companion object {
            fun defaultInstance() = MyUiState()
        }
    }

    sealed class SideEffect : UiSideEffect {
        data class NavigateToProduct(val productId: Int) : SideEffect()
        data object NavigateToMenu : SideEffect()

        /** Ask the host to raise the sign-in gate sheet. */
        data object RequireSignIn : SideEffect()
        data class ShowSnackbar(val message: String) : SideEffect()
    }
}
