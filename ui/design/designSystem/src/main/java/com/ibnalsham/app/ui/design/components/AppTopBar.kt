package com.ibnalsham.app.ui.design.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.res.stringResource
import com.ibnalsham.app.ui.design.designSystem.R
import com.ibnalsham.app.ui.design.theme.AppTheme
import com.ibnalsham.app.ui.design.theme.Motion
import com.ibnalsham.app.ui.design.theme.Spacing

/**
 * Top bar that materialises as the screen scrolls.
 *
 * [collapsedFraction] runs 0..1 — at 0 the bar is transparent so a hero image
 * shows through and the back button sits on the photo; at 1 it is a solid
 * surface with the title. Screens drive it from their scroll state, which is
 * what makes the product and menu headers feel continuous rather than snapping
 * between two layouts.
 */
@Composable
fun CollapsingTopBar(
    title: String,
    modifier: Modifier = Modifier,
    collapsedFraction: Float = 1f,
    onNavigateBack: (() -> Unit)? = null,
    actions: @Composable RowScope.() -> Unit = {},
) {
    val colors = AppTheme.colors
    val progress = collapsedFraction.coerceIn(0f, 1f)

    val background by animateColorAsState(
        targetValue = colors.surface.copy(alpha = progress),
        animationSpec = Motion.fadeIn(),
        label = "topBarBackground",
    )
    val hairlineAlpha by animateFloatAsState(
        targetValue = if (progress > 0.9f) 1f else 0f,
        animationSpec = Motion.fadeIn(),
        label = "topBarHairline",
    )
    val titleAlpha by animateFloatAsState(
        targetValue = if (progress > 0.55f) 1f else 0f,
        animationSpec = Motion.fadeIn(),
        label = "topBarTitle",
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(background),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .height(60.dp)
                .padding(horizontal = Spacing.md),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start,
        ) {
            if (onNavigateBack != null) {
                CircleIconButton(
                    icon = Icons.AutoMirrored.Rounded.ArrowBack,
                    contentDescription = stringResource(R.string.back),
                    onClick = onNavigateBack,
                    // Below the fold the bar is transparent, so the button needs
                    // its own chip to stay legible on the image behind it.
                    onImage = progress < 0.5f,
                )
                Spacer(Modifier.width(Spacing.sm))
            }

            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                color = colors.textPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .weight(1f)
                    .alpha(titleAlpha),
            )

            actions()
        }

        // Stands in for elevation — a real shadow would bleed onto the hero image
        // while the bar is still translucent.
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .height(1.dp)
                // Coerced: Color.copy throws outside 0..1, and an animation spec
                // change here would otherwise turn into a crash.
                .background(colors.outline.copy(alpha = hairlineAlpha.coerceIn(0f, 1f))),
        )
    }
}

/** Static top bar for screens without a hero. */
@Composable
fun AppTopBar(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    onNavigateBack: (() -> Unit)? = null,
    actions: @Composable RowScope.() -> Unit = {},
) {
    val colors = AppTheme.colors

    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(colors.background)
            .statusBarsPadding()
            .padding(horizontal = Spacing.md, vertical = Spacing.sm),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (onNavigateBack != null) {
            CircleIconButton(
                icon = Icons.AutoMirrored.Rounded.ArrowBack,
                contentDescription = stringResource(R.string.back),
                onClick = onNavigateBack,
            )
            Spacer(Modifier.width(Spacing.md))
        }

        androidx.compose.foundation.layout.Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.headlineSmall,
                color = colors.textPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.textSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }

        actions()
    }
}
