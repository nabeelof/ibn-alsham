package com.ibnalsham.app.feature.favorites.presentation.viewModel

import com.ibnalsham.app.core.common.onFailure
import com.ibnalsham.app.core.common.onSuccess
import com.ibnalsham.app.data.favorites.FavoritesRepository
import com.ibnalsham.app.data.network.error.DomainError
import com.ibnalsham.app.library.data.cache.CartRepository
import com.ibnalsham.app.library.data.cache.SessionManager
import com.ibnalsham.app.presentation.mvi.KMMViewModel
import com.ibnalsham.app.presentation.mvi.MVI
import com.ibnalsham.app.presentation.mvi.mvi
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class FavoritesViewModel(
    private val favoritesRepository: FavoritesRepository,
    private val cartRepository: CartRepository,
    private val sessionManager: SessionManager,
) : KMMViewModel(),
    MVI<
        FavoritesViewModelContract.MyUiState,
        FavoritesViewModelContract.Action,
        FavoritesViewModelContract.SideEffect,
        >
    by mvi(FavoritesViewModelContract.MyUiState.defaultInstance()) {

    override fun onAction(event: FavoritesViewModelContract.Action) {
        when (event) {
            FavoritesViewModelContract.Action.ViewReady -> {
                observeCart()
                observeSession()
            }

            FavoritesViewModelContract.Action.Refresh -> loadFavorites()

            FavoritesViewModelContract.Action.SignInClicked -> viewModelScope.launch {
                emitEffect { FavoritesViewModelContract.SideEffect.RequireSignIn }
            }

            is FavoritesViewModelContract.Action.ProductClicked -> viewModelScope.launch {
                emitEffect { FavoritesViewModelContract.SideEffect.NavigateToProduct(event.productId) }
            }

            is FavoritesViewModelContract.Action.RemoveFavorite -> removeFavorite(event.productId)
            is FavoritesViewModelContract.Action.AddToCart -> addToCart(event.productId)

            FavoritesViewModelContract.Action.GoToMenu -> viewModelScope.launch {
                emitEffect { FavoritesViewModelContract.SideEffect.NavigateToMenu }
            }
        }
    }

    private fun observeCart() {
        viewModelScope.launch {
            cartRepository.cartItems.collectLatest {
                updateState { copy(cartItemCounts = cartRepository.getItemCounts()) }
            }
        }
    }

    /**
     * Favourites live on the account, so a guest gets the sign-in state directly
     * rather than a spinner followed by a 401. Signing in from the gate sheet
     * flips this flow and loads the list in place.
     */
    private fun observeSession() {
        viewModelScope.launch {
            sessionManager.session.collectLatest { session ->
                if (session.isSignedIn) {
                    updateState { copy(isGuest = false) }
                    loadFavorites()
                } else {
                    updateState {
                        copy(isGuest = true, isLoading = false, error = null, products = persistentListOf())
                    }
                }
            }
        }
    }

    private fun loadFavorites() {
        if (!sessionManager.isSignedIn) return

        viewModelScope.launch {
            updateState { copy(isLoading = true, error = null) }

            favoritesRepository.getFavorites().onSuccess { products ->
                updateState { copy(isLoading = false, products = products, error = null) }
            }.onFailure { error ->
                updateState { copy(isLoading = false, error = error) }
                // A token that expired mid-session still needs to bounce the user out.
                if (error == DomainError.Unauthorized) {
                    sessionManager.signOut()
                }
            }
        }
    }

    private fun removeFavorite(productId: Int) {
        viewModelScope.launch {
            updateState { copy(removingProductId = productId) }

            favoritesRepository.toggleFavorite(productId).onSuccess { isFavorite ->
                updateState {
                    copy(
                        removingProductId = null,
                        products = if (isFavorite) {
                            products
                        } else {
                            products.filterNot { it.id == productId }.toImmutableList()
                        },
                    )
                }
                if (!isFavorite) {
                    emitEffect { FavoritesViewModelContract.SideEffect.ShowSnackbar(REMOVED_MESSAGE) }
                }
            }.onFailure {
                updateState { copy(removingProductId = null) }
                emitEffect { FavoritesViewModelContract.SideEffect.ShowSnackbar(REMOVE_FAILED_MESSAGE) }
            }
        }
    }

    private fun addToCart(productId: Int) {
        val product = currentState.products.firstOrNull { it.id == productId } ?: return

        // Products with required add-ons must be configured on the details screen.
        val needsConfiguration = product.optionGroups.any { it.isRequired }
        if (needsConfiguration) {
            viewModelScope.launch {
                emitEffect { FavoritesViewModelContract.SideEffect.NavigateToProduct(productId) }
            }
            return
        }

        cartRepository.addToCart(product)
        viewModelScope.launch {
            emitEffect { FavoritesViewModelContract.SideEffect.ShowSnackbar(ADDED_TO_CART_MESSAGE) }
        }
    }

    private companion object {
        const val REMOVED_MESSAGE = "تمت الإزالة من المفضّلة"
        const val REMOVE_FAILED_MESSAGE = "تعذّر تحديث المفضّلة"
        const val ADDED_TO_CART_MESSAGE = "تمت الإضافة إلى السلة"
    }
}
