package com.ibnalsham.app.ui.feature.more

import android.content.Intent
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material.icons.rounded.HeadsetMic
import androidx.compose.material.icons.rounded.Language
import androidx.compose.material.icons.rounded.Logout
import androidx.compose.material.icons.rounded.PersonOutline
import androidx.compose.material.icons.rounded.ReceiptLong
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.core.os.LocaleListCompat
import com.ibnalsham.app.feature.more.presentation.viewModel.MoreViewModel
import com.ibnalsham.app.feature.more.presentation.viewModel.MoreViewModelContract
import com.ibnalsham.app.ui.design.components.AppCard
import com.ibnalsham.app.ui.design.components.PrimaryButton
import com.ibnalsham.app.ui.design.components.SecondaryButton
import com.ibnalsham.app.ui.design.components.Tag
import com.ibnalsham.app.ui.design.components.TextAction
import com.ibnalsham.app.ui.design.gate.LocalAuthGate
import com.ibnalsham.app.ui.design.motion.bouncyClickable
import com.ibnalsham.app.ui.design.motion.enterFromBottom
import com.ibnalsham.app.ui.design.theme.AppTheme
import com.ibnalsham.app.ui.design.theme.Spacing
import com.ibnalsham.app.ui.design.designSystem.R as DesignR
import com.ibnalsham.app.ui.feature.moreimpl.R
import org.koin.androidx.compose.koinViewModel

@Composable
fun MoreRoute(
    viewModel: MoreViewModel = koinViewModel(),
    onNavigateToProfile: () -> Unit,
    onNavigateToFavorites: () -> Unit,
    onNavigateToOrders: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onLogoutSuccess: () -> Unit,
    onNavigateBack: () -> Unit = {},
) {
    val state by viewModel.uiState.collectAsState()
    val gate = LocalAuthGate.current
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.onAction(MoreViewModelContract.Action.ViewReady)

        viewModel.sideEffects.collect { effect ->
            when (effect) {
                MoreViewModelContract.SideEffect.NavigateToProfile -> onNavigateToProfile()
                MoreViewModelContract.SideEffect.NavigateToFavorites -> onNavigateToFavorites()
                MoreViewModelContract.SideEffect.NavigateToOrders -> onNavigateToOrders()
                MoreViewModelContract.SideEffect.NavigateToSplash -> onLogoutSuccess()
                MoreViewModelContract.SideEffect.NavigateToSettings -> onNavigateToSettings()
                // The view model decides a row is gated; the host owns the sheet.
                is MoreViewModelContract.SideEffect.RequireSignIn -> gate.prompt(effect.feature)
                is MoreViewModelContract.SideEffect.OpenUrl -> {
                    context.startActivity(Intent(Intent.ACTION_VIEW, android.net.Uri.parse(effect.url)))
                }
            }
        }
    }

    MoreScreen(state = state, onAction = viewModel::onAction)
}

/**
 * More / account hub.
 *
 * The header is the honest signal of session state: signed in it shows the
 * user's name and phone, as a guest it shows a sign-in card. Gated rows stay
 * visible either way — hiding them would leave a guest unable to discover what
 * an account is for — but tapping one raises the gate rather than dead-ending.
 */
@Composable
fun MoreScreen(
    state: MoreViewModelContract.MyUiState,
    onAction: (MoreViewModelContract.Action) -> Unit,
) {
    val colors = AppTheme.colors

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
            .verticalScroll(rememberScrollState()),
    ) {
        AccountHeader(
            isSignedIn = state.isSignedIn,
            name = state.userName,
            phone = state.userPhone,
            onProfileClick = { onAction(MoreViewModelContract.Action.ClickProfile) },
            onSignInClick = { onAction(MoreViewModelContract.Action.ClickSignIn) },
        )

        Spacer(Modifier.height(Spacing.xl))

        AppCard(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Spacing.gutter)
                .enterFromBottom(index = 1),
        ) {
            MenuRow(
                icon = Icons.Rounded.PersonOutline,
                title = stringResource(DesignR.string.profile_title),
                locked = !state.isSignedIn,
                onClick = { onAction(MoreViewModelContract.Action.ClickProfile) },
            )
            MenuRow(
                icon = Icons.Rounded.ReceiptLong,
                title = stringResource(DesignR.string.nav_orders),
                locked = !state.isSignedIn,
                onClick = { onAction(MoreViewModelContract.Action.ClickOrders) },
            )
            MenuRow(
                icon = Icons.Rounded.FavoriteBorder,
                title = stringResource(DesignR.string.favorites_title),
                locked = !state.isSignedIn,
                onClick = { onAction(MoreViewModelContract.Action.ClickFavorites) },
                showDivider = false,
            )
        }

        Spacer(Modifier.height(Spacing.md))

        AppCard(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Spacing.gutter)
                .enterFromBottom(index = 2),
        ) {
            MenuRow(
                icon = Icons.Rounded.Settings,
                title = stringResource(R.string.settings),
                onClick = { onAction(MoreViewModelContract.Action.ClickSettings) },
            )
            MenuRow(
                icon = Icons.Rounded.HeadsetMic,
                title = stringResource(R.string.contact_us),
                onClick = { onAction(MoreViewModelContract.Action.ClickContactUs) },
                showDivider = false,
            )
        }

        AnimatedVisibility(visible = state.isSignedIn, enter = fadeIn(), exit = fadeOut()) {
            Column {
                Spacer(Modifier.height(Spacing.xxl))
                SecondaryButton(
                    text = stringResource(R.string.logout),
                    onClick = { onAction(MoreViewModelContract.Action.Logout) },
                    icon = Icons.Rounded.Logout,
                    enabled = !state.isLoggingOut,
                    destructive = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = Spacing.gutter),
                )
            }
        }

        Spacer(Modifier.height(Spacing.navBarClearance))
    }
}

