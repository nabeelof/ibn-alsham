package com.ibnalsham.app.ui.feature.orders

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.rounded.ReceiptLong
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
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
import com.ibnalsham.app.library.data.cache.GatedFeature
import com.ibnalsham.app.ui.design.components.AnimatedPrice
import com.ibnalsham.app.ui.design.components.AppCard
import com.ibnalsham.app.ui.design.components.AppTopBar
import com.ibnalsham.app.ui.design.components.SignInRequiredState
import com.ibnalsham.app.ui.design.components.SkeletonBlock
import com.ibnalsham.app.ui.design.components.StateView
import com.ibnalsham.app.ui.design.components.Tag
import com.ibnalsham.app.ui.design.components.jodPrice
import com.ibnalsham.app.ui.design.gate.LocalAuthGate
import com.ibnalsham.app.ui.design.motion.enterFromBottom
import com.ibnalsham.app.ui.design.theme.AppTextStyles
import com.ibnalsham.app.ui.design.theme.AppTheme
import com.ibnalsham.app.ui.design.theme.Motion
import com.ibnalsham.app.ui.design.theme.Spacing
import com.ibnalsham.app.ui.design.designSystem.R as DesignR
import com.ibnalsham.app.ui.feature.ordersimpl.R
import com.ibnalsham.app.feature.domain.OrderDomain
import com.ibnalsham.app.feature.domain.OrderStatus
import com.ibnalsham.app.feature.orders.presentation.viewModel.OrderPhase
import com.ibnalsham.app.feature.orders.presentation.viewModel.OrdersViewModel
import com.ibnalsham.app.feature.orders.presentation.viewModel.OrdersViewModelContract
import org.koin.androidx.compose.koinViewModel

// The live-tracking screens below still address these directly; they now point
// at the design-system palette instead of one-off hex values.
private val BrandRed = com.ibnalsham.app.ui.design.theme.Ember
private val BrandOrange = com.ibnalsham.app.ui.design.theme.Saffron
private val BrandGreen = com.ibnalsham.app.ui.design.theme.Basil

