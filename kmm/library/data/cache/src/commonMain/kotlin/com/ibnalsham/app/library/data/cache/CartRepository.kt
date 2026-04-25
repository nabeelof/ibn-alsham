package com.ibnalsham.app.library.data.cache

import com.ibnalsham.app.feature.domain.OptionDomain
import com.ibnalsham.app.feature.domain.ProductDomain
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class CartItem(
    val product: ProductDomain,
    val quantity: Int,
    val selectedOptions: List<OptionDomain> = emptyList(),
) {
    val optionsTotal: Double get() = selectedOptions.sumOf { it.price }
    val itemTotal: Double get() = (product.effectivePrice + optionsTotal) * quantity
}

interface CartRepository {
    val cartItems: StateFlow<List<CartItem>>
    val cartItemCount: Int
    val cartSubtotal: Double
    fun addToCart(product: ProductDomain, options: List<OptionDomain> = emptyList())
    fun removeFromCart(productId: Int)
    fun updateQuantity(productId: Int, quantity: Int)
    fun getQuantity(productId: Int): Int
    fun clearCart()
    fun getSubtotal(): Double
    fun getItemCounts(): Map<Int, Int>
}

class InMemoryCartRepository : CartRepository {
    private val _cartItems = MutableStateFlow<List<CartItem>>(emptyList())
    override val cartItems = _cartItems.asStateFlow()

    override val cartItemCount: Int
        get() = _cartItems.value.sumOf { it.quantity }

    override val cartSubtotal: Double
        get() = _cartItems.value.sumOf { it.itemTotal }

    override fun addToCart(product: ProductDomain, options: List<OptionDomain>) {
        _cartItems.update { current ->
            val existing = current.find { it.product.id == product.id }
            if (existing != null) {
                current.map {
                    if (it.product.id == product.id) it.copy(quantity = it.quantity + 1)
                    else it
                }
            } else {
                current + CartItem(product, 1, options)
            }
        }
    }

    override fun removeFromCart(productId: Int) {
        _cartItems.update { current -> current.filter { it.product.id != productId } }
    }

    override fun updateQuantity(productId: Int, quantity: Int) {
        if (quantity <= 0) {
            removeFromCart(productId)
            return
        }
        _cartItems.update { current ->
            current.map {
                if (it.product.id == productId) it.copy(quantity = quantity) else it
            }
        }
    }

    override fun getQuantity(productId: Int): Int {
        return _cartItems.value.find { it.product.id == productId }?.quantity ?: 0
    }

    override fun clearCart() {
        _cartItems.value = emptyList()
    }

    override fun getSubtotal(): Double {
        return _cartItems.value.sumOf { it.itemTotal }
    }

    override fun getItemCounts(): Map<Int, Int> {
        return _cartItems.value.associate { it.product.id to it.quantity }
    }
}
