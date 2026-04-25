package com.ibnalsham.app.ui.feature.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.ibnalsham.app.feature.domain.BannerDomain
import com.ibnalsham.app.feature.domain.CategoryDomain
import com.ibnalsham.app.feature.domain.ProductDomain
import com.ibnalsham.app.feature.home.presentation.viewModel.HomeViewModel
import com.ibnalsham.app.feature.home.presentation.viewModel.HomeViewModelContract
import com.ibnalsham.app.library.data.cache.CartRepository
import com.ibnalsham.app.ui.design.components.CartFloatingBar
import com.ibnalsham.app.ui.feature.homeimpl.R
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
) {
    val state by viewModel.uiState.collectAsState()
    val cartItems by cartRepository.cartItems.collectAsState()
    val itemCount = cartItems.sumOf { it.quantity }
    val subtotal = cartItems.sumOf { it.product.price * it.quantity }

    LaunchedEffect(Unit) {
        viewModel.onAction(HomeViewModelContract.Action.ViewReady)

        viewModel.sideEffects.collect { effect ->
            when (effect) {
                is HomeViewModelContract.SideEffect.NavigateToProduct -> onNavigateToProduct(effect.productId)
                is HomeViewModelContract.SideEffect.NavigateToCategory -> onNavigateToCategory(effect.categoryId)
            }
        }
    }

    HomeScreen(
        state = state,
        onAction = viewModel::onAction,
        onNavigateToSearch = onNavigateToSearch,
        cartItemCount = itemCount,
        cartSubtotal = subtotal,
        onNavigateToCart = onNavigateToCart,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    state: HomeViewModelContract.MyUiState,
    onAction: (HomeViewModelContract.Action) -> Unit,
    onNavigateToSearch: () -> Unit,
    cartItemCount: Int = 0,
    cartSubtotal: Double = 0.0,
    onNavigateToCart: () -> Unit = {},
) {
    Scaffold(
        bottomBar = {
            CartFloatingBar(
                itemCount = cartItemCount,
                subtotal = cartSubtotal,
                onClick = onNavigateToCart,
            )
        },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        stringResource(R.string.home_title),
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFFE53935), // Premium Red
                        style = MaterialTheme.typography.titleLarge
                    )
                },
                actions = {
                    IconButton(onClick = { /* TODO Notifications */ }) {
                        Icon(
                            imageVector = Icons.Outlined.Notifications,
                            contentDescription = stringResource(R.string.notifications),
                            tint = Color.DarkGray
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White
                ),
                modifier = Modifier.shadow(elevation = 2.dp)
            )
        },
        containerColor = Color(0xFFF7F8FA) // Very subtle cool grey background
    ) { paddingValues ->
        if (state.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Color(0xFFE53935))
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
            ) {
                
                // Banners Slider
                if (state.banners.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(16.dp))
                    HomeCarousel(banners = state.banners)
                }

                // Categories
                if (state.categories.isNotEmpty()) {
                    var categoriesAsTabs by remember { mutableStateOf(false) }
                    Spacer(modifier = Modifier.height(24.dp))
                    SectionHeader(
                        title = stringResource(R.string.categories),
                        trailing = {
                            IconButton(onClick = { categoriesAsTabs = !categoriesAsTabs }) {
                                Icon(
                                    imageVector = if (categoriesAsTabs) Icons.Default.Menu else Icons.Default.List,
                                    contentDescription = if (categoriesAsTabs) stringResource(R.string.show_as_grid) else stringResource(R.string.show_as_tabs),
                                    tint = Color(0xFFE53935)
                                )
                            }
                        }
                    )
                    val onCategoryClick: (Int) -> Unit = { onAction(HomeViewModelContract.Action.CategoryClicked(it)) }
                    if (categoriesAsTabs) {
                        val selectedTabId = state.selectedTabCategoryId ?: state.categories.first().id
                        LaunchedEffect(selectedTabId) {
                            onAction(HomeViewModelContract.Action.CategoryTabSelected(selectedTabId))
                        }
                        CategoriesTabs(
                            categories = state.categories,
                            selectedId = selectedTabId,
                            onCategoryClick = { onAction(HomeViewModelContract.Action.CategoryTabSelected(it)) }
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        when {
                            state.isTabLoading -> {
                                Box(
                                    modifier = Modifier.fillMaxWidth().padding(32.dp),
                                    contentAlignment = Alignment.Center
                                ) { CircularProgressIndicator(color = Color(0xFFE53935)) }
                            }
                            state.tabProducts.isNotEmpty() -> {
                                ProductsGrid(
                                    products = state.tabProducts,
                                    onProductClick = { onAction(HomeViewModelContract.Action.ProductClicked(it)) }
                                )
                            }
                            else -> {
                                Box(
                                    modifier = Modifier.fillMaxWidth().padding(32.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = stringResource(R.string.no_products_category),
                                        color = Color.Gray,
                                        fontSize = 14.sp
                                    )
                                }
                            }
                        }
                    } else {
                        CategoriesRow(categories = state.categories, onCategoryClick = onCategoryClick)
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

@Composable
fun SearchBarDummy(onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .height(50.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(Color.White)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
            .padding(horizontal = 16.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Search, contentDescription = stringResource(R.string.search_placeholder), tint = Color.Gray)
            Spacer(modifier = Modifier.width(8.dp))
            Text(stringResource(R.string.search_placeholder), color = Color.Gray, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
fun HomeCarousel(banners: List<BannerDomain>) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(banners) { banner ->
            Box(
                modifier = Modifier
                    .fillParentMaxWidth()
                    .height(180.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.LightGray)
            ) {
                AsyncImage(
                    model = banner.imageUrl,
                    contentDescription = banner.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
                // Gradient overlay
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.5f)),
                                startY = 100f
                            )
                        )
                )
            }
        }
    }
}

