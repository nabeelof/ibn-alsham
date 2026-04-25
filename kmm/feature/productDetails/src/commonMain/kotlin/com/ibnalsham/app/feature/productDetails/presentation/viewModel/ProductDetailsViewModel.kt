package com.ibnalsham.app.feature.productDetails.presentation.viewModel

import com.ibnalsham.app.presentation.mvi.KMMViewModel
import com.ibnalsham.app.presentation.mvi.MVI
import kotlinx.collections.immutable.toImmutableList
import kotlinx.collections.immutable.toImmutableSet
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

import com.ibnalsham.app.feature.domain.ProductDomain
import com.ibnalsham.app.library.data.cache.CartRepository
import com.ibnalsham.app.presentation.mvi.mvi

class ProductDetailsViewModel(
    private val cartRepository: CartRepository,
) : KMMViewModel(),
    MVI<ProductDetailsViewModelContract.MyUiState, ProductDetailsViewModelContract.Action, ProductDetailsViewModelContract.SideEffect> 
    by mvi(ProductDetailsViewModelContract.MyUiState.defaultInstance()) {

    override fun onAction(event: ProductDetailsViewModelContract.Action) {
        when (event) {
            is ProductDetailsViewModelContract.Action.ViewReady -> loadProduct(event.productId)
            is ProductDetailsViewModelContract.Action.ToggleOption -> toggleOption(event.optionId)
            is ProductDetailsViewModelContract.Action.IncrementQuantity -> {
                if (currentState.quantity < event.maxAllowed) updateState { copy(quantity = quantity + 1) }
            }
            ProductDetailsViewModelContract.Action.DecrementQuantity -> {
                if (currentState.quantity > 1) updateState { copy(quantity = quantity - 1) }
            }
            ProductDetailsViewModelContract.Action.AddToCart -> handleAddToCart()
        }
    }

    private fun loadProduct(productId: Int) {
        viewModelScope.launch {
            updateState { copy(isLoading = true, productId = productId) }
            
            // Dummy network delay
            delay(800)
            
            updateState {
                copy(
                    isLoading = false,
                    name = "شاورما دجاج سوبر",
                    description = "شاورما الدجاج الأصلية بخلطتنا السورية المميزة، مع البطاطا المقلية والمثومة والمخلل الطازج.",
                    basePrice = 4.000,
                    effectivePrice = 3.500, // discounted
                    images = emptyList<ProductImageDomain>().toImmutableList(),
                    options = listOf(
                        ProductOptionDomain(1, "خبز محمص زيادة", 0.0, false),
                        ProductOptionDomain(2, "زيادة شرائح جبن", 0.500, false),
                        ProductOptionDomain(3, "علبة طحينية خارجية", 0.250, false),
                        ProductOptionDomain(4, "اختيار نوع الخبز", 0.0, true) // Required
                    ).toImmutableList(),
                    // Pre-select required option for convenience if it's single choice, but here we enforce logic
                    selectedOptionIds = emptySet<Int>().toImmutableSet()
                )
            }
        }
    }

    private fun toggleOption(optionId: Int) {
        val current = currentState.selectedOptionIds.toMutableSet()
        if (current.contains(optionId)) {
            current.remove(optionId)
        } else {
            current.add(optionId)
            // If single choice logic exists among grouped options, remove conflicting ones here.
        }
        updateState { copy(selectedOptionIds = current.toImmutableSet()) }
    }

    private fun handleAddToCart() {
        if (!currentState.isAddToCartEnabled) return
        
        viewModelScope.launch {
            updateState { copy(isAddingToCart = true) }
            val product = ProductDomain(
                id = currentState.productId,
                name = currentState.name,
                price = currentState.effectivePrice,
                image = currentState.images.firstOrNull()?.url,
            )
            repeat(currentState.quantity) { cartRepository.addToCart(product) }
            delay(500)
            updateState { copy(isAddingToCart = false) }
            emitEffect { ProductDetailsViewModelContract.SideEffect.ShowSnackbar("تم إضافة ${currentState.quantity} للحجم إلى السلة") }
            emitEffect { ProductDetailsViewModelContract.SideEffect.NavigateBack }
        }
    }
}
