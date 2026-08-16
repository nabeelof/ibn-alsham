package com.ibnalsham.app.ui.feature.notifications

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.DeleteSweep
import androidx.compose.material.icons.rounded.DoneAll
import androidx.compose.material.icons.rounded.LocalOffer
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.NotificationsNone
import androidx.compose.material.icons.rounded.ReceiptLong
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
import com.ibnalsham.app.feature.domain.NotificationDomain
import com.ibnalsham.app.feature.domain.NotificationType
import com.ibnalsham.app.feature.notifications.presentation.viewModel.NotificationsViewModel
import com.ibnalsham.app.feature.notifications.presentation.viewModel.NotificationsViewModelContract
import com.ibnalsham.app.library.data.cache.GatedFeature
import com.ibnalsham.app.ui.design.components.AppCard
import com.ibnalsham.app.ui.design.components.AppTopBar
import com.ibnalsham.app.ui.design.components.CircleIconButton
import com.ibnalsham.app.ui.design.components.ErrorState
import com.ibnalsham.app.ui.design.components.SignInRequiredState
import com.ibnalsham.app.ui.design.components.SkeletonBlock
import com.ibnalsham.app.ui.design.components.StateView
import com.ibnalsham.app.ui.design.gate.LocalAuthGate
import com.ibnalsham.app.ui.design.motion.enterFromBottom
import com.ibnalsham.app.ui.design.theme.AppTheme
import com.ibnalsham.app.ui.design.theme.Motion
import com.ibnalsham.app.ui.design.theme.Spacing
import com.ibnalsham.app.ui.design.designSystem.R as DesignR
import org.koin.androidx.compose.koinViewModel

@Composable
fun NotificationsRoute(
    onNavigateToOrder: (Int) -> Unit,
    onNavigateToProduct: (Int) -> Unit,
    onNavigateBack: () -> Unit,
    onShowSnackbar: (String) -> Unit,
    viewModel: NotificationsViewModel = koinViewModel(),
) {
    val state by viewModel.uiState.collectAsState()
    val gate = LocalAuthGate.current

    LaunchedEffect(Unit) {
        viewModel.onAction(NotificationsViewModelContract.Action.ViewReady)
    }

    LaunchedEffect(Unit) {
        viewModel.sideEffects.collect { effect ->
            when (effect) {
                is NotificationsViewModelContract.SideEffect.NavigateToOrder -> onNavigateToOrder(effect.orderId)
                is NotificationsViewModelContract.SideEffect.NavigateToProduct -> onNavigateToProduct(effect.productId)
                NotificationsViewModelContract.SideEffect.RequireSignIn -> gate.prompt(GatedFeature.Notifications)
                is NotificationsViewModelContract.SideEffect.ShowSnackbar -> onShowSnackbar(effect.message)
            }
        }
    }

    NotificationsScreen(
        state = state,
        onAction = viewModel::onAction,
        onNavigateBack = onNavigateBack,
    )
}

