package com.ibnalsham.app.ui.feature.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.DeliveryDining
import androidx.compose.material.icons.rounded.GridView
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.ShoppingBag
import androidx.compose.material.icons.rounded.ViewAgenda
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.ibnalsham.app.feature.domain.ActiveOrderDomain
import com.ibnalsham.app.feature.domain.BannerDomain
import com.ibnalsham.app.feature.domain.CategoryDomain
import com.ibnalsham.app.feature.domain.ProductDomain
import com.ibnalsham.app.feature.home.presentation.viewModel.HomeViewModel
import com.ibnalsham.app.feature.home.presentation.viewModel.HomeViewModelContract
import com.ibnalsham.app.library.data.cache.CartRepository
import com.ibnalsham.app.ui.design.components.AnimatedPrice
import com.ibnalsham.app.ui.design.components.CircleIconButton
import com.ibnalsham.app.ui.design.components.FoodImage
import com.ibnalsham.app.ui.design.components.ProductRow
import com.ibnalsham.app.ui.design.components.ProductTile
import com.ibnalsham.app.ui.design.components.ProductTileSkeleton
import com.ibnalsham.app.ui.design.components.ProductTileWidth
import com.ibnalsham.app.ui.design.components.PulsingDots
import com.ibnalsham.app.ui.design.components.SectionHeader
import com.ibnalsham.app.ui.design.components.SkeletonBlock
import com.ibnalsham.app.ui.design.components.Tag
import com.ibnalsham.app.ui.design.motion.bouncyClickable
import com.ibnalsham.app.ui.design.motion.enterFromBottom
import com.ibnalsham.app.ui.design.motion.rememberPulse
import com.ibnalsham.app.ui.design.theme.AppTextStyles
import com.ibnalsham.app.ui.design.theme.AppTheme
import com.ibnalsham.app.ui.design.theme.Motion
import com.ibnalsham.app.ui.design.theme.Spacing
import com.ibnalsham.app.ui.design.designSystem.R as DesignR
import com.ibnalsham.app.ui.feature.homeimpl.R
import kotlinx.coroutines.delay
import org.koin.androidx.compose.koinViewModel
import org.koin.compose.koinInject

@Composable
fun HomeRoute(
    viewModel: HomeViewModel = koinViewModel(),
    cartRepository: CartRepository = koinInject(),
    onNavigateToProduct: (Int) -> Unit,
    onNavigateToCategory: (Int) -> Unit,
    onNavigateToSearch: () -> Unit,
    onNavigateToCart: () -> Unit,
    onNavigateToOrder: (Int) -> Unit = {},
    onNavigateToNotifications: () -> Unit = {},
) {
    val state by viewModel.uiState.collectAsState()
    val cartItems by cartRepository.cartItems.collectAsState()
    val itemCount = cartItems.sumOf { it.quantity }

    LaunchedEffect(Unit) {
        viewModel.onAction(HomeViewModelContract.Action.ViewReady)
    }

    LaunchedEffect(Unit) {
        viewModel.sideEffects.collect { effect ->
            when (effect) {
                is HomeViewModelContract.SideEffect.NavigateToProduct -> onNavigateToProduct(effect.productId)
                is HomeViewModelContract.SideEffect.NavigateToCategory -> onNavigateToCategory(effect.categoryId)
                is HomeViewModelContract.SideEffect.NavigateToOrder -> onNavigateToOrder(effect.orderId)
                HomeViewModelContract.SideEffect.NavigateToSearch -> onNavigateToSearch()
            }
        }
    }

    HomeScreen(
        state = state,
        onAction = viewModel::onAction,
        onNavigateToSearch = onNavigateToSearch,
        cartItemCount = itemCount,
        onNavigateToCart = onNavigateToCart,
        onNavigateToNotifications = onNavigateToNotifications,
    )
}

/**
 * Home.
 *
 * A single [LazyColumn] rather than the old `verticalScroll(Column)` — with
 * nested carousels and product lists the eager version composed every off-screen
 * card on first frame. The greeting header scrolls away and the compact bar
 * fades in over it, driven by [LazyListState].
 */
