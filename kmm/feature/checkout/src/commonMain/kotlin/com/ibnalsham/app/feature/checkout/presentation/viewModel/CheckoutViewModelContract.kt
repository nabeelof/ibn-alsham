package com.ibnalsham.app.feature.checkout.presentation.viewModel

import com.ibnalsham.app.data.network.error.DomainError
import com.ibnalsham.app.presentation.mvi.UiEvent
import com.ibnalsham.app.presentation.mvi.UiSideEffect
import com.ibnalsham.app.presentation.mvi.UiState
import com.ibnalsham.app.feature.cart.presentation.viewModel.CartItemDomain
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

interface CheckoutViewModelContract {
    sealed class Action : UiEvent() {
        data object ViewReady : Action()
        data class SelectOrderType(val type: OrderType) : Action()
        data class UpdateAddress(val address: String) : Action()
        data class SetLocation(val lat: Double, val lng: Double) : Action()
        data class UpdateNotes(val notes: String) : Action()
        data object PlaceOrder : Action()
        data object RetryDeliveryFee : Action()
    }

    enum class OrderType { PICKUP, DELIVERY }

    data class MyUiState(
        val isLoading: Boolean = false,
        val isPlacingOrder: Boolean = false,
        val error: DomainError? = null,
        val orderType: OrderType = OrderType.PICKUP,
        val cartItems: ImmutableList<CartItemDomain> = persistentListOf(),
        val subtotal: Double = 0.0,
        val deliveryFee: Double = 0.0,
        val distanceKm: Double? = null,
        val address: String = "",
        val selectedLat: Double? = null,
        val selectedLng: Double? = null,
        val isDeliveryRangeValid: Boolean = true,
        val notes: String = "",
        val paymentMethod: String = "cash",
    ) : UiState {
        val total: Double get() = subtotal + (if (orderType == OrderType.DELIVERY) deliveryFee else 0.0)

        companion object {
            fun defaultInstance() = MyUiState()
        }
    }

    sealed class SideEffect : UiSideEffect {
        data class NavigateToOrderTracking(val orderId: Int) : SideEffect()
        data class ShowSnackbar(val message: String) : SideEffect()
        data object RequestLocationPermission : SideEffect()
    }
}
