package com.ibnalsham.app.ui.design.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.ibnalsham.app.feature.domain.ProductDomain
import com.ibnalsham.app.ui.design.designSystem.R
import com.ibnalsham.app.ui.design.motion.bouncyClickable
import com.ibnalsham.app.ui.design.theme.AppTextStyles
import com.ibnalsham.app.ui.design.theme.AppTheme
import com.ibnalsham.app.ui.design.theme.Motion
import com.ibnalsham.app.ui.design.theme.Spacing

/**
 * Tall card for horizontal carousels.
 *
 * The image dominates by design — the appetite-driven direction puts the photo
 * first and lets the text sit under it, rather than boxing a small thumbnail.
 */
@Composable
fun ProductTile(
    product: ProductDomain,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    cartCount: Int = 0,
    onAddToCart: () -> Unit = {},
    onIncrease: () -> Unit = onAddToCart,
    onDecrease: () -> Unit = {},
    onToggleFavorite: (() -> Unit)? = null,
) {
    val colors = AppTheme.colors

    AppCard(
        // No intrinsic width: carousels pass [ProductTileWidth], grids pass a weight.
        modifier = modifier,
        shape = MaterialTheme.shapes.medium,
        onClick = onClick,
    ) {
        Box {
            FoodImage(
                url = product.image,
                contentDescription = product.name,
                fallbackText = product.name,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1.05f),
            )

            // Discount flag, top-start so it never collides with the heart.
            if (product.hasDiscount) {
                Tag(
                    text = stringResource(R.string.offer_percent_off, product.savedPercentage),
                    background = colors.brand,
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(Spacing.sm),
                )
            }

            if (onToggleFavorite != null) {
                FavoriteHeart(
                    isFavorite = product.isFavorite,
                    onClick = onToggleFavorite,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(Spacing.xs),
                )
            }

            if (!product.isAvailable) {
                SoldOutVeil(modifier = Modifier.matchParentSize())
            }
        }

        Column(modifier = Modifier.padding(Spacing.md)) {
            Text(
                text = product.name,
                style = MaterialTheme.typography.titleSmall,
                color = colors.textPrimary,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.heightIn(min = 40.dp),
            )

            Spacer(Modifier.height(Spacing.sm))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = jodPrice(product.effectivePrice),
                        style = AppTextStyles.priceSmall,
                        color = colors.brand,
                    )
                    if (product.hasDiscount) {
                        Text(
                            text = jodPrice(product.price),
                            style = MaterialTheme.typography.labelSmall,
                            color = colors.textTertiary,
                            textDecoration = TextDecoration.LineThrough,
                        )
                    }
                }

                QuantityStepper(
                    count = cartCount,
                    onIncrease = onIncrease,
                    onDecrease = onDecrease,
                    onAdd = onAddToCart,
                    compact = true,
                )
            }
        }
    }
}

/**
 * Wide row card for vertical lists (menu, favourites, search results).
 *
 * Same information as [ProductTile] but laid out for scanning down a list, with
 * the description visible since there is horizontal room for it.
 */
@Composable
fun ProductRow(
    product: ProductDomain,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    cartCount: Int = 0,
    onAddToCart: () -> Unit = {},
    onIncrease: () -> Unit = onAddToCart,
    onDecrease: () -> Unit = {},
    onToggleFavorite: (() -> Unit)? = null,
) {
    val colors = AppTheme.colors

    AppCard(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        onClick = onClick,
        elevation = 4.dp,
    ) {
        Row(
            modifier = Modifier.padding(Spacing.md),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box {
                FoodImage(
                    url = product.image,
                    contentDescription = product.name,
                    fallbackText = product.name,
                    modifier = Modifier
                        .size(92.dp)
                        .clip(MaterialTheme.shapes.small),
                )
                if (product.hasDiscount) {
                    Tag(
                        text = "-${product.savedPercentage}%",
                        background = colors.brand,
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(Spacing.xs),
                    )
                }
                if (!product.isAvailable) {
                    SoldOutVeil(
                        modifier = Modifier
                            .matchParentSize()
                            .clip(MaterialTheme.shapes.small),
                    )
                }
            }

            Spacer(Modifier.width(Spacing.md))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.Top) {
                    Text(
                        text = product.name,
                        style = MaterialTheme.typography.titleMedium,
                        color = colors.textPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f),
                    )
                    if (onToggleFavorite != null) {
                        FavoriteHeart(
                            isFavorite = product.isFavorite,
                            onClick = onToggleFavorite,
                            size = 32.dp,
                            elevated = false,
                        )
                    }
                }

                if (!product.description.isNullOrBlank()) {
                    Text(
                        text = product.description.orEmpty(),
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.textSecondary,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                }

                Spacer(Modifier.height(Spacing.sm))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = jodPrice(product.effectivePrice),
                            style = AppTextStyles.price,
                            color = colors.textPrimary,
                        )
                        if (product.hasDiscount) {
                            Spacer(Modifier.width(Spacing.sm))
                            Text(
                                text = jodPrice(product.price),
                                style = MaterialTheme.typography.bodySmall,
                                color = colors.textTertiary,
                                textDecoration = TextDecoration.LineThrough,
                            )
                        }
                    }

                    QuantityStepper(
                        count = cartCount,
                        onIncrease = onIncrease,
                        onDecrease = onDecrease,
                        onAdd = onAddToCart,
                    )
                }
            }
        }
    }
}

