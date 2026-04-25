package com.ibnalsham.app.ui.feature.menu

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.outlined.GridView
import androidx.compose.material.icons.outlined.ViewList
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.ibnalsham.app.feature.domain.CategoryDomain
import com.ibnalsham.app.feature.domain.ProductDomain
import com.ibnalsham.app.feature.menu.presentation.viewModel.MenuViewModel
import com.ibnalsham.app.feature.menu.presentation.viewModel.MenuViewModelContract
import com.ibnalsham.app.feature.menu.presentation.viewModel.ViewType
import com.ibnalsham.app.ui.design.components.CartFloatingBar
import com.ibnalsham.app.ui.feature.menuimpl.R
import org.koin.androidx.compose.koinViewModel

private val BrandRed = Color(0xFFC62828)
private val BrandRedLight = Color(0xFFEF5350)
private val BackgroundColor = Color(0xFFFAFAFA)
private val CardColor = Color.White

@Composable
fun MenuRoute(
    viewModel: MenuViewModel = koinViewModel(),
    onNavigateToProduct: (Int) -> Unit,
    onNavigateToCart: () -> Unit,
    onShowSnackbar: (String) -> Unit,
    onNavigateBack: () -> Unit = {},
) {
    val state by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.onAction(MenuViewModelContract.Action.ViewReady)

        viewModel.sideEffects.collect { effect ->
            when (effect) {
                is MenuViewModelContract.SideEffect.NavigateToProduct -> onNavigateToProduct(effect.productId)
                is MenuViewModelContract.SideEffect.ShowCartSnackbar -> onShowSnackbar(effect.message)
            }
        }
    }

    MenuScreen(
        state = state,
        onAction = viewModel::onAction,
        onNavigateBack = onNavigateBack,
        onNavigateToCart = onNavigateToCart,
        showBackButton = false // It is a top-level tab
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MenuScreen(
    state: MenuViewModelContract.MyUiState,
    onAction: (MenuViewModelContract.Action) -> Unit,
    onNavigateBack: () -> Unit = {},
    onNavigateToCart: () -> Unit = {},
    showBackButton: Boolean = true,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.menu_title), fontWeight = FontWeight.Bold, color = Color(0xFF1E1E1E)) },
                navigationIcon = {
                    if (showBackButton) {
                        IconButton(onClick = onNavigateBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(
                                com.ibnalsham.app.ui.design.designSystem.R.string.back), tint = Color.Black)
                        }
                    }
                },
                actions = {
                    // View type toggle
                    IconButton(
                        onClick = {
                            val newType = if (state.viewType == ViewType.GRID) ViewType.LIST else ViewType.GRID
                            onAction(MenuViewModelContract.Action.ToggleViewType(newType))
                        }
                    ) {
                        Icon(
                            imageVector = if (state.viewType == ViewType.GRID) Icons.Outlined.ViewList else Icons.Outlined.GridView,
                            contentDescription = stringResource(R.string.toggle_view),
                            tint = BrandRed,
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White),
                modifier = Modifier.shadow(2.dp),
            )
        },
        bottomBar = {
            CartFloatingBar(
                itemCount = state.cartTotalItems,
                subtotal = state.cartSubtotal,
                onClick = onNavigateToCart,
            )
        },
        containerColor = BackgroundColor,
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (state.isLoading && state.products.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = BrandRed)
                }
            } else {
                // Category chips
                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp),
                ) {
                    items(state.categories) { category ->
                        CategoryItem(
                            category = category,
                            isSelected = state.selectedCategoryId == category.id,
                            onClick = { onAction(MenuViewModelContract.Action.CategorySelected(category.id)) },
                        )
                    }
                }

                // Products — grid or list
                AnimatedContent(
                    targetState = state.viewType,
                    transitionSpec = { fadeIn() togetherWith fadeOut() },
                    label = "view_toggle",
                ) { viewType ->
                    when (viewType) {
                        ViewType.GRID -> ProductGrid(
                            products = state.products,
                            cartCounts = state.cartItemCounts,
                            onAction = onAction,
                        )
                        ViewType.LIST -> ProductList(
                            products = state.products,
                            cartCounts = state.cartItemCounts,
                            onAction = onAction,
                        )
                    }
                }
            }
        }
    }
}

