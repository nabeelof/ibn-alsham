package com.ibnalsham.app.ui.design.theme

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.Easing
import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.ui.unit.IntOffset

/**
 * One motion vocabulary for the whole app.
 *
 * Everything that moves picks a spec from here, so a press on the home screen
 * feels the same as a press in the cart. Springs are used for anything the user
 * directly caused (taps, drags, expansion) and tweens for ambient motion
 * (fades, shimmer, staggered reveals) where a bounce would read as noise.
 */
object Motion {

    /** Durations, in milliseconds. */
    const val DURATION_INSTANT = 90
    const val DURATION_FAST = 180
    const val DURATION_MEDIUM = 300
    const val DURATION_SLOW = 480
    const val DURATION_HERO = 700

    /** Delay between consecutive items in a staggered list reveal. */
    const val STAGGER_STEP = 45

    /** Items past this index all animate together — long lists should not crawl. */
    const val STAGGER_MAX_INDEX = 8

    /** Standard easing — accelerate out, settle in. Matches Material 3 emphasized. */
    val EasingStandard: Easing = CubicBezierEasing(0.2f, 0f, 0f, 1f)

    /** Entering elements: slow start, fast finish. */
    val EasingDecelerate: Easing = CubicBezierEasing(0.05f, 0.7f, 0.1f, 1f)

    /** Exiting elements. */
    val EasingAccelerate: Easing = CubicBezierEasing(0.3f, 0f, 0.8f, 0.15f)

    /** Press feedback and small toggles — tight, barely perceptible overshoot. */
    fun <T> snappy(): FiniteAnimationSpec<T> = spring(
        dampingRatio = 0.82f,
        stiffness = Spring.StiffnessMediumLow,
    )

    /** Cards settling into place, sheets, expansion. Visible but controlled bounce. */
    fun <T> bouncy(): FiniteAnimationSpec<T> = spring(
        dampingRatio = 0.68f,
        stiffness = Spring.StiffnessLow,
    )

    /** No overshoot at all — use where an overshoot would clip content. */
    fun <T> smooth(): FiniteAnimationSpec<T> = spring(
        dampingRatio = Spring.DampingRatioNoBouncy,
        stiffness = Spring.StiffnessMediumLow,
    )

    /** Offset-typed spring; the generic springs lose IntOffset's visibility threshold. */
    fun offsetSpring(): FiniteAnimationSpec<IntOffset> = spring(
        dampingRatio = 0.75f,
        stiffness = Spring.StiffnessLow,
        visibilityThreshold = IntOffset(1, 1),
    )

    fun <T> fadeIn(delayMillis: Int = 0): FiniteAnimationSpec<T> =
        tween(durationMillis = DURATION_MEDIUM, delayMillis = delayMillis, easing = EasingDecelerate)

    fun <T> fadeOut(): FiniteAnimationSpec<T> =
        tween(durationMillis = DURATION_FAST, easing = EasingAccelerate)

    /** Delay for the item at [index] in a staggered reveal. */
    fun staggerDelay(index: Int): Int = minOf(index, STAGGER_MAX_INDEX) * STAGGER_STEP
}
