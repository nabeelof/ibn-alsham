package com.ibnalsham.app.ui.feature.favorites

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.ibnalsham.app.feature.favorites.presentation.viewModel.FavoritesViewModel
import com.ibnalsham.app.feature.favorites.presentation.viewModel.FavoritesViewModelContract
import com.ibnalsham.app.library.data.cache.GatedFeature
import com.ibnalsham.app.ui.design.components.AppTopBar
import com.ibnalsham.app.ui.design.components.ErrorState
import com.ibnalsham.app.ui.design.components.ProductRow
import com.ibnalsham.app.ui.design.components.ProductRowSkeleton
import com.ibnalsham.app.ui.design.components.SignInRequiredState
import com.ibnalsham.app.ui.design.components.StateView
import com.ibnalsham.app.ui.design.gate.LocalAuthGate
import com.ibnalsham.app.ui.design.motion.enterFromBottom
import com.ibnalsham.app.ui.design.theme.AppTheme
import com.ibnalsham.app.ui.design.theme.Spacing
import com.ibnalsham.app.ui.design.designSystem.R as DesignR
import org.koin.androidx.compose.koinViewModel

@Composable
fun FavoritesRoute(
    onNavigateToProduct: (Int) -> Unit,
    onNavigateToMenu: () -> Unit,
    onNavigateBack: () -> Unit,
    onShowSnackbar: (String) -> Unit,
    viewModel: FavoritesViewModel = koinViewModel(),
) {
    val state by viewModel.uiState.collectAsState()
    val gate = LocalAuthGate.current

    LaunchedEffect(Unit) {
        viewModel.onAction(FavoritesViewModelContract.Action.ViewReady)
    }

    LaunchedEffect(Unit) {
        viewModel.sideEffects.collect { effect ->
            when (effect) {
                is FavoritesViewModelContract.SideEffect.NavigateToProduct -> onNavigateToProduct(effect.productId)
                FavoritesViewModelContract.SideEffect.NavigateToMenu -> onNavigateToMenu()
                // Signing in updates the session, and the view model reloads the
                // list from its session flow — nothing to do here but ask.
                FavoritesViewModelContract.SideEffect.RequireSignIn -> gate.prompt(GatedFeature.Favorites)
                is FavoritesViewModelContract.SideEffect.ShowSnackbar -> onShowSnackbar(effect.message)
            }
        }
    }

    FavoritesScreen(
        state = state,
        onAction = viewModel::onAction,
        onNavigateBack = onNavigateBack,
    )
}

/**
 * Favourites.
 *
 * A guest lands on the sign-in state rather than being bounced to a login
 * screen, so they can see what the feature is before deciding.
 */
@Composable
private fun FavoritesScreen(
    state: FavoritesViewModelContract.MyUiState,
    onAction: (FavoritesViewModelContract.Action) -> Unit,
    onNavigateBack: () -> Unit,
) {
    val colors = AppTheme.colors

    Column(modifier = Modifier.fillMaxSize().background(colors.background)) {
        AppTopBar(
            title = stringResource(DesignR.string.favorites_title),
            onNavigateBack = onNavigateBack,
        )

        when {
            state.requiresLogin -> SignInRequiredState(
                title = stringResource(DesignR.string.gate_favorites_title),
                message = stringResource(DesignR.string.gate_favorites_message),
                onSignIn = { onAction(FavoritesViewModelContract.Action.SignInClicked) },
                browseActionText = stringResource(DesignR.string.action_browse_menu),
                onBrowse = { onAction(FavoritesViewModelContract.Action.GoToMenu) },
            )

            state.isLoading -> Column(
                modifier = Modifier.padding(horizontal = Spacing.gutter, vertical = Spacing.sm),
                verticalArrangement = Arrangement.spacedBy(Spacing.md),
            ) {
                repeat(4) { ProductRowSkeleton() }
            }

            state.error != null -> ErrorState(
                onRetry = { onAction(FavoritesViewModelContract.Action.Refresh) },
            )

            state.isEmpty -> StateView(
                icon = Icons.Rounded.FavoriteBorder,
                title = stringResource(DesignR.string.favorites_empty),
                message = stringResource(DesignR.string.favorites_empty_message),
                primaryActionText = stringResource(DesignR.string.favorites_browse_menu),
                onPrimaryAction = { onAction(FavoritesViewModelContract.Action.GoToMenu) },
                modifier = Modifier.fillMaxSize(),
            )

            else -> LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(
                    start = Spacing.gutter,
                    end = Spacing.gutter,
                    top = Spacing.sm,
                    bottom = Spacing.navBarClearance,
                ),
                verticalArrangement = Arrangement.spacedBy(Spacing.md),
            ) {
                itemsIndexed(state.products, key = { _, product -> product.id }) { index, product ->
                    ProductRow(
                        product = product,
                        cartCount = state.cartItemCounts[product.id] ?: 0,
                        onClick = { onAction(FavoritesViewModelContract.Action.ProductClicked(product.id)) },
                        onAddToCart = { onAction(FavoritesViewModelContract.Action.AddToCart(product.id)) },
                        onToggleFavorite = {
                            onAction(FavoritesViewModelContract.Action.RemoveFavorite(product.id))
                        },
                        modifier = Modifier.enterFromBottom(index = index),
                    )
                }
            }
        }
    }
}
