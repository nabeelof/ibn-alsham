package com.ibnalsham.app.ui.design.motion

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.clickable
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.ibnalsham.app.ui.design.theme.AppTheme
import com.ibnalsham.app.ui.design.theme.Motion
import kotlinx.coroutines.delay

/**
 * Shrinks the element while it is held.
 *
 * Attach this to anything tappable that is bigger than an icon — product cards,
 * category tiles, the checkout button. Pair it with [Modifier.clickable] using
 * the same [interactionSource] and `indication = null` so the scale replaces the
 * ripple rather than fighting it.
 */
fun Modifier.pressScale(
    interactionSource: MutableInteractionSource,
    pressedScale: Float = 0.965f,
): Modifier = composed {
    val pressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed) pressedScale else 1f,
        animationSpec = Motion.snappy(),
        label = "pressScale",
    )
    graphicsLayer {
        scaleX = scale
        scaleY = scale
    }
}

/**
 * Tappable with the press-scale response and no ripple.
 *
 * Convenience over wiring [pressScale] and [Modifier.clickable] to a shared
 * interaction source at every call site.
 */
fun Modifier.bouncyClickable(
    enabled: Boolean = true,
    pressedScale: Float = 0.965f,
    onClick: () -> Unit,
): Modifier = composed {
    val interactionSource = remember { MutableInteractionSource() }
    this
        .pressScale(interactionSource, pressedScale)
        .clickable(
            interactionSource = interactionSource,
            indication = null,
            enabled = enabled,
            onClick = onClick,
        )
}

/**
 * Fades and lifts the element in on first composition.
 *
 * [index] staggers siblings so a list assembles itself rather than snapping in
 * as one block. Delay is capped in [Motion.staggerDelay] so long lists stay
 * responsive.
 */
fun Modifier.enterFromBottom(
    index: Int = 0,
    distance: Dp = 24.dp,
    enabled: Boolean = true,
): Modifier = composed {
    if (!enabled) return@composed this

    var visible by remember { mutableStateOf(false) }
    val density = LocalDensity.current

    LaunchedEffect(Unit) {
        delay(Motion.staggerDelay(index).toLong())
        visible = true
    }

    val progress by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = Motion.fadeIn(),
        label = "enterFromBottom",
    )

    graphicsLayer {
        alpha = progress
        translationY = with(density) { distance.toPx() } * (1f - progress)
    }
}

/**
 * Parallax for a hero image inside a scrolling screen.
 *
 * [scrollOffsetPx] is the container's scroll position; the image drifts at
 * [rate] of that and scales up when the list is over-scrolled downward, which
 * is what makes a pulled-down header feel elastic instead of static.
 */
fun Modifier.parallaxHero(
    scrollOffsetPx: () -> Float,
    rate: Float = 0.45f,
): Modifier = graphicsLayer {
    val offset = scrollOffsetPx()
    translationY = offset * rate
    if (offset > 0f) {
        // Over-scrolled downward: grow from the top edge so no gap appears.
        val stretch = 1f + (offset / size.height).coerceAtMost(0.6f)
        scaleX = stretch
        scaleY = stretch
    }
}

/**
 * Sweeping highlight for skeleton placeholders.
 *
 * Draws over the element's own content with [BlendMode.SrcAtop], so it only
 * lights up the placeholder shape rather than a surrounding rectangle.
 */
fun Modifier.shimmer(enabled: Boolean = true): Modifier = composed {
    if (!enabled) return@composed this

    val transition = rememberInfiniteTransition(label = "shimmer")
    val progress by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1_400, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "shimmerProgress",
    )
    val highlight = if (AppTheme.colors.isDark) {
        Color.White.copy(alpha = 0.07f)
    } else {
        Color.White.copy(alpha = 0.85f)
    }

    drawWithContent {
        drawContent()
        // Sweep travels one full width past each edge so it never pops in place.
        val travel = size.width * 2f
        val start = -size.width + travel * progress
        drawRect(
            brush = Brush.linearGradient(
                colors = listOf(Color.Transparent, highlight, Color.Transparent),
                start = Offset(start, 0f),
                end = Offset(start + size.width * 0.7f, size.height),
            ),
            blendMode = BlendMode.SrcAtop,
        )
    }
}

/** Placeholder block: the sunken surface plus the shimmer sweep. */
fun Modifier.skeleton(shape: androidx.compose.ui.graphics.Shape): Modifier = composed {
    this
        .background(AppTheme.colors.surfaceSunken, shape)
        .shimmer()
}

/** Gentle pulse for badges and live-status dots. */
@Composable
fun rememberPulse(enabled: Boolean = true, min: Float = 0.85f, max: Float = 1.12f): Float {
    if (!enabled) return 1f
    val transition = rememberInfiniteTransition(label = "pulse")
    val scale by transition.animateFloat(
        initialValue = min,
        targetValue = max,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 900, easing = Motion.EasingStandard),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "pulseScale",
    )
    return scale
}
