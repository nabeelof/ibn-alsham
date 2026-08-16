package com.ibnalsham.app.ui.design.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Remove
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.ibnalsham.app.ui.design.motion.pressScale
import com.ibnalsham.app.ui.design.theme.AppTheme
import com.ibnalsham.app.ui.design.theme.Motion
import com.ibnalsham.app.ui.design.theme.Spacing

private val ButtonHeight = 56.dp
private val ButtonHeightCompact = 44.dp

/**
 * Primary call to action.
 *
 * Filled with the brand gradient rather than a flat colour so it still reads as
 * the most important thing on a screen dominated by food photography.
 */
@Composable
fun PrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    loading: Boolean = false,
    icon: ImageVector? = null,
    compact: Boolean = false,
) {
    val colors = AppTheme.colors
    val interactionSource = remember { MutableInteractionSource() }
    val active = enabled && !loading

    val contentAlpha by animateFloatAsState(
        targetValue = if (active) 1f else 0.55f,
        animationSpec = Motion.snappy(),
        label = "buttonAlpha",
    )

    Box(
        modifier = modifier
            .height(if (compact) ButtonHeightCompact else ButtonHeight)
            .pressScale(interactionSource, pressedScale = 0.97f)
            .clip(CircleShape)
            .background(
                brush = if (active) {
                    Brush.horizontalGradient(listOf(colors.brand, colors.brandBright))
                } else {
                    Brush.horizontalGradient(listOf(colors.outline, colors.outline))
                },
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                enabled = active,
                onClick = onClick,
            ),
        contentAlignment = Alignment.Center,
    ) {
        // Crossfading keeps the button width steady while the spinner swaps in.
        AnimatedVisibility(
            visible = loading,
            enter = fadeIn(Motion.fadeIn()) + scaleIn(Motion.bouncy(), initialScale = 0.6f),
            exit = fadeOut(Motion.fadeOut()) + scaleOut(Motion.fadeOut(), targetScale = 0.6f),
        ) {
            CircularProgressIndicator(
                modifier = Modifier.size(22.dp),
                color = colors.onBrand,
                strokeWidth = 2.5.dp,
            )
        }

        AnimatedVisibility(
            visible = !loading,
            enter = fadeIn(Motion.fadeIn()),
            exit = fadeOut(Motion.fadeOut()),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier
                    .padding(horizontal = Spacing.xxl)
                    .alpha(contentAlpha),
            ) {
                if (icon != null) {
                    Icon(icon, contentDescription = null, tint = colors.onBrand, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(Spacing.sm))
                }
                Text(
                    text = text,
                    style = MaterialTheme.typography.titleMedium,
                    color = colors.onBrand,
                )
            }
        }
    }
}

/** Secondary action — outlined, sits next to a [PrimaryButton] without competing. */
@Composable
fun SecondaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    icon: ImageVector? = null,
    compact: Boolean = false,
    destructive: Boolean = false,
) {
    val colors = AppTheme.colors
    val interactionSource = remember { MutableInteractionSource() }
    val tint = if (destructive) colors.danger else colors.brand

    Row(
        modifier = modifier
            .height(if (compact) ButtonHeightCompact else ButtonHeight)
            .pressScale(interactionSource, pressedScale = 0.97f)
            .clip(CircleShape)
            .border(BorderStroke(1.5.dp, tint.copy(alpha = if (enabled) 0.5f else 0.2f)), CircleShape)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                enabled = enabled,
                onClick = onClick,
            )
            .padding(horizontal = Spacing.xxl),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
    ) {
        if (icon != null) {
            Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(Spacing.sm))
        }
        Text(
            text = text,
            style = MaterialTheme.typography.titleMedium,
            color = tint.copy(alpha = if (enabled) 1f else 0.4f),
        )
    }
}

/** Low-emphasis text action. */
@Composable
fun TextAction(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    color: Color? = null,
) {
    val interactionSource = remember { MutableInteractionSource() }
    Text(
        text = text,
        style = MaterialTheme.typography.labelLarge,
        color = color ?: AppTheme.colors.brand,
        modifier = modifier
            .pressScale(interactionSource, pressedScale = 0.94f)
            .clip(RoundedCornerShape(Spacing.sm))
            .clickable(interactionSource = interactionSource, indication = null, onClick = onClick)
            .padding(horizontal = Spacing.sm, vertical = Spacing.xs),
    )
}

/**
 * Circular icon button used in top bars and over imagery.
 *
 * [onImage] switches to a translucent dark chip so the icon stays legible when
 * it sits directly on a photo.
 */
