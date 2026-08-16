package com.ibnalsham.app.feature.profile.presentation.viewModel

import com.ibnalsham.app.core.common.onFailure
import com.ibnalsham.app.core.common.onSuccess
import com.ibnalsham.app.data.network.error.DomainError
import com.ibnalsham.app.data.profile.ProfileRepository
import com.ibnalsham.app.feature.domain.UserProfileDomain
import com.ibnalsham.app.library.data.cache.SessionManager
import com.ibnalsham.app.library.data.cache.UserPreferencesRepository
import com.ibnalsham.app.presentation.mvi.KMMViewModel
import com.ibnalsham.app.presentation.mvi.MVI
import com.ibnalsham.app.presentation.mvi.mvi
import kotlinx.coroutines.launch

class ProfileViewModel(
    private val profileRepository: ProfileRepository,
    private val userPreferences: UserPreferencesRepository,
    private val sessionManager: SessionManager,
) : KMMViewModel(),
    MVI<ProfileViewModelContract.MyUiState, ProfileViewModelContract.Action, ProfileViewModelContract.SideEffect>
    by mvi(ProfileViewModelContract.MyUiState.defaultInstance()) {

    override fun onAction(event: ProfileViewModelContract.Action) {
        when (event) {
            ProfileViewModelContract.Action.ViewReady -> loadProfile()
            ProfileViewModelContract.Action.Refresh -> loadProfile()

            ProfileViewModelContract.Action.StartEditing ->
                updateState { copy(isEditing = true, nameInput = profile?.name.orEmpty()) }

            ProfileViewModelContract.Action.CancelEditing ->
                updateState { copy(isEditing = false, nameInput = profile?.name.orEmpty()) }

            is ProfileViewModelContract.Action.UpdateName -> updateState { copy(nameInput = event.name) }
            ProfileViewModelContract.Action.SaveProfile -> saveProfile()

            ProfileViewModelContract.Action.RequestDeleteAccount -> updateState { copy(showDeleteDialog = true) }
            ProfileViewModelContract.Action.DismissDeleteDialog -> updateState { copy(showDeleteDialog = false) }
            ProfileViewModelContract.Action.ConfirmDeleteAccount -> deleteAccount()
            ProfileViewModelContract.Action.Logout -> logout()
        }
    }

    private fun loadProfile() {
        // A guest has nothing to fetch — skip the round trip that would only 401.
        if (!sessionManager.isSignedIn) {
            viewModelScope.launch { emitEffect { ProfileViewModelContract.SideEffect.NavigateToAuth } }
            return
        }

        viewModelScope.launch {
            updateState { copy(isLoading = true, error = null) }

            profileRepository.getProfile().onSuccess { profile ->
                cacheIdentity(profile)
                updateState {
                    copy(isLoading = false, error = null, profile = profile, nameInput = profile.name)
                }
            }.onFailure { error ->
                // Fall back to the locally cached identity so the screen is not blank offline.
                val cached = cachedProfile()
                updateState { copy(isLoading = false, error = error, profile = profile ?: cached) }

                if (error == DomainError.Unauthorized) {
                    emitEffect { ProfileViewModelContract.SideEffect.NavigateToAuth }
                }
            }
        }
    }

    private fun saveProfile() {
        val name = currentState.nameInput.trim()
        if (!currentState.canSave) return

        viewModelScope.launch {
            updateState { copy(isSaving = true) }

            profileRepository.updateName(name).onSuccess { updated ->
                userPreferences.saveUserName(updated.name)
                updateState {
                    copy(
                        isSaving = false,
                        isEditing = false,
                        // The update endpoint does not return counters; keep the loaded ones.
                        profile = profile?.copy(name = updated.name) ?: updated,
                        nameInput = updated.name,
                    )
                }
                emitEffect { ProfileViewModelContract.SideEffect.ShowSnackbar(SAVED_MESSAGE) }
            }.onFailure {
                updateState { copy(isSaving = false) }
                emitEffect { ProfileViewModelContract.SideEffect.ShowSnackbar(SAVE_FAILED_MESSAGE) }
            }
        }
    }

    private fun deleteAccount() {
        viewModelScope.launch {
            updateState { copy(isDeleting = true) }

            profileRepository.deleteAccount().onSuccess {
                clearSession()
                updateState { copy(isDeleting = false, showDeleteDialog = false) }
                emitEffect { ProfileViewModelContract.SideEffect.AccountDeleted }
            }.onFailure {
                updateState { copy(isDeleting = false, showDeleteDialog = false) }
                emitEffect { ProfileViewModelContract.SideEffect.ShowSnackbar(DELETE_FAILED_MESSAGE) }
            }
        }
    }

    private fun logout() {
        clearSession()
        viewModelScope.launch { emitEffect { ProfileViewModelContract.SideEffect.NavigateToAuth } }
    }

    private fun clearSession() {
        sessionManager.signOut()
    }

    private fun cacheIdentity(profile: UserProfileDomain) {
        userPreferences.saveUserName(profile.name)
        userPreferences.saveUserPhone(profile.phone)
    }

    private fun cachedProfile(): UserProfileDomain? {
        val name = userPreferences.getUserName() ?: return null
        return UserProfileDomain(
            id = 0,
            name = name,
            phone = userPreferences.getUserPhone().orEmpty(),
        )
    }

    private companion object {
        const val SAVED_MESSAGE = "تم حفظ التعديلات"
        const val SAVE_FAILED_MESSAGE = "تعذّر حفظ التعديلات"
        const val DELETE_FAILED_MESSAGE = "تعذّر حذف الحساب"
    }
}
