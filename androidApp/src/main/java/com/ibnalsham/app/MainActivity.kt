package com.ibnalsham.app

import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.animation.AnimatedContentTransitionScope.SlideDirection
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.MoreHoriz
import androidx.compose.material.icons.outlined.ReceiptLong
import androidx.compose.material.icons.outlined.RestaurantMenu
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.MoreHoriz
import androidx.compose.material.icons.rounded.ReceiptLong
import androidx.compose.material.icons.rounded.RestaurantMenu
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.core.os.LocaleListCompat
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.ibnalsham.app.data.notification.NotificationRepository
import com.ibnalsham.app.feature.navigation.DeepLink
import com.ibnalsham.app.feature.navigation.Destination
import com.ibnalsham.app.library.data.cache.AppPreferences
import com.ibnalsham.app.library.data.cache.GatedFeature
import com.ibnalsham.app.library.data.cache.SessionManager
import com.ibnalsham.app.library.data.cache.UserPreferencesRepository
import com.ibnalsham.app.push.PushTokenSync
import com.ibnalsham.app.ui.design.components.FloatingNavBar
import com.ibnalsham.app.ui.design.components.NavItem
import com.ibnalsham.app.ui.design.gate.AuthGateHost
import com.ibnalsham.app.ui.design.gate.LocalAuthGate
import com.ibnalsham.app.ui.design.designSystem.R as DesignR
import com.ibnalsham.app.ui.design.theme.IbnAlshamTheme
import com.ibnalsham.app.ui.design.theme.isDark
import com.ibnalsham.app.ui.design.theme.Motion
import com.ibnalsham.app.ui.feature.auth.AuthRoute
import com.ibnalsham.app.ui.feature.auth.GateAuthForm
import com.ibnalsham.app.ui.feature.cart.CartRoute
import com.ibnalsham.app.ui.feature.checkout.CheckoutRoute
import com.ibnalsham.app.ui.feature.favorites.FavoritesRoute
import com.ibnalsham.app.ui.feature.home.HomeRoute
import com.ibnalsham.app.ui.feature.menu.MenuRoute
import com.ibnalsham.app.ui.feature.more.MoreRoute
import com.ibnalsham.app.ui.feature.notifications.NotificationsRoute
import com.ibnalsham.app.ui.feature.orders.OrdersRoute
import com.ibnalsham.app.ui.feature.productDetails.ProductDetailsRoute
import com.ibnalsham.app.ui.feature.profile.ProfileRoute
import com.ibnalsham.app.ui.feature.search.SearchRoute
import com.ibnalsham.app.ui.feature.settings.SettingsRoute
import com.ibnalsham.app.ui.feature.splash.SplashScreen
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

class MainActivity : AppCompatActivity() {

    private var pendingDeepLink by mutableStateOf<String?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // The floating nav bar and hero images run under the system bars.
        enableEdgeToEdge()

        // Force Arabic by default on first run if nothing is set
        if (AppCompatDelegate.getApplicationLocales().isEmpty) {
            val localeList = LocaleListCompat.forLanguageTags("ar")
            AppCompatDelegate.setApplicationLocales(localeList)
        }

        pendingDeepLink = DeepLink.resolve(intent?.dataString)

        setContent {
            // Theme is read here, at the composition root, so choosing a mode in
            // Settings re-themes the whole app immediately.
            val appPreferences: AppPreferences = koinInject()
            val themeMode by appPreferences.themeMode.collectAsState()

            IbnAlshamTheme(darkTheme = themeMode.isDark()) {
                AppRoot(
                    deepLinkRoute = pendingDeepLink,
                    onDeepLinkHandled = { pendingDeepLink = null },
                )
            }
        }
    }

    /** Links opened while the app is already running arrive here. */
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        DeepLink.resolve(intent.dataString)?.let { pendingDeepLink = it }
    }
}

/**
 * Wraps navigation in the sign-in gate, so any screen can raise the sheet
 * through [LocalAuthGate] without threading callbacks through every route.
 */
@Composable
private fun AppRoot(
    deepLinkRoute: String?,
    onDeepLinkHandled: () -> Unit,
) {
    val sessionManager: SessionManager = koinInject()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    val showSnackbar: (String) -> Unit = { message ->
        scope.launch { snackbarHostState.showSnackbar(message) }
    }

    AuthGateHost(
        sessionManager = sessionManager,
        authForm = { onSignedIn ->
            GateAuthForm(onSignedIn = onSignedIn, onShowSnackbar = showSnackbar)
        },
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            AppNavigation(
                deepLinkRoute = deepLinkRoute,
                onDeepLinkHandled = onDeepLinkHandled,
                showSnackbar = showSnackbar,
            )
            SnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier.align(Alignment.BottomCenter),
            )
        }
    }
}

