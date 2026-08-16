package com.ibnalsham.app.library.di

import com.ibnalsham.app.feature.auth.presentation.viewModel.AuthViewModel
import com.ibnalsham.app.feature.cart.presentation.viewModel.CartViewModel
import com.ibnalsham.app.feature.catalog.presentation.viewModel.CatalogViewModel
import com.ibnalsham.app.feature.checkout.presentation.viewModel.CheckoutViewModel
import com.ibnalsham.app.feature.favorites.presentation.viewModel.FavoritesViewModel
import com.ibnalsham.app.feature.home.presentation.viewModel.HomeViewModel
import com.ibnalsham.app.feature.menu.presentation.viewModel.MenuViewModel
import com.ibnalsham.app.feature.more.presentation.viewModel.MoreViewModel
import com.ibnalsham.app.feature.notifications.presentation.viewModel.NotificationsViewModel
import com.ibnalsham.app.feature.orders.presentation.viewModel.OrdersViewModel
import com.ibnalsham.app.feature.productDetails.presentation.viewModel.ProductDetailsViewModel
import com.ibnalsham.app.feature.profile.presentation.viewModel.ProfileViewModel
import com.ibnalsham.app.feature.search.presentation.viewModel.SearchViewModel
import com.ibnalsham.app.feature.settings.presentation.viewModel.SettingsViewModel
import org.koin.core.component.KoinComponent
import org.koin.core.context.startKoin

fun initKoin() {
    startKoin {
        modules(appModule)
    }
}

/**
 * Bridge for iOS: SwiftUI cannot use Koin's `inject()` directly, so each
 * ViewModel is exposed as a property here.
 */
class KoinHelper : KoinComponent {

    val provideHomeViewModel: HomeViewModel get() = getKoin().get()
    val provideMenuViewModel: MenuViewModel get() = getKoin().get()
    val provideAuthViewModel: AuthViewModel get() = getKoin().get()
    val provideCartViewModel: CartViewModel get() = getKoin().get()
    val provideCheckoutViewModel: CheckoutViewModel get() = getKoin().get()
    val provideOrdersViewModel: OrdersViewModel get() = getKoin().get()
    val provideProductDetailsViewModel: ProductDetailsViewModel get() = getKoin().get()
    val provideMoreViewModel: MoreViewModel get() = getKoin().get()
    val provideCatalogViewModel: CatalogViewModel get() = getKoin().get()
    val provideSearchViewModel: SearchViewModel get() = getKoin().get()
    val provideFavoritesViewModel: FavoritesViewModel get() = getKoin().get()
    val provideProfileViewModel: ProfileViewModel get() = getKoin().get()
    val provideNotificationsViewModel: NotificationsViewModel get() = getKoin().get()
    val provideSettingsViewModel: SettingsViewModel get() = getKoin().get()

    fun <T : Any> get(clazz: kotlin.reflect.KClass<T>): T = getKoin().get(clazz, null, null)
}
