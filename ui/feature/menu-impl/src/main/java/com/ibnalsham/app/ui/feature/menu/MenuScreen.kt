package com.ibnalsham.app.ui.feature.menu

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.GridView
import androidx.compose.material.icons.rounded.ViewAgenda
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.ibnalsham.app.feature.domain.CategoryDomain
import com.ibnalsham.app.feature.menu.presentation.viewModel.MenuViewModel
import com.ibnalsham.app.feature.menu.presentation.viewModel.MenuViewModelContract
import com.ibnalsham.app.feature.menu.presentation.viewModel.ViewType
import com.ibnalsham.app.ui.design.components.AppTopBar
import com.ibnalsham.app.ui.design.components.CartFloatingBar
import com.ibnalsham.app.ui.design.components.CircleIconButton
import com.ibnalsham.app.ui.design.components.ErrorState
import com.ibnalsham.app.ui.design.components.ProductRow
import com.ibnalsham.app.ui.design.components.ProductRowSkeleton
import com.ibnalsham.app.ui.design.components.ProductTile
import com.ibnalsham.app.ui.design.components.ProductTileSkeleton
import com.ibnalsham.app.ui.design.components.StateView
import com.ibnalsham.app.ui.design.motion.bouncyClickable
import com.ibnalsham.app.ui.design.motion.enterFromBottom
import com.ibnalsham.app.ui.design.theme.AppTheme
import com.ibnalsham.app.ui.design.theme.Motion
import com.ibnalsham.app.ui.design.theme.Spacing
import com.ibnalsham.app.ui.design.designSystem.R as DesignR
import com.ibnalsham.app.ui.feature.menuimpl.R
import org.koin.androidx.compose.koinViewModel

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
        // It is a top-level tab, so there is nothing to go back to.
        showBackButton = false,
    )
}

/**
 * Menu.
 *
 * One [LazyVerticalGrid] serves both view types: list mode spans every item
 * across both columns. Keeping a single list means switching views animates the
 * items rather than tearing the whole screen down and rebuilding it.
 */
