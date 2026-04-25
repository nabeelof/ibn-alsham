package com.ibnalsham.app.feature.checkout.presentation.viewModel

import com.ibnalsham.app.core.common.onFailure
import com.ibnalsham.app.core.common.onSuccess
import com.ibnalsham.app.data.order.DeliveryDataDto
import com.ibnalsham.app.data.order.OrderItemDto
import com.ibnalsham.app.data.order.OrderRepository
import com.ibnalsham.app.data.order.PlaceOrderRequestDto
import com.ibnalsham.app.feature.cart.presentation.viewModel.CartItemDomain
import com.ibnalsham.app.library.data.cache.CartRepository
import com.ibnalsham.app.presentation.mvi.KMMViewModel
import com.ibnalsham.app.presentation.mvi.MVI
import com.ibnalsham.app.presentation.mvi.mvi
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.launch

class CheckoutViewModel(
    private val cartRepository: CartRepository,
    private val orderRepository: OrderRepository,
) : KMMViewModel(),
    MVI<CheckoutViewModelContract.MyUiState, CheckoutViewModelContract.Action, CheckoutViewModelContract.SideEffect>
    by mvi(CheckoutViewModelContract.MyUiState.defaultInstance()) {

    override fun onAction(event: CheckoutViewModelContract.Action) {
        when (event) {
            CheckoutViewModelContract.Action.ViewReady -> loadCheckoutData()
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
        }
    }

    private fun loadCheckoutData() {
        val items = cartRepository.cartItems.value.map { item ->
            CartItemDomain(
                id = item.product.id.toString(),
                productId = item.product.id,
                name = item.product.name,
                price = item.product.effectivePrice,
                quantity = item.quantity,
                image = item.product.image,
                optionsTotal = item.optionsTotal,
            )
        }.toImmutableList()

        updateState {
            copy(
                subtotal = cartRepository.getSubtotal(),
                cartItems = items,
            )
        }
    }

    private fun switchOrderType(type: CheckoutViewModelContract.OrderType) {
        updateState { copy(orderType = type) }
        if (type == CheckoutViewModelContract.OrderType.DELIVERY && currentState.selectedLat == null) {
            viewModelScope.launch { emitEffect { CheckoutViewModelContract.SideEffect.RequestLocationPermission } }
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

    private fun placeOrder() {
        val s = currentState
        if (s.orderType == CheckoutViewModelContract.OrderType.DELIVERY && (s.address.isBlank() || !s.isDeliveryRangeValid)) {
            viewModelScope.launch { emitEffect { CheckoutViewModelContract.SideEffect.ShowSnackbar("الرجاء التأكد من التوصيل") } }
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
            } else null

            val request = PlaceOrderRequestDto(
                items = orderItems,
                orderType = s.orderType.name.lowercase(),
                delivery = deliveryData,
                notes = s.notes.ifBlank { null },
            )

            orderRepository.placeOrder(request).onSuccess { response ->
                updateState { copy(isPlacingOrder = false) }
                cartRepository.clearCart()
                emitEffect { CheckoutViewModelContract.SideEffect.NavigateToOrderTracking(response.id) }
            }.onFailure { error ->
                updateState { copy(isPlacingOrder = false, error = error) }
            }
        }
    }
}
