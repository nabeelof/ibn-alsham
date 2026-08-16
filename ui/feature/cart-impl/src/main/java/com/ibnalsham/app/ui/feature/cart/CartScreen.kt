package com.ibnalsham.app.ui.feature.cart

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.ShoppingBag
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.ibnalsham.app.feature.cart.presentation.viewModel.CartItemDomain
import com.ibnalsham.app.feature.cart.presentation.viewModel.CartViewModel
import com.ibnalsham.app.feature.cart.presentation.viewModel.CartViewModelContract
import com.ibnalsham.app.feature.domain.OptionDomain
import com.ibnalsham.app.feature.domain.OptionGroupDomain
import com.ibnalsham.app.ui.design.components.AnimatedPrice
import com.ibnalsham.app.ui.design.components.AppCard
import com.ibnalsham.app.ui.design.components.AppTopBar
import com.ibnalsham.app.ui.design.components.CircleIconButton
import com.ibnalsham.app.ui.design.components.FoodImage
import com.ibnalsham.app.ui.design.components.FullWidthButton
import com.ibnalsham.app.ui.design.components.QuantityStepper
import com.ibnalsham.app.ui.design.components.SecondaryButton
import com.ibnalsham.app.ui.design.components.StateView
import com.ibnalsham.app.ui.design.components.Tag
import com.ibnalsham.app.ui.design.components.TextAction
import com.ibnalsham.app.ui.design.components.jodPrice
import com.ibnalsham.app.ui.design.components.jodPriceSigned
import com.ibnalsham.app.ui.design.motion.bouncyClickable
import com.ibnalsham.app.ui.design.motion.enterFromBottom
import com.ibnalsham.app.ui.design.theme.AppTextStyles
import com.ibnalsham.app.ui.design.theme.AppTheme
import com.ibnalsham.app.ui.design.theme.Motion
import com.ibnalsham.app.ui.design.theme.Spacing
import com.ibnalsham.app.ui.design.designSystem.R as DesignR
import com.ibnalsham.app.ui.feature.cartimpl.R
import org.koin.androidx.compose.koinViewModel

@Composable
fun CartRoute(
    viewModel: CartViewModel = koinViewModel(),
    onNavigateToCheckout: () -> Unit,
    onNavigateToMenu: () -> Unit,
    onNavigateBack: () -> Unit = {},
    onShowSnackbar: (String) -> Unit = {},
) {
    val state by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.onAction(CartViewModelContract.Action.ViewReady)
    }

    LaunchedEffect(Unit) {
        viewModel.sideEffects.collect { effect ->
            when (effect) {
                CartViewModelContract.SideEffect.NavigateToCheckout -> onNavigateToCheckout()
                CartViewModelContract.SideEffect.NavigateToMenu -> onNavigateToMenu()
                is CartViewModelContract.SideEffect.ShowSnackbar -> onShowSnackbar(effect.message)
            }
        }
    }

    CartScreen(state = state, onAction = viewModel::onAction, onNavigateBack = onNavigateBack)

    if (state.editingItem != null) {
        EditAddonsSheet(state = state, onAction = viewModel::onAction)
    }
}

/**
 * Cart.
 *
 * The checkout button is the last step a guest can take without an account —
 * the host wraps [CartViewModelContract.Action.ProceedToCheckout] in the sign-in
 * gate, so tapping it as a guest raises the sheet and then continues straight
 * into checkout with the cart intact.
 */