/**
 * Heart toggle with a pop on activation.
 *
 * The scale punch is what sells the toggle — a colour swap alone reads as a
 * rendering glitch at this size.
 */
@Composable
fun FavoriteHeart(
    isFavorite: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    size: androidx.compose.ui.unit.Dp = 36.dp,
    elevated: Boolean = true,
) {
    val colors = AppTheme.colors
    val scale by animateFloatAsState(
        targetValue = if (isFavorite) 1.14f else 1f,
        animationSpec = Motion.bouncy(),
        label = "favoriteScale",
    )

    Box(
        modifier = modifier
            .size(size)
            .bouncyClickable(pressedScale = 0.8f, onClick = onClick)
            .then(
                if (elevated) {
                    Modifier
                        .clip(CircleShape)
                        .background(colors.surface.copy(alpha = 0.92f))
                } else {
                    Modifier
                },
            ),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = if (isFavorite) Icons.Rounded.Favorite else Icons.Rounded.FavoriteBorder,
            contentDescription = stringResource(
                if (isFavorite) R.string.product_remove_from_favorites else R.string.product_add_to_favorites,
            ),
            tint = if (isFavorite) colors.brand else colors.textSecondary,
            modifier = Modifier
                .size(size * 0.55f)
                .scale(scale),
        )
    }
}

/** Dim + label over an unavailable item. */
@Composable
private fun SoldOutVeil(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.background(Color.Black.copy(alpha = 0.55f)),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = stringResource(R.string.product_sold_out),
            style = MaterialTheme.typography.labelMedium,
            color = Color.White,
        )
    }
}

/** Loading placeholder shaped like [ProductTile]. */
@Composable
fun ProductTileSkeleton(modifier: Modifier = Modifier) {
    AppCard(modifier = modifier, elevation = 2.dp) {
        SkeletonBlock(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1.05f),
            shape = RoundedCornerShape(0.dp),
        )
        Column(modifier = Modifier.padding(Spacing.md)) {
            SkeletonBlock(modifier = Modifier.fillMaxWidth().height(14.dp))
            Spacer(Modifier.height(Spacing.sm))
            SkeletonBlock(modifier = Modifier.width(80.dp).height(14.dp))
        }
    }
}

/** Loading placeholder shaped like [ProductRow]. */
@Composable
fun ProductRowSkeleton(modifier: Modifier = Modifier) {
    AppCard(modifier = modifier.fillMaxWidth(), elevation = 2.dp) {
        Row(modifier = Modifier.padding(Spacing.md), verticalAlignment = Alignment.CenterVertically) {
            SkeletonBlock(modifier = Modifier.size(92.dp), shape = MaterialTheme.shapes.small)
            Spacer(Modifier.width(Spacing.md))
            Column(modifier = Modifier.weight(1f)) {
                SkeletonBlock(modifier = Modifier.fillMaxWidth().height(16.dp))
                Spacer(Modifier.height(Spacing.sm))
                SkeletonBlock(modifier = Modifier.fillMaxWidth(0.7f).height(12.dp))
                Spacer(Modifier.height(Spacing.md))
                SkeletonBlock(modifier = Modifier.width(70.dp).height(18.dp))
            }
        }
    }
}

/** Width a [ProductTile] takes inside a horizontal carousel. */
val ProductTileWidth = 172.dp