@Composable
fun AppNavigation(
    deepLinkRoute: String? = null,
    onDeepLinkHandled: () -> Unit = {},
    showSnackbar: (String) -> Unit = {},
) {
    val navController = rememberNavController()
    val context = LocalContext.current
    val userPreferences: UserPreferencesRepository = koinInject()
    val notificationRepository: NotificationRepository = koinInject()
    val gate = LocalAuthGate.current

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination?.route

    LaunchedEffect(deepLinkRoute) {
        val route = deepLinkRoute ?: return@LaunchedEffect
        navController.navigate(route) { launchSingleTop = true }
        onDeepLinkHandled()
    }

    val navItems = listOf(
        NavItem(
            route = Destination.Home.route,
            icon = Icons.Outlined.Home,
            selectedIcon = Icons.Rounded.Home,
            label = stringResource(DesignR.string.nav_home),
        ),
        NavItem(
            route = Destination.Menu.route,
            icon = Icons.Outlined.RestaurantMenu,
            selectedIcon = Icons.Rounded.RestaurantMenu,
            label = stringResource(DesignR.string.nav_menu),
        ),
        NavItem(
            route = ORDERS_ROUTE,
            icon = Icons.Outlined.ReceiptLong,
            selectedIcon = Icons.Rounded.ReceiptLong,
            label = stringResource(DesignR.string.nav_orders),
            requiresAuth = true,
        ),
        NavItem(
            route = Destination.More.route,
            icon = Icons.Outlined.MoreHoriz,
            selectedIcon = Icons.Rounded.MoreHoriz,
            label = stringResource(DesignR.string.nav_more),
        ),
    )

    val orderIdArg = navBackStackEntry?.arguments?.getInt(Destination.Orders.ARG_ORDER_ID) ?: -1
    val showNavBar = when (currentDestination) {
        Destination.Home.route, Destination.Menu.route, Destination.More.route -> true
        // Tracking a specific order is a detail view, not the tab.
        Destination.Orders().route -> orderIdArg == -1
        else -> false
    }

    Box(modifier = Modifier.fillMaxSize()) {
        NavHost(
            navController = navController,
            startDestination = Destination.Splash.route,
            modifier = Modifier.fillMaxSize(),
            // slideIntoContainer is layout-direction aware, which matters here:
            // the app defaults to Arabic, where "forward" is leftward.
            enterTransition = {
                slideIntoContainer(SlideDirection.Start, Motion.offsetSpring()) + fadeIn(Motion.fadeIn())
            },
            exitTransition = {
                slideOutOfContainer(SlideDirection.Start, Motion.offsetSpring()) + fadeOut(Motion.fadeOut())
            },
            popEnterTransition = {
                slideIntoContainer(SlideDirection.End, Motion.offsetSpring()) + fadeIn(Motion.fadeIn())
            },
            popExitTransition = {
                slideOutOfContainer(SlideDirection.End, Motion.offsetSpring()) + fadeOut(Motion.fadeOut())
            },
        ) {
            composable(Destination.Splash.route) {
                SplashScreen(
                    onSplashFinished = {
                        navController.navigate(Destination.Home.route) {
                            popUpTo(Destination.Splash.route) { inclusive = true }
                        }
                    },
                )
            }

            composable(Destination.Auth.route) {
                AuthRoute(
                    onNavigateToHome = {
                        // The device now has an account to attach its push token to.
                        PushTokenSync.syncIfSignedIn(context, userPreferences, notificationRepository)
                        navController.popBackStack()
                    },
                    onShowSnackbar = showSnackbar,
                    onDismiss = { navController.popBackStack() },
                )
            }

            composable(Destination.Home.route) {
                HomeRoute(
                    onNavigateToCategory = { navController.navigate(Destination.Menu.route) },
                    onNavigateToProduct = { productId -> navController.navigate("product/$productId") },
                    onNavigateToSearch = { navController.navigate(Destination.Search.route) },
                    onNavigateToCart = { navController.navigate(Destination.Cart.route) },
                    onNavigateToOrder = { orderId -> navController.navigate("orders?orderId=$orderId") },
                    onNavigateToNotifications = {
                        gate.require(GatedFeature.Notifications) {
                            navController.navigate(Destination.Notifications.route)
                        }
                    },
                )
            }

            composable(Destination.Menu.route) {
                MenuRoute(
                    onNavigateToProduct = { productId -> navController.navigate("product/$productId") },
                    onNavigateToCart = { navController.navigate(Destination.Cart.route) },
                    onShowSnackbar = showSnackbar,
                    onNavigateBack = { navController.popBackStack() },
                )
            }

            composable(Destination.Search.route) {
                SearchRoute(
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToProduct = { productId -> navController.navigate("product/$productId") },
                )
            }

            composable(Destination.Cart.route) {
                CartRoute(
                    // Checkout is where a guest actually has to have an account,
                    // so the gate goes here rather than on arrival at checkout.
                    onNavigateToCheckout = {
                        gate.require(GatedFeature.Checkout) {
                            navController.navigate(Destination.Checkout.route)
                        }
                    },
                    onNavigateToMenu = { navController.popBackStack() },
                    onNavigateBack = { navController.popBackStack() },
                    onShowSnackbar = showSnackbar,
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
                    onShowSnackbar = showSnackbar,
                )
            }

            composable(
                route = Destination.Orders().route,
                arguments = listOf(
                    navArgument(Destination.Orders.ARG_ORDER_ID) {
                        type = NavType.IntType
                        defaultValue = -1
                    },
                ),
            ) { backStackEntry ->
                val orderId = backStackEntry.arguments?.getInt(Destination.Orders.ARG_ORDER_ID)?.takeIf { it != -1 }
                OrdersRoute(
                    initialTrackingOrderId = orderId,
                    onNavigateToOrderDetails = { /* tracking view is shown in place */ },
                    onShowSnackbar = showSnackbar,
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToMenu = { navController.navigate(Destination.Menu.route) },
                )
            }

            composable(
                route = Destination.ProductDetails(0).route,
                arguments = listOf(
                    navArgument(Destination.ProductDetails.ARG_PRODUCT_ID) { type = NavType.IntType },
                ),
            ) { backStackEntry ->
                val productId = backStackEntry.arguments?.getInt(Destination.ProductDetails.ARG_PRODUCT_ID) ?: 0
                ProductDetailsRoute(
                    productId = productId,
                    onNavigateBack = { navController.popBackStack() },
                    onShowSnackbar = showSnackbar,
                )
            }

            composable(Destination.Favorites.route) {
                FavoritesRoute(
                    onNavigateToProduct = { productId -> navController.navigate("product/$productId") },
                    onNavigateToMenu = { navController.navigate(Destination.Menu.route) },
                    onNavigateBack = { navController.popBackStack() },
                    onShowSnackbar = showSnackbar,
                )
            }

            composable(Destination.Notifications.route) {
                NotificationsRoute(
                    onNavigateToOrder = { orderId -> navController.navigate("orders?orderId=$orderId") },
                    onNavigateToProduct = { productId -> navController.navigate("product/$productId") },
                    onNavigateBack = { navController.popBackStack() },
                    onShowSnackbar = showSnackbar,
                )
            }

            composable(Destination.Profile.route) {
                ProfileRoute(
                    onNavigateBack = { navController.popBackStack() },
                    onSignedOut = {
                        navController.navigate(Destination.Home.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    },
                    onShowSnackbar = showSnackbar,
                )
            }

            composable(Destination.Settings.route) {
                SettingsRoute(onNavigateBack = { navController.popBackStack() })
            }

            composable(Destination.More.route) {
                MoreRoute(
                    onNavigateToSettings = { navController.navigate(Destination.Settings.route) },
                    onNavigateToProfile = { navController.navigate(Destination.Profile.route) },
                    onNavigateToFavorites = { navController.navigate(Destination.Favorites.route) },
                    onNavigateToOrders = { navController.navigate(ORDERS_ROUTE) },
                    onLogoutSuccess = {
                        navController.navigate(Destination.Home.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    },
                    onNavigateBack = { navController.popBackStack() },
                )
            }
        }

        AnimatedVisibility(
            visible = showNavBar,
            enter = slideInVertically(Motion.offsetSpring()) { it } + fadeIn(Motion.fadeIn()),
            exit = slideOutVertically(Motion.offsetSpring()) { it } + fadeOut(Motion.fadeOut()),
            modifier = Modifier.align(Alignment.BottomCenter),
        ) {
            FloatingNavBar(
                items = navItems,
                selectedRoute = navItems.firstOrNull { currentDestination?.startsWith(it.route) == true }?.route,
                onSelect = { item ->
                    val navigate = {
                        if (currentDestination?.startsWith(item.route) != true) {
                            navController.navigate(item.route) {
                                if (item.route == Destination.Home.route) {
                                    popUpTo(Destination.Home.route)
                                } else {
                                    popUpTo(Destination.Home.route) { saveState = true }
                                    restoreState = true
                                }
                                launchSingleTop = true
                            }
                        }
                    }
                    // The orders tab used to open for guests and show a bare
                    // error; now it asks for sign-in and then opens.
                    if (item.requiresAuth) {
                        gate.require(GatedFeature.Orders) { navigate() }
                    } else {
                        navigate()
                    }
                },
            )
        }
    }
}

/** The orders tab route without an order id argument. */
private const val ORDERS_ROUTE = "orders"
