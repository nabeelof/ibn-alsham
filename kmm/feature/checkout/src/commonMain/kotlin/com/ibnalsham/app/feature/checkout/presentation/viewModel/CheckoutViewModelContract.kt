package com.ibnalsham.app.feature.checkout.presentation.viewModel

import com.ibnalsham.app.data.network.error.DomainError
import com.ibnalsham.app.feature.cart.presentation.viewModel.CartItemDomain
import com.ibnalsham.app.feature.domain.CouponDomain
import com.ibnalsham.app.feature.domain.PaymentMethodDomain
import com.ibnalsham.app.feature.domain.PaymentMethodType
import com.ibnalsham.app.presentation.mvi.UiEvent
import com.ibnalsham.app.presentation.mvi.UiSideEffect
import com.ibnalsham.app.presentation.mvi.UiState
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

        // Coupons
        data class UpdateCouponInput(val code: String) : Action()
        data object ApplyCoupon : Action()
        data object RemoveCoupon : Action()
        data object ToggleCouponList : Action()

        // Payment
        data class SelectPaymentMethod(val type: PaymentMethodType) : Action()
        data class UpdatePaymentReference(val reference: String) : Action()
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
        // Coupons
        val couponInput: String = "",
        val appliedCoupon: CouponDomain? = null,
        val isApplyingCoupon: Boolean = false,
        val couponError: String? = null,
        val availableCoupons: ImmutableList<CouponDomain> = persistentListOf(),
        val isCouponListExpanded: Boolean = false,
        // Payment
        val paymentMethods: ImmutableList<PaymentMethodDomain> = persistentListOf(),
        val selectedPaymentMethod: PaymentMethodType = PaymentMethodType.CASH,
        val paymentReference: String = "",
    ) : UiState {

        val effectiveDeliveryFee: Double
            get() = if (orderType == OrderType.DELIVERY) deliveryFee else 0.0

        /** Coupons discount items only — never the delivery fee. */
        val discountAmount: Double
            get() = minOf(appliedCoupon?.discountAmount ?: 0.0, subtotal)

        val total: Double
            get() = subtotal - discountAmount + effectiveDeliveryFee

        val selectedMethod: PaymentMethodDomain?
            get() = paymentMethods.firstOrNull { it.type == selectedPaymentMethod }

        val requiresPaymentReference: Boolean
            get() = selectedMethod?.requiresReference == true

        val canPlaceOrder: Boolean
            get() = cartItems.isNotEmpty() &&
                !isPlacingOrder &&
                (orderType == OrderType.PICKUP || (address.isNotBlank() && isDeliveryRangeValid)) &&
                (!requiresPaymentReference || paymentReference.isNotBlank())

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
