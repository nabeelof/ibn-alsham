package com.ibnalsham.app.ui.design.components

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.LockPerson
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.WifiOff
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.ibnalsham.app.ui.design.designSystem.R
import com.ibnalsham.app.ui.design.motion.enterFromBottom
import com.ibnalsham.app.ui.design.theme.AppTheme
import com.ibnalsham.app.ui.design.theme.Motion
import com.ibnalsham.app.ui.design.theme.Spacing

/**
 * The one empty/error/guest state used across the app.
 *
 * The icon lands with a spring on first composition — without it a state screen
 * appearing after a spinner reads as a failure rather than a resolution.
 */
@Composable
fun StateView(
    icon: ImageVector,
    title: String,
    modifier: Modifier = Modifier,
    message: String? = null,
    primaryActionText: String? = null,
    onPrimaryAction: (() -> Unit)? = null,
    secondaryActionText: String? = null,
    onSecondaryAction: (() -> Unit)? = null,
) {
    val colors = AppTheme.colors
    var landed by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { landed = true }

    val iconScale by animateFloatAsState(
        targetValue = if (landed) 1f else 0.4f,
        animationSpec = Motion.bouncy(),
        label = "stateIconScale",
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = Spacing.xxxl, vertical = Spacing.xxxl),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Box(
            modifier = Modifier
                .size(96.dp)
                .scale(iconScale)
                .clip(CircleShape)
                .background(colors.brand.copy(alpha = 0.10f)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = colors.brand,
                modifier = Modifier.size(44.dp),
            )
        }

        Spacer(Modifier.height(Spacing.xxl))

        Text(
            text = title,
            style = MaterialTheme.typography.headlineSmall,
            color = colors.textPrimary,
            textAlign = TextAlign.Center,
            modifier = Modifier.enterFromBottom(index = 1),
        )

        if (message != null) {
            Spacer(Modifier.height(Spacing.sm))
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = colors.textSecondary,
                textAlign = TextAlign.Center,
                modifier = Modifier.enterFromBottom(index = 2),
            )
        }

        if (primaryActionText != null && onPrimaryAction != null) {
            Spacer(Modifier.height(Spacing.xxl))
            PrimaryButton(
                text = primaryActionText,
                onClick = onPrimaryAction,
                modifier = Modifier.enterFromBottom(index = 3),
            )
        }

        if (secondaryActionText != null && onSecondaryAction != null) {
            Spacer(Modifier.height(Spacing.md))
            TextAction(
                text = secondaryActionText,
                onClick = onSecondaryAction,
                color = colors.textSecondary,
                modifier = Modifier.enterFromBottom(index = 4),
            )
        }
    }
}

/**
 * What a guest sees on a screen that needs an account.
 *
 * This is the in-place half of the gating: reaching Favourites or Orders as a
 * guest lands here instead of bouncing straight to a sign-in screen, so the
 * user can see what they would get before deciding.
 */
@Composable
fun SignInRequiredState(
    title: String,
    message: String,
    onSignIn: () -> Unit,
    modifier: Modifier = Modifier,
    browseActionText: String? = null,
    onBrowse: (() -> Unit)? = null,
) {
    StateView(
        icon = Icons.Rounded.LockPerson,
        title = title,
        message = message,
        primaryActionText = stringResource(R.string.gate_sign_in),
        onPrimaryAction = onSignIn,
        secondaryActionText = browseActionText,
        onSecondaryAction = onBrowse,
        modifier = modifier.fillMaxSize(),
    )
}

/** Network / server failure with a retry. */
@Composable
fun ErrorState(
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
    title: String? = null,
    message: String? = null,
) {
    StateView(
        icon = Icons.Rounded.WifiOff,
        title = title ?: stringResource(R.string.error_generic_title),
        message = message ?: stringResource(R.string.error_generic_message),
        primaryActionText = stringResource(R.string.action_retry),
        onPrimaryAction = onRetry,
        modifier = modifier.fillMaxSize(),
    )
}

/** Full-screen loading that matches the theme instead of a bare spinner. */
@Composable
fun LoadingState(modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        PulsingDots()
    }
}

/** Three dots breathing in sequence — the app's loading signature. */
@Composable
fun PulsingDots(modifier: Modifier = Modifier) {
    val colors = AppTheme.colors
    val transition = rememberInfiniteTransition(label = "pulsingDots")

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        repeat(3) { index ->
            // Staggering the delay is what makes the row read as a wave.
            val scale by transition.animateFloat(
                initialValue = 0.5f,
                targetValue = 1f,
                animationSpec = infiniteRepeatable(
                    animation = tween(
                        durationMillis = 480,
                        delayMillis = index * 140,
                        easing = Motion.EasingStandard,
                    ),
                    repeatMode = RepeatMode.Reverse,
                ),
                label = "dotScale$index",
            )
            Box(
                modifier = Modifier
                    .size(11.dp)
                    .scale(scale)
                    .clip(CircleShape)
                    .background(colors.brand),
            )
        }
    }
}