@Composable
fun CartScreen(
    state: CartViewModelContract.MyUiState,
    onAction: (CartViewModelContract.Action) -> Unit,
    onNavigateBack: () -> Unit = {},
) {
    val colors = AppTheme.colors

    Box(modifier = Modifier.fillMaxSize().background(colors.background)) {
        Column(modifier = Modifier.fillMaxSize()) {
            AppTopBar(
                title = stringResource(R.string.cart_title),
                subtitle = if (state.itemCount > 0) {
                    stringResource(R.string.items_count, state.itemCount)
                } else {
                    null
                },
                onNavigateBack = onNavigateBack,
                actions = {
                    AnimatedVisibility(visible = state.items.isNotEmpty(), enter = fadeIn(), exit = fadeOut()) {
                        TextAction(
                            text = stringResource(R.string.clear),
                            onClick = { onAction(CartViewModelContract.Action.ClearCart) },
                            color = colors.danger,
                        )
                    }
                },
            )

            if (state.isEmpty) {
                StateView(
                    icon = Icons.Rounded.ShoppingBag,
                    title = stringResource(R.string.empty_cart_title),
                    message = stringResource(R.string.empty_cart_desc),
                    primaryActionText = stringResource(R.string.start_shopping),
                    onPrimaryAction = { onAction(CartViewModelContract.Action.ContinueShopping) },
                    modifier = Modifier.fillMaxSize(),
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(
                        start = Spacing.gutter,
                        end = Spacing.gutter,
                        top = Spacing.sm,
                        bottom = SUMMARY_CLEARANCE,
                    ),
                    verticalArrangement = Arrangement.spacedBy(Spacing.md),
                ) {
                    itemsIndexed(state.items, key = { _, item -> item.id }) { index, item ->
                        CartItemCard(
                            item = item,
                            onIncrease = { onAction(CartViewModelContract.Action.IncreaseQuantity(item.id)) },
                            onDecrease = { onAction(CartViewModelContract.Action.DecreaseQuantity(item.id)) },
                            onRemove = { onAction(CartViewModelContract.Action.RemoveItem(item.id)) },
                            onEditAddons = { onAction(CartViewModelContract.Action.StartEditingOptions(item.id)) },
                            modifier = Modifier.enterFromBottom(index = index),
                        )
                    }
                }
            }
        }

        AnimatedVisibility(
            visible = !state.isEmpty,
            enter = slideInVertically(Motion.offsetSpring()) { it } + fadeIn(Motion.fadeIn()),
            exit = shrinkVertically(Motion.smooth()) + fadeOut(Motion.fadeOut()),
            modifier = Modifier.align(Alignment.BottomCenter),
        ) {
            CartSummaryBar(
                subtotal = state.subtotal,
                onCheckout = { onAction(CartViewModelContract.Action.ProceedToCheckout) },
            )
        }
    }
}

@Composable
private fun CartItemCard(
    item: CartItemDomain,
    onIncrease: () -> Unit,
    onDecrease: () -> Unit,
    onRemove: () -> Unit,
    onEditAddons: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = AppTheme.colors

    AppCard(modifier = modifier.fillMaxWidth(), elevation = 4.dp) {
        Row(modifier = Modifier.padding(Spacing.md)) {
            FoodImage(
                url = item.image,
                contentDescription = item.name,
                fallbackText = item.name,
                modifier = Modifier
                    .size(84.dp)
                    .clip(MaterialTheme.shapes.small),
            )

            Spacer(Modifier.width(Spacing.md))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.Top) {
                    Text(
                        text = item.name,
                        style = MaterialTheme.typography.titleMedium,
                        color = colors.textPrimary,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f),
                    )
                    CircleIconButton(
                        icon = Icons.Rounded.DeleteOutline,
                        contentDescription = stringResource(DesignR.string.remove),
                        onClick = onRemove,
                        size = 32.dp,
                        tint = colors.textTertiary,
                    )
                }

                if (item.selectedOptions.isNotEmpty()) {
                    Text(
                        text = item.optionsLabel,
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.textSecondary,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                }

                if (item.canEditOptions) {
                    Spacer(Modifier.height(Spacing.xs))
                    Tag(
                        text = stringResource(DesignR.string.cart_edit_addons),
                        icon = Icons.Rounded.Tune,
                        background = colors.surfaceSunken,
                        contentColor = colors.textSecondary,
                        modifier = Modifier.bouncyClickable(pressedScale = 0.92f, onClick = onEditAddons),
                    )
                }

                Spacer(Modifier.height(Spacing.md))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        // Unit total already folds in the selected add-ons.
                        text = jodPrice(item.unitTotal),
                        style = AppTextStyles.price,
                        color = colors.textPrimary,
                    )
                    QuantityStepper(
                        count = item.quantity,
                        onIncrease = onIncrease,
                        onDecrease = onDecrease,
                        compact = true,
                    )
                }
            }
        }
    }
}

