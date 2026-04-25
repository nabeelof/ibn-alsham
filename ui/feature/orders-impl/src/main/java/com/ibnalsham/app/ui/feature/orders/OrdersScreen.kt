package com.ibnalsham.app.ui.feature.orders

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.ibnalsham.app.ui.feature.ordersimpl.R
import com.ibnalsham.app.feature.domain.OrderDomain
import com.ibnalsham.app.feature.domain.OrderStatus
import com.ibnalsham.app.feature.orders.presentation.viewModel.OrderPhase
import com.ibnalsham.app.feature.orders.presentation.viewModel.OrdersViewModel
import com.ibnalsham.app.feature.orders.presentation.viewModel.OrdersViewModelContract
import org.koin.androidx.compose.koinViewModel

private val BrandRed = Color(0xFFC62828)
private val BrandOrange = Color(0xFFFF6D00)
private val BrandGreen = Color(0xFF2E7D32)

@Composable
fun OrdersRoute(
    viewModel: OrdersViewModel = koinViewModel(),
    initialTrackingOrderId: Int? = null,
    onNavigateToOrderDetails: (Int) -> Unit = {},
    onShowSnackbar: (String) -> Unit = {},
    onNavigateBack: () -> Unit = {},
) {
    val state by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.onAction(OrdersViewModelContract.Action.ViewReady)

        viewModel.sideEffects.collect { effect ->
            when (effect) {
                is OrdersViewModelContract.SideEffect.NavigateToOrderDetails -> onNavigateToOrderDetails(effect.orderId)
                is OrdersViewModelContract.SideEffect.ShowSnackbar -> onShowSnackbar(effect.message)
                OrdersViewModelContract.SideEffect.OrderCancelled -> onShowSnackbar("Order cancelled successfully")
                OrdersViewModelContract.SideEffect.ReviewSubmitted -> onShowSnackbar("Thank you for your review! 🎉")
            }
        }
    }

    // Start tracking if we came from checkout
    LaunchedEffect(initialTrackingOrderId) {
        initialTrackingOrderId?.let {
            viewModel.onAction(OrdersViewModelContract.Action.StartTracking(it))
        }
    }

    // Show tracking overlay or order list
    if (state.isTracking && state.activeOrder != null) {
        OrderTrackingScreen(
            state = state,
            onAction = viewModel::onAction,
            onBack = {
                viewModel.onAction(OrdersViewModelContract.Action.StopTracking)
            },
        )
    } else {
        OrderListScreen(
            state = state,
            onAction = viewModel::onAction,
            onNavigateBack = onNavigateBack,
        )
    }

    // Review dialog
    if (state.showReviewDialog && state.reviewOrderId != null) {
        ReviewDialog(
            orderId = state.reviewOrderId!!,
            isSubmitting = state.isSubmittingReview,
            onSubmit = { orderId, rating, note ->
                viewModel.onAction(OrdersViewModelContract.Action.SubmitReview(orderId, rating, note))
            },
            onDismiss = { viewModel.onAction(OrdersViewModelContract.Action.DismissReviewDialog) },
        )
    }
}

