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
import com.ibnalsham.app.ui.design.designSystem.R as DesignR
import com.ibnalsham.app.ui.design.components.jodPrice
import com.ibnalsham.app.feature.domain.DiscountType
import com.ibnalsham.app.feature.checkout.presentation.viewModel.CheckoutViewModel
import com.ibnalsham.app.feature.checkout.presentation.viewModel.CheckoutViewModelContract
import com.ibnalsham.app.feature.checkout.presentation.viewModel.CheckoutViewModelContract.OrderType
import com.ibnalsham.app.ui.design.components.AppTopBar
import com.ibnalsham.app.ui.design.components.FullWidthButton
import com.ibnalsham.app.ui.design.theme.AppTheme
import com.ibnalsham.app.ui.design.theme.Spacing
import org.koin.androidx.compose.koinViewModel

// Local alias so this screen's many call sites stay short.
private val BrandRed = com.ibnalsham.app.ui.design.theme.Ember

/**
 * Checkout.
 *
 * Reaching this screen already implies a session: the sign-in gate sits on the
 * cart's checkout button, so a guest is asked there and arrives here signed in
 * with their cart intact. This screen no longer bounces anyone out mid-flow.
 */
@Composable
fun CheckoutRoute(
    viewModel: CheckoutViewModel = koinViewModel(),
    onNavigateToOrderTracking: (Int) -> Unit,
    onShowSnackbar: (String) -> Unit,
    onNavigateBack: () -> Unit = {},
) {
    val state by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
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
            AppTopBar(
                title = stringResource(R.string.checkout_title),
                onNavigateBack = onNavigateBack,
            )
        },
        bottomBar = {
            CheckoutBottomBar(
                total = state.total,
                isPlacing = state.isPlacingOrder,
                isEnabled = state.canPlaceOrder,
                onPlaceOrder = { onAction(CheckoutViewModelContract.Action.PlaceOrder) },
            )
        },
        containerColor = AppTheme.colors.background,
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
                            stringResource(R.string.delivery_fee, jodPrice(state.deliveryFee)),
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
                    Text(jodPrice(item.total), fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = BrandRed)
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

            // Coupon
            item { CouponSection(state = state, onAction = onAction) }

            // Payment method (Jordan)
            item { PaymentSection(state = state, onAction = onAction) }

            // Totals
            item {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    color = Color.White,
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        TotalRow(stringResource(R.string.subtotal), state.subtotal)
                        if (state.discountAmount > 0) {
                            TotalRow(
                                label = stringResource(DesignR.string.checkout_discount),
                                amount = -state.discountAmount,
                                valueColor = Color(0xFF2E7D32),
                            )
                        }
                        if (state.orderType == OrderType.DELIVERY) {
                            TotalRow(stringResource(R.string.delivery_fee_label), state.effectiveDeliveryFee)
                        }
                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            Text(stringResource(R.string.total), fontWeight = FontWeight.ExtraBold, fontSize = 18.sp, color = Color(0xFF1E1E1E))
                            Text(
                                text = jodPrice(state.total),
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 18.sp,
                                color = BrandRed,
                            )
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
private fun TotalRow(label: String, amount: Double, valueColor: Color = Color.Unspecified) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(label, color = Color.Gray, fontSize = 14.sp)
        Text(
            text = jodPrice(amount),
            fontWeight = FontWeight.Medium,
            fontSize = 14.sp,
            color = valueColor,
        )
    }
}

/**
 * Coupon entry with inline validation, plus the list of coupons the customer
 * is currently eligible for.
 */
