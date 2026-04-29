package com.ibnalsham.app

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.ibnalsham.app.feature.navigation.Destination
import com.ibnalsham.app.ui.design.theme.IbnAlshamTheme
import com.ibnalsham.app.ui.feature.splash.SplashScreen

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.Modifier
import androidx.navigation.compose.currentBackStackEntryAsState
import com.ibnalsham.app.ui.design.designSystem.R as DesignR
import com.ibnalsham.app.ui.feature.more.MoreRoute
import com.ibnalsham.app.ui.feature.auth.AuthRoute
import com.ibnalsham.app.ui.feature.cart.CartRoute
import com.ibnalsham.app.ui.feature.checkout.CheckoutRoute
import com.ibnalsham.app.ui.feature.home.HomeRoute
import com.ibnalsham.app.ui.feature.menu.MenuRoute
import com.ibnalsham.app.ui.feature.orders.OrdersRoute
import com.ibnalsham.app.ui.feature.productDetails.ProductDetailsRoute
import kotlinx.coroutines.launch



class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Force Arabic by default on first run if nothing is set
        if (AppCompatDelegate.getApplicationLocales().isEmpty) {
            val localeList = LocaleListCompat.forLanguageTags("ar")
            AppCompatDelegate.setApplicationLocales(localeList)
        }

        setContent {
            IbnAlshamTheme {
                AppNavigation()
            }
        }
    }
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    
    val showSnackbar: (String) -> Unit = { message ->
        scope.launch { snackbarHostState.showSnackbar(message) }
    }

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val bottomNavItems = listOf(
        Triple(Destination.Home.route, Icons.Default.Home, stringResource(DesignR.string.nav_home)),
        Triple(Destination.Menu.route, Icons.Default.Restaurant, stringResource(DesignR.string.nav_menu)),
        Triple("orders", Icons.Default.Receipt, stringResource(DesignR.string.nav_orders)),
        Triple(Destination.More.route, Icons.Default.MoreHoriz, stringResource(DesignR.string.nav_more)),
    )

    val currentDestination = navBackStackEntry?.destination?.route
    val orderIdArg = navBackStackEntry?.arguments?.getInt(Destination.Orders.ARG_ORDER_ID) ?: -1
    
    val showBottomBar = when (currentDestination) {
        Destination.Home.route, Destination.Menu.route, Destination.More.route -> true
        Destination.Orders().route -> orderIdArg == -1 // Only show if we're not tracking a specific order
        else -> false
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            if (showBottomBar) {
                NavigationBar(containerColor = androidx.compose.ui.graphics.Color.White) {
                    bottomNavItems.forEach { (route, icon, label) ->
                        NavigationBarItem(
                            icon = { Icon(icon, contentDescription = label) },
                            label = { Text(label) },
                            selected = currentDestination?.startsWith(route) == true,
                            onClick = {
                                if (currentDestination != route) {
                                    navController.navigate(route) {
                                        if (route == Destination.Home.route) {
                                            popUpTo(Destination.Home.route)
                                        } else {
                                            popUpTo(Destination.Home.route) { saveState = true }
                                            restoreState = true
                                        }
                                        launchSingleTop = true
                                    }
                                }
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = androidx.compose.ui.graphics.Color(0xFFC62828),
                                selectedTextColor = androidx.compose.ui.graphics.Color(0xFFC62828),
                                indicatorColor = androidx.compose.ui.graphics.Color(0xFFFFEBEE)
                            )
                        )
                    }
                }
            }
        }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = Destination.Splash.route,
            modifier = Modifier.padding(paddingValues)
        ) {
            composable(Destination.Splash.route) {
                SplashScreen(
                    onSplashFinished = {
                        navController.navigate(Destination.Home.route) {
                            popUpTo(Destination.Splash.route) { inclusive = true }
                        }
                    }
                )
            }

            composable(Destination.Auth.route) {
                AuthRoute(
                    onNavigateToHome = {
                        navController.navigate(Destination.Home.route) {
                            popUpTo(Destination.Auth.route) { inclusive = true }
                        }
                    },
                    onShowSnackbar = showSnackbar
                )
            }

            composable(Destination.Home.route) {
                HomeRoute(
                    onNavigateToCategory = { navController.navigate(Destination.Menu.route) },
                    onNavigateToProduct = { productId ->
                        navController.navigate("product/$productId")
                    },
                    onNavigateToSearch = {
                        // TODO: Wire to a dedicated Search destination when added to the app nav graph.
                    },
                    onNavigateToCart = { navController.navigate(Destination.Cart.route) },
                )
            }

            composable(Destination.Menu.route) {
                MenuRoute(
                    onNavigateToProduct = { productId ->
                        navController.navigate("product/$productId")
                    },
                    onNavigateToCart = { navController.navigate(Destination.Cart.route) },
                    onShowSnackbar = showSnackbar,
                    onNavigateBack = { navController.popBackStack() },
                )
            }

            composable(Destination.Cart.route) {
                CartRoute(
                    onNavigateToCheckout = { navController.navigate(Destination.Checkout.route) },
                    onNavigateToMenu = { navController.popBackStack() },
                    onNavigateBack = { navController.popBackStack() },
                )
            }

            composable(Destination.Checkout.route) {
                CheckoutRoute(
                    onNavigateToOrderTracking = { orderId ->
                        navController.navigate("orders?orderId=$orderId") {
                            popUpTo(Destination.Home.route) { inclusive = false }
                        }
                    },
                    onNavigateBack = { navController.popBackStack() },
                    onShowSnackbar = showSnackbar
                )
            }

            composable(
                route = Destination.Orders().route,
                arguments = listOf(navArgument(Destination.Orders.ARG_ORDER_ID) { 
                    type = NavType.IntType
                    defaultValue = -1
                })
            ) { backStackEntry ->
                val orderId = backStackEntry.arguments?.getInt(Destination.Orders.ARG_ORDER_ID)?.takeIf { it != -1 }
                OrdersRoute(
                    initialTrackingOrderId = orderId,
                    onNavigateToOrderDetails = { orderId ->
                        // Navigate to details if screen existed, otherwise just stay here
                    },
                    onShowSnackbar = showSnackbar,
                    onNavigateBack = { navController.popBackStack() },
                )
            }

            composable(
                route = Destination.ProductDetails(0).route,
                arguments = listOf(navArgument(Destination.ProductDetails.ARG_PRODUCT_ID) { type = NavType.IntType })
            ) { backStackEntry ->
                val productId = backStackEntry.arguments?.getInt(Destination.ProductDetails.ARG_PRODUCT_ID) ?: 0
                ProductDetailsRoute(
                    productId = productId,
                    onNavigateBack = { navController.popBackStack() },
                    onShowSnackbar = showSnackbar,
                )
            }

            composable(Destination.More.route) {
                MoreRoute(
                    onNavigateToProfile = { /* TODO */ },
                    onNavigateToFavorites = { /* TODO */ },
                    onLogoutSuccess = {
                        navController.navigate(Destination.Auth.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    },
                    onNavigateBack = { navController.popBackStack() }
                )
            }
        }
    }
}
