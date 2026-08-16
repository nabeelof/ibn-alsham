package com.ibnalsham.app.ui.design.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ShoppingBag
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.ibnalsham.app.ui.design.designSystem.R
import com.ibnalsham.app.ui.design.motion.bouncyClickable
import com.ibnalsham.app.ui.design.theme.AppTextStyles
import com.ibnalsham.app.ui.design.theme.AppTheme
import com.ibnalsham.app.ui.design.theme.Motion
import com.ibnalsham.app.ui.design.theme.Spacing

/**
 * Cart summary that slides up as soon as the cart stops being empty.
 *
 * The total counts up rather than snapping (see [AnimatedPrice]) so adding an
 * item visibly moves the number the user is deciding on.
 */
@Composable
fun CartFloatingBar(
    itemCount: Int,
    subtotal: Double,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = AppTheme.colors

    AnimatedVisibility(
        visible = itemCount > 0,
        enter = slideInVertically(Motion.offsetSpring()) { it } + fadeIn(Motion.fadeIn()),
        exit = slideOutVertically(Motion.offsetSpring()) { it } + fadeOut(Motion.fadeOut()),
        modifier = modifier,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = Spacing.gutter, vertical = Spacing.md)
                .shadow(
                    elevation = 16.dp,
                    shape = CircleShape,
                    ambientColor = colors.brand.copy(alpha = 0.5f),
                    spotColor = colors.brand.copy(alpha = 0.5f),
                )
                .clip(CircleShape)
                .background(Brush.horizontalGradient(listOf(colors.brandDeep, colors.brand)))
                .bouncyClickable(pressedScale = 0.98f, onClick = onClick)
                .padding(horizontal = Spacing.md, vertical = Spacing.md),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.18f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Rounded.ShoppingBag,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(20.dp),
                )
            }

            Spacer(Modifier.width(Spacing.md))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.view_cart),
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White,
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    AnimatedCounter(
                        value = itemCount,
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White.copy(alpha = 0.85f),
                    )
                    Spacer(Modifier.width(Spacing.xs))
                    Text(
                        text = stringResource(R.string.cart_items_suffix),
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White.copy(alpha = 0.85f),
                    )
                }
            }

            AnimatedPrice(
                value = subtotal,
                style = AppTextStyles.price,
                color = Color.White,
                modifier = Modifier.padding(end = Spacing.sm),
            )
        }
    }
}

/** Spacer that keeps scrollable content clear of the floating cart bar. */
@Composable
fun CartBarSpacer(visible: Boolean) {
    AnimatedVisibility(visible = visible, enter = fadeIn(), exit = fadeOut()) {
        Spacer(Modifier.height(96.dp))
    }
}
