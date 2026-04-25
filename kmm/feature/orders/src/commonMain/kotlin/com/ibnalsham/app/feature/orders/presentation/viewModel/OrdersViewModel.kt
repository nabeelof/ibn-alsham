package com.ibnalsham.app.feature.orders.presentation.viewModel

import com.ibnalsham.app.core.common.onFailure
import com.ibnalsham.app.core.common.onSuccess
import com.ibnalsham.app.data.order.OrderRepository
import com.ibnalsham.app.data.order.OrderResponseDto
import com.ibnalsham.app.feature.domain.OrderDomain
import com.ibnalsham.app.feature.domain.OrderStatus
import com.ibnalsham.app.presentation.mvi.KMMViewModel
import com.ibnalsham.app.presentation.mvi.MVI
import com.ibnalsham.app.presentation.mvi.mvi
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class OrdersViewModel(
    private val orderRepository: OrderRepository,
) : KMMViewModel(),
    MVI<OrdersViewModelContract.MyUiState, OrdersViewModelContract.Action, OrdersViewModelContract.SideEffect>
    by mvi(OrdersViewModelContract.MyUiState.defaultInstance()) {

    private var pollingJob: Job? = null

    override fun onAction(event: OrdersViewModelContract.Action) {
        when (event) {
            OrdersViewModelContract.Action.ViewReady -> loadOrders()
            is OrdersViewModelContract.Action.SelectTab -> {
                updateState { copy(selectedTab = event.tab) }
                loadOrders()
            }
            is OrdersViewModelContract.Action.OrderClicked -> {
                viewModelScope.launch {
                    emitEffect { OrdersViewModelContract.SideEffect.NavigateToOrderDetails(event.orderId) }
                }
            }
            is OrdersViewModelContract.Action.StartTracking -> startTrackingOrder(event.orderId)
            OrdersViewModelContract.Action.StopTracking -> stopTracking()
            is OrdersViewModelContract.Action.CancelOrder -> cancelOrder(event.orderId, event.reason)
            is OrdersViewModelContract.Action.ConfirmDelivery -> confirmDelivery(event.orderId)
            is OrdersViewModelContract.Action.SubmitReview -> submitReview(event.orderId, event.rating, event.note)
            OrdersViewModelContract.Action.DismissReviewDialog -> updateState { copy(showReviewDialog = false) }
        }
    }

    private fun loadOrders() {
        viewModelScope.launch {
            updateState { copy(isLoading = true, error = null) }

            val tab = if (currentState.selectedTab == OrdersViewModelContract.OrderTab.CURRENT) "current" else "previous"

            orderRepository.getOrders(tab).onSuccess { orders ->
                val mapped = orders.map { it.toDomain() }.toImmutableList()

                if (tab == "current") {
                    updateState { copy(isLoading = false, currentOrders = mapped) }
                } else {
                    updateState { copy(isLoading = false, previousOrders = mapped) }
                }
            }.onFailure { error ->
                updateState { copy(isLoading = false, error = error) }
            }
        }
    }

    private fun startTrackingOrder(orderId: Int) {
        stopTracking()
        updateState { copy(isTracking = true) }

        pollingJob = viewModelScope.launch {
            while (isActive) {
                orderRepository.getOrderStatus(orderId).onSuccess { statusDto ->
                    val order = OrderDomain(
                        id = statusDto.id,
                        orderNumber = "",
                        type = "",
                        status = OrderStatus.fromString(statusDto.status),
                        statusLabel = statusDto.statusLabel,
                        subtotal = 0.0,
                        deliveryFee = 0.0,
                        total = 0.0,
                        canCancel = statusDto.canCancel,
                        cancellationDeadline = statusDto.cancellationDeadline,
                        estimatedTime = statusDto.estimatedTime,
                        createdAt = "",
                        confirmedAt = statusDto.confirmedAt,
                        needsDeliveryConfirmation = statusDto.needsDeliveryConfirmation,
                        hasReview = statusDto.hasReview,
                    )

                    val phase = order.toPhase()
                    updateState {
                        copy(
                            activeOrder = order,
                            orderPhase = phase,
                            showReviewDialog = phase == OrderPhase.REVIEW,
                            reviewOrderId = if (phase == OrderPhase.REVIEW) orderId else reviewOrderId,
                        )
                    }

                    // Stop polling if terminal state
                    if (phase == OrderPhase.COMPLETED) {
                        stopTracking()
                        loadOrders()
                        return@onSuccess
                    }
                }

                delay(5000) // Poll every 5 seconds
            }
        }
    }

    private fun stopTracking() {
        pollingJob?.cancel()
        pollingJob = null
        updateState { copy(isTracking = false) }
    }

    private fun cancelOrder(orderId: Int, reason: String?) {
        viewModelScope.launch {
            updateState { copy(isCancelling = true) }
            orderRepository.cancelOrder(orderId, reason).onSuccess {
                updateState { copy(isCancelling = false, activeOrder = null, orderPhase = OrderPhase.NONE) }
                stopTracking()
                loadOrders()
                emitEffect { OrdersViewModelContract.SideEffect.OrderCancelled }
            }.onFailure { error ->
                updateState { copy(isCancelling = false) }
                emitEffect { OrdersViewModelContract.SideEffect.ShowSnackbar("Failed to cancel order") }
            }
        }
    }

    private fun confirmDelivery(orderId: Int) {
        viewModelScope.launch {
            orderRepository.confirmDelivery(orderId).onSuccess {
                updateState {
                    copy(
                        orderPhase = OrderPhase.REVIEW,
                        showReviewDialog = true,
                        reviewOrderId = orderId,
                    )
                }
            }.onFailure {
                emitEffect { OrdersViewModelContract.SideEffect.ShowSnackbar("Failed to confirm delivery") }
            }
        }
    }

    private fun submitReview(orderId: Int, rating: Int, note: String) {
        viewModelScope.launch {
            updateState { copy(isSubmittingReview = true) }
            orderRepository.submitReview(orderId, rating, note.ifBlank { null }).onSuccess {
                updateState {
                    copy(
                        isSubmittingReview = false,
                        showReviewDialog = false,
                        orderPhase = OrderPhase.COMPLETED,
                        activeOrder = null,
                    )
                }
                stopTracking()
                loadOrders()
                emitEffect { OrdersViewModelContract.SideEffect.ReviewSubmitted }
            }.onFailure {
                updateState { copy(isSubmittingReview = false) }
                emitEffect { OrdersViewModelContract.SideEffect.ShowSnackbar("Failed to submit review") }
            }
        }
    }

    private fun OrderResponseDto.toDomain(): OrderDomain = OrderDomain(
        id = id,
        orderNumber = orderNumber,
        type = type,
        status = OrderStatus.fromString(status),
        statusLabel = statusLabel,
        subtotal = subtotal,
        deliveryFee = deliveryFee,
        total = total,
        notes = notes,
        paymentMethod = paymentMethod,
        canCancel = canCancel,
        cancellationDeadline = cancellationDeadline,
        estimatedTime = estimatedTime,
        createdAt = createdAt,
        confirmedAt = confirmedAt,
        needsDeliveryConfirmation = needsDeliveryConfirmation,
        hasReview = hasReview,
        rating = rating,
        reviewNote = reviewNote,
    )
}