@Composable
fun HomeScreen(
    state: HomeViewModelContract.MyUiState,
    onAction: (HomeViewModelContract.Action) -> Unit,
    onNavigateToSearch: () -> Unit,
    cartItemCount: Int = 0,
    onNavigateToCart: () -> Unit = {},
    onNavigateToNotifications: () -> Unit = {},
) {
    val colors = AppTheme.colors
    val listState = rememberLazyListState()

    // LazyColumn's content lambda is LazyListScope, not a composable scope, so
    // every string the sections need has to be resolved out here.
    val labels = HomeLabels(
        browseEyebrow = stringResource(R.string.home_browse_eyebrow),
        categories = stringResource(R.string.categories),
        gridToggle = stringResource(R.string.show_as_grid),
        tabsToggle = stringResource(R.string.show_as_tabs),
        emptyCategory = stringResource(R.string.no_products_category),
        offersTitle = stringResource(DesignR.string.home_special_offers),
        offersEyebrow = stringResource(R.string.home_offers_eyebrow),
        bestSellersTitle = stringResource(DesignR.string.home_best_sellers),
        bestSellersEyebrow = stringResource(R.string.home_popular_eyebrow),
        featuredTitle = stringResource(DesignR.string.home_featured),
        featuredEyebrow = stringResource(R.string.home_chef_eyebrow),
    )

    // Collapse once the greeting block has left; index 0 is the header itself.
    val collapsed by remember {
        derivedStateOf {
            listState.firstVisibleItemIndex > 0 || listState.firstVisibleItemScrollOffset > 120
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(colors.background)) {
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = Spacing.navBarClearance),
        ) {
            item(key = "header") {
                HomeHeader(
                    isSignedIn = state.isSignedIn,
                    name = state.greetingName,
                    cartItemCount = cartItemCount,
                    onSearchClick = onNavigateToSearch,
                    onCartClick = onNavigateToCart,
                    onNotificationsClick = onNavigateToNotifications,
                )
            }

            if (state.isLoading) {
                item(key = "loading") { HomeSkeleton() }
                return@LazyColumn
            }

            state.activeOrder?.let { order ->
                item(key = "activeOrder") {
                    ActiveOrderCard(
                        order = order,
                        onClick = { onAction(HomeViewModelContract.Action.ActiveOrderClicked) },
                        modifier = Modifier.padding(horizontal = Spacing.gutter, vertical = Spacing.sm),
                    )
                }
            }

            if (state.banners.isNotEmpty()) {
                item(key = "banners") {
                    BannerCarousel(
                        banners = state.banners,
                        onBannerClick = { onAction(HomeViewModelContract.Action.BannerClicked(it)) },
                    )
                }
            }

            if (state.categories.isNotEmpty()) {
                item(key = "categoriesHeader") {
                    SectionHeader(
                        title = labels.categories,
                        eyebrow = labels.browseEyebrow,
                        trailing = {
                            CircleIconButton(
                                icon = if (state.categoriesAsTabs) Icons.Rounded.GridView else Icons.Rounded.ViewAgenda,
                                contentDescription =
                                    if (state.categoriesAsTabs) labels.gridToggle else labels.tabsToggle,
                                onClick = {
                                    onAction(
                                        HomeViewModelContract.Action.SetCategoriesAsTabs(!state.categoriesAsTabs),
                                    )
                                },
                                size = 38.dp,
                            )
                        },
                    )
                }

                if (state.categoriesAsTabs) {
                    item(key = "categoryTabs") {
                        val selectedTabId = state.selectedTabCategoryId ?: state.categories.first().id
                        LaunchedEffect(selectedTabId) {
                            onAction(HomeViewModelContract.Action.CategoryTabSelected(selectedTabId))
                        }
                        ChipRow(
                            items = state.categories,
                            selectedId = selectedTabId,
                            onClick = { onAction(HomeViewModelContract.Action.CategoryTabSelected(it)) },
                        )
                    }

                    if (state.hasSubCategories) {
                        item(key = "subCategories") {
                            ChipRow(
                                items = state.subCategories,
                                selectedId = state.selectedSubCategoryId,
                                onClick = { onAction(HomeViewModelContract.Action.SubCategoryClicked(it)) },
                                subtle = true,
                            )
                        }
                    }

                    when {
                        state.isTabLoading -> item(key = "tabLoading") {
                            Box(
                                modifier = Modifier.fillMaxWidth().padding(Spacing.xxxl),
                                contentAlignment = Alignment.Center,
                            ) { PulsingDots() }
                        }

                        state.tabProducts.isEmpty() -> item(key = "tabEmpty") {
                            Text(
                                text = labels.emptyCategory,
                                style = MaterialTheme.typography.bodyMedium,
                                color = colors.textTertiary,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth().padding(Spacing.xxxl),
                            )
                        }

                        else -> itemsIndexed(
                            items = state.tabProducts,
                            key = { _, product -> "tab-${product.id}" },
                        ) { index, product ->
                            ProductRow(
                                product = product,
                                cartCount = state.cartItemCounts[product.id] ?: 0,
                                onClick = { onAction(HomeViewModelContract.Action.ProductClicked(product.id)) },
                                onAddToCart = { onAction(HomeViewModelContract.Action.AddToCart(product.id)) },
                                onIncrease = { onAction(HomeViewModelContract.Action.IncreaseQuantity(product.id)) },
                                onDecrease = { onAction(HomeViewModelContract.Action.DecreaseQuantity(product.id)) },
                                modifier = Modifier
                                    .padding(horizontal = Spacing.gutter, vertical = Spacing.xs)
                                    .enterFromBottom(index = index),
                            )
                        }
                    }
                } else {
                    item(key = "categoryGrid") {
                        CategoryGrid(
                            categories = state.categories,
                            onCategoryClick = { onAction(HomeViewModelContract.Action.CategoryClicked(it)) },
                        )
                    }
                }
            }

            productCarousel(
                key = "offers",
                title = labels.offersTitle,
                eyebrow = labels.offersEyebrow,
                products = state.specialOffers,
                state = state,
                onAction = onAction,
            )

            productCarousel(
                key = "bestSellers",
                title = labels.bestSellersTitle,
                eyebrow = labels.bestSellersEyebrow,
                products = state.mostOrdered,
                state = state,
                onAction = onAction,
            )

            productCarousel(
                key = "featured",
                title = labels.featuredTitle,
                eyebrow = labels.featuredEyebrow,
                products = state.featuredProducts,
                state = state,
                onAction = onAction,
            )
        }

        // Compact bar that takes over once the greeting has scrolled off.
        AnimatedVisibility(
            visible = collapsed,
            enter = slideInVertically(Motion.offsetSpring()) { -it } + fadeIn(Motion.fadeIn()),
            exit = slideOutVertically(Motion.offsetSpring()) { -it } + fadeOut(Motion.fadeOut()),
        ) {
            CompactHomeBar(
                cartItemCount = cartItemCount,
                onSearchClick = onNavigateToSearch,
                onCartClick = onNavigateToCart,
                onNotificationsClick = onNavigateToNotifications,
            )
        }
    }
}

