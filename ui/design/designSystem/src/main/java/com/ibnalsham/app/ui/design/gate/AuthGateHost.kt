package com.ibnalsham.app.ui.design.gate

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material.icons.rounded.LocalMall
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material.icons.rounded.NotificationsActive
import androidx.compose.material.icons.rounded.PersonOutline
import androidx.compose.material.icons.rounded.ReceiptLong
import androidx.compose.material.icons.rounded.StarOutline
import androidx.compose.material.icons.rounded.Wallet
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.ibnalsham.app.library.data.cache.GatedFeature
import com.ibnalsham.app.library.data.cache.SessionManager
import com.ibnalsham.app.ui.design.designSystem.R
import com.ibnalsham.app.ui.design.motion.enterFromBottom
import com.ibnalsham.app.ui.design.theme.AppTextStyles
import com.ibnalsham.app.ui.design.theme.AppTheme
import com.ibnalsham.app.ui.design.theme.Spacing

/**
 * Wraps the app and shows the sign-in sheet whenever a gate is raised.
 *
 * [authForm] supplies the actual sign-in UI — this module cannot depend on the
 * auth feature without a cycle, so the caller injects it. The form calls the
 * lambda it is handed once the user is signed in.
 *
 * The sheet is deliberately dismissible: a guest who declines keeps browsing
 * from exactly where they were, which is the whole reason for gating here
 * rather than redirecting to a full-screen login.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuthGateHost(
    sessionManager: SessionManager,
    modifier: Modifier = Modifier,
    authForm: @Composable (onSignedIn: () -> Unit) -> Unit,
    content: @Composable () -> Unit,
) {
    val controller = rememberAuthGateController(sessionManager)
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val colors = AppTheme.colors

    CompositionLocalProvider(LocalAuthGate provides controller) {
        Box(modifier = modifier) {
            content()

            val feature = controller.pending
            if (feature != null) {
                // Signing in from anywhere else (deep link, expiry recovery) should
                // also satisfy a gate that happens to be open.
                val session by sessionManager.session.collectAsState()
                LaunchedEffect(session) {
                    if (session.isSignedIn) controller.onSignedIn()
                }

                ModalBottomSheet(
                    onDismissRequest = controller::onDismissed,
                    sheetState = sheetState,
                    containerColor = colors.surface,
                    contentColor = colors.textPrimary,
                    // Material's default tonal elevation tints the sheet with the
                    // primary colour, which turns this surface pink.
                    tonalElevation = 0.dp,
                    shape = RoundedCornerShape(topStart = 30.dp, topEnd = 30.dp),
                    dragHandle = { SheetHandle() },
                ) {
                    GateSheetContent(
                        feature = feature,
                        authForm = { authForm(controller::onSignedIn) },
                    )
                }
            }
        }
    }
}

@Composable
private fun GateSheetContent(
    feature: GatedFeature,
    authForm: @Composable () -> Unit,
) {
    val colors = AppTheme.colors
    val copy = feature.copy()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = Spacing.gutter)
            .padding(bottom = Spacing.xxl),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier
                .size(72.dp)
                .clip(CircleShape)
                .background(colors.brand.copy(alpha = 0.10f))
                .enterFromBottom(index = 0, distance = 12.dp),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = copy.icon,
                contentDescription = null,
                tint = colors.brand,
                modifier = Modifier.size(34.dp),
            )
        }

        Spacer(Modifier.height(Spacing.lg))

        Text(
            text = stringResource(R.string.gate_eyebrow).uppercase(),
            style = AppTextStyles.eyebrow,
            color = colors.brand,
            modifier = Modifier.enterFromBottom(index = 1, distance = 12.dp),
        )

        Spacer(Modifier.height(Spacing.xs))

        Text(
            text = stringResource(copy.titleRes),
            style = MaterialTheme.typography.headlineSmall,
            color = colors.textPrimary,
            textAlign = TextAlign.Center,
            modifier = Modifier.enterFromBottom(index = 2, distance = 12.dp),
        )

        Spacer(Modifier.height(Spacing.sm))

        Text(
            text = stringResource(copy.messageRes),
            style = MaterialTheme.typography.bodyMedium,
            color = colors.textSecondary,
            textAlign = TextAlign.Center,
            modifier = Modifier.enterFromBottom(index = 3, distance = 12.dp),
        )

        Spacer(Modifier.height(Spacing.xxl))

        Box(modifier = Modifier.enterFromBottom(index = 4, distance = 16.dp)) {
            authForm()
        }
    }
}

@Composable
private fun SheetHandle() {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Spacer(Modifier.height(Spacing.md))
        Box(
            modifier = Modifier
                .width(44.dp)
                .height(5.dp)
                .clip(CircleShape)
                .background(AppTheme.colors.outline),
        )
        Spacer(Modifier.height(Spacing.xl))
    }
}

/** Icon plus copy shown for each gate, so the prompt explains itself. */
private data class GateCopy(
    val icon: ImageVector,
    val titleRes: Int,
    val messageRes: Int,
)

private fun GatedFeature.copy(): GateCopy = when (this) {
    GatedFeature.Favorites -> GateCopy(
        Icons.Rounded.FavoriteBorder,
        R.string.gate_favorites_title,
        R.string.gate_favorites_message,
    )

    GatedFeature.Orders -> GateCopy(
        Icons.Rounded.ReceiptLong,
        R.string.gate_orders_title,
        R.string.gate_orders_message,
    )

    GatedFeature.Profile -> GateCopy(
        Icons.Rounded.PersonOutline,
        R.string.gate_profile_title,
        R.string.gate_profile_message,
    )

    GatedFeature.Checkout -> GateCopy(
        Icons.Rounded.LocalMall,
        R.string.gate_checkout_title,
        R.string.gate_checkout_message,
    )

    GatedFeature.Notifications -> GateCopy(
        Icons.Rounded.NotificationsActive,
        R.string.gate_notifications_title,
        R.string.gate_notifications_message,
    )

    GatedFeature.Reviews -> GateCopy(
        Icons.Rounded.StarOutline,
        R.string.gate_reviews_title,
        R.string.gate_reviews_message,
    )

    GatedFeature.Addresses -> GateCopy(
        Icons.Rounded.LocationOn,
        R.string.gate_addresses_title,
        R.string.gate_addresses_message,
    )

    GatedFeature.Wallet -> GateCopy(
        Icons.Rounded.Wallet,
        R.string.gate_wallet_title,
        R.string.gate_wallet_message,
    )
}
