package com.ibnalsham.app.ui.design.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.ibnalsham.app.ui.design.motion.pressScale
import com.ibnalsham.app.ui.design.motion.shimmer
import com.ibnalsham.app.ui.design.theme.AppTextStyles
import com.ibnalsham.app.ui.design.theme.AppTheme
import com.ibnalsham.app.ui.design.theme.Spacing

/**
 * The app's card surface.
 *
 * Shadow is applied before the clip so the elevation reads on the warm
 * background; Material's default `Card` elevation disappears against it.
 */
@Composable
fun AppCard(
    modifier: Modifier = Modifier,
    shape: Shape = MaterialTheme.shapes.medium,
    color: Color? = null,
    elevation: Dp = 6.dp,
    bordered: Boolean = false,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    val colors = AppTheme.colors
    val interactionSource = remember { MutableInteractionSource() }

    androidx.compose.foundation.layout.Column(
        modifier = modifier
            .then(
                if (onClick != null) Modifier.pressScale(interactionSource) else Modifier,
            )
            .shadow(
                elevation = elevation,
                shape = shape,
                ambientColor = Color.Black.copy(alpha = 0.35f),
                spotColor = Color.Black.copy(alpha = 0.35f),
            )
            .clip(shape)
            .background(color ?: colors.surface)
            .then(
                if (bordered) Modifier.border(1.dp, colors.outline, shape) else Modifier,
            )
            .then(
                if (onClick != null) {
                    Modifier.clickable(
                        interactionSource = interactionSource,
                        indication = null,
                        onClick = onClick,
                    )
                } else {
                    Modifier
                },
            ),
        content = content,
    )
}

/**
 * Image with the standard scrim, placeholder and crop applied.
 *
 * Falls back to the first letter on the brand gradient when there is no image —
 * the catalogue has plenty of items without photography and a grey box next to
 * a photographed item looks broken.
 */
@Composable
fun FoodImage(
    url: String?,
    contentDescription: String?,
    fallbackText: String,
    modifier: Modifier = Modifier,
    scrim: Boolean = false,
    overlay: @Composable BoxScope.() -> Unit = {},
) {
    val colors = AppTheme.colors

    Box(modifier = modifier.background(colors.surfaceSunken)) {
        if (!url.isNullOrBlank()) {
            AsyncImage(
                model = url,
                contentDescription = contentDescription,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
            )
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(colors.heroGradient),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = fallbackText.take(1).uppercase(),
                    style = MaterialTheme.typography.displayMedium,
                    color = Color.White.copy(alpha = 0.85f),
                )
            }
        }

        if (scrim) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(colors.cardScrim),
            )
        }

        overlay()
    }
}

/** Pill label — discounts, "new", dietary tags, order status. */
@Composable
fun Tag(
    text: String,
    modifier: Modifier = Modifier,
    background: Color? = null,
    contentColor: Color? = null,
    icon: ImageVector? = null,
) {
    val colors = AppTheme.colors
    Row(
        modifier = modifier
            .clip(CircleShape)
            .background(background ?: colors.brand)
            .padding(horizontal = Spacing.sm, vertical = 3.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = contentColor ?: Color.White,
                modifier = Modifier.size(12.dp),
            )
            Spacer(Modifier.width(3.dp))
        }
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            color = contentColor ?: Color.White,
            maxLines = 1,
        )
    }
}

/**
 * Section title with an optional trailing action.
 *
 * [eyebrow] renders a small caps line above the title; it is what gives the
 * home screen its editorial rhythm between otherwise similar carousels.
 */
@Composable
fun SectionHeader(
    title: String,
    modifier: Modifier = Modifier,
    eyebrow: String? = null,
    actionText: String? = null,
    onActionClick: (() -> Unit)? = null,
    trailing: (@Composable RowScope.() -> Unit)? = null,
) {
    val colors = AppTheme.colors
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = Spacing.gutter, vertical = Spacing.sm),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        androidx.compose.foundation.layout.Column(modifier = Modifier.weight(1f)) {
            if (eyebrow != null) {
                Text(
                    text = eyebrow.uppercase(),
                    style = AppTextStyles.eyebrow,
                    color = colors.brand,
                )
                Spacer(Modifier.size(Spacing.xxs))
            }
            Text(
                text = title,
                style = MaterialTheme.typography.headlineSmall,
                color = colors.textPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }

        if (actionText != null && onActionClick != null) {
            TextAction(text = actionText, onClick = onActionClick)
        }
        trailing?.invoke(this)
    }
}

/** Hairline divider that respects the page gutter. */
@Composable
fun AppDivider(modifier: Modifier = Modifier, inset: Dp = Spacing.lg) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = inset)
            .height(1.dp)
            .background(AppTheme.colors.outline),
    )
}

/** Rounded rectangle placeholder used while content loads. */
@Composable
fun SkeletonBlock(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(Spacing.md),
) {
    Box(
        modifier = modifier
            .clip(shape)
            .background(AppTheme.colors.surfaceSunken)
            .shimmer(),
    )
}
