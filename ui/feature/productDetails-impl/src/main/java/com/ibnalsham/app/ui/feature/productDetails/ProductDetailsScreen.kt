package com.ibnalsham.app.ui.feature.productDetails

import android.content.Intent
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material.icons.rounded.ShoppingBag
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.ibnalsham.app.feature.domain.OptionDomain
import com.ibnalsham.app.feature.domain.OptionGroupDomain
import com.ibnalsham.app.feature.productDetails.presentation.viewModel.ProductDetailsViewModel
import com.ibnalsham.app.feature.productDetails.presentation.viewModel.ProductDetailsViewModelContract
import com.ibnalsham.app.library.data.cache.GatedFeature
import com.ibnalsham.app.ui.design.components.AnimatedPrice
import com.ibnalsham.app.ui.design.components.CircleIconButton
import com.ibnalsham.app.ui.design.components.CollapsingTopBar
import com.ibnalsham.app.ui.design.components.FavoriteHeart
import com.ibnalsham.app.ui.design.components.FoodImage
import com.ibnalsham.app.ui.design.components.LoadingState
import com.ibnalsham.app.ui.design.components.PrimaryButton
import com.ibnalsham.app.ui.design.components.QuantityStepper
import com.ibnalsham.app.ui.design.components.Tag
import com.ibnalsham.app.ui.design.components.jodPrice
import com.ibnalsham.app.ui.design.components.jodPriceSigned
import com.ibnalsham.app.ui.design.gate.LocalAuthGate
import com.ibnalsham.app.ui.design.motion.enterFromBottom
import com.ibnalsham.app.ui.design.motion.parallaxHero
import com.ibnalsham.app.ui.design.theme.AppTextStyles
import com.ibnalsham.app.ui.design.theme.AppTheme
import com.ibnalsham.app.ui.design.theme.Motion
import com.ibnalsham.app.ui.design.theme.Spacing
import com.ibnalsham.app.ui.design.designSystem.R as DesignR
import com.ibnalsham.app.ui.feature.productDetailsimpl.R
import org.koin.androidx.compose.koinViewModel

@Composable
fun ProductDetailsRoute(
    productId: Int,
    viewModel: ProductDetailsViewModel = koinViewModel(),
    onNavigateBack: () -> Unit,
    onShowSnackbar: (String) -> Unit,
) {
    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val gate = LocalAuthGate.current
    val shareTemplate = stringResource(DesignR.string.product_share_message)

    LaunchedEffect(productId) {
        viewModel.onAction(ProductDetailsViewModelContract.Action.ViewReady(productId))
    }

    LaunchedEffect(Unit) {
        viewModel.sideEffects.collect { effect ->
            when (effect) {
                ProductDetailsViewModelContract.SideEffect.NavigateBack -> onNavigateBack()
                is ProductDetailsViewModelContract.SideEffect.ShowSnackbar -> onShowSnackbar(effect.message)

                // Favouriting needs an account. Raise the sheet and, once signed
                // in, apply the tap the user actually made.
                ProductDetailsViewModelContract.SideEffect.RequireSignIn -> gate.require(GatedFeature.Favorites) {
                    viewModel.onAction(ProductDetailsViewModelContract.Action.ToggleFavorite)
                }

                is ProductDetailsViewModelContract.SideEffect.ShareLink -> {
                    val intent = Intent(Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        putExtra(Intent.EXTRA_TEXT, shareTemplate.format(effect.title, effect.url))
                    }
                    context.startActivity(Intent.createChooser(intent, effect.title))
                }
            }
        }
    }

    ProductDetailsScreen(
        state = state,
        onAction = viewModel::onAction,
        onNavigateBack = onNavigateBack,
    )
}

/**
 * Product details.
 *
 * Photo-led: the image runs edge to edge under the status bar with a parallax
 * drift, and the content sheet rides over it. The top bar stays transparent
 * until the sheet reaches it, then materialises with the product name.
 */
