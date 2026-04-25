package com.ibnalsham.app.feature.cart.presentation.viewModel

import com.ibnalsham.app.presentation.mvi.KMMViewModel
import com.ibnalsham.app.presentation.mvi.MVI
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.launch

import com.ibnalsham.app.library.data.cache.CartRepository
import com.ibnalsham.app.presentation.mvi.mvi
import kotlinx.coroutines.flow.collectLatest

class CartViewModel(
    private val cartRepository: CartRepository
) : KMMViewModel(), 
    MVI<CartViewModelContract.MyUiState, CartViewModelContract.Action, CartViewModelContract.SideEffect> 
    by mvi(CartViewModelContract.MyUiState.defaultInstance()) {

    override fun onAction(event: CartViewModelContract.Action) {
        when (event) {
            CartViewModelContract.Action.ViewReady -> observeCart()
            is CartViewModelContract.Action.IncreaseQuantity -> updateQuantity(event.itemId, 1)
            is CartViewModelContract.Action.DecreaseQuantity -> updateQuantity(event.itemId, -1)
            is CartViewModelContract.Action.RemoveItem -> removeItem(event.itemId)
            CartViewModelContract.Action.ClearCart -> cartRepository.clearCart()
            CartViewModelContract.Action.ProceedToCheckout -> handleProceedToCheckout()
            CartViewModelContract.Action.ContinueShopping -> handleContinueShopping()
        }
    }

    private fun observeCart() {
        viewModelScope.launch {
            cartRepository.cartItems.collectLatest { items ->
                val domainItems = items.map { item ->
                    CartItemDomain(
                        id = item.product.id.toString(),
                        productId = item.product.id,
                        name = item.product.name,
                        price = item.product.price,
                        quantity = item.quantity,
                        image = item.product.image
                    )
                }.toImmutableList()

                val subtotal = cartRepository.getSubtotal()
                val count = items.sumOf { it.quantity }

                updateState { 
                    copy(
                        items = domainItems,
                        subtotal = subtotal,
                        itemCount = count
                    ) 
                }
            }
        }
    }

    private fun handleProceedToCheckout() {
        if (currentState.items.isNotEmpty()) {
            viewModelScope.launch { emitEffect { CartViewModelContract.SideEffect.NavigateToCheckout } }
        }
    }

    private fun handleContinueShopping() {
        viewModelScope.launch { emitEffect { CartViewModelContract.SideEffect.NavigateToMenu } }
    }

    private fun updateQuantity(itemId: String, delta: Int) {
        val item = currentState.items.find { it.id == itemId } ?: return
        cartRepository.updateQuantity(item.productId, item.quantity + delta)
    }

    private fun removeItem(itemId: String) {
        val item = currentState.items.find { it.id == itemId } ?: return
        cartRepository.removeFromCart(item.productId)
    }
}