@Composable
fun OrdersRoute(
    viewModel: OrdersViewModel = koinViewModel(),
    initialTrackingOrderId: Int? = null,
    onNavigateToOrderDetails: (Int) -> Unit = {},
    onShowSnackbar: (String) -> Unit = {},
    onNavigateBack: () -> Unit = {},
    onNavigateToMenu: () -> Unit = {},
) {
    val state by viewModel.uiState.collectAsState()
    val gate = LocalAuthGate.current
    val cancelledMessage = stringResource(R.string.order_cancelled_message)
    val reviewThanksMessage = stringResource(R.string.review_thanks_message)

    LaunchedEffect(Unit) {
        viewModel.onAction(OrdersViewModelContract.Action.ViewReady)

        viewModel.sideEffects.collect { effect ->
            when (effect) {
                is OrdersViewModelContract.SideEffect.NavigateToOrderDetails -> onNavigateToOrderDetails(effect.orderId)
                is OrdersViewModelContract.SideEffect.ShowSnackbar -> onShowSnackbar(effect.message)
                OrdersViewModelContract.SideEffect.OrderCancelled -> onShowSnackbar(cancelledMessage)
                OrdersViewModelContract.SideEffect.ReviewSubmitted -> onShowSnackbar(reviewThanksMessage)
                OrdersViewModelContract.SideEffect.RequireSignIn -> gate.prompt(GatedFeature.Orders)
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
            onNavigateToMenu = onNavigateToMenu,
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

/**
 * Order history.
 *
 * This tab used to load unconditionally, so a guest tapping it saw a bare
 * error. It now shows the sign-in state, and signing in loads the list in
 * place without leaving the tab.
 */
@Composable
private fun OrderListScreen(
    state: OrdersViewModelContract.MyUiState,
    onAction: (OrdersViewModelContract.Action) -> Unit,
    onNavigateBack: () -> Unit,
    onNavigateToMenu: () -> Unit,
) {
    val colors = AppTheme.colors

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background),
    ) {
        AppTopBar(title = stringResource(R.string.orders_title))

        if (state.requiresLogin) {
            SignInRequiredState(
                title = stringResource(DesignR.string.gate_orders_title),
                message = stringResource(DesignR.string.gate_orders_message),
                onSignIn = { onAction(OrdersViewModelContract.Action.SignInClicked) },
                browseActionText = stringResource(DesignR.string.action_browse_menu),
                onBrowse = onNavigateToMenu,
            )
            return@Column
        }

        OrderTabs(
            selected = state.selectedTab,
            onSelect = { onAction(OrdersViewModelContract.Action.SelectTab(it)) },
        )

        when {
            state.isLoading -> Column(
                modifier = Modifier.padding(horizontal = Spacing.gutter, vertical = Spacing.md),
                verticalArrangement = Arrangement.spacedBy(Spacing.md),
            ) {
                repeat(4) {
                    SkeletonBlock(
                        modifier = Modifier.fillMaxWidth().height(96.dp),
                        shape = MaterialTheme.shapes.medium,
                    )
                }
            }

            state.displayedOrders.isEmpty() -> StateView(
                icon = Icons.Rounded.ReceiptLong,
                title = stringResource(DesignR.string.orders_empty_title),
                message = stringResource(DesignR.string.orders_empty_message),
                primaryActionText = stringResource(DesignR.string.action_browse_menu),
                onPrimaryAction = onNavigateToMenu,
                modifier = Modifier.fillMaxSize(),
            )

            else -> LazyColumn(
                contentPadding = PaddingValues(
                    start = Spacing.gutter,
                    end = Spacing.gutter,
                    top = Spacing.md,
                    bottom = Spacing.navBarClearance,
                ),
                verticalArrangement = Arrangement.spacedBy(Spacing.md),
            ) {
                itemsIndexed(state.displayedOrders, key = { _, order -> order.id }) { index, order ->
                    OrderCard(
                        order = order,
                        onClick = { onAction(OrdersViewModelContract.Action.StartTracking(order.id)) },
                        modifier = Modifier.enterFromBottom(index = index),
                    )
                }
            }
        }
    }
}

/** Segmented current/previous switch with a sliding pill. */
@Composable
private fun OrderTabs(
    selected: OrdersViewModelContract.OrderTab,
    onSelect: (OrdersViewModelContract.OrderTab) -> Unit,
) {
    val colors = AppTheme.colors
    val tabs = listOf(
        OrdersViewModelContract.OrderTab.CURRENT to stringResource(R.string.tab_current),
        OrdersViewModelContract.OrderTab.PREVIOUS to stringResource(R.string.tab_previous),
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = Spacing.gutter, vertical = Spacing.sm)
            .clip(CircleShape)
            .background(colors.surfaceSunken)
            .padding(Spacing.xs),
    ) {
        tabs.forEach { (tab, label) ->
            val isSelected = tab == selected
            val background by animateColorAsState(
                targetValue = if (isSelected) colors.surface else Color.Transparent,
                animationSpec = Motion.snappy(),
                label = "orderTabBackground",
            )
            val content by animateColorAsState(
                targetValue = if (isSelected) colors.brand else colors.textSecondary,
                animationSpec = Motion.snappy(),
                label = "orderTabContent",
            )

            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(CircleShape)
                    .background(background)
                    .clickable { onSelect(tab) }
                    .padding(vertical = Spacing.md),
                contentAlignment = Alignment.Center,
            ) {
                Text(text = label, style = MaterialTheme.typography.labelLarge, color = content)
            }
        }
    }
}

@Composable
private fun OrderCard(order: OrderDomain, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val colors = AppTheme.colors

    AppCard(modifier = modifier.fillMaxWidth(), onClick = onClick, elevation = 4.dp) {
        Column(modifier = Modifier.padding(Spacing.lg)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "#${order.orderNumber}",
                    style = MaterialTheme.typography.titleMedium,
                    color = colors.textPrimary,
                )
                StatusChip(order.status, order.statusLabel)
            }

            Spacer(modifier = Modifier.height(Spacing.sm))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = order.createdAt,
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.textTertiary,
                )
                Text(
                    text = jodPrice(order.total),
                    style = AppTextStyles.price,
                    color = colors.textPrimary,
                )
            }

            order.rating?.let { rating ->
                Spacer(modifier = Modifier.height(Spacing.sm))
                Row(horizontalArrangement = Arrangement.spacedBy(Spacing.xxs)) {
                    repeat(rating) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            tint = colors.accent,
                            modifier = Modifier.size(14.dp),
                        )
                    }
                }
            }
        }
    }
}

/** Status pill, coloured by what the status means rather than by brand. */
@Composable
private fun StatusChip(status: OrderStatus, label: String) {
    val colors = AppTheme.colors
    val (background, content) = when (status) {
        OrderStatus.PENDING -> colors.warningSoft to colors.warning
        OrderStatus.CONFIRMED,
        OrderStatus.PREPARING,
        OrderStatus.READY,
        OrderStatus.OUT_FOR_DELIVERY,
        -> colors.brand.copy(alpha = 0.12f) to colors.brand

        OrderStatus.DELIVERED, OrderStatus.COMPLETED -> colors.successSoft to colors.success
        OrderStatus.CANCELLED -> colors.dangerSoft to colors.danger
    }

    Tag(text = label, background = background, contentColor = content)
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