@Composable
fun HomeQuickMenu() {
    val items = listOf("Today's Deal", "Flash Deal", "Brands", "Top Sellers")
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        items.forEachIndexed { index, title ->
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(if (index == 0) Color(0xFFE53935) else Color.White)
                        .shadow(elevation = if (index != 0) 1.dp else 4.dp, shape = CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    // Icon logic depends on text for now
                    val color = if (index == 0) Color.White else Color(0xFFE53935)
                    Icon(Icons.Default.ShoppingCart, contentDescription = null, tint = color, modifier = Modifier.size(24.dp))
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(title, fontSize = 11.sp, fontWeight = FontWeight.Medium, color = Color.DarkGray)
            }
        }
    }
}

@Composable
fun SectionHeader(
    title: String,
    actionText: String? = null,
    trailing: (@Composable () -> Unit)? = null,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.titleMedium,
            color = Color(0xFF1E1E1E)
        )
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (actionText != null) {
                Text(
                    text = actionText,
                    fontWeight = FontWeight.Medium,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFFE53935),
                    modifier = Modifier.clickable { /* TODO */ }
                )
            }
            if (trailing != null) {
                trailing()
            }
        }
    }
}

@Composable
fun CategoriesTabs(
    categories: List<CategoryDomain>,
    selectedId: Int,
    onCategoryClick: (Int) -> Unit,
) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(categories) { cat ->
            val selected = cat.id == selectedId
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(if (selected) Color(0xFFE53935) else Color.White)
                    .clickable { onCategoryClick(cat.id) }
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = cat.name,
                    fontWeight = FontWeight.Medium,
                    fontSize = 13.sp,
                    color = if (selected) Color.White else Color(0xFF1E1E1E)
                )
            }
        }
    }
}

@Composable
fun CategoriesRow(
    categories: List<CategoryDomain>,
    onCategoryClick: (Int) -> Unit,
    columns: Int = 3,
) {
    val chunked = categories.chunked(columns)
    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
        chunked.forEach { rowItems ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                rowItems.forEach { cat ->
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .weight(1f)
                            .clickable { onCategoryClick(cat.id) }
                    ) {
                        Box(
                            modifier = Modifier
                                .size(100.dp)
                                .clip(CircleShape)
                                .background(if (cat.image.isNullOrBlank()) Color(0xFFFFEBEE) else Color.White)
                                .shadow(1.dp, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            if (!cat.image.isNullOrBlank()) {
                                AsyncImage(
                                    model = cat.image,
                                    contentDescription = cat.name,
                                    modifier = Modifier.fillMaxSize().clip(CircleShape),
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                Text(
                                    text = cat.name.take(1).uppercase(),
                                    color = Color(0xFFE53935),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 36.sp
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = cat.name,
                            fontWeight = FontWeight.Medium,
                            fontSize = 20.sp,
                            color = Color.DarkGray,
                            textAlign = TextAlign.Center,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
                repeat(columns - rowItems.size) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun ProductsHorizontalList(products: List<ProductDomain>, onProductClick: (Int) -> Unit) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(products) { product ->
            ProductCard(product, modifier = Modifier.width(150.dp), onClick = { onProductClick(product.id) })
        }
    }
}

@Composable
fun ProductsGrid(
    products: List<ProductDomain>,
    onProductClick: (Int) -> Unit,
    columns: Int = 2,
) {
    val chunked = products.chunked(columns)
    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
        chunked.forEach { rowItems ->
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                rowItems.forEach { product ->
                    ProductCard(product, modifier = Modifier.weight(1f), onClick = { onProductClick(product.id) })
                }
                repeat(columns - rowItems.size) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}

@Composable
fun ProductCard(product: ProductDomain, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Card(
        modifier = modifier.clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .background(if (product.image.isNullOrBlank()) Color(0xFFFFEBEE) else Color.White),
                contentAlignment = Alignment.Center
            ) {
                if (!product.image.isNullOrBlank()) {
                    AsyncImage(
                        model = product.image,
                        contentDescription = product.name,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Text(
                        text = product.name.take(1).uppercase(),
                        color = Color(0xFFE53935),
                        fontWeight = FontWeight.Bold,
                        fontSize = 40.sp
                    )
                }
            }
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = product.name,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    fontWeight = FontWeight.Medium,
                    fontSize = 14.sp,
                    color = Color(0xFF1E1E1E),
                    modifier = Modifier.height(40.dp) // Fixed height for 2 lines
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(com.ibnalsham.app.ui.design.designSystem.R.string.currency_symbol) + " ${product.price}",
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFE53935),
                        fontSize = 14.sp
                    )
                    // Minor action e.g. add to cart
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFE53935).copy(alpha = 0.1f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.ShoppingCart, contentDescription = "Add", tint = Color(0xFFE53935), modifier = Modifier.size(16.dp))
                    }
                }
            }
        }
    }
}
