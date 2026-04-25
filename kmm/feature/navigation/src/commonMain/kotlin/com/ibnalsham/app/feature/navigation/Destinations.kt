package com.ibnalsham.app.feature.navigation

sealed class Destination(val route: String) {
    data object Splash : Destination("splash")
    data object Auth : Destination("auth")
    data object Home : Destination("home")
    data object Menu : Destination("menu")
    data object Cart : Destination("cart")
    data object Checkout : Destination("checkout")
    data object More : Destination("more")
    
    data class Orders(val orderId: Int? = null) : Destination("orders?orderId={orderId}") {
        companion object {
            const val ARG_ORDER_ID = "orderId"
        }
    }
    
    data class ProductDetails(val productId: Int) : Destination("product/{productId}") {
        companion object {
            const val ARG_PRODUCT_ID = "productId"
        }
    }
}
