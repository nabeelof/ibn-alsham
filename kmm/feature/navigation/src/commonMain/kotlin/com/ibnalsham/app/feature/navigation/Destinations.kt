package com.ibnalsham.app.feature.navigation

sealed class Destination(val route: String) {
    data object Splash : Destination("splash")
    data object Auth : Destination("auth")
    data object Home : Destination("home")
    data object Menu : Destination("menu")
    data object Cart : Destination("cart")
    data object Checkout : Destination("checkout")
    data object More : Destination("more")
    data object Search : Destination("search")
    data object Favorites : Destination("favorites")
    data object Profile : Destination("profile")
    data object Notifications : Destination("notifications")
    data object Settings : Destination("settings")

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

    data class Category(val categoryId: Int) : Destination("category/{categoryId}") {
        companion object {
            const val ARG_CATEGORY_ID = "categoryId"
        }
    }
}

/**
 * Deep links the app answers to, e.g. https://ibnalsham.prolabtech.net/product/42
 * or ibnalsham://product/42.
 */
object DeepLink {
    const val SCHEME = "ibnalsham"
    const val HOST = "ibnalsham.prolabtech.net"
    const val WEB_BASE_URL = "https://$HOST"

    fun productUrl(productId: Int): String = "$WEB_BASE_URL/product/$productId"

    fun categoryUrl(categoryId: Int): String = "$WEB_BASE_URL/category/$categoryId"

    fun orderUrl(orderId: Int): String = "$WEB_BASE_URL/orders/$orderId"

    /**
     * Resolve an incoming deep link to an in-app route, or null when it is not
     * one this app handles. Accepts both the custom scheme and https links.
     */
    fun resolve(url: String?): String? {
        if (url.isNullOrBlank()) return null

        val withoutScheme = url
            .substringAfter("://", missingDelimiterValue = url)
            .removePrefix(HOST)
            .substringBefore('?')
            .trim('/')

        val segments = withoutScheme.split('/').filter { it.isNotBlank() }
        if (segments.isEmpty()) return Destination.Home.route

        val id = segments.getOrNull(1)?.toIntOrNull()

        return when (segments[0]) {
            "product", "products" -> id?.let { "product/$it" }
            "category", "categories" -> id?.let { "category/$it" }
            "orders", "order" -> if (id != null) "orders?orderId=$id" else "orders?orderId=-1"
            "menu" -> Destination.Menu.route
            "cart" -> Destination.Cart.route
            "favorites", "wishlist" -> Destination.Favorites.route
            "notifications" -> Destination.Notifications.route
            "profile", "account" -> Destination.Profile.route
            "search" -> Destination.Search.route
            "settings" -> Destination.Settings.route
            "home" -> Destination.Home.route
            else -> null
        }
    }
}