/** Adds a titled horizontal product carousel, or nothing when the list is empty. */
private fun androidx.compose.foundation.lazy.LazyListScope.productCarousel(
    key: String,
    title: String,
    eyebrow: String,
    products: List<ProductDomain>,
    state: HomeViewModelContract.MyUiState,
    onAction: (HomeViewModelContract.Action) -> Unit,
) {
    if (products.isEmpty()) return

    item(key = "${key}Header") {
        Spacer(Modifier.height(Spacing.lg))
        SectionHeader(title = title, eyebrow = eyebrow)
    }

    item(key = "${key}Row") {
        LazyRow(
            contentPadding = PaddingValues(horizontal = Spacing.gutter, vertical = Spacing.sm),
            horizontalArrangement = Arrangement.spacedBy(Spacing.md),
        ) {
            itemsIndexed(products, key = { _, p -> "$key-${p.id}" }) { index, product ->
                ProductTile(
                    product = product,
                    cartCount = state.cartItemCounts[product.id] ?: 0,
                    onClick = { onAction(HomeViewModelContract.Action.ProductClicked(product.id)) },
                    onAddToCart = { onAction(HomeViewModelContract.Action.AddToCart(product.id)) },
                    onIncrease = { onAction(HomeViewModelContract.Action.IncreaseQuantity(product.id)) },
                    onDecrease = { onAction(HomeViewModelContract.Action.DecreaseQuantity(product.id)) },
                    modifier = Modifier
                        .width(ProductTileWidth)
                        .enterFromBottom(index = index),
                )
            }
        }
    }
}

