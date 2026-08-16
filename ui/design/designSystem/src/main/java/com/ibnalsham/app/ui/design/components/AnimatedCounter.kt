package com.ibnalsham.app.ui.design.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import com.ibnalsham.app.ui.design.theme.AppTextStyles
import com.ibnalsham.app.ui.design.theme.AppTheme
import com.ibnalsham.app.ui.design.theme.Motion
import kotlin.math.roundToInt

/**
 * Integer that rolls up or down when it changes.
 *
 * Direction follows the change, so incrementing a cart count slides the new
 * digit up from below and decrementing slides it down — the movement reads as
 * the same number changing rather than two unrelated numbers swapping.
 */
@Composable
fun AnimatedCounter(
    value: Int,
    modifier: Modifier = Modifier,
    style: TextStyle = MaterialTheme.typography.titleMedium,
    color: Color = AppTheme.colors.textPrimary,
) {
    AnimatedContent(
        targetState = value,
        transitionSpec = {
            val goingUp = targetState > initialState
            val enter = slideInVertically(Motion.bouncy()) { height ->
                if (goingUp) height else -height
            } + fadeIn(Motion.fadeIn())
            val exit = slideOutVertically(Motion.smooth()) { height ->
                if (goingUp) -height else height
            } + fadeOut(Motion.fadeOut())
            (enter togetherWith exit).using(androidx.compose.animation.SizeTransform(clip = false))
        },
        modifier = modifier,
        label = "animatedCounter",
    ) { target ->
        Text(text = target.toString(), style = style, color = color, textAlign = TextAlign.Center)
    }
}

/**
 * Price that counts up to its new value instead of jumping.
 *
 * Used for cart and checkout totals, where a jumping figure makes it hard to
 * tell that a change was the result of the action just taken.
 */
@Composable
fun AnimatedPrice(
    value: Double,
    modifier: Modifier = Modifier,
    style: TextStyle = AppTextStyles.price,
    color: Color = AppTheme.colors.brand,
    animate: Boolean = true,
) {
    val target = if (animate) {
        // Animate in fils so the two decimal places move smoothly.
        val animated by animateFloatAsState(
            targetValue = (value * 1000).toFloat(),
            animationSpec = Motion.fadeIn(),
            label = "animatedPrice",
        )
        animated.roundToInt() / 1000.0
    } else {
        value
    }

    Text(text = jodPrice(target), style = style, color = color, modifier = modifier)
}
