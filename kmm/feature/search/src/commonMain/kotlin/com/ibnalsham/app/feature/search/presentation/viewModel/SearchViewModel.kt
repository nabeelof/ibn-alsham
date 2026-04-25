package com.ibnalsham.app.feature.search.presentation.viewModel

import com.ibnalsham.app.core.common.AppResult
import com.ibnalsham.app.data.product.ProductRepository
import com.ibnalsham.app.feature.domain.ProductDomain
import com.ibnalsham.app.presentation.mvi.KMMViewModel
import com.ibnalsham.app.presentation.mvi.MVI
import com.ibnalsham.app.presentation.mvi.mvi
import kotlinx.coroutines.launch
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay

class SearchViewModel(
    private val productRepository: ProductRepository
) : KMMViewModel(),
    MVI<SearchViewModelContract.MyUiState, SearchViewModelContract.Action, SearchViewModelContract.SideEffect> by mvi(
        SearchViewModelContract.MyUiState.defaultInstance()
    ) {

    private var searchJob: Job? = null

    override fun onAction(action: SearchViewModelContract.Action) {
        when (action) {
            is SearchViewModelContract.Action.QueryChanged -> {
                updateState { copy(query = action.query) }
                debounceSearch(action.query)
            }
            is SearchViewModelContract.Action.SubmitSearch -> performSearch(uiState.value.query)
            is SearchViewModelContract.Action.ProductClicked -> {
                viewModelScope.launch {
                    emitEffect { SearchViewModelContract.SideEffect.NavigateToProduct(action.productId) }
                }
            }
        }
    }

    private fun debounceSearch(query: String) {
        searchJob?.cancel()
        if (query.isBlank()) {
            updateState { copy(results = kotlinx.collections.immutable.persistentListOf(), hasSearched = false) }
            return
        }
        searchJob = viewModelScope.launch {
            delay(500) // 500ms debounce
            performSearch(query)
        }
    }

    private fun performSearch(query: String) {
        if (query.isBlank()) return
        
        viewModelScope.launch {
            updateState { copy(isLoading = true, error = null, hasSearched = true) }
            val result = productRepository.searchProducts(query)
            when (result) {
                is AppResult.Success -> {
                    val domains = result.value.map {
                        ProductDomain(
                            id = it.id,
                            name = it.name,
                            description = null,
                            price = it.price,
                            discountedPrice = it.discountedPrice,
                            image = it.imageUrl
                        )
                    }.toImmutableList()
                    updateState { copy(isLoading = false, results = domains) }
                }
                is AppResult.Failure -> {
                    updateState { copy(isLoading = false, error = result.errorValue) }
                }
            }
        }
    }
}