/**
 * Greeting block.
 *
 * Names the user when signed in and stays warm but anonymous when not — the
 * guest state is a normal state here, not a nag to sign in.
 */
@Composable
private fun HomeHeader(
    isSignedIn: Boolean,
    name: String,
    cartItemCount: Int,
    onSearchClick: () -> Unit,
    onCartClick: () -> Unit,
    onNotificationsClick: () -> Unit,
) {
    val colors = AppTheme.colors

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(colors.background)
            .statusBarsPadding()
            .padding(horizontal = Spacing.gutter)
            .padding(top = Spacing.md, bottom = Spacing.sm),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = if (isSignedIn && name.isNotBlank()) {
                        stringResource(DesignR.string.greeting_named, name)
                    } else {
                        stringResource(DesignR.string.greeting_guest)
                    },
                    style = MaterialTheme.typography.displaySmall,
                    color = colors.textPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = stringResource(DesignR.string.greeting_subtitle),
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.textSecondary,
                )
            }

            CircleIconButton(
                icon = Icons.Rounded.Notifications,
                contentDescription = stringResource(DesignR.string.notifications_title),
                onClick = onNotificationsClick,
            )
            Spacer(Modifier.width(Spacing.sm))
            CircleIconButton(
                icon = Icons.Rounded.ShoppingBag,
                contentDescription = stringResource(DesignR.string.cart_title),
                onClick = onCartClick,
                badgeCount = cartItemCount,
            )
        }

        Spacer(Modifier.height(Spacing.lg))

        SearchEntry(onClick = onSearchClick)
    }
}

/** Slim bar shown after the greeting scrolls away. */
@Composable
private fun CompactHomeBar(
    cartItemCount: Int,
    onSearchClick: () -> Unit,
    onCartClick: () -> Unit,
    onNotificationsClick: () -> Unit,
) {
    val colors = AppTheme.colors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(colors.surface.copy(alpha = 0.97f))
            .statusBarsPadding()
            .padding(horizontal = Spacing.gutter, vertical = Spacing.sm),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(modifier = Modifier.weight(1f)) {
            SearchEntry(onClick = onSearchClick, compact = true)
        }
        Spacer(Modifier.width(Spacing.sm))
        CircleIconButton(
            icon = Icons.Rounded.Notifications,
            contentDescription = stringResource(DesignR.string.notifications_title),
            onClick = onNotificationsClick,
            size = 40.dp,
        )
        Spacer(Modifier.width(Spacing.xs))
        CircleIconButton(
            icon = Icons.Rounded.ShoppingBag,
            contentDescription = stringResource(DesignR.string.cart_title),
            onClick = onCartClick,
            size = 40.dp,
            badgeCount = cartItemCount,
        )
    }
}