@Composable
private fun NotificationsScreen(
    state: NotificationsViewModelContract.MyUiState,
    onAction: (NotificationsViewModelContract.Action) -> Unit,
    onNavigateBack: () -> Unit,
) {
    val colors = AppTheme.colors

    Column(modifier = Modifier.fillMaxSize().background(colors.background)) {
        AppTopBar(
            title = stringResource(DesignR.string.notifications_title),
            subtitle = if (state.hasUnread) {
                stringResource(DesignR.string.notifications_unread_count, state.unreadCount)
            } else {
                null
            },
            onNavigateBack = onNavigateBack,
            actions = {
                AnimatedVisibility(visible = state.hasUnread, enter = fadeIn(), exit = fadeOut()) {
                    CircleIconButton(
                        icon = Icons.Rounded.DoneAll,
                        contentDescription = stringResource(DesignR.string.notifications_mark_all_read),
                        onClick = { onAction(NotificationsViewModelContract.Action.MarkAllRead) },
                    )
                }
                AnimatedVisibility(
                    visible = state.notifications.isNotEmpty(),
                    enter = fadeIn(),
                    exit = fadeOut(),
                ) {
                    CircleIconButton(
                        icon = Icons.Rounded.DeleteSweep,
                        contentDescription = stringResource(DesignR.string.notifications_clear_all),
                        onClick = { onAction(NotificationsViewModelContract.Action.ClearAll) },
                    )
                }
            },
        )

        when {
            state.requiresLogin -> SignInRequiredState(
                title = stringResource(DesignR.string.gate_notifications_title),
                message = stringResource(DesignR.string.gate_notifications_message),
                onSignIn = { onAction(NotificationsViewModelContract.Action.SignInClicked) },
            )

            state.isLoading -> Column(
                modifier = Modifier.padding(horizontal = Spacing.gutter, vertical = Spacing.sm),
                verticalArrangement = Arrangement.spacedBy(Spacing.md),
            ) {
                repeat(5) {
                    SkeletonBlock(
                        modifier = Modifier.fillMaxWidth().height(84.dp),
                        shape = MaterialTheme.shapes.medium,
                    )
                }
            }

            state.error != null -> ErrorState(
                onRetry = { onAction(NotificationsViewModelContract.Action.Refresh) },
            )

            state.isEmpty -> StateView(
                icon = Icons.Rounded.NotificationsNone,
                title = stringResource(DesignR.string.notifications_empty),
                message = stringResource(DesignR.string.notifications_empty_message),
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
                verticalArrangement = Arrangement.spacedBy(Spacing.sm),
            ) {
                itemsIndexed(state.notifications, key = { _, item -> item.id }) { index, notification ->
                    NotificationCard(
                        notification = notification,
                        onClick = {
                            onAction(NotificationsViewModelContract.Action.NotificationClicked(notification.id))
                        },
                        modifier = Modifier.enterFromBottom(index = index),
                    )
                }
            }
        }
    }
}

/**
 * One notification.
 *
 * Unread rows carry a tinted surface and an accent dot; marking them read
 * animates the tint away rather than swapping the row out, so a bulk
 * "mark all read" reads as one gesture.
 */
@Composable
private fun NotificationCard(
    notification: NotificationDomain,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = AppTheme.colors

    val background by animateColorAsState(
        targetValue = if (notification.isRead) colors.surface else colors.brand.copy(alpha = 0.07f),
        animationSpec = Motion.fadeIn(),
        label = "notificationBackground",
    )

    AppCard(
        modifier = modifier.fillMaxWidth(),
        color = background,
        elevation = if (notification.isRead) 2.dp else 5.dp,
        onClick = onClick,
    ) {
        Row(modifier = Modifier.padding(Spacing.md), verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(colors.brand.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center,
            ) {
                androidx.compose.material3.Icon(
                    imageVector = when (notification.type) {
                        NotificationType.ORDER -> Icons.Rounded.ReceiptLong
                        NotificationType.PROMOTION -> Icons.Rounded.LocalOffer
                        NotificationType.GENERAL -> Icons.Rounded.Notifications
                    },
                    contentDescription = null,
                    tint = colors.brand,
                    modifier = Modifier.size(21.dp),
                )
            }

            Spacer(Modifier.width(Spacing.md))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = notification.title,
                    style = if (notification.isRead) {
                        MaterialTheme.typography.titleSmall
                    } else {
                        MaterialTheme.typography.titleMedium
                    },
                    color = colors.textPrimary,
                )
                Text(
                    text = notification.body,
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.textSecondary,
                )
                Spacer(Modifier.height(Spacing.xxs))
                Text(
                    text = notification.createdAtHuman,
                    style = MaterialTheme.typography.labelSmall,
                    color = colors.textTertiary,
                )
            }

            AnimatedVisibility(visible = !notification.isRead, enter = fadeIn(), exit = fadeOut()) {
                Box(
                    modifier = Modifier
                        .size(9.dp)
                        .clip(CircleShape)
                        .background(colors.brand),
                )
            }
        }
    }
}
