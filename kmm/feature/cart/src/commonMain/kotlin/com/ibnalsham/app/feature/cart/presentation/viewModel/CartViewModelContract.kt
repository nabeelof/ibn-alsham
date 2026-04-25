package com.ibnalsham.app.feature.cart.presentation.viewModel

import com.ibnalsham.app.presentation.mvi.UiEvent
import com.ibnalsham.app.presentation.mvi.UiSideEffect
import com.ibnalsham.app.presentation.mvi.UiState
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

// Dummy domain models
data class CartItemDomain(
    val id: String, 
    val productId: Int, 
    val name: String, 
    val price: Double, 
    val quantity: Int, 
    val image: String?, 
    val optionsTotal: Double = 0.0
) {
    val total: Double get() = (price + optionsTotal) * quantity
}

interface CartViewModelContract {
    sealed class Action : UiEvent() {
        data object ViewReady : Action()
        data class IncreaseQuantity(val itemId: String) : Action()
        data class DecreaseQuantity(val itemId: String) : Action()
        data class RemoveItem(val itemId: String) : Action()
        data object ClearCart : Action()
        data object ProceedToCheckout : Action()
        data object ContinueShopping : Action()
    }

    data class MyUiState(
        val isLoading: Boolean = false,
        val items: ImmutableList<CartItemDomain> = persistentListOf(),
        val subtotal: Double = 0.0,
        val itemCount: Int = 0
    ) : UiState {
        companion object {
            fun defaultInstance() = MyUiState()
        }
    }

    sealed class SideEffect : UiSideEffect {
        data object NavigateToCheckout : SideEffect()
        data object NavigateToMenu : SideEffect()
    }
}