// ─── Grid View ───────────────────────────────────────────────────────────────

@Composable
private fun ProductGrid(
    products: List<ProductDomain>,
    cartCounts: Map<Int, Int>,
    onAction: (MenuViewModelContract.Action) -> Unit,
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        items(products, key = { it.id }) { product ->
            ProductGridCard(
                product = product,
                cartCount = cartCounts[product.id] ?: 0,
                onInfoClick = { onAction(MenuViewModelContract.Action.ProductClicked(product.id)) },
                onAddToCart = { onAction(MenuViewModelContract.Action.AddToCart(product.id)) },
                onIncrease = { onAction(MenuViewModelContract.Action.IncreaseQuantity(product.id)) },
                onDecrease = { onAction(MenuViewModelContract.Action.DecreaseQuantity(product.id)) },
            )
        }
    }
}

@Composable
private fun ProductGridCard(
    product: ProductDomain,
    cartCount: Int,
    onInfoClick: () -> Unit,
    onAddToCart: () -> Unit,
    onIncrease: () -> Unit,
    onDecrease: () -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize()
            .clickable{
                onInfoClick()
            },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CardColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
    ) {
        Box {
            Column {
                // Image
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp)
                        .background(Color(0xFFF5F5F5)),
                    contentAlignment = Alignment.Center,
                ) {
                    if (!product.image.isNullOrBlank()) {
                        AsyncImage(
                            model = product.image,
                            contentDescription = product.name,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize(),
                        )
                    }

                    // Gradient overlay at bottom
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(40.dp)
                            .align(Alignment.BottomCenter)
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.3f))
                                )
                            )
                    )
                }

                // Details
                Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp)) {
                    Text(
                        text = product.name,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp,
                        color = Color(0xFF1E1E1E),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        lineHeight = 18.sp,
                    )
                    Spacer(modifier = Modifier.height(4.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (product.hasDiscount && product.discountedPrice != null) {
                            Text(
                                text = "%.2f".format(product.discountedPrice),
                                color = BrandRed,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "%.2f".format(product.price),
                                color = Color.Gray,
                                fontSize = 12.sp,
                                style = LocalTextStyle.current.copy(
                                    textDecoration = androidx.compose.ui.text.style.TextDecoration.LineThrough,
                                ),
                            )
                        } else {
                            Text(
                                text = "%.3f ".format(product.price) + stringResource(com.ibnalsham.app.ui.design.designSystem.R.string.currency_symbol),
                                color = BrandRed,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Add to cart / quantity controls
                    AnimatedContent(
                        targetState = cartCount > 0,
                        label = "cart_controls",
                    ) { inCart ->
                        if (inCart) {
                            // +/- controls
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(36.dp)
                                    .background(Color(0xFFF5F5F5), RoundedCornerShape(18.dp)),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceEvenly,
                            ) {
                                IconButton(onClick = onDecrease, modifier = Modifier.size(32.dp)) {
                                    Icon(imageVector = Icons.Default.Remove, contentDescription = "Decrease", tint = BrandRed, modifier = Modifier.size(16.dp))
                                }
                                Text(
                                    text = cartCount.toString(),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp,
                                    color = Color(0xFF1E1E1E),
                                )
                                IconButton(onClick = onIncrease, modifier = Modifier.size(32.dp)) {
                                    Icon(Icons.Default.Add, contentDescription = "Increase", tint = BrandRed, modifier = Modifier.size(16.dp))
                                }
                            }
                        } else {
                            Button(
                                onClick = onAddToCart,
                                modifier = Modifier.fillMaxWidth().height(36.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = BrandRed),
                                shape = RoundedCornerShape(18.dp),
                                contentPadding = PaddingValues(0.dp),
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(stringResource(R.string.add), fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            // Cart count badge — top-right
            if (cartCount > 0) {
                Surface(
                    shape = CircleShape,
                    color = BrandRed,
                    modifier = Modifier.size(40.dp).align(Alignment.TopEnd).padding(6.dp),
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = cartCount.toString(),
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                        )
                    }
                }
            }
        }
    }
}

// ─── List View ───────────────────────────────────────────────────────────────

@Composable
private fun ProductList(
    products: List<ProductDomain>,
    cartCounts: Map<Int, Int>,
    onAction: (MenuViewModelContract.Action) -> Unit,
) {
    LazyColumn(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        items(products, key = { it.id }) { product ->
            ProductListCard(
                product = product,
                cartCount = cartCounts[product.id] ?: 0,
                onInfoClick = { onAction(MenuViewModelContract.Action.ProductClicked(product.id)) },
                onAddToCart = { onAction(MenuViewModelContract.Action.AddToCart(product.id)) },
                onIncrease = { onAction(MenuViewModelContract.Action.IncreaseQuantity(product.id)) },
                onDecrease = { onAction(MenuViewModelContract.Action.DecreaseQuantity(product.id)) },
            )
        }
    }
}

@Composable
private fun ProductListCard(
    product: ProductDomain,
    cartCount: Int,
    onInfoClick: () -> Unit,
    onAddToCart: () -> Unit,
    onIncrease: () -> Unit,
    onDecrease: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable{onInfoClick()},
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CardColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Image
            Box(
                modifier = Modifier
                    .size(90.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFFF5F5F5)),
                contentAlignment = Alignment.Center,
            ) {
                if (!product.image.isNullOrBlank()) {
                    AsyncImage(
                        model = product.image,
                        contentDescription = product.name,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize(),
                    )
                }

                // Count badge
                if (cartCount > 0) {
                    Surface(shape = CircleShape, color = BrandRed, modifier = Modifier.size(32.dp).align(Alignment.TopEnd).padding(2.dp)) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(cartCount.toString(), color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = product.name,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 15.sp,
                    color = Color(0xFF1E1E1E),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                if (!product.description.isNullOrBlank()) {
                    Text(
                        text = product.description!!,
                        color = Color.Gray,
                        fontSize = 12.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (product.hasDiscount && product.discountedPrice != null) {
                        Text("%.2f".format(product.discountedPrice), color = BrandRed, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            "%.2f".format(product.price),
                            color = Color.Gray,
                            fontSize = 12.sp,
                            style = LocalTextStyle.current.copy(textDecoration = androidx.compose.ui.text.style.TextDecoration.LineThrough),
                        )
                        Text(" JOD", color = BrandRed, fontSize = 12.sp)
                    } else {
                        Text("%.2f JOD".format(product.price), color = BrandRed, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Cart controls
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                AnimatedContent(
                    targetState = cartCount > 0,
                    label = "list_cart",
                ) { inCart ->
                    if (inCart) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .background(BrandRed, CircleShape)
                                    .clickable(onClick = onIncrease),
                                contentAlignment = Alignment.Center,
                            ) {
                                Icon(Icons.Default.Add, contentDescription = "Add", tint = Color.White, modifier = Modifier.size(12.dp))
                            }
                            Text(cartCount.toString(), fontWeight = FontWeight.Bold, fontSize = 15.sp, modifier = Modifier.padding(vertical = 2.dp))
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .background(Color(0xFFF5F5F5), CircleShape)
                                    .clickable(onClick = onDecrease),
                                contentAlignment = Alignment.Center,
                            ) {
                                Icon(imageVector = Icons.Default.Remove, contentDescription = "Remove", tint = BrandRed, modifier = Modifier.size(12.dp))
                            }
                        }
                    } else {
                        IconButton(
                            onClick = onAddToCart,
                            modifier = Modifier.size(40.dp).background(BrandRed, CircleShape),
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "Add to cart", tint = Color.White, modifier = Modifier.size(20.dp))
                        }
                    }
                }
            }
        }
    }
}

// ─── Category Chip ───────────────────────────────────────────────────────────

@Composable
private fun CategoryItem(
    category: CategoryDomain,
    isSelected: Boolean,
    onClick: () -> Unit,
) {
    Surface(
        modifier = Modifier
            .clip(RoundedCornerShape(24.dp))
            .clickable { onClick() },
        color = if (isSelected) BrandRed else Color.White,
        shadowElevation = if (isSelected) 4.dp else 1.dp,
    ) {
        Text(
            text = category.name,
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp),
            color = if (isSelected) Color.White else Color.Black,
            fontWeight = FontWeight.Bold,
        )
    }
}
