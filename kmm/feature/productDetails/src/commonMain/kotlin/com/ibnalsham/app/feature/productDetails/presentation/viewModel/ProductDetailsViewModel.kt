package com.ibnalsham.app.feature.productDetails.presentation.viewModel

import com.ibnalsham.app.core.common.onFailure
import com.ibnalsham.app.core.common.onSuccess
import com.ibnalsham.app.data.favorites.FavoritesRepository
import com.ibnalsham.app.data.product.ProductRepository
import com.ibnalsham.app.feature.domain.OptionGroupDomain
import com.ibnalsham.app.feature.domain.ProductDomain
import com.ibnalsham.app.library.data.cache.CartRepository
import com.ibnalsham.app.library.data.cache.GatedFeature
import com.ibnalsham.app.library.data.cache.SessionManager
import com.ibnalsham.app.presentation.mvi.KMMViewModel
import com.ibnalsham.app.presentation.mvi.MVI
import com.ibnalsham.app.presentation.mvi.mvi
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.collections.immutable.toImmutableSet
import kotlinx.coroutines.launch

class ProductDetailsViewModel(
    private val productRepository: ProductRepository,
    private val cartRepository: CartRepository,
    private val favoritesRepository: FavoritesRepository,
    private val sessionManager: SessionManager,
) : KMMViewModel(),
    MVI<
        ProductDetailsViewModelContract.MyUiState,
        ProductDetailsViewModelContract.Action,
        ProductDetailsViewModelContract.SideEffect,
        >
    by mvi(ProductDetailsViewModelContract.MyUiState.defaultInstance()) {

    private var product: ProductDomain? = null

    override fun onAction(event: ProductDetailsViewModelContract.Action) {
        when (event) {
            is ProductDetailsViewModelContract.Action.ViewReady -> loadProduct(event.productId)
            is ProductDetailsViewModelContract.Action.ToggleOption -> toggleOption(event.groupId, event.optionId)
            is ProductDetailsViewModelContract.Action.IncrementQuantity -> {
                if (currentState.quantity < event.maxAllowed) updateState { copy(quantity = quantity + 1) }
            }

            ProductDetailsViewModelContract.Action.DecrementQuantity -> {
                if (currentState.quantity > 1) updateState { copy(quantity = quantity - 1) }
            }

            ProductDetailsViewModelContract.Action.AddToCart -> handleAddToCart()
            ProductDetailsViewModelContract.Action.ToggleFavorite -> toggleFavorite()
            ProductDetailsViewModelContract.Action.ShareProduct -> shareProduct()
        }
    }

    private fun loadProduct(productId: Int) {
        viewModelScope.launch {
            updateState { copy(isLoading = true, productId = productId, error = null) }

            productRepository.getProduct(productId).onSuccess { loaded ->
                product = loaded
                updateState {
                    copy(
                        isLoading = false,
                        error = null,
                        productId = loaded.id,
                        name = loaded.name,
                        description = loaded.description.orEmpty(),
                        basePrice = loaded.price,
                        effectivePrice = loaded.effectivePrice,
                        hasDiscount = loaded.hasDiscount,
                        discountPercentage = loaded.savedPercentage,
                        images = loaded.image
                            ?.let { persistentListOf(ProductImageDomain(id = loaded.id, url = it)) }
                            ?: persistentListOf(),
                        optionGroups = loaded.optionGroups.toImmutableList(),
                        selectedOptionIds = loaded.defaultSelections().toImmutableSet(),
                        isFavorite = loaded.isFavorite,
                    )
                }
            }.onFailure { error ->
                updateState { copy(isLoading = false, error = error) }
            }
        }
    }

    /** Options the admin marked as pre-selected, plus a forced pick for required radio groups. */
    private fun ProductDomain.defaultSelections(): Set<Int> = buildSet {
        optionGroups.forEach { group ->
            val defaults = group.options.filter { it.defaultSelected }
            when {
                defaults.isNotEmpty() && group.isSingleChoice -> add(defaults.first().id)
                defaults.isNotEmpty() -> addAll(defaults.map { it.id })
                group.isRequired && group.isSingleChoice ->
                    group.options.firstOrNull()?.let { add(it.id) }
            }
        }
    }

    private fun toggleOption(groupId: Int, optionId: Int) {
        val group = currentState.optionGroups.firstOrNull { it.id == groupId } ?: return
        val option = group.options.firstOrNull { it.id == optionId } ?: return
        if (!option.isUserChangeable) return

        val selected = currentState.selectedOptionIds
        val maxSelections = group.maxSelections
        val next = when {
            optionId in selected -> {
                // Never let a required single-choice group end up empty.
                if (group.isSingleChoice && group.isRequired) selected else selected - optionId
            }

            group.isSingleChoice -> (selected - group.options.map { it.id }.toSet()) + optionId

            maxSelections != null &&
                group.options.count { it.id in selected } >= maxSelections -> selected

            else -> selected + optionId
        }

        updateState { copy(selectedOptionIds = next.toImmutableSet()) }
    }

    private fun handleAddToCart() {
        val state = currentState
        val loaded = product ?: return

        if (!state.isSelectionValid) {
            viewModelScope.launch {
                emitEffect { ProductDetailsViewModelContract.SideEffect.ShowSnackbar(REQUIRED_OPTIONS_MESSAGE) }
            }
            return
        }
        if (!state.isAddToCartEnabled) return

        viewModelScope.launch {
            updateState { copy(isAddingToCart = true) }

            val options = state.optionGroups
                .flatMap(OptionGroupDomain::options)
                .filter { it.id in state.selectedOptionIds }

            cartRepository.addToCart(loaded, options, state.quantity)

            updateState { copy(isAddingToCart = false) }
            emitEffect { ProductDetailsViewModelContract.SideEffect.ShowSnackbar(ADDED_TO_CART_MESSAGE) }
            emitEffect { ProductDetailsViewModelContract.SideEffect.NavigateBack }
        }
    }

    private fun toggleFavorite() {
        val productId = currentState.productId
        if (productId == 0 || currentState.isTogglingFavorite) return

        // Favouriting needs an account. Offer the gate rather than failing the call.
        if (!sessionManager.canAccess(GatedFeature.Favorites)) {
            viewModelScope.launch {
                emitEffect { ProductDetailsViewModelContract.SideEffect.RequireSignIn }
            }
            return
        }

        viewModelScope.launch {
            updateState { copy(isTogglingFavorite = true) }

            favoritesRepository.toggleFavorite(productId).onSuccess { isFavorite ->
                product = product?.copy(isFavorite = isFavorite)
                updateState { copy(isTogglingFavorite = false, isFavorite = isFavorite) }
            }.onFailure {
                updateState { copy(isTogglingFavorite = false) }
                emitEffect { ProductDetailsViewModelContract.SideEffect.ShowSnackbar(FAVORITE_FAILED_MESSAGE) }
            }
        }
    }

    private fun shareProduct() {
        val state = currentState
        if (state.productId == 0) return

        viewModelScope.launch {
            emitEffect {
                ProductDetailsViewModelContract.SideEffect.ShareLink(
                    url = "$SHARE_BASE_URL/product/${state.productId}",
                    title = state.name,
                )
            }
        }
    }

    private companion object {
        const val SHARE_BASE_URL = "https://ibnalsham.prolabtech.net"
        const val REQUIRED_OPTIONS_MESSAGE = "الرجاء اختيار الإضافات المطلوبة"
        const val ADDED_TO_CART_MESSAGE = "تمت الإضافة إلى السلة"
        const val FAVORITE_FAILED_MESSAGE = "تعذّر تحديث المفضّلة"
    }
}
