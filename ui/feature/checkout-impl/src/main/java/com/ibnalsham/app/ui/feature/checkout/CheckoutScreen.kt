package com.ibnalsham.app.ui.feature.checkout

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DeliveryDining
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ibnalsham.app.ui.feature.checkoutimpl.R
import com.ibnalsham.app.feature.checkout.presentation.viewModel.CheckoutViewModel
import com.ibnalsham.app.feature.checkout.presentation.viewModel.CheckoutViewModelContract
import com.ibnalsham.app.feature.checkout.presentation.viewModel.CheckoutViewModelContract.OrderType
import com.ibnalsham.app.library.data.cache.UserPreferencesRepository
import org.koin.androidx.compose.koinViewModel
import org.koin.compose.koinInject

private val BrandRed = Color(0xFFC62828)

@Composable
fun CheckoutRoute(
    viewModel: CheckoutViewModel = koinViewModel(),
    userPreferencesRepository: UserPreferencesRepository = koinInject(),
    onNavigateToOrderTracking: (Int) -> Unit,
    onNavigateToAuth: () -> Unit,
    onShowSnackbar: (String) -> Unit,
    onNavigateBack: () -> Unit = {},
) {
    val state by viewModel.uiState.collectAsState()
    val isLoggedIn = remember { userPreferencesRepository.getAuthToken().isNullOrBlank().not() }

    LaunchedEffect(isLoggedIn) {
        if (!isLoggedIn) {
            onShowSnackbar("سجّل الدخول لإكمال الطلب")
            onNavigateToAuth()
        }
    }

    LaunchedEffect(Unit) {
        if (!isLoggedIn) return@LaunchedEffect
        viewModel.onAction(CheckoutViewModelContract.Action.ViewReady)

        viewModel.sideEffects.collect { effect ->
            when (effect) {
                is CheckoutViewModelContract.SideEffect.NavigateToOrderTracking -> onNavigateToOrderTracking(effect.orderId)
                is CheckoutViewModelContract.SideEffect.ShowSnackbar -> onShowSnackbar(effect.message)
                CheckoutViewModelContract.SideEffect.RequestLocationPermission -> { /* TODO: Handle location permission */ }
            }
        }
    }

    CheckoutScreen(state = state, onAction = viewModel::onAction, onNavigateBack = onNavigateBack)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CheckoutScreen(
    state: CheckoutViewModelContract.MyUiState,
    onAction: (CheckoutViewModelContract.Action) -> Unit,
    onNavigateBack: () -> Unit = {},
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.checkout_title), fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(
                            com.ibnalsham.app.ui.design.designSystem.R.string.back))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White),
                modifier = Modifier.shadow(2.dp),
            )
        },
        bottomBar = {
            CheckoutBottomBar(
                total = state.total,
                isPlacing = state.isPlacingOrder,
                onPlaceOrder = { onAction(CheckoutViewModelContract.Action.PlaceOrder) },
            )
        },
        containerColor = Color(0xFFFAFAFA),
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            // Order type selector
            item {
                Text(stringResource(R.string.order_type), fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color(0xFF1E1E1E))
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    OrderTypeOption(
                        label = stringResource(R.string.pickup),
                        icon = Icons.Default.Storefront,
                        isSelected = state.orderType == OrderType.PICKUP,
                        onClick = { onAction(CheckoutViewModelContract.Action.SelectOrderType(OrderType.PICKUP)) },
                        modifier = Modifier.weight(1f),
                    )
                    OrderTypeOption(
                        label = stringResource(R.string.delivery),
                        icon = Icons.Default.DeliveryDining,
                        isSelected = state.orderType == OrderType.DELIVERY,
                        onClick = { onAction(CheckoutViewModelContract.Action.SelectOrderType(OrderType.DELIVERY)) },
                        modifier = Modifier.weight(1f),
                    )
                }
            }

            // Delivery address (if delivery)
            if (state.orderType == OrderType.DELIVERY) {
                item {
                    OutlinedTextField(
                        value = state.address,
                        onValueChange = { onAction(CheckoutViewModelContract.Action.UpdateAddress(it)) },
                        label = { Text(stringResource(R.string.delivery_address)) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = BrandRed),
                    )

                    if (state.deliveryFee > 0) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            stringResource(R.string.delivery_fee, "%.2f".format(state.deliveryFee)),
                            color = Color.Gray,
                            fontSize = 13.sp,
                        )
                    }
                }
            }

            // Cart items summary
            item {
                Text(stringResource(R.string.order_summary), fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color(0xFF1E1E1E))
            }

            items(state.cartItems) { item ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White, RoundedCornerShape(12.dp))
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(item.name, fontWeight = FontWeight.Medium, fontSize = 14.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        Text("x${item.quantity}", color = Color.Gray, fontSize = 12.sp)
                    }
                    Text(stringResource(com.ibnalsham.app.ui.design.designSystem.R.string.currency_symbol) + " ${"%.3f".format(item.total)}", fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = BrandRed)
                }
            }

            // Notes
            item {
                Text(stringResource(R.string.order_notes), fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color(0xFF1E1E1E))
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = state.notes,
                    onValueChange = { onAction(CheckoutViewModelContract.Action.UpdateNotes(it)) },
                    placeholder = { Text(stringResource(R.string.notes_placeholder)) },
                    modifier = Modifier.fillMaxWidth().height(100.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = BrandRed),
                    maxLines = 4,
                )
            }

            // Cash payment notice
            item {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFFFFF3E0),
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text("💵", fontSize = 24.sp)
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(stringResource(R.string.cash_payment), fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFFE65100))
                            val typeLabel = if (state.orderType == OrderType.DELIVERY) stringResource(R.string.payment_on_delivery) else stringResource(R.string.payment_on_pickup)
                            Text(stringResource(R.string.cash_payment_desc, typeLabel), color = Color(0xFF795548), fontSize = 13.sp)
                        }
                    }
                }
            }

            // Totals
            item {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    color = Color.White,
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        TotalRow(stringResource(R.string.subtotal), state.subtotal)
                        if (state.orderType == OrderType.DELIVERY) {
                            TotalRow(stringResource(R.string.delivery_fee_label), state.deliveryFee)
                        }
                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            Text(stringResource(R.string.total), fontWeight = FontWeight.ExtraBold, fontSize = 18.sp, color = Color(0xFF1E1E1E))
                            Text(stringResource(com.ibnalsham.app.ui.design.designSystem.R.string.currency_symbol) + " ${"%.3f".format(state.total)}", fontWeight = FontWeight.ExtraBold, fontSize = 18.sp, color = BrandRed)
                        }
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(80.dp)) }
        }
    }
}

