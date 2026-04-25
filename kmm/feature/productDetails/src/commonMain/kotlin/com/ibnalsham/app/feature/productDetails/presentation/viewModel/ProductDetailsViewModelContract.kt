package com.ibnalsham.app.feature.productDetails.presentation.viewModel

import com.ibnalsham.app.data.network.error.DomainError
import com.ibnalsham.app.presentation.mvi.UiEvent
import com.ibnalsham.app.presentation.mvi.UiSideEffect
import com.ibnalsham.app.presentation.mvi.UiState
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.ImmutableSet
import kotlinx.collections.immutable.persistentSetOf

data class ProductOptionDomain(val id: Int, val name: String, val price: Double, val isRequired: Boolean)
data class ProductImageDomain(val id: Int, val url: String)

interface ProductDetailsViewModelContract {
    sealed class Action : UiEvent() {
        data class ViewReady(val productId: Int) : Action()
        data class ToggleOption(val optionId: Int) : Action()
        data class IncrementQuantity(val maxAllowed: Int = 10) : Action()
        data object DecrementQuantity : Action()
        data object AddToCart : Action()
    }

    data class MyUiState(
        val isLoading: Boolean = true,
        val error: DomainError? = null,
        val productId: Int = 0,
        val name: String = "",
        val description: String = "",
        val basePrice: Double = 0.0,
        val effectivePrice: Double = 0.0,
        val images: ImmutableList<ProductImageDomain> = persistentListOf(),
        val options: ImmutableList<ProductOptionDomain> = persistentListOf(),
        val selectedOptionIds: ImmutableSet<Int> = persistentSetOf(),
        val quantity: Int = 1,
        val isAddingToCart: Boolean = false
    ) : UiState {
        val calculatedTotal: Double
            get() {
                val optionsTotal = options.filter { it.id in selectedOptionIds }.sumOf { it.price }
                return (effectivePrice + optionsTotal) * quantity
            }
            
        val isAddToCartEnabled: Boolean
            get() {
                val requiredOptions = options.filter { it.isRequired }.map { it.id }
                return selectedOptionIds.containsAll(requiredOptions) && !isAddingToCart && !isLoading
            }

        companion object {
            fun defaultInstance() = MyUiState()
        }
    }

    sealed class SideEffect : UiSideEffect {
        data object NavigateBack : SideEffect()
        data class ShowSnackbar(val message: String) : SideEffect()
    }
}
