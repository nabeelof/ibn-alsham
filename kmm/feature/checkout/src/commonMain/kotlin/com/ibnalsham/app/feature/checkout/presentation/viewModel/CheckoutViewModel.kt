package com.ibnalsham.app.feature.checkout.presentation.viewModel

import com.ibnalsham.app.core.common.onFailure
import com.ibnalsham.app.core.common.onSuccess
import com.ibnalsham.app.data.coupon.CouponRepository
import com.ibnalsham.app.data.network.error.DomainError
import com.ibnalsham.app.data.order.DeliveryDataDto
import com.ibnalsham.app.data.order.OrderItemDto
import com.ibnalsham.app.data.order.OrderRepository
import com.ibnalsham.app.data.order.PlaceOrderRequestDto
import com.ibnalsham.app.data.payment.PaymentRepository
import com.ibnalsham.app.feature.cart.presentation.viewModel.CartItemDomain
import com.ibnalsham.app.feature.domain.PaymentMethodType
import com.ibnalsham.app.library.data.cache.CartRepository
import com.ibnalsham.app.presentation.mvi.KMMViewModel
import com.ibnalsham.app.presentation.mvi.MVI
import com.ibnalsham.app.presentation.mvi.mvi
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.launch

class CheckoutViewModel(
    private val cartRepository: CartRepository,
    private val orderRepository: OrderRepository,
    private val couponRepository: CouponRepository,
    private val paymentRepository: PaymentRepository,
) : KMMViewModel(),
    MVI<CheckoutViewModelContract.MyUiState, CheckoutViewModelContract.Action, CheckoutViewModelContract.SideEffect>
    by mvi(CheckoutViewModelContract.MyUiState.defaultInstance()) {

    override fun onAction(event: CheckoutViewModelContract.Action) {
        when (event) {
            CheckoutViewModelContract.Action.ViewReady -> {
                loadCheckoutData()
                loadPaymentMethods()
                loadAvailableCoupons()
            }

            is CheckoutViewModelContract.Action.SelectOrderType -> switchOrderType(event.type)
            is CheckoutViewModelContract.Action.UpdateAddress -> updateState { copy(address = event.address) }
            is CheckoutViewModelContract.Action.SetLocation -> calculateDeliveryFee(event.lat, event.lng)
            is CheckoutViewModelContract.Action.UpdateNotes -> updateState { copy(notes = event.notes) }
            CheckoutViewModelContract.Action.PlaceOrder -> placeOrder()
            CheckoutViewModelContract.Action.RetryDeliveryFee -> {
                val s = currentState
                if (s.selectedLat != null && s.selectedLng != null) {
                    calculateDeliveryFee(s.selectedLat, s.selectedLng)
                }
            }

            is CheckoutViewModelContract.Action.UpdateCouponInput ->
                updateState { copy(couponInput = event.code, couponError = null) }

            CheckoutViewModelContract.Action.ApplyCoupon -> applyCoupon(currentState.couponInput)
            CheckoutViewModelContract.Action.RemoveCoupon -> removeCoupon()
            CheckoutViewModelContract.Action.ToggleCouponList ->
                updateState { copy(isCouponListExpanded = !isCouponListExpanded) }

            is CheckoutViewModelContract.Action.SelectPaymentMethod -> selectPaymentMethod(event.type)
            is CheckoutViewModelContract.Action.UpdatePaymentReference ->
                updateState { copy(paymentReference = event.reference) }
        }
    }

    private fun loadCheckoutData() {
        val items = cartRepository.cartItems.value.map { item ->
            CartItemDomain(
                id = item.lineId,
                productId = item.product.id,
                name = item.product.name,
                price = item.product.effectivePrice,
                quantity = item.quantity,
                image = item.product.image,
                optionsTotal = item.optionsTotal,
                selectedOptions = item.selectedOptions.toImmutableList(),
                hasDiscount = item.product.hasDiscount,
                originalPrice = item.product.price,
            )
        }.toImmutableList()

        updateState {
            copy(
                subtotal = cartRepository.getSubtotal(),
                cartItems = items,
            )
        }
    }

    private fun loadPaymentMethods() {
        viewModelScope.launch {
            val orderType = currentState.orderType.name.lowercase()
            paymentRepository.getPaymentMethods(orderType).onSuccess { methods ->
                updateState {
                    copy(
                        paymentMethods = methods,
                        // Keep the current pick when still offered, else fall back to the first.
                        selectedPaymentMethod = methods.firstOrNull { it.type == selectedPaymentMethod }?.type
                            ?: methods.firstOrNull()?.type
                            ?: PaymentMethodType.CASH,
                    )
                }
            }
        }
    }

    private fun loadAvailableCoupons() {
        viewModelScope.launch {
            couponRepository.list(currentState.subtotal).onSuccess { coupons ->
                updateState { copy(availableCoupons = coupons) }
            }
        }
    }

    private fun switchOrderType(type: CheckoutViewModelContract.OrderType) {
        updateState { copy(orderType = type) }
        loadPaymentMethods()
        if (type == CheckoutViewModelContract.OrderType.DELIVERY && currentState.selectedLat == null) {
            viewModelScope.launch { emitEffect { CheckoutViewModelContract.SideEffect.RequestLocationPermission } }
        }
    }

    private fun selectPaymentMethod(type: PaymentMethodType) {
        updateState {
            copy(
                selectedPaymentMethod = type,
                // The reference only belongs to transfer methods; clear it otherwise.
                paymentReference = if (paymentMethods.firstOrNull { it.type == type }?.requiresReference == true) {
                    paymentReference
                } else {
                    ""
                },
            )
        }
    }

    private fun calculateDeliveryFee(lat: Double, lng: Double) {
        viewModelScope.launch {
            updateState { copy(isLoading = true, selectedLat = lat, selectedLng = lng) }

            orderRepository.calculateDeliveryFee(lat, lng).onSuccess { response ->
                updateState {
                    copy(
                        isLoading = false,
                        isDeliveryRangeValid = response.isValid,
                        distanceKm = response.distanceKm,
                        deliveryFee = response.deliveryFee,
                    )
                }
            }.onFailure { error ->
                updateState { copy(isLoading = false, error = error) }
            }
        }
    }

    // ─── Coupons ─────────────────────────────────────────────────────────

    private fun applyCoupon(code: String) {
        val trimmed = code.trim()
        if (trimmed.isEmpty()) return

        viewModelScope.launch {
            updateState { copy(isApplyingCoupon = true, couponError = null) }

            couponRepository.apply(trimmed, currentState.subtotal).onSuccess { coupon ->
                updateState {
                    copy(
                        isApplyingCoupon = false,
                        appliedCoupon = coupon,
                        couponInput = coupon.code,
                        couponError = null,
                        isCouponListExpanded = false,
                    )
                }
            }.onFailure { error ->
                updateState {
                    copy(
                        isApplyingCoupon = false,
                        appliedCoupon = null,
                        couponError = error.messageOrDefault(COUPON_INVALID_MESSAGE),
                    )
                }
            }
        }
    }

    private fun removeCoupon() {
        viewModelScope.launch {
            couponRepository.remove()
            updateState { copy(appliedCoupon = null, couponInput = "", couponError = null) }
        }
    }

    // ─── Placing the order ───────────────────────────────────────────────

    private fun placeOrder() {
        val s = currentState
        if (!s.canPlaceOrder) {
            val message = when {
                s.orderType == CheckoutViewModelContract.OrderType.DELIVERY &&
                    (s.address.isBlank() || !s.isDeliveryRangeValid) -> DELIVERY_INVALID_MESSAGE

                s.requiresPaymentReference && s.paymentReference.isBlank() -> REFERENCE_REQUIRED_MESSAGE
                else -> return
            }
            viewModelScope.launch { emitEffect { CheckoutViewModelContract.SideEffect.ShowSnackbar(message) } }
            return
        }

        viewModelScope.launch {
            updateState { copy(isPlacingOrder = true) }

            val orderItems = cartRepository.cartItems.value.map { item ->
                OrderItemDto(
                    productId = item.product.id,
                    quantity = item.quantity,
                    optionIds = item.selectedOptions.map { it.id },
                )
            }

            val deliveryData = if (s.orderType == CheckoutViewModelContract.OrderType.DELIVERY) {
                DeliveryDataDto(
                    address = s.address,
                    lat = s.selectedLat ?: 0.0,
                    lng = s.selectedLng ?: 0.0,
                )
            } else {
                null
            }

            val request = PlaceOrderRequestDto(
                items = orderItems,
                orderType = s.orderType.name.lowercase(),
                delivery = deliveryData,
                notes = s.notes.ifBlank { null },
                couponCode = s.appliedCoupon?.code,
                paymentMethod = s.selectedPaymentMethod.key,
                paymentReference = s.paymentReference.ifBlank { null },
            )

            orderRepository.placeOrder(request).onSuccess { response ->
                updateState { copy(isPlacingOrder = false) }
                cartRepository.clearCart()
                emitEffect { CheckoutViewModelContract.SideEffect.NavigateToOrderTracking(response.id) }
            }.onFailure { error ->
                updateState { copy(isPlacingOrder = false, error = error) }
                emitEffect {
                    CheckoutViewModelContract.SideEffect.ShowSnackbar(
                        error.messageOrDefault(ORDER_FAILED_MESSAGE)
                    )
                }
            }
        }
    }

    private fun DomainError.messageOrDefault(fallback: String): String =
        (this as? DomainError.Unknown)?.message ?: fallback

    private companion object {
        const val COUPON_INVALID_MESSAGE = "كود الخصم غير صحيح"
        const val DELIVERY_INVALID_MESSAGE = "الرجاء التأكد من عنوان التوصيل"
        const val REFERENCE_REQUIRED_MESSAGE = "الرجاء إدخال رقم الحوالة"
        const val ORDER_FAILED_MESSAGE = "تعذّر إتمام الطلب، حاول مرة أخرى"
    }
}