@Composable
fun MenuScreen(
    state: MenuViewModelContract.MyUiState,
    onAction: (MenuViewModelContract.Action) -> Unit,
    onNavigateBack: () -> Unit = {},
    onNavigateToCart: () -> Unit = {},
    showBackButton: Boolean = true,
) {
    val colors = AppTheme.colors
    val isGrid = state.viewType == ViewType.GRID

    Box(modifier = Modifier.fillMaxSize().background(colors.background)) {
        Column(modifier = Modifier.fillMaxSize()) {
            AppTopBar(
                title = stringResource(R.string.menu_title),
                onNavigateBack = onNavigateBack.takeIf { showBackButton },
                actions = {
                    CircleIconButton(
                        icon = if (isGrid) Icons.Rounded.ViewAgenda else Icons.Rounded.GridView,
                        contentDescription = stringResource(
                            if (isGrid) R.string.menu_view_list else R.string.menu_view_grid,
                        ),
                        onClick = {
                            onAction(
                                MenuViewModelContract.Action.ToggleViewType(
                                    if (isGrid) ViewType.LIST else ViewType.GRID,
                                ),
                            )
                        },
                    )
                },
            )

            if (state.categories.isNotEmpty()) {
                CategoryStrip(
                    categories = state.categories,
                    selectedId = state.selectedCategoryId,
                    onSelect = { onAction(MenuViewModelContract.Action.CategorySelected(it)) },
                )
            }

            when {
                state.isLoading -> MenuSkeleton(isGrid = isGrid)

                state.error != null && state.products.isEmpty() -> ErrorState(
                    onRetry = { onAction(MenuViewModelContract.Action.ViewReady) },
                )

                state.products.isEmpty() -> StateView(
                    icon = Icons.Rounded.GridView,
                    title = stringResource(R.string.menu_empty_title),
                    message = stringResource(R.string.menu_empty_message),
                    modifier = Modifier.fillMaxSize(),
                )

                else -> LazyVerticalGrid(
                    columns = GridCells.Fixed(GRID_COLUMNS),
                    contentPadding = PaddingValues(
                        start = Spacing.gutter,
                        end = Spacing.gutter,
                        top = Spacing.sm,
                        bottom = Spacing.navBarClearance + CART_BAR_CLEARANCE,
                    ),
                    horizontalArrangement = Arrangement.spacedBy(Spacing.md),
                    verticalArrangement = Arrangement.spacedBy(Spacing.md),
                    modifier = Modifier.fillMaxSize(),
                ) {
                    itemsIndexed(
                        items = state.products,
                        key = { _, product -> product.id },
                        // List mode is the same grid with full-width rows.
                        span = { _, _ -> GridItemSpan(if (isGrid) 1 else GRID_COLUMNS) },
                    ) { index, product ->
                        val cartCount = state.cartItemCounts[product.id] ?: 0
                        val onClick = { onAction(MenuViewModelContract.Action.ProductClicked(product.id)) }
                        val onAdd = { onAction(MenuViewModelContract.Action.AddToCart(product.id)) }
                        val onIncrease = { onAction(MenuViewModelContract.Action.IncreaseQuantity(product.id)) }
                        val onDecrease = { onAction(MenuViewModelContract.Action.DecreaseQuantity(product.id)) }

                        // Deliberately not wrapped in AnimatedContent: its size
                        // transform clips to the animated bounds, which cut off
                        // the struck-through price on discounted tiles. The
                        // grid/list switch is a whole-layout change anyway, so
                        // the per-item crossfade bought nothing.
                        if (isGrid) {
                            ProductTile(
                                product = product,
                                cartCount = cartCount,
                                onClick = onClick,
                                onAddToCart = onAdd,
                                onIncrease = onIncrease,
                                onDecrease = onDecrease,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .enterFromBottom(index = index),
                            )
                        } else {
                            ProductRow(
                                product = product,
                                cartCount = cartCount,
                                onClick = onClick,
                                onAddToCart = onAdd,
                                onIncrease = onIncrease,
                                onDecrease = onDecrease,
                                modifier = Modifier.enterFromBottom(index = index),
                            )
                        }
                    }
                }
            }
        }

        CartFloatingBar(
            itemCount = state.cartTotalItems,
            subtotal = state.cartSubtotal,
            onClick = onNavigateToCart,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = Spacing.navBarClearance - Spacing.xxl),
        )
    }
}

/** Sticky-feeling category chips under the top bar. */
@Composable
private fun CategoryStrip(
    categories: List<CategoryDomain>,
    selectedId: Int?,
    onSelect: (Int) -> Unit,
) {
    val colors = AppTheme.colors

    LazyRow(
        contentPadding = PaddingValues(horizontal = Spacing.gutter, vertical = Spacing.sm),
        horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
        modifier = Modifier.fillMaxWidth().background(colors.background),
    ) {
        items(categories, key = { it.id }) { category ->
            val selected = category.id == selectedId
            val background by animateColorAsState(
                targetValue = if (selected) colors.brand else colors.surfaceSunken,
                animationSpec = Motion.snappy(),
                label = "menuChipBackground",
            )
            val content by animateColorAsState(
                targetValue = if (selected) colors.onBrand else colors.textSecondary,
                animationSpec = Motion.snappy(),
                label = "menuChipContent",
            )

            Box(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(background)
                    .bouncyClickable(pressedScale = 0.94f) { onSelect(category.id) }
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

@Composable
private fun MenuSkeleton(isGrid: Boolean) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = Spacing.gutter),
        verticalArrangement = Arrangement.spacedBy(Spacing.md),
    ) {
        if (isGrid) {
            repeat(3) {
                Row(horizontalArrangement = Arrangement.spacedBy(Spacing.md)) {
                    repeat(GRID_COLUMNS) {
                        ProductTileSkeleton(modifier = Modifier.weight(1f))
                    }
                }
            }
        } else {
            repeat(5) { ProductRowSkeleton() }
        }
    }
}

private const val GRID_COLUMNS = 2
private val CART_BAR_CLEARANCE = 72.dp
