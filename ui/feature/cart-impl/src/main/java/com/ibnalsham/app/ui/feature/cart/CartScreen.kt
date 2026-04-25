package com.ibnalsham.app.ui.feature.cart

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.rounded.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.ibnalsham.app.feature.cart.presentation.viewModel.CartItemDomain
import com.ibnalsham.app.feature.cart.presentation.viewModel.CartViewModel
import com.ibnalsham.app.feature.cart.presentation.viewModel.CartViewModelContract
import com.ibnalsham.app.ui.feature.cartimpl.R
import org.koin.androidx.compose.koinViewModel

@Composable
fun CartRoute(
    viewModel: CartViewModel = koinViewModel(),
    onNavigateToCheckout: () -> Unit,
    onNavigateToMenu: () -> Unit,
    onNavigateBack: () -> Unit = {},
) {
    val state by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.onAction(CartViewModelContract.Action.ViewReady)
        
        viewModel.sideEffects.collect { effect ->
            when (effect) {
                CartViewModelContract.SideEffect.NavigateToCheckout -> onNavigateToCheckout()
                CartViewModelContract.SideEffect.NavigateToMenu -> onNavigateToMenu()
            }
        }
    }

    CartScreen(
        state = state,
        onAction = viewModel::onAction,
        onNavigateBack = onNavigateBack,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CartScreen(
    state: CartViewModelContract.MyUiState,
    onAction: (CartViewModelContract.Action) -> Unit,
    onNavigateBack: () -> Unit = {},
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.cart_title), fontWeight = FontWeight.Bold, color = Color(0xFF1E1E1E), fontSize = 22.sp) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(com.ibnalsham.app.ui.design.designSystem.R.string.back), tint = Color.Black)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White),
                actions = {
                    if (state.items.isNotEmpty()) {
                        TextButton(onClick = { onAction(CartViewModelContract.Action.ClearCart) }) {
                            Text(stringResource(R.string.clear), color = Color(0xFFE53935), fontWeight = FontWeight.SemiBold)
                        }
                    }
                },
                modifier = Modifier.shadow(2.dp)
            )
        },
        bottomBar = {
            if (state.items.isNotEmpty()) {
                CartBottomBar(state.subtotal, onCheckout = { onAction(CartViewModelContract.Action.ProceedToCheckout) })
            }
        },
        containerColor = Color(0xFFF7F8FA)
    ) { paddingValues ->
        if (state.items.isEmpty()) {
            EmptyCartView(onBrowse = { onAction(CartViewModelContract.Action.ContinueShopping) })
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(state.items) { item ->
                    CartItemRow(
                        item = item,
                        onIncrease = { onAction(CartViewModelContract.Action.IncreaseQuantity(item.id)) },
                        onDecrease = { onAction(CartViewModelContract.Action.DecreaseQuantity(item.id)) },
                        onRemove = { onAction(CartViewModelContract.Action.RemoveItem(item.id)) }
                    )
                }
                
                // Add some space at the bottom for safety
                item { Spacer(modifier = Modifier.height(30.dp)) }
            }
        }
    }
}

@Composable
fun EmptyCartView(onBrowse: () -> Unit) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(32.dp)) {
            Icon(
                imageVector = Icons.Rounded.ShoppingCart,
                contentDescription = null,
                modifier = Modifier.size(80.dp),
                tint = Color.LightGray
            )
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                stringResource(R.string.empty_cart_title),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1E1E1E)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                stringResource(R.string.empty_cart_desc),
                color = Color.Gray,
                textAlign = TextAlign.Center,
                fontSize = 15.sp
            )
            Spacer(modifier = Modifier.height(32.dp))
            Button(
                onClick = onBrowse,
                shape = RoundedCornerShape(28.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE53935)),
                modifier = Modifier.fillMaxWidth().height(56.dp)
            ) {
                Text(stringResource(R.string.start_shopping), color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        }
    }
}

@Composable
fun CartItemRow(
    item: CartItemDomain,
    onIncrease: () -> Unit,
    onDecrease: () -> Unit,
    onRemove: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFFFAFAFA)),
                contentAlignment = Alignment.Center
            ) {
                AsyncImage(
                    model = item.image ?: "", // Assuming CartItemDomain has image mapped
                    contentDescription = item.name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.name,
                    fontWeight = FontWeight.Medium,
                    fontSize = 15.sp,
                    color = Color(0xFF1E1E1E),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = stringResource(com.ibnalsham.app.ui.design.designSystem.R.string.currency_symbol) + " ${String.format("%.3f", item.price)}",
                    color = Color(0xFFE53935),
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // Bottom Row for Quantity & Delete
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Quantity
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .height(36.dp)
                            .background(Color(0xFFF7F8FA), RoundedCornerShape(18.dp))
                            .padding(horizontal = 4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .shadow(1.dp, CircleShape)
                                .background(Color.White, CircleShape)
                                .clickable(onClick = onDecrease),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("-", fontWeight = FontWeight.Bold, color = Color.Black)
                        }
                        Text(
                            text = item.quantity.toString(),
                            modifier = Modifier.padding(horizontal = 12.dp),
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .shadow(1.dp, CircleShape)
                                .background(Color.White, CircleShape)
                                .clickable(onClick = onIncrease),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(imageVector = Icons.Default.Add, contentDescription = "Add", modifier = Modifier.size(14.dp), tint = Color.Black)
                        }
                    }
                    
                    // Delete
                    IconButton(onClick = onRemove, modifier = Modifier.size(36.dp)) {
                        Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete", tint = Color.Gray, modifier = Modifier.size(20.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun CartBottomBar(subtotal: Double, onCheckout: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color.White,
        shadowElevation = 24.dp
    ) {
        Column(modifier = Modifier.padding(16.dp).navigationBarsPadding()) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    stringResource(R.string.total),
                    fontWeight = FontWeight.Medium,
                    fontSize = 16.sp,
                    color = Color.Gray
                )
                Text(
                    stringResource(com.ibnalsham.app.ui.design.designSystem.R.string.currency_symbol) + " ${String.format("%.3f", subtotal)}",
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 22.sp,
                    color = Color(0xFF1E1E1E)
                )
            }
            
            Button(
                onClick = onCheckout,
                modifier = Modifier.fillMaxWidth().height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE53935)),
                shape = RoundedCornerShape(28.dp)
            ) {
                Text(stringResource(R.string.proceed_checkout), color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        }
    }
}