@Composable
fun ProductDetailsScreen(
    state: ProductDetailsViewModelContract.MyUiState,
    onAction: (ProductDetailsViewModelContract.Action) -> Unit,
    onNavigateBack: () -> Unit,
) {
    val colors = AppTheme.colors
    val scrollState = rememberScrollState()

    val collapsedFraction by remember {
        derivedStateOf { (scrollState.value / HERO_HEIGHT_PX).coerceIn(0f, 1f) }
    }

    Box(modifier = Modifier.fillMaxSize().background(colors.background)) {
        if (state.isLoading) {
            LoadingState()
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState),
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1f)
                        // Negative scroll offset makes the image drift slower than
                        // the sheet, which is what sells the depth.
                        .parallaxHero(scrollOffsetPx = { -scrollState.value.toFloat() }),
                ) {
                    FoodImage(
                        url = state.images.firstOrNull()?.url,
                        contentDescription = state.name,
                        fallbackText = state.name,
                        modifier = Modifier.fillMaxSize(),
                    )
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        // Pulls the sheet up over the bottom of the photo.
                        .offset(y = (-28).dp)
                        .clip(RoundedCornerShape(topStart = 30.dp, topEnd = 30.dp))
                        .background(colors.surface)
                        .padding(Spacing.xxl)
                        .padding(bottom = ACTION_BAR_CLEARANCE),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top,
                    ) {
                        Text(
                            text = state.name,
                            style = MaterialTheme.typography.headlineMedium,
                            color = colors.textPrimary,
                            modifier = Modifier
                                .weight(1f)
                                .enterFromBottom(index = 0),
                        )

                        Spacer(Modifier.width(Spacing.lg))

                        Column(
                            horizontalAlignment = Alignment.End,
                            modifier = Modifier.enterFromBottom(index = 1),
                        ) {
                            Text(
                                text = jodPrice(state.effectivePrice),
                                style = AppTextStyles.priceLarge,
                                color = colors.brand,
                            )
                            if (state.hasDiscount) {
                                Text(
                                    text = jodPrice(state.basePrice),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = colors.textTertiary,
                                    textDecoration = TextDecoration.LineThrough,
                                )
                                if (state.discountPercentage > 0) {
                                    Spacer(Modifier.height(Spacing.xs))
                                    Tag(
                                        text = stringResource(
                                            DesignR.string.offer_percent_off,
                                            state.discountPercentage,
                                        ),
                                    )
                                }
                            }
                        }
                    }

                    if (state.description.isNotBlank()) {
                        Spacer(Modifier.height(Spacing.lg))
                        Text(
                            text = state.description,
                            style = MaterialTheme.typography.bodyLarge,
                            color = colors.textSecondary,
                            modifier = Modifier.enterFromBottom(index = 2),
                        )
                    }

                    if (state.optionGroups.isNotEmpty()) {
                        Spacer(Modifier.height(Spacing.xxl))
                        Text(
                            text = stringResource(R.string.customize_order),
                            style = MaterialTheme.typography.headlineSmall,
                            color = colors.textPrimary,
                            modifier = Modifier.enterFromBottom(index = 3),
                        )

                        state.optionGroups.forEachIndexed { index, group ->
                            OptionGroupSection(
                                group = group,
                                selectedOptionIds = state.selectedOptionIds,
                                isUnsatisfied = group.id in state.unsatisfiedGroupIds,
                                onToggle = { optionId ->
                                    onAction(
                                        ProductDetailsViewModelContract.Action.ToggleOption(group.id, optionId),
                                    )
                                },
                                modifier = Modifier.enterFromBottom(index = index + 4),
                            )
                        }
                    }
                }
            }
        }

        CollapsingTopBar(
            title = state.name,
            collapsedFraction = collapsedFraction,
            onNavigateBack = onNavigateBack,
            actions = {
                CircleIconButton(
                    icon = Icons.Rounded.Share,
                    contentDescription = stringResource(DesignR.string.product_share),
                    onClick = { onAction(ProductDetailsViewModelContract.Action.ShareProduct) },
                    onImage = collapsedFraction < 0.5f,
                )
                Spacer(Modifier.width(Spacing.xs))
                Box(
                    modifier = Modifier
                        .clip(androidx.compose.foundation.shape.CircleShape)
                        .background(
                            if (collapsedFraction < 0.5f) {
                                Color.Black.copy(alpha = 0.35f)
                            } else {
                                colors.surfaceSunken
                            },
                        ),
                ) {
                    FavoriteHeart(
                        isFavorite = state.isFavorite,
                        onClick = { onAction(ProductDetailsViewModelContract.Action.ToggleFavorite) },
                        size = 44.dp,
                        elevated = false,
                    )
                }
            },
            modifier = Modifier.align(Alignment.TopCenter),
        )

        if (!state.isLoading) {
            BottomActionBar(
                state = state,
                onAction = onAction,
                modifier = Modifier.align(Alignment.BottomCenter),
            )
        }
    }
}

/**
 * One add-on group: radio when a single choice is allowed, checkboxes otherwise.
 * The subtitle spells out the rule the admin configured (required, max, min),
 * and turns red once the user tries to add to cart without satisfying it.
 */