/** Tappable stand-in that opens the real search screen. */
@Composable
private fun SearchEntry(onClick: () -> Unit, compact: Boolean = false) {
    val colors = AppTheme.colors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(if (compact) 44.dp else 54.dp)
            .clip(CircleShape)
            .background(colors.surfaceSunken)
            .bouncyClickable(pressedScale = 0.985f, onClick = onClick)
            .padding(horizontal = Spacing.lg),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = Icons.Rounded.Search,
            contentDescription = null,
            tint = colors.brand,
            modifier = Modifier.size(20.dp),
        )
        Spacer(Modifier.width(Spacing.md))
        Text(
            text = stringResource(R.string.search_placeholder),
            style = MaterialTheme.typography.bodyMedium,
            color = colors.textTertiary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

/**
 * Auto-advancing banner pager with a page indicator.
 *
 * Auto-advance pauses while the user is dragging so it never fights them.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun BannerCarousel(
    banners: List<BannerDomain>,
    onBannerClick: (Int) -> Unit,
) {
    val colors = AppTheme.colors
    val pagerState = rememberPagerState(pageCount = { banners.size })

    LaunchedEffect(pagerState, banners.size) {
        if (banners.size <= 1) return@LaunchedEffect
        while (true) {
            delay(BANNER_DWELL_MS)
            if (!pagerState.isScrollInProgress) {
                pagerState.animateScrollToPage((pagerState.currentPage + 1) % banners.size)
            }
        }
    }

    Column {
        HorizontalPager(
            state = pagerState,
            contentPadding = PaddingValues(horizontal = Spacing.gutter),
            pageSpacing = Spacing.md,
            modifier = Modifier.padding(vertical = Spacing.sm),
        ) { page ->
            val banner = banners[page]
            // Neighbouring pages sit slightly back, so the active one reads as
            // the focus of a deck rather than one of several equal cards.
            val scale by animateFloatAsState(
                targetValue = if (page == pagerState.currentPage) 1f else 0.94f,
                animationSpec = Motion.snappy(),
                label = "bannerScale",
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1.85f)
                    .scale(scale)
                    .clip(MaterialTheme.shapes.large)
                    .bouncyClickable { onBannerClick(banner.id) },
            ) {
                val title = banner.title.orEmpty()
                FoodImage(
                    url = banner.imageUrl,
                    contentDescription = title,
                    fallbackText = title,
                    scrim = true,
                    modifier = Modifier.fillMaxSize(),
                )
                if (title.isNotBlank()) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.headlineSmall,
                        color = Color.White,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(Spacing.lg),
                    )
                }
            }
        }

        if (banners.size > 1) {
            Box(modifier = Modifier.fillMaxWidth().padding(top = Spacing.sm)) {
                PageIndicator(
                    pageCount = banners.size,
                    current = pagerState.currentPage,
                    modifier = Modifier.align(Alignment.Center),
                )
            }
        }
    }
}

/** Dots that stretch into a pill on the active page. */
@Composable
private fun PageIndicator(pageCount: Int, current: Int, modifier: Modifier = Modifier) {
    val colors = AppTheme.colors
    Row(modifier = modifier, horizontalArrangement = Arrangement.spacedBy(Spacing.xs)) {
        repeat(pageCount) { index ->
            val active = index == current
            val width by animateDpAsState(
                targetValue = if (active) 22.dp else 7.dp,
                animationSpec = Motion.bouncy(),
                label = "indicatorWidth",
            )
            val color by animateColorAsState(
                targetValue = if (active) colors.brand else colors.outline,
                animationSpec = Motion.snappy(),
                label = "indicatorColor",
            )
            Box(
                modifier = Modifier
                    .size(width = width, height = 7.dp)
                    .clip(CircleShape)
                    .background(color),
            )
        }
    }
}

/** Horizontal selectable chips, used for category tabs and subcategories. */
@Composable
private fun ChipRow(
    items: List<CategoryDomain>,
    selectedId: Int?,
    onClick: (Int) -> Unit,
    subtle: Boolean = false,
) {
    val colors = AppTheme.colors
    LazyRow(
        contentPadding = PaddingValues(horizontal = Spacing.gutter, vertical = Spacing.xs),
        horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
    ) {
        items(items, key = { it.id }) { category ->
            val selected = category.id == selectedId
            val background by animateColorAsState(
                targetValue = when {
                    selected && subtle -> colors.brand.copy(alpha = 0.12f)
                    selected -> colors.brand
                    else -> colors.surfaceSunken
                },
                animationSpec = Motion.snappy(),
                label = "chipBackground",
            )
            val content by animateColorAsState(
                targetValue = when {
                    selected && subtle -> colors.brand
                    selected -> colors.onBrand
                    else -> colors.textSecondary
                },
                animationSpec = Motion.snappy(),
                label = "chipContent",
            )

            Box(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(background)
                    .bouncyClickable(pressedScale = 0.94f) { onClick(category.id) }
                    .padding(horizontal = Spacing.lg, vertical = Spacing.md),
            ) {
                Text(
                    text = category.name,
                    style = MaterialTheme.typography.labelLarge,
                    color = content,
                    maxLines = 1,
                )
            }
        }
    }
}

/** Category tiles — photo-led, two per row. */
@Composable
private fun CategoryGrid(
    categories: List<CategoryDomain>,
    onCategoryClick: (Int) -> Unit,
    columns: Int = 2,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = Spacing.gutter),
        verticalArrangement = Arrangement.spacedBy(Spacing.md),
    ) {
        categories.chunked(columns).forEachIndexed { rowIndex, rowItems ->
            Row(horizontalArrangement = Arrangement.spacedBy(Spacing.md)) {
                rowItems.forEachIndexed { columnIndex, category ->
                    CategoryTile(
                        category = category,
                        onClick = { onCategoryClick(category.id) },
                        modifier = Modifier
                            .weight(1f)
                            .enterFromBottom(index = rowIndex * columns + columnIndex),
                    )
                }
                repeat(columns - rowItems.size) { Spacer(Modifier.weight(1f)) }
            }
        }
    }
}

