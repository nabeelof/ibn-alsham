package com.ibnalsham.app.feature.notifications.presentation.viewModel

import com.ibnalsham.app.core.common.onFailure
import com.ibnalsham.app.core.common.onSuccess
import com.ibnalsham.app.data.network.error.DomainError
import com.ibnalsham.app.data.notification.NotificationRepository
import com.ibnalsham.app.library.data.cache.SessionManager
import com.ibnalsham.app.presentation.mvi.KMMViewModel
import com.ibnalsham.app.presentation.mvi.MVI
import com.ibnalsham.app.presentation.mvi.mvi
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class NotificationsViewModel(
    private val notificationRepository: NotificationRepository,
    private val sessionManager: SessionManager,
) : KMMViewModel(),
    MVI<
        NotificationsViewModelContract.MyUiState,
        NotificationsViewModelContract.Action,
        NotificationsViewModelContract.SideEffect,
        >
    by mvi(NotificationsViewModelContract.MyUiState.defaultInstance()) {

    override fun onAction(event: NotificationsViewModelContract.Action) {
        when (event) {
            NotificationsViewModelContract.Action.ViewReady -> observeSession()
            NotificationsViewModelContract.Action.Refresh -> load()
            is NotificationsViewModelContract.Action.NotificationClicked -> open(event.id)
            NotificationsViewModelContract.Action.MarkAllRead -> markAllRead()
            NotificationsViewModelContract.Action.ClearAll -> clearAll()
            NotificationsViewModelContract.Action.SignInClicked -> viewModelScope.launch {
                emitEffect { NotificationsViewModelContract.SideEffect.RequireSignIn }
            }
        }
    }

    /** Notifications are per-account; a guest sees the sign-in state, not a 401. */
    private fun observeSession() {
        viewModelScope.launch {
            sessionManager.session.collectLatest { session ->
                if (session.isSignedIn) {
                    updateState { copy(isGuest = false) }
                    load()
                } else {
                    updateState {
                        copy(
                            isGuest = true,
                            isLoading = false,
                            error = null,
                            notifications = persistentListOf(),
                            unreadCount = 0,
                        )
                    }
                }
            }
        }
    }

    private fun load() {
        if (!sessionManager.isSignedIn) return

        viewModelScope.launch {
            updateState { copy(isLoading = true, error = null) }

            notificationRepository.getNotifications().onSuccess { notifications ->
                updateState {
                    copy(
                        isLoading = false,
                        error = null,
                        notifications = notifications,
                        unreadCount = notifications.count { !it.isRead },
                    )
                }
            }.onFailure { error ->
                updateState { copy(isLoading = false, error = error) }
                // A token that expired mid-session still needs to drop the session.
                if (error == DomainError.Unauthorized) {
                    sessionManager.signOut()
                }
            }
        }
    }

    private fun open(id: Int) {
        val notification = currentState.notifications.firstOrNull { it.id == id } ?: return

        viewModelScope.launch {
            if (!notification.isRead) {
                notificationRepository.markRead(id)
                updateState {
                    copy(
                        notifications = notifications
                            .map { if (it.id == id) it.copy(isRead = true) else it }
                            .toImmutableList(),
                        unreadCount = (unreadCount - 1).coerceAtLeast(0),
                    )
                }
            }

            val orderId = notification.orderId
            val productId = notification.productId
            when {
                orderId != null ->
                    emitEffect { NotificationsViewModelContract.SideEffect.NavigateToOrder(orderId) }

                productId != null ->
                    emitEffect { NotificationsViewModelContract.SideEffect.NavigateToProduct(productId) }
            }
        }
    }

    private fun markAllRead() {
        if (!currentState.hasUnread) return

        viewModelScope.launch {
            notificationRepository.markAllRead().onSuccess {
                updateState {
                    copy(
                        notifications = notifications.map { it.copy(isRead = true) }.toImmutableList(),
                        unreadCount = 0,
                    )
                }
            }.onFailure {
                emitEffect { NotificationsViewModelContract.SideEffect.ShowSnackbar(ACTION_FAILED_MESSAGE) }
            }
        }
    }

    private fun clearAll() {
        if (currentState.notifications.isEmpty()) return

        viewModelScope.launch {
            notificationRepository.delete(emptyList()).onSuccess {
                updateState { copy(notifications = persistentListOf(), unreadCount = 0) }
            }.onFailure {
                emitEffect { NotificationsViewModelContract.SideEffect.ShowSnackbar(ACTION_FAILED_MESSAGE) }
            }
        }
    }

    private companion object {
        const val ACTION_FAILED_MESSAGE = "تعذّر تنفيذ العملية"
    }
}
