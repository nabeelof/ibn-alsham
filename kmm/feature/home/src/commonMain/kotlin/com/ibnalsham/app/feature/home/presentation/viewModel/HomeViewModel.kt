package com.ibnalsham.app.feature.home.presentation.viewModel

import com.ibnalsham.app.core.common.onFailure
import com.ibnalsham.app.core.common.onSuccess
import com.ibnalsham.app.data.home.HomeRepository
import com.ibnalsham.app.data.menu.MenuRepository
import com.ibnalsham.app.library.data.cache.CartRepository
import com.ibnalsham.app.library.data.cache.SessionManager
import com.ibnalsham.app.library.data.cache.UserPreferencesRepository
import com.ibnalsham.app.presentation.mvi.KMMViewModel
import com.ibnalsham.app.presentation.mvi.MVI
import com.ibnalsham.app.presentation.mvi.mvi
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.launch

class HomeViewModel(
    private val homeRepository: HomeRepository,
    private val menuRepository: MenuRepository,
    private val cartRepository: CartRepository,
    private val userPreferences: UserPreferencesRepository,
    private val sessionManager: SessionManager,
) : KMMViewModel(),
    MVI<HomeViewModelContract.MyUiState, HomeViewModelContract.Action, HomeViewModelContract.SideEffect>
    by mvi(HomeViewModelContract.MyUiState.defaultInstance()) {

    override fun onAction(event: HomeViewModelContract.Action) {
        when (event) {
            HomeViewModelContract.Action.ViewReady -> {
                updateState { copy(categoriesAsTabs = userPreferences.getHomeCategoriesAsTabs()) }
                observeCart()
                observeSession()
                loadHomeData()
            }

            HomeViewModelContract.Action.Refresh -> loadHomeData()
            is HomeViewModelContract.Action.ProductClicked -> navigateToProduct(event.productId)
            is HomeViewModelContract.Action.CategoryClicked -> navigateToCategory(event.categoryId)
            is HomeViewModelContract.Action.SubCategoryClicked -> selectSubCategory(event.categoryId)
            is HomeViewModelContract.Action.BannerClicked -> handleBannerClick(event.bannerId)
            is HomeViewModelContract.Action.CategoryTabSelected -> selectTab(event.categoryId)
            is HomeViewModelContract.Action.AddToCart -> handleAddToCart(event.productId)
            is HomeViewModelContract.Action.IncreaseQuantity -> handleIncreaseQuantity(event.productId)
            is HomeViewModelContract.Action.DecreaseQuantity -> handleDecreaseQuantity(event.productId)
            is HomeViewModelContract.Action.SetCategoriesAsTabs -> {
                updateState { copy(categoriesAsTabs = event.value) }
                userPreferences.saveHomeCategoriesAsTabs(event.value)
            }

            HomeViewModelContract.Action.ActiveOrderClicked -> {
                val orderId = currentState.activeOrder?.id ?: return
                viewModelScope.launch { emitEffect { HomeViewModelContract.SideEffect.NavigateToOrder(orderId) } }
            }

            HomeViewModelContract.Action.SearchClicked -> {
                viewModelScope.launch { emitEffect { HomeViewModelContract.SideEffect.NavigateToSearch } }
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
     * Signing in changes what home shows — the greeting, the active-order card and
     * the per-product favourite flags all come back different — so re-fetch on any
     * session change. The initial value is skipped because [loadHomeData] already
     * runs on ViewReady.
     */
    private fun observeSession() {
        viewModelScope.launch {
            sessionManager.session.collectLatest { session ->
                val signedIn = session as? SessionManager.Session.SignedIn
                updateState {
                    copy(isSignedIn = signedIn != null, userName = signedIn?.name.orEmpty())
                }
            }
        }
        viewModelScope.launch {
            sessionManager.session.drop(1).collectLatest { loadHomeData() }
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
                        mostOrdered = data.mostOrdered,
                        activeOrder = data.activeOrder,
                        announcement = data.announcement,
                    )
                }
            }.onFailure { error ->
                updateState { copy(isLoading = false, error = error) }
            }
        }
    }

    private fun selectTab(categoryId: Int) {
        updateState { copy(selectedSubCategoryId = null) }
        loadTabProducts(categoryId)
    }

    private fun selectSubCategory(categoryId: Int) {
        updateState { copy(selectedSubCategoryId = categoryId) }
        loadProductsFor(categoryId)
    }

    private fun loadTabProducts(categoryId: Int) {
        updateState { copy(selectedTabCategoryId = categoryId) }
        loadProductsFor(categoryId)
    }

    private fun loadProductsFor(categoryId: Int) {
        viewModelScope.launch {
            updateState { copy(isTabLoading = true, tabProducts = persistentListOf()) }

            menuRepository.getProducts(categoryId = categoryId, page = 1).onSuccess { data ->
                updateState { copy(isTabLoading = false, tabProducts = data.products) }
            }.onFailure {
                updateState { copy(isTabLoading = false) }
            }
        }
    }

    private fun handleBannerClick(bannerId: Int) {
        val banner = currentState.banners.firstOrNull { it.id == bannerId } ?: return
        val target = banner.linkValue?.toIntOrNull() ?: return

        viewModelScope.launch {
            when (banner.linkType) {
                "product" -> emitEffect { HomeViewModelContract.SideEffect.NavigateToProduct(target) }
                "category" -> emitEffect { HomeViewModelContract.SideEffect.NavigateToCategory(target) }
                else -> Unit
            }
        }
    }

    private fun allLoadedProducts() = currentState.tabProducts +
        currentState.featuredProducts +
        currentState.specialOffers +
        currentState.mostOrdered

    private fun handleAddToCart(productId: Int) {
        val product = allLoadedProducts().firstOrNull { it.id == productId } ?: return
        cartRepository.addToCart(product)
    }

    private fun handleIncreaseQuantity(productId: Int) {
        val current = cartRepository.getQuantity(productId)
        if (current == 0) {
            handleAddToCart(productId)
        } else {
            cartRepository.updateQuantity(productId, current + 1)
        }
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
