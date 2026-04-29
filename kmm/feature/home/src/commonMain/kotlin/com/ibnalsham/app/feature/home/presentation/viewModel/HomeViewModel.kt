package com.ibnalsham.app.feature.home.presentation.viewModel

import com.ibnalsham.app.data.home.HomeRepository
import com.ibnalsham.app.data.menu.MenuRepository
import com.ibnalsham.app.core.common.onSuccess
import com.ibnalsham.app.core.common.onFailure
import com.ibnalsham.app.library.data.cache.CartRepository
import com.ibnalsham.app.library.data.cache.UserPreferencesRepository
import com.ibnalsham.app.presentation.mvi.KMMViewModel
import com.ibnalsham.app.presentation.mvi.MVI
import com.ibnalsham.app.presentation.mvi.mvi
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class HomeViewModel(
    private val homeRepository: HomeRepository,
    private val menuRepository: MenuRepository,
    private val cartRepository: CartRepository,
    private val userPreferences: UserPreferencesRepository,
) : KMMViewModel(),
    MVI<HomeViewModelContract.MyUiState, HomeViewModelContract.Action, HomeViewModelContract.SideEffect>
    by mvi(HomeViewModelContract.MyUiState.defaultInstance()) {

    override fun onAction(event: HomeViewModelContract.Action) {
        when (event) {
            HomeViewModelContract.Action.ViewReady -> {
                updateState { copy(categoriesAsTabs = userPreferences.getHomeCategoriesAsTabs()) }
                observeCart()
                loadHomeData()
            }
            HomeViewModelContract.Action.Refresh -> loadHomeData()
            is HomeViewModelContract.Action.ProductClicked -> navigateToProduct(event.productId)
            is HomeViewModelContract.Action.CategoryClicked -> navigateToCategory(event.categoryId)
            is HomeViewModelContract.Action.BannerClicked -> { /* banner link */ }
            is HomeViewModelContract.Action.CategoryTabSelected -> loadTabProducts(event.categoryId)
            is HomeViewModelContract.Action.AddToCart -> handleAddToCart(event.productId)
            is HomeViewModelContract.Action.IncreaseQuantity -> handleIncreaseQuantity(event.productId)
            is HomeViewModelContract.Action.DecreaseQuantity -> handleDecreaseQuantity(event.productId)
            is HomeViewModelContract.Action.SetCategoriesAsTabs -> {
                updateState { copy(categoriesAsTabs = event.value) }
                userPreferences.saveHomeCategoriesAsTabs(event.value)
            }
        }
    }

    private fun observeCart() {
        viewModelScope.launch {
            cartRepository.cartItems.collectLatest { items ->
                updateState { copy(cartItemCounts = items.associate { it.product.id to it.quantity }) }
            }
        }
    }

    private fun loadHomeData() {
        viewModelScope.launch {
            updateState { copy(isLoading = true, error = null) }

            homeRepository.getHomeData().onSuccess { data ->
                updateState {
                    copy(
                        isLoading = false,
                        banners = data.banners,
                        categories = data.categories,
                        featuredProducts = data.featuredProducts,
                        specialOffers = data.specialOffers,
                    )
                }
            }.onFailure { error ->
                updateState { copy(isLoading = false, error = error) }
            }
        }
    }

    private fun loadTabProducts(categoryId: Int) {
        viewModelScope.launch {
            updateState {
                copy(
                    selectedTabCategoryId = categoryId,
                    isTabLoading = true,
                    tabProducts = persistentListOf(),
                )
            }
            menuRepository.getProducts(categoryId = categoryId, page = 1).onSuccess { data ->
                updateState { copy(isTabLoading = false, tabProducts = data.products) }
            }.onFailure {
                updateState { copy(isTabLoading = false) }
            }
        }
    }

    private fun handleAddToCart(productId: Int) {
        val allProducts = currentState.tabProducts + currentState.featuredProducts +
            currentState.specialOffers + currentState.mostOrdered
        val product = allProducts.firstOrNull { it.id == productId } ?: return
        cartRepository.addToCart(product)
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

    private fun navigateToProduct(id: Int) {
        viewModelScope.launch { emitEffect { HomeViewModelContract.SideEffect.NavigateToProduct(id) } }
    }

    private fun navigateToCategory(id: Int) {
        viewModelScope.launch { emitEffect { HomeViewModelContract.SideEffect.NavigateToCategory(id) } }
    }
}