/** Signed-in identity, or the guest sign-in invitation. */
@Composable
private fun AccountHeader(
    isSignedIn: Boolean,
    name: String,
    phone: String,
    onProfileClick: () -> Unit,
    onSignInClick: () -> Unit,
) {
    val colors = AppTheme.colors

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(colors.heroGradient)
            .statusBarsPadding()
            .padding(horizontal = Spacing.gutter)
            .padding(top = Spacing.xxl, bottom = Spacing.xxxl),
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = stringResource(R.string.more_title),
                style = MaterialTheme.typography.displaySmall,
                color = Color.White,
                // Without an explicit width the title measured to its intrinsic
                // size and broke mid-word.
                modifier = Modifier.fillMaxWidth(),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )

            Spacer(Modifier.height(Spacing.xl))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(MaterialTheme.shapes.medium)
                    .background(Color.White.copy(alpha = 0.16f))
                    .bouncyClickable(
                        pressedScale = 0.98f,
                        onClick = if (isSignedIn) onProfileClick else onSignInClick,
                    )
                    .padding(Spacing.lg),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.22f)),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.Rounded.PersonOutline,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(28.dp),
                    )
                }

                Spacer(Modifier.width(Spacing.md))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = name.ifBlank { stringResource(DesignR.string.gate_guest_name) },
                        style = MaterialTheme.typography.titleLarge,
                        color = Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        text = phone.ifBlank { stringResource(DesignR.string.gate_guest_subtitle) },
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.85f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }

                if (isSignedIn) {
                    Icon(
                        imageVector = forwardChevron(),
                        contentDescription = null,
                        tint = Color.White.copy(alpha = 0.8f),
                    )
                } else {
                    PrimaryButton(
                        text = stringResource(DesignR.string.gate_sign_in),
                        onClick = onSignInClick,
                        compact = true,
                    )
                }
            }
        }
    }
}

@Composable
private fun MenuRow(
    icon: ImageVector,
    title: String,
    onClick: () -> Unit,
    locked: Boolean = false,
    showDivider: Boolean = true,
) {
    val colors = AppTheme.colors

    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .bouncyClickable(pressedScale = 0.99f, onClick = onClick)
                .padding(horizontal = Spacing.lg, vertical = Spacing.lg),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(RoundedCornerShape(Spacing.md))
                    .background(colors.brand.copy(alpha = 0.10f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(icon, contentDescription = null, tint = colors.brand, modifier = Modifier.size(19.dp))
            }

            Spacer(Modifier.width(Spacing.md))

            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                color = colors.textPrimary,
                modifier = Modifier.weight(1f),
            )

            // Marks the row as account-only so the gate is never a surprise.
            if (locked) {
                Tag(
                    text = stringResource(DesignR.string.gate_sign_in),
                    background = colors.surfaceSunken,
                    contentColor = colors.textTertiary,
                )
                Spacer(Modifier.width(Spacing.sm))
            }

            Icon(
                imageVector = forwardChevron(),
                contentDescription = null,
                tint = colors.textTertiary,
                modifier = Modifier.size(20.dp),
            )
        }

        if (showDivider) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 70.dp, end = Spacing.lg)
                    .height(1.dp)
                    .background(colors.outline),
            )
        }
    }
}

/** Chevron pointing the way navigation goes in the current layout direction. */
@Composable
private fun forwardChevron(): ImageVector =
    if (LocalLayoutDirection.current == LayoutDirection.Rtl) {
        Icons.AutoMirrored.Rounded.KeyboardArrowLeft
    } else {
        Icons.AutoMirrored.Rounded.KeyboardArrowRight
    }