// ─── Order List Screen ───────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun OrderListScreen(
    state: OrdersViewModelContract.MyUiState,
    onAction: (OrdersViewModelContract.Action) -> Unit,
    onNavigateBack: () -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.orders_title), fontWeight = FontWeight.Bold) },
                navigationIcon = { },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White),
                modifier = Modifier.shadow(2.dp),
            )
        },
        containerColor = Color(0xFFFAFAFA),
    ) { paddingValues ->
        Column(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            // Tabs
            TabRow(
                selectedTabIndex = if (state.selectedTab == OrdersViewModelContract.OrderTab.CURRENT) 0 else 1,
                containerColor = Color.White,
                contentColor = BrandRed,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        modifier = Modifier.tabIndicatorOffset(tabPositions[if (state.selectedTab == OrdersViewModelContract.OrderTab.CURRENT) 0 else 1]),
                        color = BrandRed,
                    )
                },
            ) {
                Tab(
                    selected = state.selectedTab == OrdersViewModelContract.OrderTab.CURRENT,
                    onClick = { onAction(OrdersViewModelContract.Action.SelectTab(OrdersViewModelContract.OrderTab.CURRENT)) },
                    text = { Text(stringResource(R.string.tab_current), fontWeight = FontWeight.Bold) },
                )
                Tab(
                    selected = state.selectedTab == OrdersViewModelContract.OrderTab.PREVIOUS,
                    onClick = { onAction(OrdersViewModelContract.Action.SelectTab(OrdersViewModelContract.OrderTab.PREVIOUS)) },
                    text = { Text(stringResource(R.string.tab_previous), fontWeight = FontWeight.Bold) },
                )
            }

            if (state.isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = BrandRed)
                }
            } else if (state.displayedOrders.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(imageVector = Icons.Default.Receipt, contentDescription = null, modifier = Modifier.size(64.dp), tint = Color.LightGray)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(stringResource(R.string.no_orders), fontSize = 18.sp, color = Color.Gray, fontWeight = FontWeight.Medium)
                    }
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    items(state.displayedOrders, key = { it.id }) { order ->
                        OrderCard(
                            order = order,
                            onClick = { onAction(OrdersViewModelContract.Action.StartTracking(order.id)) },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun OrderCard(order: OrderDomain, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text("#${order.orderNumber}", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                StatusChip(order.status, order.statusLabel)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(order.createdAt, color = Color.Gray, fontSize = 12.sp)
                Text(stringResource(com.ibnalsham.app.ui.design.designSystem.R.string.currency_symbol) + " ${"%.3f".format(order.total)}", fontWeight = FontWeight.Bold, color = BrandRed, fontSize = 15.sp)
            }
            order.rating?.let { rating ->
                Spacer(modifier = Modifier.height(4.dp))
                Row {
                    for (i in 1..rating) {
                        Text("⭐", fontSize = 12.sp)
                    }
                }
            }
        }
    }
}

@Composable
private fun StatusChip(status: OrderStatus, label: String) {
    val (bgColor, textColor) = when (status) {
        OrderStatus.PENDING -> Color(0xFFFFF8E1) to Color(0xFFF57F17)
        OrderStatus.CONFIRMED -> Color(0xFFE3F2FD) to Color(0xFF1565C0)
        OrderStatus.PREPARING -> Color(0xFFE8F5E9) to Color(0xFF2E7D32)
        OrderStatus.READY -> Color(0xFFE0F7FA) to Color(0xFF00838F)
        OrderStatus.OUT_FOR_DELIVERY -> Color(0xFFFCE4EC) to Color(0xFFC62828)
        OrderStatus.DELIVERED -> Color(0xFFE8F5E9) to BrandGreen
        OrderStatus.COMPLETED -> Color(0xFFE8F5E9) to BrandGreen
        OrderStatus.CANCELLED -> Color(0xFFFFEBEE) to Color(0xFFC62828)
    }
    Surface(shape = RoundedCornerShape(20.dp), color = bgColor) {
        Text(label, modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp), color = textColor, fontSize = 12.sp, fontWeight = FontWeight.Bold)
    }
}

// ─── Order Tracking Screen ───────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun OrderTrackingScreen(
    state: OrdersViewModelContract.MyUiState,
    onAction: (OrdersViewModelContract.Action) -> Unit,
    onBack: () -> Unit,
) {
    val order = state.activeOrder ?: return

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.tracking_title), fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(
                            com.ibnalsham.app.ui.design.designSystem.R.string.back))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White),
            )
        },
        containerColor = Color(0xFFFAFAFA),
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            when (state.orderPhase) {
                OrderPhase.WAITING_CONFIRMATION -> WaitingConfirmationContent()
                OrderPhase.CONFIRMED_CAN_CANCEL -> ConfirmedCanCancelContent(
                    orderId = order.id,
                    isCancelling = state.isCancelling,
                    onCancel = { onAction(OrdersViewModelContract.Action.CancelOrder(order.id)) },
                )
                OrderPhase.PROCESSING -> ProcessingContent(estimatedTime = order.estimatedTime)
                OrderPhase.DELIVERY_CHECK -> DeliveryCheckContent(
                    orderId = order.id,
                    onConfirm = { onAction(OrdersViewModelContract.Action.ConfirmDelivery(order.id)) },
                )
                OrderPhase.REVIEW -> ReviewPromptContent()
                OrderPhase.COMPLETED -> CompletedContent(onBack = onBack)
                OrderPhase.NONE -> {
                    CircularProgressIndicator(color = BrandRed)
                }
            }
        }
    }
}

