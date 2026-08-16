package com.ibnalsham.app.feature.notifications.presentation.viewModel

import com.ibnalsham.app.data.network.error.DomainError
import com.ibnalsham.app.feature.domain.NotificationDomain
import com.ibnalsham.app.presentation.mvi.UiEvent
import com.ibnalsham.app.presentation.mvi.UiSideEffect
import com.ibnalsham.app.presentation.mvi.UiState
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

interface NotificationsViewModelContract {
    sealed class Action : UiEvent() {
        data object ViewReady : Action()
        data object Refresh : Action()
        data class NotificationClicked(val id: Int) : Action()
        data object MarkAllRead : Action()
        data object ClearAll : Action()
        data object SignInClicked : Action()
    }

    data class MyUiState(
        val isLoading: Boolean = true,
        val error: DomainError? = null,
        val notifications: ImmutableList<NotificationDomain> = persistentListOf(),
        val unreadCount: Int = 0,
        val isGuest: Boolean = false,
    ) : UiState {
        val isEmpty: Boolean get() = !isLoading && !isGuest && error == null && notifications.isEmpty()
        val requiresLogin: Boolean get() = isGuest || error == DomainError.Unauthorized
        val hasUnread: Boolean get() = unreadCount > 0

        companion object {
            fun defaultInstance() = MyUiState()
        }
    }

    sealed class SideEffect : UiSideEffect {
        /** Order notifications deep-link into order tracking. */
        data class NavigateToOrder(val orderId: Int) : SideEffect()
        data class NavigateToProduct(val productId: Int) : SideEffect()

        /** Ask the host to raise the sign-in gate sheet. */
        data object RequireSignIn : SideEffect()
        data class ShowSnackbar(val message: String) : SideEffect()
    }
}
