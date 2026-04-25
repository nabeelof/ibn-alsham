package com.ibnalsham.app.feature.home.presentation.viewModel

import com.ibnalsham.app.data.home.HomeRepository
import com.ibnalsham.app.data.menu.MenuRepository
import com.ibnalsham.app.core.common.onSuccess
import com.ibnalsham.app.core.common.onFailure
import com.ibnalsham.app.presentation.mvi.KMMViewModel
import com.ibnalsham.app.presentation.mvi.MVI
import com.ibnalsham.app.presentation.mvi.mvi
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.launch

class HomeViewModel(
    private val homeRepository: HomeRepository,
    private val menuRepository: MenuRepository,
) : KMMViewModel(),
    MVI<HomeViewModelContract.MyUiState, HomeViewModelContract.Action, HomeViewModelContract.SideEffect>
    by mvi(HomeViewModelContract.MyUiState.defaultInstance()) {

    override fun onAction(event: HomeViewModelContract.Action) {
        when (event) {
            HomeViewModelContract.Action.ViewReady, HomeViewModelContract.Action.Refresh -> loadHomeData()
            is HomeViewModelContract.Action.ProductClicked -> navigateToProduct(event.productId)
            is HomeViewModelContract.Action.CategoryClicked -> navigateToCategory(event.categoryId)
            is HomeViewModelContract.Action.BannerClicked -> { /* banner link */ }
            is HomeViewModelContract.Action.CategoryTabSelected -> loadTabProducts(event.categoryId)
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

    private fun navigateToProduct(id: Int) {
        viewModelScope.launch { emitEffect { HomeViewModelContract.SideEffect.NavigateToProduct(id) } }
    }

    private fun navigateToCategory(id: Int) {
        viewModelScope.launch { emitEffect { HomeViewModelContract.SideEffect.NavigateToCategory(id) } }
    }
}
