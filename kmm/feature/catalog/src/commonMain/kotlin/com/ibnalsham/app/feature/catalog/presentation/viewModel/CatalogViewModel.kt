package com.ibnalsham.app.feature.catalog.presentation.viewModel

import com.ibnalsham.app.core.common.AppResult
import com.ibnalsham.app.data.catalog.CatalogRepository
import com.ibnalsham.app.feature.domain.CategoryDomain
import com.ibnalsham.app.presentation.mvi.KMMViewModel
import com.ibnalsham.app.presentation.mvi.MVI
import com.ibnalsham.app.presentation.mvi.mvi
import kotlinx.coroutines.launch
import kotlinx.collections.immutable.toImmutableList

class CatalogViewModel(
    private val catalogRepository: CatalogRepository
) : KMMViewModel(),
    MVI<CatalogViewModelContract.MyUiState, CatalogViewModelContract.Action, CatalogViewModelContract.SideEffect> by mvi(
        CatalogViewModelContract.MyUiState.defaultInstance()
    ) {

    override fun onAction(action: CatalogViewModelContract.Action) {
        when (action) {
            is CatalogViewModelContract.Action.ViewReady -> fetchCategories()
            is CatalogViewModelContract.Action.CategoryClicked -> {
                viewModelScope.launch { emitEffect { CatalogViewModelContract.SideEffect.NavigateToCategory(action.categoryId) } }
            }
        }
    }

    private fun fetchCategories() {
        viewModelScope.launch {
            updateState { copy(isLoading = true, error = null) }
            val result = catalogRepository.getCategories()
            when (result) {
                is AppResult.Success -> {
                    val domains = result.value.map { CategoryDomain(it.id, it.name, it.banner ?: it.icon) }.toImmutableList()
                    updateState { copy(isLoading = false, categories = domains) }
                }
                is AppResult.Failure -> {
                    updateState { copy(isLoading = false, error = result.errorValue) }
                }
            }
        }
    }
}
