package com.ibnalsham.app.ui.feature.splash

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.ibnalsham.app.ui.design.components.PulsingDots
import com.ibnalsham.app.ui.design.theme.AppTheme
import com.ibnalsham.app.ui.design.theme.Motion
import com.ibnalsham.app.ui.design.theme.Spacing
import com.ibnalsham.app.ui.feature.splashimpl.R
import kotlinx.coroutines.delay

/**
 * Splash.
 *
 * The mark settles with an overshoot and the tagline follows a beat later, so
 * the first thing the app does has some life in it. Total dwell is unchanged.
 */
@Composable
fun SplashScreen(
    onSplashFinished: () -> Unit,
) {
    val colors = AppTheme.colors
    val markScale = remember { Animatable(0.6f) }
    val taglineAlpha = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        markScale.animateTo(targetValue = 1f, animationSpec = Motion.bouncy())
    }

    LaunchedEffect(Unit) {
        delay(TAGLINE_DELAY_MS)
        taglineAlpha.animateTo(1f, animationSpec = tween(Motion.DURATION_SLOW))
    }

    LaunchedEffect(Unit) {
        delay(TOTAL_DWELL_MS)
        onSplashFinished()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.heroGradient),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier
                    .size(132.dp)
                    .scale(markScale.value)
                    .clip(RoundedCornerShape(38.dp))
                    .background(Color.White.copy(alpha = 0.14f)),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = stringResource(R.string.app_name_ar),
                    style = MaterialTheme.typography.displaySmall,
                    color = colors.accent,
                    textAlign = TextAlign.Center,
                )
            }

            Spacer(Modifier.height(Spacing.xl))

            Text(
                text = stringResource(R.string.app_tagline),
                style = MaterialTheme.typography.bodyLarge,
                color = Color.White,
                textAlign = TextAlign.Center,
                modifier = Modifier.alpha(taglineAlpha.value),
            )

            Spacer(Modifier.height(Spacing.huge))

            Box(modifier = Modifier.alpha(taglineAlpha.value)) {
                PulsingDots()
            }
        }
    }
}

private const val TAGLINE_DELAY_MS = 350L
private const val TOTAL_DWELL_MS = 2_100L