/** Total plus checkout, floating over the list. */
@Composable
private fun CartSummaryBar(subtotal: Double, onCheckout: () -> Unit) {
    val colors = AppTheme.colors

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(topStart = 30.dp, topEnd = 30.dp))
            .background(colors.surface)
            .padding(horizontal = Spacing.gutter, vertical = Spacing.xl)
            .navigationBarsPadding(),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = stringResource(R.string.total),
                style = MaterialTheme.typography.bodyLarge,
                color = colors.textSecondary,
            )
            AnimatedPrice(
                value = subtotal,
                style = AppTextStyles.priceLarge,
                color = colors.textPrimary,
            )
        }

        Spacer(Modifier.height(Spacing.lg))

        FullWidthButton(
            text = stringResource(R.string.proceed_checkout),
            onClick = onCheckout,
        )
    }
}

/**
 * Bottom sheet for changing the add-ons on an existing cart line.
 * Confirming merges the line with an identical one if the selection collides.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EditAddonsSheet(
    state: CartViewModelContract.MyUiState,
    onAction: (CartViewModelContract.Action) -> Unit,
) {
    val item = state.editingItem ?: return
    val colors = AppTheme.colors

    ModalBottomSheet(
        onDismissRequest = { onAction(CartViewModelContract.Action.CancelEditingOptions) },
        containerColor = colors.surface,
        // Material's default tonal elevation tints the sheet with the primary colour.
        tonalElevation = 0.dp,
        shape = RoundedCornerShape(topStart = 30.dp, topEnd = 30.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Spacing.gutter)
                .padding(bottom = Spacing.xxl)
                .navigationBarsPadding(),
        ) {
            Text(
                text = item.name,
                style = MaterialTheme.typography.headlineSmall,
                color = colors.textPrimary,
            )
            Text(
                text = stringResource(DesignR.string.cart_addons),
                style = MaterialTheme.typography.bodySmall,
                color = colors.textSecondary,
            )

            Spacer(Modifier.height(Spacing.md))

            item.optionGroups.forEach { group ->
                AddonGroup(
                    group = group,
                    selectedIds = state.editingSelectedOptionIds,
                    onToggle = { optionId ->
                        onAction(CartViewModelContract.Action.ToggleEditedOption(group.id, optionId))
                    },
                )
            }

            Spacer(Modifier.height(Spacing.xl))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = stringResource(DesignR.string.cart_subtotal),
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.textSecondary,
                )
                AnimatedPrice(
                    value = state.editingUnitTotal * item.quantity,
                    style = AppTextStyles.priceLarge,
                    color = colors.textPrimary,
                )
            }

            Spacer(Modifier.height(Spacing.lg))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(Spacing.md),
            ) {
                SecondaryButton(
                    text = stringResource(DesignR.string.cancel),
                    onClick = { onAction(CartViewModelContract.Action.CancelEditingOptions) },
                    modifier = Modifier.weight(1f),
                )
                FullWidthButton(
                    text = stringResource(DesignR.string.confirm),
                    onClick = { onAction(CartViewModelContract.Action.ConfirmEditedOptions) },
                    enabled = state.isEditingSelectionValid,
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

@Composable
private fun AddonGroup(
    group: OptionGroupDomain,
    selectedIds: Set<Int>,
    onToggle: (Int) -> Unit,
) {
    val colors = AppTheme.colors

    Spacer(Modifier.height(Spacing.md))

    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = group.name,
            style = MaterialTheme.typography.titleSmall,
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

    group.options.forEach { option ->
        AddonRow(
            option = option,
            isSingleChoice = group.isSingleChoice,
            isSelected = option.id in selectedIds,
            onToggle = { onToggle(option.id) },
        )
    }
}

@Composable
private fun AddonRow(
    option: OptionDomain,
    isSingleChoice: Boolean,
    isSelected: Boolean,
    onToggle: () -> Unit,
) {
    val colors = AppTheme.colors

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.small)
            .clickable(enabled = option.isUserChangeable, onClick = onToggle)
            .padding(vertical = Spacing.sm),
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
        Spacer(Modifier.width(Spacing.sm))
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

private val SUMMARY_CLEARANCE = 200.dp
