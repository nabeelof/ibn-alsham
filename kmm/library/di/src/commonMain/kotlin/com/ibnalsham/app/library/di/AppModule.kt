package com.ibnalsham.app.library.di

import com.ibnalsham.app.data.auth.AuthRepository
import com.ibnalsham.app.data.auth.NetworkAuthRepository
import com.ibnalsham.app.data.cart.CartApiRepository
import com.ibnalsham.app.data.cart.NetworkCartApiRepository
import com.ibnalsham.app.data.catalog.CatalogRepository
import com.ibnalsham.app.data.catalog.NetworkCatalogRepository
import com.ibnalsham.app.data.home.HomeRepository
import com.ibnalsham.app.data.home.NetworkHomeRepository
import com.ibnalsham.app.data.menu.MenuRepository
import com.ibnalsham.app.data.menu.NetworkMenuRepository
import com.ibnalsham.app.data.payment.NetworkPaymentRepository
import com.ibnalsham.app.data.payment.PaymentRepository
import com.ibnalsham.app.data.product.NetworkProductRepository
import com.ibnalsham.app.data.product.ProductRepository
import com.ibnalsham.app.data.search.NetworkSearchRepository
import com.ibnalsham.app.data.search.SearchRepository
import com.ibnalsham.app.data.address.AddressRepository
import com.ibnalsham.app.data.address.NetworkAddressRepository
import com.ibnalsham.app.data.coupon.CouponRepository
import com.ibnalsham.app.data.coupon.NetworkCouponRepository
import com.ibnalsham.app.data.settings.NetworkSettingsRepository
import com.ibnalsham.app.data.settings.SettingsRepository
import com.ibnalsham.app.data.wallet.NetworkWalletRepository
import com.ibnalsham.app.data.wallet.WalletRepository
import com.ibnalsham.app.data.upload.NetworkUploadRepository
import com.ibnalsham.app.data.upload.UploadRepository
import com.ibnalsham.app.data.review.NetworkReviewRepository
import com.ibnalsham.app.data.review.ReviewRepository
import com.ibnalsham.app.library.data.network.createHttpClient
import com.ibnalsham.app.library.data.network.networkJson
import com.ibnalsham.app.data.order.OrderRepository
import com.ibnalsham.app.data.order.NetworkOrderRepository
import com.ibnalsham.app.library.data.cache.CartRepository
import com.ibnalsham.app.library.data.cache.InMemoryCartRepository
import com.ibnalsham.app.library.data.cache.UserPreferencesRepository
import com.ibnalsham.app.library.data.cache.InMemoryUserPreferencesRepository
import com.ibnalsham.app.feature.home.presentation.viewModel.HomeViewModel
import com.ibnalsham.app.feature.menu.presentation.viewModel.MenuViewModel
import com.ibnalsham.app.feature.auth.presentation.viewModel.AuthViewModel
import com.ibnalsham.app.feature.cart.presentation.viewModel.CartViewModel
import com.ibnalsham.app.feature.checkout.presentation.viewModel.CheckoutViewModel
import com.ibnalsham.app.feature.orders.presentation.viewModel.OrdersViewModel
import com.ibnalsham.app.feature.productDetails.presentation.viewModel.ProductDetailsViewModel
import com.ibnalsham.app.feature.more.presentation.viewModel.MoreViewModel
import org.koin.dsl.module

val appModule = module {
    // Infrastructure
    single { networkJson }
    single<UserPreferencesRepository> { InMemoryUserPreferencesRepository() }
    single { createHttpClient(get(), get()) }
    single<CartRepository> { InMemoryCartRepository() }

    // Repositories
    single<HomeRepository> { NetworkHomeRepository(get()) }
    single<MenuRepository> { NetworkMenuRepository(get()) }
    single<AuthRepository> { NetworkAuthRepository(get()) }
    single<OrderRepository> { NetworkOrderRepository(get()) }
    single<CatalogRepository> { NetworkCatalogRepository(get()) }
    single<ProductRepository> { NetworkProductRepository(get()) }
    single<SearchRepository> { NetworkSearchRepository(get()) }
    single<CartApiRepository> { NetworkCartApiRepository(get()) }
    single<PaymentRepository> { NetworkPaymentRepository(get()) }
    single<AddressRepository> { NetworkAddressRepository(get()) }
    single<CouponRepository> { NetworkCouponRepository(get()) }
    single<SettingsRepository> { NetworkSettingsRepository(get()) }
    single<WalletRepository> { NetworkWalletRepository(get()) }
    single<UploadRepository> { NetworkUploadRepository(get()) }
    single<ReviewRepository> { NetworkReviewRepository(get()) }

    // ViewModels
    factory { HomeViewModel(get(), get(), get(), get()) }
    factory { MenuViewModel(get(), get(), get()) }
    factory { AuthViewModel(get(), get()) }
    factory { CartViewModel(get()) }
    factory { CheckoutViewModel(get(), get()) }
    factory { OrdersViewModel(get()) }
    factory { ProductDetailsViewModel(get()) }
    factory { MoreViewModel(get()) }
}
