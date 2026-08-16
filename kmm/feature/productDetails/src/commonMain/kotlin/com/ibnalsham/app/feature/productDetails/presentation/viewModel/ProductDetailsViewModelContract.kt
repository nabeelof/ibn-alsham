package com.ibnalsham.app.feature.productDetails.presentation.viewModel

import com.ibnalsham.app.data.network.error.DomainError
import com.ibnalsham.app.feature.domain.OptionGroupDomain
import com.ibnalsham.app.presentation.mvi.UiEvent
import com.ibnalsham.app.presentation.mvi.UiSideEffect
import com.ibnalsham.app.presentation.mvi.UiState
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableSet
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.persistentSetOf
import kotlinx.collections.immutable.toImmutableList

data class ProductImageDomain(val id: Int, val url: String)

interface ProductDetailsViewModelContract {
    sealed class Action : UiEvent() {
        data class ViewReady(val productId: Int) : Action()
        data class ToggleOption(val groupId: Int, val optionId: Int) : Action()
        data class IncrementQuantity(val maxAllowed: Int = 99) : Action()
        data object DecrementQuantity : Action()
        data object AddToCart : Action()
        data object ToggleFavorite : Action()
        data object ShareProduct : Action()
    }

    data class MyUiState(
        val isLoading: Boolean = true,
        val error: DomainError? = null,
        val productId: Int = 0,
        val name: String = "",
        val description: String = "",
        val basePrice: Double = 0.0,
        val effectivePrice: Double = 0.0,
        val hasDiscount: Boolean = false,
        val discountPercentage: Int = 0,
        val images: ImmutableList<ProductImageDomain> = persistentListOf(),
        val optionGroups: ImmutableList<OptionGroupDomain> = persistentListOf(),
        val selectedOptionIds: ImmutableSet<Int> = persistentSetOf(),
        val quantity: Int = 1,
        val isFavorite: Boolean = false,
        val isTogglingFavorite: Boolean = false,
        val isAddingToCart: Boolean = false,
    ) : UiState {

        val optionsTotal: Double
            get() = optionGroups.flatMap { it.options }
                .filter { it.id in selectedOptionIds }
                .sumOf { it.price }

        val calculatedTotal: Double
            get() = (effectivePrice + optionsTotal) * quantity

        /** Every required group must have a valid number of picks. */
        val isSelectionValid: Boolean
            get() = optionGroups.all { group ->
                group.isSelectionValid(group.options.count { it.id in selectedOptionIds })
            }

        val isAddToCartEnabled: Boolean
            get() = isSelectionValid && !isAddingToCart && !isLoading && error == null

        /** Groups still missing a required selection, for inline validation hints. */
        val unsatisfiedGroupIds: ImmutableList<Int>
            get() = optionGroups
                .filterNot { group ->
                    group.isSelectionValid(group.options.count { it.id in selectedOptionIds })
                }
                .map { it.id }
                .toImmutableList()

        companion object {
            fun defaultInstance() = MyUiState()
        }
    }

    sealed class SideEffect : UiSideEffect {
        data object NavigateBack : SideEffect()
        data class ShowSnackbar(val message: String) : SideEffect()

        /** Deep link to this product, handed to the platform share sheet. */
        data class ShareLink(val url: String, val title: String) : SideEffect()

        /** Guest tapped the heart — ask the host to raise the sign-in gate sheet. */
        data object RequireSignIn : SideEffect()
    }
}