@Composable
private fun CouponSection(
    state: CheckoutViewModelContract.MyUiState,
    onAction: (CheckoutViewModelContract.Action) -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = Color.White,
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                stringResource(DesignR.string.checkout_coupon),
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = Color(0xFF1E1E1E),
            )
            Spacer(modifier = Modifier.height(10.dp))

            val applied = state.appliedCoupon
            if (applied != null) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFE8F5E9), RoundedCornerShape(10.dp))
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            stringResource(DesignR.string.checkout_coupon_applied, applied.code),
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp,
                            color = Color(0xFF2E7D32),
                        )
                        Text(applied.title, fontSize = 12.sp, color = Color(0xFF558B2F))
                    }
                    TextButton(onClick = { onAction(CheckoutViewModelContract.Action.RemoveCoupon) }) {
                        Text(stringResource(DesignR.string.remove), color = BrandRed)
                    }
                }
            } else {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTextField(
                        value = state.couponInput,
                        onValueChange = { onAction(CheckoutViewModelContract.Action.UpdateCouponInput(it)) },
                        placeholder = { Text(stringResource(DesignR.string.checkout_coupon_hint)) },
                        singleLine = true,
                        isError = state.couponError != null,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = BrandRed),
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = { onAction(CheckoutViewModelContract.Action.ApplyCoupon) },
                        enabled = state.couponInput.isNotBlank() && !state.isApplyingCoupon,
                        colors = ButtonDefaults.buttonColors(containerColor = BrandRed),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.height(56.dp),
                    ) {
                        if (state.isApplyingCoupon) {
                            CircularProgressIndicator(
                                color = Color.White,
                                modifier = Modifier.size(18.dp),
                                strokeWidth = 2.dp,
                            )
                        } else {
                            Text(stringResource(DesignR.string.apply), color = Color.White)
                        }
                    }
                }

                state.couponError?.let { error ->
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(error, color = BrandRed, fontSize = 12.sp)
                }
            }

            if (state.availableCoupons.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                TextButton(onClick = { onAction(CheckoutViewModelContract.Action.ToggleCouponList) }) {
                    Text(
                        stringResource(DesignR.string.checkout_available_coupons),
                        color = BrandRed,
                        fontSize = 13.sp,
                    )
                }

                if (state.isCouponListExpanded) {
                    state.availableCoupons.forEach { coupon ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .clickable {
                                    onAction(CheckoutViewModelContract.Action.UpdateCouponInput(coupon.code))
                                    onAction(CheckoutViewModelContract.Action.ApplyCoupon)
                                }
                                .padding(vertical = 10.dp, horizontal = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(coupon.code, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Text(coupon.title, fontSize = 12.sp, color = Color.Gray)
                            }
                            Text(
                                text = when (coupon.discountType) {
                                    DiscountType.PERCENT -> stringResource(
                                        DesignR.string.offer_percent_off,
                                        coupon.discountValue.toInt(),
                                    )

                                    else -> stringResource(
                                        DesignR.string.offer_amount_off,
                                        jodPrice(coupon.discountValue),
                                    )
                                },
                                color = Color(0xFF2E7D32),
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 13.sp,
                            )
                        }
                    }
                }
            }
        }
    }
}

/** Jordan payment options: cash on delivery, CliQ transfer, card, wallet. */
@Composable
private fun PaymentSection(
    state: CheckoutViewModelContract.MyUiState,
    onAction: (CheckoutViewModelContract.Action) -> Unit,
) {
    if (state.paymentMethods.isEmpty()) return

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = Color.White,
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                stringResource(DesignR.string.checkout_payment_method),
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = Color(0xFF1E1E1E),
            )
            Spacer(modifier = Modifier.height(8.dp))

            state.paymentMethods.forEach { method ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .clickable {
                            onAction(CheckoutViewModelContract.Action.SelectPaymentMethod(method.type))
                        }
                        .padding(vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    RadioButton(
                        selected = state.selectedPaymentMethod == method.type,
                        onClick = null,
                        colors = RadioButtonDefaults.colors(selectedColor = BrandRed),
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(method.name, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                        method.description?.let {
                            Text(it, fontSize = 12.sp, color = Color.Gray)
                        }
                    }
                }
            }

            // CliQ and other transfer methods need the customer's reference number.
            val selected = state.selectedMethod
            if (selected?.requiresReference == true) {
                Spacer(modifier = Modifier.height(8.dp))
                selected.accountAlias?.let { alias ->
                    Text(
                        stringResource(DesignR.string.checkout_cliq_alias, alias),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFFE65100),
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
                OutlinedTextField(
                    value = state.paymentReference,
                    onValueChange = { onAction(CheckoutViewModelContract.Action.UpdatePaymentReference(it)) },
                    label = { Text(stringResource(DesignR.string.checkout_payment_reference)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = BrandRed),
                )
            }
        }
    }
}

@Composable
private fun CheckoutBottomBar(
    total: Double,
    isPlacing: Boolean,
    isEnabled: Boolean,
    onPlaceOrder: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(topStart = 30.dp, topEnd = 30.dp))
            .background(AppTheme.colors.surface)
            .padding(horizontal = Spacing.gutter, vertical = Spacing.lg)
            .navigationBarsPadding(),
    ) {
        FullWidthButton(
            text = stringResource(DesignR.string.place_order, jodPrice(total)),
            onClick = onPlaceOrder,
            enabled = isEnabled,
            loading = isPlacing,
        )
    }
}