@Composable
private fun CategoryTile(
    category: CategoryDomain,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .aspectRatio(1.35f)
            .clip(MaterialTheme.shapes.medium)
            .bouncyClickable(onClick = onClick),
    ) {
        FoodImage(
            url = category.image,
            contentDescription = category.name,
            fallbackText = category.name,
            scrim = true,
            modifier = Modifier.fillMaxSize(),
        )
        Text(
            text = category.name,
            style = MaterialTheme.typography.titleMedium,
            color = Color.White,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(Spacing.md),
        )
    }
}

/**
 * Live order card.
 *
 * The status dot pulses while the order is in flight — it is the one element on
 * home that should look like it is updating in real time.
 */
@Composable
fun ActiveOrderCard(
    order: ActiveOrderDomain,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = AppTheme.colors
    val pulse = rememberPulse()

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.medium)
            .background(colors.heroGradient)
            .bouncyClickable(pressedScale = 0.98f, onClick = onClick)
            .padding(Spacing.lg),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(46.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.18f)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Rounded.DeliveryDining,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(24.dp),
            )
        }

        Spacer(Modifier.width(Spacing.md))

        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(7.dp)
                        .scale(pulse)
                        .clip(CircleShape)
                        .background(colors.accent),
                )
                Spacer(Modifier.width(Spacing.xs))
                Text(
                    text = stringResource(DesignR.string.active_order_title),
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White.copy(alpha = 0.85f),
                )
            }
            Text(
                text = order.statusLabel.ifBlank { order.orderNumber },
                style = MaterialTheme.typography.titleMedium,
                color = Color.White,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            order.estimatedTime?.let { minutes ->
                Text(
                    text = stringResource(DesignR.string.active_order_eta, minutes),
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.85f),
                )
            }
        }

        Column(horizontalAlignment = Alignment.End) {
            AnimatedPrice(value = order.total, style = AppTextStyles.price, color = Color.White)
            Tag(
                text = stringResource(DesignR.string.active_order_track),
                background = Color.White.copy(alpha = 0.2f),
            )
        }
    }
}

/** Placeholder layout matching home's real structure while data loads. */
@Composable
private fun HomeSkeleton() {
    Column(modifier = Modifier.padding(horizontal = Spacing.gutter)) {
        SkeletonBlock(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1.85f),
            shape = MaterialTheme.shapes.large,
        )
        Spacer(Modifier.height(Spacing.xxl))
        SkeletonBlock(modifier = Modifier.width(140.dp).height(22.dp))
        Spacer(Modifier.height(Spacing.md))
        Row(horizontalArrangement = Arrangement.spacedBy(Spacing.md)) {
            repeat(2) { ProductTileSkeleton(modifier = Modifier.width(ProductTileWidth)) }
        }
        Spacer(Modifier.height(Spacing.xxl))
        SkeletonBlock(modifier = Modifier.width(180.dp).height(22.dp))
        Spacer(Modifier.height(Spacing.md))
        Row(horizontalArrangement = Arrangement.spacedBy(Spacing.md)) {
            repeat(2) { ProductTileSkeleton(modifier = Modifier.width(ProductTileWidth)) }
        }
    }
}

private const val BANNER_DWELL_MS = 4_500L


/**
 * Strings the lazy-list sections need.
 *
 * Resolved once in the composable scope and handed down, because LazyListScope
 * builders cannot call [stringResource] themselves.
 */
private data class HomeLabels(
    val browseEyebrow: String,
    val categories: String,
    val gridToggle: String,
    val tabsToggle: String,
    val emptyCategory: String,
    val offersTitle: String,
    val offersEyebrow: String,
    val bestSellersTitle: String,
    val bestSellersEyebrow: String,
    val featuredTitle: String,
    val featuredEyebrow: String,
)