// ─── Phase Contents ──────────────────────────────────────────────────────────

@Composable
private fun WaitingConfirmationContent() {
    val infiniteTransition = rememberInfiniteTransition(label = "waiting")
    val pulse by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = EaseInOutCubic),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "pulse",
    )
    val rotate by infiniteTransition.animateFloat(
        initialValue = -10f,
        targetValue = 10f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = EaseInOutCubic),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "rotate",
    )

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        // Animated food emojis
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            Text("🥔", fontSize = (40 * pulse).sp, modifier = Modifier.rotate(rotate))
            Text("🌯", fontSize = (48 * pulse).sp, modifier = Modifier.rotate(-rotate))
            Text("🧆", fontSize = (40 * pulse).sp, modifier = Modifier.rotate(rotate))
        }

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            stringResource(R.string.phase_waiting),
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1E1E1E),
            textAlign = TextAlign.Center,
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            stringResource(R.string.phase_waiting_desc),
            fontSize = 15.sp,
            color = Color.Gray,
            textAlign = TextAlign.Center,
            lineHeight = 22.sp,
        )

        Spacer(modifier = Modifier.height(24.dp))
        LinearProgressIndicator(
            modifier = Modifier.fillMaxWidth(0.6f).height(4.dp),
            color = BrandOrange,
            trackColor = Color(0xFFE0E0E0),
        )
    }
}

@Composable
private fun ConfirmedCanCancelContent(
    orderId: Int,
    isCancelling: Boolean,
    onCancel: () -> Unit,
) {
    val infiniteTransition = rememberInfiniteTransition(label = "confirmed")
    val bounce by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = EaseInOutCubic),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "bounce",
    )

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text("✅", fontSize = (56 * bounce).sp)

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            stringResource(R.string.phase_confirmed),
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = BrandGreen,
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            stringResource(R.string.phase_confirmed_desc),
            fontSize = 15.sp,
            color = Color.Gray,
            textAlign = TextAlign.Center,
            lineHeight = 22.sp,
        )

        Spacer(modifier = Modifier.height(32.dp))

        OutlinedButton(
            onClick = onCancel,
            modifier = Modifier.fillMaxWidth(0.7f).height(48.dp),
            shape = RoundedCornerShape(24.dp),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFE53935)),
            border = ButtonDefaults.outlinedButtonBorder.copy(
                brush = Brush.horizontalGradient(listOf(Color(0xFFE53935), Color(0xFFE53935))),
            ),
            enabled = !isCancelling,
        ) {
            if (isCancelling) {
                CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp, color = Color(0xFFE53935))
                Spacer(modifier = Modifier.width(8.dp))
            }
            Text(stringResource(R.string.cancel_order), fontWeight = FontWeight.Bold, fontSize = 15.sp)
        }
    }
}

@Composable
private fun ProcessingContent(estimatedTime: Int?) {
    val infiniteTransition = rememberInfiniteTransition(label = "processing")
    val rotate by infiniteTransition.animateFloat(
        initialValue = -15f,
        targetValue = 15f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = EaseInOutCubic),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "rotate",
    )
    val pulse by infiniteTransition.animateFloat(
        initialValue = 0.9f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = EaseInOutCubic),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "pulse",
    )

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("👨‍🍳", fontSize = (44 * pulse).sp, modifier = Modifier.rotate(rotate))
            Text("🍕", fontSize = (36 * pulse).sp, modifier = Modifier.rotate(-rotate * 0.5f))
            Text("🔥", fontSize = (40 * pulse).sp, modifier = Modifier.rotate(rotate * 0.7f))
        }

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            stringResource(R.string.phase_processing),
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1E1E1E),
            textAlign = TextAlign.Center,
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            stringResource(R.string.phase_processing_desc),
            fontSize = 15.sp,
            color = Color.Gray,
            textAlign = TextAlign.Center,
        )

        if (estimatedTime != null) {
            Spacer(modifier = Modifier.height(20.dp))
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color(0xFFFFF3E0),
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text("⏱️", fontSize = 20.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        stringResource(R.string.estimated_time, estimatedTime),
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFE65100),
                        fontSize = 15.sp,
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
        LinearProgressIndicator(
            modifier = Modifier.fillMaxWidth(0.6f).height(4.dp),
            color = BrandOrange,
            trackColor = Color(0xFFE0E0E0),
        )
    }
}

