package com.ibnalsham.app.ui.feature.productDetails

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.rounded.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.ibnalsham.app.feature.productDetails.presentation.viewModel.ProductDetailsViewModel
import com.ibnalsham.app.feature.productDetails.presentation.viewModel.ProductDetailsViewModelContract
import com.ibnalsham.app.ui.feature.productDetailsimpl.R
import org.koin.androidx.compose.koinViewModel

@Composable
fun ProductDetailsRoute(
    productId: Int,
    viewModel: ProductDetailsViewModel = koinViewModel(),
    onNavigateBack: () -> Unit,
    onShowSnackbar: (String) -> Unit
) {
    val state by viewModel.uiState.collectAsState()

    LaunchedEffect(productId) {
        viewModel.onAction(ProductDetailsViewModelContract.Action.ViewReady(productId))
        
        viewModel.sideEffects.collect { effect ->
            when (effect) {
                ProductDetailsViewModelContract.SideEffect.NavigateBack -> onNavigateBack()
                is ProductDetailsViewModelContract.SideEffect.ShowSnackbar -> onShowSnackbar(effect.message)
            }
        }
    }

    ProductDetailsScreen(
        state = state, 
        onAction = viewModel::onAction,
        onNavigateBack = onNavigateBack
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductDetailsScreen(
    state: ProductDetailsViewModelContract.MyUiState,
    onAction: (ProductDetailsViewModelContract.Action) -> Unit,
    onNavigateBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = state.name,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1E1E1E),
                        maxLines = 1,
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier
                            .padding(start = 8.dp)
                            .size(36.dp)
                            .shadow(2.dp, CircleShape)
                            .background(Color.White, CircleShape)
                    ) {
                        Icon(
                            Icons.Default.ArrowBack, 
                            contentDescription = stringResource(com.ibnalsham.app.ui.design.designSystem.R.string.back), 
                            tint = Color.Black, 
                            modifier = Modifier.size(20.dp)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        },
        bottomBar = {
            if (!state.isLoading) {
                BottomActionBar(state, onAction)
            }
        },
        containerColor = Color(0xFFF7F8FA)
    ) { paddingValues ->
        if (state.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Color(0xFFE53935))
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = paddingValues.calculateBottomPadding())
                    .verticalScroll(rememberScrollState())
            ) {
                // Header Image Carousel
                if (state.images.isNotEmpty()) {
                    ProductImageCarousel(images = state.images.map { it.url })
                } else {
                    Box(modifier = Modifier.fillMaxWidth().height(300.dp).background(Color.White))
                }

                // Info Section
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                        .background(Color.White)
                        .padding(24.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(), 
                        horizontalArrangement = Arrangement.SpaceBetween, 
                        verticalAlignment = Alignment.Top
                    ) {
                        Text(
                            text = state.name, 
                            fontWeight = FontWeight.Bold, 
                            style = MaterialTheme.typography.headlineSmall, 
                            color = Color(0xFF1E1E1E),
                            modifier = Modifier.weight(1f)
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = stringResource(com.ibnalsham.app.ui.design.designSystem.R.string.currency_symbol) + " ${String.format("%.3f", state.effectivePrice)}", 
                                fontWeight = FontWeight.ExtraBold, 
                                fontSize = 20.sp, 
                                color = Color(0xFFE53935)
                            )
                            if (state.basePrice > state.effectivePrice) {
                                Text(
                                    text = stringResource(com.ibnalsham.app.ui.design.designSystem.R.string.currency_symbol) + " ${String.format("%.3f", state.basePrice)}", 
                                    textDecoration = TextDecoration.LineThrough,
                                    color = Color.Gray,
                                    fontSize = 14.sp
                                )
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = state.description, 
                        color = Color(0xFF666666), 
                        lineHeight = 22.sp,
                        style = MaterialTheme.typography.bodyMedium
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // Options
                    if (state.options.isNotEmpty()) {
                        Text(
                            stringResource(R.string.customize_order), 
                            fontWeight = FontWeight.Bold, 
                            fontSize = 18.sp,
                            color = Color(0xFF1E1E1E)
                        )
                        Spacer(modifier = Modifier.height(16.dp))

                        state.options.forEach { option ->
                            OptionRow(
                                title = option.name,
                                price = option.price,
                                isRequired = option.isRequired,
                                isSelected = state.selectedOptionIds.contains(option.id),
                                onToggle = { onAction(ProductDetailsViewModelContract.Action.ToggleOption(option.id)) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ProductImageCarousel(images: List<String>) {
    LazyRow(
        modifier = Modifier.fillMaxWidth().height(300.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(images) { url ->
            AsyncImage(
                model = url,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillParentMaxWidth()
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp))
            )
        }
    }
}

@Composable
fun OptionRow(title: String, price: Double, isRequired: Boolean, isSelected: Boolean, onToggle: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(if (isSelected) Color(0xFFE53935).copy(alpha = 0.05f) else Color.Transparent)
            .clickable { onToggle() }
            .padding(vertical = 12.dp, horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(
            checked = isSelected,
            onCheckedChange = null,
            colors = CheckboxDefaults.colors(checkedColor = Color(0xFFE53935))
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(title, modifier = Modifier.weight(1f), fontSize = 16.sp, color = Color(0xFF1E1E1E))
        
        if (price > 0) {
            Text("+" + stringResource(com.ibnalsham.app.ui.design.designSystem.R.string.currency_symbol) + " ${String.format("%.3f", price)}", color = Color(0xFFE53935), fontWeight = FontWeight.SemiBold)
        } else if (isRequired) {
            Text(stringResource(R.string.required), color = Color.Gray, fontSize = 12.sp)
        }
    }
}

@Composable
fun BottomActionBar(
    state: ProductDetailsViewModelContract.MyUiState,
    onAction: (ProductDetailsViewModelContract.Action) -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(), 
        color = Color.White, 
        shadowElevation = 24.dp
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 24.dp, vertical = 16.dp)
                .navigationBarsPadding(),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Quantity Control
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .height(56.dp)
                    .background(Color(0xFFF0F0F0), RoundedCornerShape(28.dp))
                    .padding(horizontal = 4.dp)
            ) {
                IconButton(
                    onClick = { onAction(ProductDetailsViewModelContract.Action.DecrementQuantity) }, 
                    modifier = Modifier.size(40.dp).background(Color.White, CircleShape).shadow(1.dp, CircleShape)
                ) {
                    Text("-", fontWeight = FontWeight.Bold, fontSize = 20.sp, color = Color.Black)
                }
                Text(
                    state.quantity.toString(), 
                    modifier = Modifier.padding(horizontal = 20.dp), 
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
                IconButton(
                    onClick = { onAction(ProductDetailsViewModelContract.Action.IncrementQuantity(10)) }, 
                    modifier = Modifier.size(40.dp).background(Color.White, CircleShape).shadow(1.dp, CircleShape)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(20.dp))
                }
            }

            // Add Button
            Button(
                onClick = { onAction(ProductDetailsViewModelContract.Action.AddToCart) },
                modifier = Modifier.weight(1f).height(56.dp),
                enabled = state.isAddToCartEnabled,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFE53935), 
                    disabledContainerColor = Color(0xFFE0E0E0)
                ),
                shape = RoundedCornerShape(28.dp)
            ) {
                if (state.isAddingToCart) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                } else {
                    Icon(Icons.Rounded.ShoppingCart, contentDescription = null, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = stringResource(com.ibnalsham.app.ui.design.designSystem.R.string.currency_symbol) + " ${String.format("%.3f", state.calculatedTotal)}", 
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }
            }
        }
    }
}