@Composable
private fun OrderTypeOption(
    label: String,
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .border(
                width = 2.dp,
                color = if (isSelected) BrandRed else Color(0xFFE0E0E0),
                shape = RoundedCornerShape(14.dp),
            )
            .clickable { onClick() },
        color = if (isSelected) BrandRed.copy(alpha = 0.08f) else Color.White,
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Icon(imageVector = icon, contentDescription = label, tint = if (isSelected) BrandRed else Color.Gray, modifier = Modifier.size(32.dp))
            Spacer(modifier = Modifier.height(8.dp))
            Text(label, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium, color = if (isSelected) BrandRed else Color.Gray)
        }
    }
}

@Composable
private fun TotalRow(label: String, amount: Double) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(label, color = Color.Gray, fontSize = 14.sp)
        Text(stringResource(com.ibnalsham.app.ui.design.designSystem.R.string.currency_symbol) + " ${"%.3f".format(amount)}", fontWeight = FontWeight.Medium, fontSize = 14.sp)
    }
}

@Composable
private fun CheckoutBottomBar(
    total: Double,
    isPlacing: Boolean,
    onPlaceOrder: () -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color.White,
        shadowElevation = 24.dp,
    ) {
        Column(modifier = Modifier.padding(16.dp).navigationBarsPadding()) {
            Button(
                onClick = onPlaceOrder,
                modifier = Modifier.fillMaxWidth().height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = BrandRed),
                shape = RoundedCornerShape(28.dp),
                enabled = !isPlacing,
            ) {
                if (isPlacing) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Text(
                    stringResource(R.string.place_order, stringResource(com.ibnalsham.app.ui.design.designSystem.R.string.currency_symbol) + " ${"%.3f".format(total)}"),
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                )
            }
        }
    }
}
