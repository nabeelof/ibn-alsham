package com.ibnalsham.app.feature.cart.presentation.viewModel

import com.ibnalsham.app.feature.domain.OptionDomain
import com.ibnalsham.app.feature.domain.OptionGroupDomain
import com.ibnalsham.app.presentation.mvi.UiEvent
import com.ibnalsham.app.presentation.mvi.UiSideEffect
import com.ibnalsham.app.presentation.mvi.UiState
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableSet
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.persistentSetOf

/** One line in the cart. Same product with different add-ons = different lines. */
data class CartItemDomain(
    val id: String,
    val productId: Int,
    val name: String,
    val price: Double,
    val quantity: Int,
    val image: String?,
    val optionsTotal: Double = 0.0,
    val selectedOptions: ImmutableList<OptionDomain> = persistentListOf(),
    val optionGroups: ImmutableList<OptionGroupDomain> = persistentListOf(),
    val hasDiscount: Boolean = false,
    val originalPrice: Double = 0.0,
) {
    val unitTotal: Double get() = price + optionsTotal
    val total: Double get() = unitTotal * quantity
    val canEditOptions: Boolean get() = optionGroups.isNotEmpty()
    val optionsLabel: String get() = selectedOptions.joinToString(", ") { it.name }
}

interface CartViewModelContract {
    sealed class Action : UiEvent() {
        data object ViewReady : Action()
        data class IncreaseQuantity(val itemId: String) : Action()
        data class DecreaseQuantity(val itemId: String) : Action()
        data class RemoveItem(val itemId: String) : Action()
        data object ClearCart : Action()
        data object ProceedToCheckout : Action()
        data object ContinueShopping : Action()

        // Add-on editing
        data class StartEditingOptions(val itemId: String) : Action()
        data class ToggleEditedOption(val groupId: Int, val optionId: Int) : Action()
        data object ConfirmEditedOptions : Action()
        data object CancelEditingOptions : Action()
    }

    data class MyUiState(
        val isLoading: Boolean = false,
        val items: ImmutableList<CartItemDomain> = persistentListOf(),
        val subtotal: Double = 0.0,
        val itemCount: Int = 0,
        // Add-on edit sheet
        val editingItemId: String? = null,
        val editingSelectedOptionIds: ImmutableSet<Int> = persistentSetOf(),
    ) : UiState {
        val isEmpty: Boolean get() = items.isEmpty()

        val editingItem: CartItemDomain?
            get() = items.firstOrNull { it.id == editingItemId }

        /** Live total for the line being edited, so the sheet can show it. */
        val editingUnitTotal: Double
            get() {
                val item = editingItem ?: return 0.0
                val extras = item.optionGroups
                    .flatMap { it.options }
                    .filter { it.id in editingSelectedOptionIds }
                    .sumOf { it.price }
                return item.price + extras
            }

        /** Required groups must be satisfied before the edit can be confirmed. */
        val isEditingSelectionValid: Boolean
            get() {
                val item = editingItem ?: return false
                return item.optionGroups.all { group ->
                    val count = group.options.count { it.id in editingSelectedOptionIds }
                    group.isSelectionValid(count)
                }
            }

        companion object {
            fun defaultInstance() = MyUiState()
        }
    }

    sealed class SideEffect : UiSideEffect {
        data object NavigateToCheckout : SideEffect()
        data object NavigateToMenu : SideEffect()
        data class ShowSnackbar(val message: String) : SideEffect()
    }
}