@Composable
private fun OptionGroupSection(
    group: OptionGroupDomain,
    selectedOptionIds: Set<Int>,
    isUnsatisfied: Boolean,
    onToggle: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = AppTheme.colors

    Column(modifier = modifier.padding(top = Spacing.xl)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = group.name,
                style = MaterialTheme.typography.titleMedium,
                color = colors.textPrimary,
            )
            Spacer(Modifier.width(Spacing.sm))
            Tag(
                text = stringResource(
                    if (group.isRequired) DesignR.string.product_required else DesignR.string.product_optional,
                ),
                background = if (group.isRequired) colors.dangerSoft else colors.surfaceSunken,
                contentColor = if (group.isRequired) colors.danger else colors.textSecondary,
            )
        }

        groupRule(group)?.let { rule ->
            val ruleColor by animateColorAsState(
                targetValue = if (isUnsatisfied) colors.danger else colors.textTertiary,
                animationSpec = Motion.snappy(),
                label = "optionRuleColor",
            )
            Text(
                text = rule,
                style = MaterialTheme.typography.bodySmall,
                color = ruleColor,
                modifier = Modifier.padding(top = Spacing.xxs),
            )
        }

        Spacer(Modifier.height(Spacing.sm))

        group.options.forEach { option ->
            OptionRow(
                option = option,
                isSingleChoice = group.isSingleChoice,
                isSelected = option.id in selectedOptionIds,
                onToggle = { onToggle(option.id) },
            )
        }
    }
}

@Composable
private fun groupRule(group: OptionGroupDomain): String? = when {
    group.isSingleChoice -> stringResource(DesignR.string.product_choose_one)
    group.maxSelections != null -> stringResource(DesignR.string.product_choose_up_to, group.maxSelections!!)
    group.minSelections > 0 -> stringResource(DesignR.string.product_choose_at_least, group.minSelections)
    else -> null
}

@Composable
private fun OptionRow(
    option: OptionDomain,
    isSingleChoice: Boolean,
    isSelected: Boolean,
    onToggle: () -> Unit,
) {
    val colors = AppTheme.colors
    val background by animateColorAsState(
        targetValue = if (isSelected) colors.brand.copy(alpha = 0.07f) else Color.Transparent,
        animationSpec = Motion.snappy(),
        label = "optionBackground",
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.small)
            .background(background)
            .selectable(
                selected = isSelected,
                enabled = option.isUserChangeable,
                onClick = onToggle,
            )
            .padding(vertical = Spacing.md, horizontal = Spacing.sm),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (isSingleChoice) {
            RadioButton(
                selected = isSelected,
                onClick = null,
                enabled = option.isUserChangeable,
                colors = RadioButtonDefaults.colors(selectedColor = colors.brand),
            )
        } else {
            Checkbox(
                checked = isSelected,
                onCheckedChange = null,
                enabled = option.isUserChangeable,
                colors = CheckboxDefaults.colors(checkedColor = colors.brand),
            )
        }

        Spacer(Modifier.width(Spacing.md))
        Text(
            text = option.name,
            style = MaterialTheme.typography.bodyLarge,
            color = colors.textPrimary,
            modifier = Modifier.weight(1f),
        )

        if (option.isFree) {
            Text(
                text = stringResource(DesignR.string.product_free),
                style = MaterialTheme.typography.labelMedium,
                color = colors.success,
            )
        } else {
            Text(
                text = jodPriceSigned(option.price),
                style = MaterialTheme.typography.labelLarge,
                color = colors.brand,
            )
        }
    }
}

/** Quantity plus the live total, floating over the content. */
@Composable
private fun BottomActionBar(
    state: ProductDetailsViewModelContract.MyUiState,
    onAction: (ProductDetailsViewModelContract.Action) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = AppTheme.colors

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(topStart = 30.dp, topEnd = 30.dp))
            .background(colors.surface)
            .padding(horizontal = Spacing.gutter, vertical = Spacing.lg)
            .navigationBarsPadding(),
        horizontalArrangement = Arrangement.spacedBy(Spacing.md),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        QuantityStepper(
            // Always at least one: this is a configurator, not a cart line.
            count = state.quantity,
            onIncrease = { onAction(ProductDetailsViewModelContract.Action.IncrementQuantity(MAX_QUANTITY)) },
            onDecrease = { onAction(ProductDetailsViewModelContract.Action.DecrementQuantity) },
        )

        PrimaryButton(
            text = jodPrice(state.calculatedTotal),
            onClick = { onAction(ProductDetailsViewModelContract.Action.AddToCart) },
            enabled = state.isAddToCartEnabled,
            loading = state.isAddingToCart,
            icon = Icons.Rounded.ShoppingBag,
            modifier = Modifier.weight(1f),
        )
    }
}

private const val MAX_QUANTITY = 99

/** Roughly the hero height in pixels; only used to drive the top-bar fade. */
private const val HERO_HEIGHT_PX = 700f

private val ACTION_BAR_CLEARANCE = 120.dp