@Composable
private fun DeliveryCheckContent(orderId: Int, onConfirm: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text("📦", fontSize = 64.sp)

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            stringResource(R.string.delivery_check_title),
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1E1E1E),
            textAlign = TextAlign.Center,
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            stringResource(R.string.delivery_check_desc),
            fontSize = 15.sp,
            color = Color.Gray,
            textAlign = TextAlign.Center,
        )

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = onConfirm,
            modifier = Modifier.fillMaxWidth(0.7f).height(52.dp),
            colors = ButtonDefaults.buttonColors(containerColor = BrandGreen),
            shape = RoundedCornerShape(26.dp),
        ) {
            Text(stringResource(R.string.confirm_received), fontWeight = FontWeight.Bold, fontSize = 16.sp)
        }
    }
}

@Composable
private fun ReviewPromptContent() {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text("🎉", fontSize = 64.sp)
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            stringResource(R.string.phase_delivered),
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = BrandGreen,
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            stringResource(R.string.rate_experience),
            color = Color.Gray,
            fontSize = 15.sp,
        )
        Spacer(modifier = Modifier.height(16.dp))
        CircularProgressIndicator(color = BrandRed, modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
    }
}

@Composable
private fun CompletedContent(onBack: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text("🎊", fontSize = 64.sp)
        Spacer(modifier = Modifier.height(24.dp))
        Text(stringResource(R.string.thank_you), fontSize = 26.sp, fontWeight = FontWeight.Bold, color = BrandGreen)
        Spacer(modifier = Modifier.height(8.dp))
        Text(stringResource(R.string.order_complete), color = Color.Gray, fontSize = 15.sp)
        Spacer(modifier = Modifier.height(32.dp))
        Button(
            onClick = onBack,
            modifier = Modifier.fillMaxWidth(0.7f).height(48.dp),
            colors = ButtonDefaults.buttonColors(containerColor = BrandRed),
            shape = RoundedCornerShape(24.dp),
        ) {
            Text(stringResource(R.string.back_to_orders), fontWeight = FontWeight.Bold)
        }
    }
}

// ─── Review Dialog ───────────────────────────────────────────────────────────

@Composable
private fun ReviewDialog(
    orderId: Int,
    isSubmitting: Boolean,
    onSubmit: (Int, Int, String) -> Unit,
    onDismiss: () -> Unit,
) {
    var rating by remember { mutableIntStateOf(0) }
    var note by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = Color.White,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(stringResource(R.string.rate_order), fontWeight = FontWeight.Bold, fontSize = 20.sp)
                Spacer(modifier = Modifier.height(8.dp))
                Text(stringResource(R.string.how_experience), color = Color.Gray, fontSize = 14.sp)

                Spacer(modifier = Modifier.height(20.dp))

                // Star rating
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    (1..5).forEach { star ->
                        Text(
                            text = if (star <= rating) "⭐" else "☆",
                            fontSize = 36.sp,
                            modifier = Modifier.clickable { rating = star },
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    placeholder = { Text(stringResource(R.string.order_notes_placeholder)) },
                    modifier = Modifier.fillMaxWidth().height(90.dp),
                    shape = RoundedCornerShape(12.dp),
                    maxLines = 3,
                )

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f).height(48.dp),
                        shape = RoundedCornerShape(24.dp),
                    ) {
                        Text(stringResource(R.string.skip))
                    }
                    Button(
                        onClick = {
                            if (rating > 0) onSubmit(orderId, rating, note)
                        },
                        modifier = Modifier.weight(1f).height(48.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = BrandRed),
                        shape = RoundedCornerShape(24.dp),
                        enabled = rating > 0 && !isSubmitting,
                    ) {
                        if (isSubmitting) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                        } else {
                            Text(stringResource(R.string.submit), fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}
