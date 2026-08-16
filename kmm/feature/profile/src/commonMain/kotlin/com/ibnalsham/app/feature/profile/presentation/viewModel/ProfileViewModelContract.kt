package com.ibnalsham.app.feature.profile.presentation.viewModel

import com.ibnalsham.app.data.network.error.DomainError
import com.ibnalsham.app.feature.domain.UserProfileDomain
import com.ibnalsham.app.presentation.mvi.UiEvent
import com.ibnalsham.app.presentation.mvi.UiSideEffect
import com.ibnalsham.app.presentation.mvi.UiState

interface ProfileViewModelContract {
    sealed class Action : UiEvent() {
        data object ViewReady : Action()
        data object Refresh : Action()
        data object StartEditing : Action()
        data object CancelEditing : Action()
        data class UpdateName(val name: String) : Action()
        data object SaveProfile : Action()
        data object RequestDeleteAccount : Action()
        data object ConfirmDeleteAccount : Action()
        data object DismissDeleteDialog : Action()
        data object Logout : Action()
    }

    data class MyUiState(
        val isLoading: Boolean = true,
        val error: DomainError? = null,
        val profile: UserProfileDomain? = null,
        val isEditing: Boolean = false,
        val nameInput: String = "",
        val isSaving: Boolean = false,
        val showDeleteDialog: Boolean = false,
        val isDeleting: Boolean = false,
    ) : UiState {
        val requiresLogin: Boolean get() = error == DomainError.Unauthorized

        /** Name must be a real name before the save button lights up. */
        val canSave: Boolean
            get() = !isSaving && nameInput.trim().length >= MIN_NAME_LENGTH &&
                nameInput.trim() != profile?.name

        companion object {
            const val MIN_NAME_LENGTH = 2

            fun defaultInstance() = MyUiState()
        }
    }

    sealed class SideEffect : UiSideEffect {
        data class ShowSnackbar(val message: String) : SideEffect()
        data object NavigateToAuth : SideEffect()
        data object NavigateBack : SideEffect()
        data object AccountDeleted : SideEffect()
    }
}
