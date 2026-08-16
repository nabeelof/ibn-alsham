package com.ibnalsham.app.ui.feature.catalog

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.Alignment
import com.ibnalsham.app.ui.feature.catalogimpl.R
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.ibnalsham.app.feature.domain.CategoryDomain
import com.ibnalsham.app.feature.catalog.presentation.viewModel.CatalogViewModel
import com.ibnalsham.app.feature.catalog.presentation.viewModel.CatalogViewModelContract
import com.ibnalsham.app.library.data.cache.CartRepository
import com.ibnalsham.app.ui.design.components.CartFloatingBar
import org.koin.androidx.compose.koinViewModel
import org.koin.compose.koinInject

@Composable
fun CatalogRoute(
    viewModel: CatalogViewModel = koinViewModel(),
    cartRepository: CartRepository = koinInject(),
    onNavigateToCategory: (Int) -> Unit,
    onNavigateToCart: () -> Unit,
    onNavigateBack: () -> Unit = {},
) {
    val state by viewModel.uiState.collectAsState()
    val cartItems by cartRepository.cartItems.collectAsState()
    val itemCount = cartItems.sumOf { it.quantity }
    val subtotal = cartItems.sumOf { it.product.price * it.quantity }

    LaunchedEffect(Unit) {
        viewModel.onAction(CatalogViewModelContract.Action.ViewReady)

        viewModel.sideEffects.collect { effect ->
            when (effect) {
                is CatalogViewModelContract.SideEffect.NavigateToCategory -> onNavigateToCategory(effect.categoryId)
            }
        }
    }

    CatalogScreen(
        state = state,
        onAction = viewModel::onAction,
        onNavigateBack = onNavigateBack,
        cartItemCount = itemCount,
        cartSubtotal = subtotal,
        onNavigateToCart = onNavigateToCart,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CatalogScreen(
    state: CatalogViewModelContract.MyUiState,
    onAction: (CatalogViewModelContract.Action) -> Unit,
    onNavigateBack: () -> Unit = {},
    cartItemCount: Int = 0,
    cartSubtotal: Double = 0.0,
    onNavigateToCart: () -> Unit = {},
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.all_categories), fontWeight = FontWeight.Bold, color = Color(0xFF1E1E1E)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(com.ibnalsham.app.ui.design.designSystem.R.string.back), tint = Color.Black)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White),
                modifier = Modifier.shadow(2.dp)
            )
        },
        bottomBar = {
            CartFloatingBar(
                itemCount = cartItemCount,
                subtotal = cartSubtotal,
                onClick = onNavigateToCart,
            )
        },
        containerColor = Color(0xFFF7F8FA)
    ) { paddingValues ->
        if (state.isLoading) {
            Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Color(0xFFE53935))
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(3), // 3 columns for categories
                modifier = Modifier.fillMaxSize().padding(paddingValues),
                contentPadding = PaddingValues(16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                items(state.categories) { category ->
                    CategoryItem(category = category, onClick = { onAction(CatalogViewModelContract.Action.CategoryClicked(category.id)) })
                }
            }
        }
    }
}

@Composable
fun CategoryItem(category: CategoryDomain, onClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable { onClick() }
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f) // Makes the box perfectly square
                .clip(RoundedCornerShape(16.dp))
                .background(Color.White)
                .shadow(2.dp, RoundedCornerShape(16.dp)),
            contentAlignment = Alignment.Center
        ) {
            AsyncImage(
                model = category.image,
                contentDescription = category.name,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        }
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = category.name,
            fontWeight = FontWeight.Medium,
            fontSize = 13.sp,
            color = Color(0xFF1E1E1E),
            textAlign = TextAlign.Center,
            maxLines = 2
        )
    }
}
