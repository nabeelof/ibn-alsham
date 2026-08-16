package com.ibnalsham.app.feature.orders.presentation.viewModel

import com.ibnalsham.app.data.network.error.DomainError
import com.ibnalsham.app.feature.domain.OrderDomain
import com.ibnalsham.app.feature.domain.OrderStatus
import com.ibnalsham.app.presentation.mvi.UiEvent
import com.ibnalsham.app.presentation.mvi.UiSideEffect
import com.ibnalsham.app.presentation.mvi.UiState
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

enum class OrderPhase {
    NONE,
    WAITING_CONFIRMATION,
    CONFIRMED_CAN_CANCEL,
    PROCESSING,
    DELIVERY_CHECK,
    REVIEW,
    COMPLETED,
}

interface OrdersViewModelContract {
    sealed class Action : UiEvent() {
        data object ViewReady : Action()
        data class SelectTab(val tab: OrderTab) : Action()
        data class OrderClicked(val orderId: Int) : Action()
        data class CancelOrder(val orderId: Int, val reason: String? = null) : Action()
        data class ConfirmDelivery(val orderId: Int) : Action()
        data class SubmitReview(val orderId: Int, val rating: Int, val note: String) : Action()
        data class StartTracking(val orderId: Int) : Action()
        data object StopTracking : Action()
        data object DismissReviewDialog : Action()
        data object SignInClicked : Action()
    }

    enum class OrderTab { CURRENT, PREVIOUS }

    data class MyUiState(
        val isLoading: Boolean = true,
        val error: DomainError? = null,
        val selectedTab: OrderTab = OrderTab.CURRENT,
        val currentOrders: ImmutableList<OrderDomain> = persistentListOf(),
        val previousOrders: ImmutableList<OrderDomain> = persistentListOf(),
        // Tracking state
        val activeOrder: OrderDomain? = null,
        val orderPhase: OrderPhase = OrderPhase.NONE,
        val isTracking: Boolean = false,
        // Review dialog
        val showReviewDialog: Boolean = false,
        val reviewOrderId: Int? = null,
        val isSubmittingReview: Boolean = false,
        val isCancelling: Boolean = false,
        val isGuest: Boolean = false,
    ) : UiState {
        val displayedOrders: ImmutableList<OrderDomain>
            get() = if (selectedTab == OrderTab.CURRENT) currentOrders else previousOrders

        /** Guests have no order history to show — they get the sign-in state. */
        val requiresLogin: Boolean get() = isGuest

        val isEmpty: Boolean
            get() = !isLoading && !isGuest && error == null && displayedOrders.isEmpty()

        companion object {
            fun defaultInstance() = MyUiState()
        }
    }

    sealed class SideEffect : UiSideEffect {
        data class NavigateToOrderDetails(val orderId: Int) : SideEffect()
        data class ShowSnackbar(val message: String) : SideEffect()
        data object OrderCancelled : SideEffect()
        data object ReviewSubmitted : SideEffect()

        /** Ask the host to raise the sign-in gate sheet. */
        data object RequireSignIn : SideEffect()
    }
}

fun OrderDomain.toPhase(): OrderPhase = when {
    status == OrderStatus.PENDING -> OrderPhase.WAITING_CONFIRMATION
    status == OrderStatus.CONFIRMED && canCancel -> OrderPhase.CONFIRMED_CAN_CANCEL
    status in listOf(OrderStatus.CONFIRMED, OrderStatus.PREPARING, OrderStatus.READY, OrderStatus.OUT_FOR_DELIVERY) && needsDeliveryConfirmation -> OrderPhase.DELIVERY_CHECK
    status in listOf(OrderStatus.CONFIRMED, OrderStatus.PREPARING, OrderStatus.READY, OrderStatus.OUT_FOR_DELIVERY) -> OrderPhase.PROCESSING
    status == OrderStatus.DELIVERED && !hasReview -> OrderPhase.REVIEW
    status in listOf(OrderStatus.COMPLETED, OrderStatus.CANCELLED) -> OrderPhase.COMPLETED
    status == OrderStatus.DELIVERED && hasReview -> OrderPhase.COMPLETED
    else -> OrderPhase.NONE
}
