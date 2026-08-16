package com.ibnalsham.app.feature.menu.presentation.viewModel

import com.ibnalsham.app.presentation.mvi.KMMViewModel
import com.ibnalsham.app.presentation.mvi.MVI
import kotlinx.coroutines.launch

import com.ibnalsham.app.data.menu.MenuRepository
import com.ibnalsham.app.core.common.onSuccess
import com.ibnalsham.app.core.common.onFailure
import com.ibnalsham.app.library.data.cache.CartRepository
import com.ibnalsham.app.library.data.cache.UserPreferencesRepository
import com.ibnalsham.app.presentation.mvi.mvi
import kotlinx.coroutines.flow.collectLatest

class MenuViewModel(
    private val menuRepository: MenuRepository,
    private val cartRepository: CartRepository,
    private val userPreferences: UserPreferencesRepository,
) : KMMViewModel(),
    MVI<MenuViewModelContract.MyUiState, MenuViewModelContract.Action, MenuViewModelContract.SideEffect>
    by mvi(MenuViewModelContract.MyUiState.defaultInstance()) {

    override fun onAction(event: MenuViewModelContract.Action) {
        when (event) {
            MenuViewModelContract.Action.ViewReady -> {
                val savedViewType = ViewType.valueOf(userPreferences.getViewType())
                updateState { copy(viewType = savedViewType) }
                loadMenuData()
                observeCart()
            }
            is MenuViewModelContract.Action.CategorySelected -> loadProductsForCategory(event.categoryId)
            is MenuViewModelContract.Action.AddToCart -> handleAddToCart(event.productId)
            is MenuViewModelContract.Action.ProductClicked -> handleProductClicked(event.productId)
            is MenuViewModelContract.Action.IncreaseQuantity -> handleIncreaseQuantity(event.productId)
            is MenuViewModelContract.Action.DecreaseQuantity -> handleDecreaseQuantity(event.productId)
            is MenuViewModelContract.Action.ToggleViewType -> {
                updateState { copy(viewType = event.viewType) }
                userPreferences.saveViewType(event.viewType.name)
            }
        }
    }

    private fun observeCart() {
        viewModelScope.launch {
            cartRepository.cartItems.collectLatest { items ->
                val counts = items.associate { it.product.id to it.quantity }
                val totalItems = items.sumOf { it.quantity }
                val subtotal = cartRepository.getSubtotal()
                updateState {
                    copy(
                        cartItemCounts = counts,
                        cartTotalItems = totalItems,
                        cartSubtotal = subtotal,
                    )
                }
            }
        }
    }

    private fun loadMenuData() {
        viewModelScope.launch {
            updateState { copy(isLoading = true, error = null) }

            menuRepository.getCategories().onSuccess { categories ->
                updateState { copy(categories = categories) }
                if (categories.isNotEmpty()) {
                    loadProductsForCategory(categories.first().id)
                } else {
                    updateState { copy(isLoading = false) }
                }
            }.onFailure { error ->
                updateState { copy(isLoading = false, error = error) }
            }
        }
    }

    private fun loadProductsForCategory(categoryId: Int) {
        viewModelScope.launch {
            updateState { copy(isLoading = true, selectedCategoryId = categoryId) }

            menuRepository.getProducts(categoryId).onSuccess { paginated ->
                updateState {
                    copy(
                        isLoading = false,
                        products = paginated.products,
                    )
                }
            }.onFailure { error ->
                updateState { copy(isLoading = false, error = error) }
            }
        }
    }

    private fun handleAddToCart(productId: Int) {
        viewModelScope.launch {
            val product = currentState.products.find { it.id == productId } ?: return@launch
            cartRepository.addToCart(product)
            emitEffect { MenuViewModelContract.SideEffect.ShowCartSnackbar("تمت الإضافة إلى السلة") }
        }
    }

    private fun handleIncreaseQuantity(productId: Int) {
        val current = cartRepository.getQuantity(productId)
        cartRepository.updateQuantity(productId, current + 1)
    }

    private fun handleDecreaseQuantity(productId: Int) {
        val current = cartRepository.getQuantity(productId)
        if (current > 0) {
            cartRepository.updateQuantity(productId, current - 1)
        }
    }

    private fun handleProductClicked(productId: Int) {
        viewModelScope.launch {
            emitEffect { MenuViewModelContract.SideEffect.NavigateToProduct(productId) }
        }
    }
}
