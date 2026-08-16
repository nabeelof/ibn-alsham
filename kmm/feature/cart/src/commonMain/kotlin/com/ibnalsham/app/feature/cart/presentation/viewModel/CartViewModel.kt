package com.ibnalsham.app.feature.cart.presentation.viewModel

import com.ibnalsham.app.feature.domain.OptionGroupDomain
import com.ibnalsham.app.library.data.cache.CartItem
import com.ibnalsham.app.library.data.cache.CartRepository
import com.ibnalsham.app.presentation.mvi.KMMViewModel
import com.ibnalsham.app.presentation.mvi.MVI
import com.ibnalsham.app.presentation.mvi.mvi
import kotlinx.collections.immutable.persistentSetOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.collections.immutable.toImmutableSet
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class CartViewModel(
    private val cartRepository: CartRepository,
) : KMMViewModel(),
    MVI<CartViewModelContract.MyUiState, CartViewModelContract.Action, CartViewModelContract.SideEffect>
    by mvi(CartViewModelContract.MyUiState.defaultInstance()) {

    override fun onAction(event: CartViewModelContract.Action) {
        when (event) {
            CartViewModelContract.Action.ViewReady -> observeCart()
            is CartViewModelContract.Action.IncreaseQuantity -> changeQuantity(event.itemId, 1)
            is CartViewModelContract.Action.DecreaseQuantity -> changeQuantity(event.itemId, -1)
            is CartViewModelContract.Action.RemoveItem -> cartRepository.removeLine(event.itemId)
            CartViewModelContract.Action.ClearCart -> cartRepository.clearCart()
            CartViewModelContract.Action.ProceedToCheckout -> handleProceedToCheckout()
            CartViewModelContract.Action.ContinueShopping -> handleContinueShopping()
            is CartViewModelContract.Action.StartEditingOptions -> startEditing(event.itemId)
            is CartViewModelContract.Action.ToggleEditedOption -> toggleEditedOption(event.groupId, event.optionId)
            CartViewModelContract.Action.ConfirmEditedOptions -> confirmEditing()
            CartViewModelContract.Action.CancelEditingOptions -> updateState {
                copy(editingItemId = null, editingSelectedOptionIds = persistentSetOf())
            }
        }
    }

    private fun observeCart() {
        viewModelScope.launch {
            cartRepository.cartItems.collectLatest { items ->
                val domainItems = items.map { it.toUiItem() }.toImmutableList()

                updateState {
                    copy(
                        items = domainItems,
                        subtotal = items.sumOf { it.itemTotal },
                        itemCount = items.sumOf { it.quantity },
                        // Drop the edit sheet if its line disappeared.
                        editingItemId = editingItemId?.takeIf { id -> domainItems.any { it.id == id } },
                    )
                }
            }
        }
    }

    private fun CartItem.toUiItem() = CartItemDomain(
        id = lineId,
        productId = product.id,
        name = product.name,
        price = product.effectivePrice,
        quantity = quantity,
        image = product.image,
        optionsTotal = optionsTotal,
        selectedOptions = selectedOptions.toImmutableList(),
        optionGroups = product.optionGroups.toImmutableList(),
        hasDiscount = product.hasDiscount,
        originalPrice = product.price,
    )

    private fun handleProceedToCheckout() {
        if (currentState.items.isNotEmpty()) {
            viewModelScope.launch { emitEffect { CartViewModelContract.SideEffect.NavigateToCheckout } }
        }
    }

    private fun handleContinueShopping() {
        viewModelScope.launch { emitEffect { CartViewModelContract.SideEffect.NavigateToMenu } }
    }

    private fun changeQuantity(lineId: String, delta: Int) {
        val item = currentState.items.firstOrNull { it.id == lineId } ?: return
        cartRepository.updateLineQuantity(lineId, item.quantity + delta)
    }

    // ─── Add-on editing ──────────────────────────────────────────────────

    private fun startEditing(lineId: String) {
        val item = currentState.items.firstOrNull { it.id == lineId } ?: return
        updateState {
            copy(
                editingItemId = lineId,
                editingSelectedOptionIds = item.selectedOptions.map { it.id }.toImmutableSet(),
            )
        }
    }

    private fun toggleEditedOption(groupId: Int, optionId: Int) {
        val item = currentState.editingItem ?: return
        val group = item.optionGroups.firstOrNull { it.id == groupId } ?: return
        val option = group.options.firstOrNull { it.id == optionId } ?: return
        if (!option.isUserChangeable) return

        val selected = currentState.editingSelectedOptionIds
        val maxSelections = group.maxSelections
        val next = when {
            optionId in selected -> {
                // A required single-choice group must keep exactly one pick.
                if (group.isSingleChoice && group.isRequired) selected else selected - optionId
            }

            group.isSingleChoice -> {
                // Radio behaviour: replace whatever else is picked in this group.
                val otherGroupIds = group.options.map { it.id }.toSet()
                (selected - otherGroupIds) + optionId
            }

            maxSelections != null &&
                group.options.count { it.id in selected } >= maxSelections -> selected

            else -> selected + optionId
        }

        updateState { copy(editingSelectedOptionIds = next.toImmutableSet()) }
    }

    private fun confirmEditing() {
        val state = currentState
        val lineId = state.editingItemId ?: return
        val item = state.editingItem ?: return

        if (!state.isEditingSelectionValid) {
            viewModelScope.launch {
                emitEffect { CartViewModelContract.SideEffect.ShowSnackbar(REQUIRED_OPTIONS_MESSAGE) }
            }
            return
        }

        val options = item.optionGroups
            .flatMap(OptionGroupDomain::options)
            .filter { it.id in state.editingSelectedOptionIds }

        cartRepository.updateLineOptions(lineId, options)
        updateState { copy(editingItemId = null, editingSelectedOptionIds = persistentSetOf()) }
    }

    private companion object {
        const val REQUIRED_OPTIONS_MESSAGE = "الرجاء اختيار الإضافات المطلوبة"
    }
}