@Composable
fun CircleIconButton(
    icon: ImageVector,
    contentDescription: String?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    onImage: Boolean = false,
    tint: Color? = null,
    size: androidx.compose.ui.unit.Dp = 44.dp,
    badgeCount: Int = 0,
) {
    val colors = AppTheme.colors
    val interactionSource = remember { MutableInteractionSource() }
    val background = if (onImage) Color.Black.copy(alpha = 0.35f) else colors.surfaceSunken
    val iconTint = tint ?: if (onImage) Color.White else colors.textPrimary

    Box(modifier = modifier.size(size)) {
        Box(
            modifier = Modifier
                .size(size)
                .pressScale(interactionSource, pressedScale = 0.88f)
                .clip(CircleShape)
                .background(background)
                .clickable(
                    interactionSource = interactionSource,
                    indication = null,
                    onClick = onClick,
                ),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = contentDescription,
                tint = iconTint,
                modifier = Modifier.size(size * 0.46f),
            )
        }

        CountBadge(
            count = badgeCount,
            modifier = Modifier.align(Alignment.TopEnd),
        )
    }
}

/** Animated numeric badge. Renders nothing at zero and pops in on first item. */
@Composable
fun CountBadge(
    count: Int,
    modifier: Modifier = Modifier,
    color: Color? = null,
) {
    val colors = AppTheme.colors
    AnimatedVisibility(
        visible = count > 0,
        enter = scaleIn(Motion.bouncy(), initialScale = 0.3f) + fadeIn(Motion.fadeIn()),
        exit = scaleOut(Motion.fadeOut(), targetScale = 0.3f) + fadeOut(Motion.fadeOut()),
        modifier = modifier,
    ) {
        Box(
            modifier = Modifier
                .defaultMinSize(minWidth = 18.dp, minHeight = 18.dp)
                .clip(CircleShape)
                .background(color ?: colors.brand)
                .border(2.dp, colors.surface, CircleShape)
                .padding(horizontal = 5.dp),
            contentAlignment = Alignment.Center,
        ) {
            AnimatedCounter(
                value = count,
                style = com.ibnalsham.app.ui.design.theme.AppTextStyles.badge,
                color = Color.White,
            )
        }
    }
}

/**
 * Quantity control that morphs between a single "+" and a full stepper.
 *
 * The morph is the point: adding the first item should feel like the button
 * grew into a counter rather than being replaced by a different control.
 */
@Composable
fun QuantityStepper(
    count: Int,
    onIncrease: () -> Unit,
    onDecrease: () -> Unit,
    modifier: Modifier = Modifier,
    onAdd: () -> Unit = onIncrease,
    compact: Boolean = false,
) {
    val colors = AppTheme.colors
    val height = if (compact) 34.dp else 40.dp
    val shape = RoundedCornerShape(height / 2)

    val containerColor by animateColorAsState(
        targetValue = if (count > 0) colors.surfaceSunken else colors.brand,
        animationSpec = Motion.snappy(),
        label = "stepperContainer",
    )

    Row(
        modifier = modifier
            .height(height)
            .clip(shape)
            .background(containerColor),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        AnimatedVisibility(
            visible = count > 0,
            enter = fadeIn(Motion.fadeIn()) + androidx.compose.animation.expandHorizontally(Motion.bouncy()),
            exit = fadeOut(Motion.fadeOut()) + androidx.compose.animation.shrinkHorizontally(Motion.smooth()),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                StepperIcon(
                    icon = Icons.Rounded.Remove,
                    description = "Decrease",
                    tint = colors.brand,
                    size = height,
                    onClick = onDecrease,
                )
                AnimatedCounter(
                    value = count,
                    style = MaterialTheme.typography.titleMedium,
                    color = colors.brand,
                    modifier = Modifier.defaultMinSize(minWidth = 22.dp),
                )
            }
        }

        StepperIcon(
            icon = Icons.Rounded.Add,
            description = "Add",
            tint = if (count > 0) colors.brand else colors.onBrand,
            size = height,
            onClick = { if (count > 0) onIncrease() else onAdd() },
        )
    }
}

@Composable
private fun StepperIcon(
    icon: ImageVector,
    description: String,
    tint: Color,
    size: androidx.compose.ui.unit.Dp,
    onClick: () -> Unit,
) {
    val interactionSource = remember { MutableInteractionSource() }
    Box(
        modifier = Modifier
            .size(size)
            .pressScale(interactionSource, pressedScale = 0.82f)
            .clickable(interactionSource = interactionSource, indication = null, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(icon, contentDescription = description, tint = tint, modifier = Modifier.size(size * 0.45f))
    }
}

/** Full-width variant used at the bottom of sheets and forms. */
@Composable
fun FullWidthButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    loading: Boolean = false,
    icon: ImageVector? = null,
) {
    PrimaryButton(
        text = text,
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        enabled = enabled,
        loading = loading,
        icon = icon,
    )
}
