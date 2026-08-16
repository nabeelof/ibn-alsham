package com.ibnalsham.app.library.data.cache

import com.ibnalsham.app.feature.domain.OptionDomain
import com.ibnalsham.app.feature.domain.ProductDomain
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class CartItem(
    /**
     * Stable identity for a cart line. The same product added twice with
     * different add-ons is two lines, so the id folds in the option selection.
     */
    val lineId: String,
    val product: ProductDomain,
    val quantity: Int,
    val selectedOptions: List<OptionDomain> = emptyList(),
) {
    val optionsTotal: Double get() = selectedOptions.sumOf { it.price }
    val unitTotal: Double get() = product.effectivePrice + optionsTotal
    val itemTotal: Double get() = unitTotal * quantity

    companion object {
        fun lineIdFor(productId: Int, options: List<OptionDomain>): String {
            val optionPart = options.map { it.id }.sorted().joinToString(",")
            return if (optionPart.isEmpty()) "$productId" else "$productId#$optionPart"
        }
    }
}

interface CartRepository {
    val cartItems: StateFlow<List<CartItem>>
    val cartItemCount: Int
    val cartSubtotal: Double

    fun addToCart(product: ProductDomain, options: List<OptionDomain> = emptyList(), quantity: Int = 1)
    fun removeLine(lineId: String)
    fun updateLineQuantity(lineId: String, quantity: Int)

    /** Replace the add-on selection on an existing line, merging if it collides. */
    fun updateLineOptions(lineId: String, options: List<OptionDomain>)

    fun removeFromCart(productId: Int)
    fun updateQuantity(productId: Int, quantity: Int)
    fun getQuantity(productId: Int): Int
    fun clearCart()
    fun getSubtotal(): Double

    /** Total quantity per product id, for the "+/-" steppers on product cards. */
    fun getItemCounts(): Map<Int, Int>
}

class InMemoryCartRepository : CartRepository {
    private val _cartItems = MutableStateFlow<List<CartItem>>(emptyList())
    override val cartItems = _cartItems.asStateFlow()

    override val cartItemCount: Int
        get() = _cartItems.value.sumOf { it.quantity }

    override val cartSubtotal: Double
        get() = _cartItems.value.sumOf { it.itemTotal }

    override fun addToCart(product: ProductDomain, options: List<OptionDomain>, quantity: Int) {
        if (quantity <= 0) return
        val lineId = CartItem.lineIdFor(product.id, options)

        _cartItems.update { current ->
            val existing = current.firstOrNull { it.lineId == lineId }
            if (existing != null) {
                current.map {
                    if (it.lineId == lineId) it.copy(quantity = it.quantity + quantity) else it
                }
            } else {
                current + CartItem(
                    lineId = lineId,
                    product = product,
                    quantity = quantity,
                    selectedOptions = options,
                )
            }
        }
    }

    override fun removeLine(lineId: String) {
        _cartItems.update { current -> current.filterNot { it.lineId == lineId } }
    }

    override fun updateLineQuantity(lineId: String, quantity: Int) {
        if (quantity <= 0) {
            removeLine(lineId)
            return
        }
        _cartItems.update { current ->
            current.map { if (it.lineId == lineId) it.copy(quantity = quantity) else it }
        }
    }

    override fun updateLineOptions(lineId: String, options: List<OptionDomain>) {
        _cartItems.update { current ->
            val target = current.firstOrNull { it.lineId == lineId } ?: return@update current
            val newLineId = CartItem.lineIdFor(target.product.id, options)
            val rest = current.filterNot { it.lineId == lineId }

            // Editing into an option set that already exists merges the two lines.
            val collision = rest.firstOrNull { it.lineId == newLineId }
            if (collision != null) {
                rest.map {
                    if (it.lineId == newLineId) it.copy(quantity = it.quantity + target.quantity) else it
                }
            } else {
                current.map {
                    if (it.lineId == lineId) {
                        it.copy(lineId = newLineId, selectedOptions = options)
                    } else {
                        it
                    }
                }
            }
        }
    }

    override fun removeFromCart(productId: Int) {
        _cartItems.update { current -> current.filterNot { it.product.id == productId } }
    }

    /**
     * Quantity control from product cards, which have no add-on context.
     * Applies to the plain (no add-ons) line for the product.
     */
    override fun updateQuantity(productId: Int, quantity: Int) {
        val plainLineId = CartItem.lineIdFor(productId, emptyList())
        val hasPlainLine = _cartItems.value.any { it.lineId == plainLineId }

        if (hasPlainLine) {
            updateLineQuantity(plainLineId, quantity)
            return
        }

        // Otherwise fall back to the only line for that product, if unambiguous.
        val lines = _cartItems.value.filter { it.product.id == productId }
        if (lines.size == 1) {
            updateLineQuantity(lines.first().lineId, quantity)
        }
    }

    override fun getQuantity(productId: Int): Int =
        _cartItems.value.filter { it.product.id == productId }.sumOf { it.quantity }

    override fun clearCart() {
        _cartItems.value = emptyList()
    }

    override fun getSubtotal(): Double = cartSubtotal

    override fun getItemCounts(): Map<Int, Int> =
        _cartItems.value.groupBy { it.product.id }
            .mapValues { (_, lines) -> lines.sumOf { it.quantity } }
}
