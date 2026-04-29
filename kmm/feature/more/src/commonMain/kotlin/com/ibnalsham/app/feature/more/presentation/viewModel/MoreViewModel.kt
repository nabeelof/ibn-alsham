package com.ibnalsham.app.feature.more.presentation.viewModel

import com.ibnalsham.app.presentation.mvi.KMMViewModel
import com.ibnalsham.app.presentation.mvi.MVI
import com.ibnalsham.app.library.data.cache.UserPreferencesRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

import com.ibnalsham.app.presentation.mvi.mvi
class MoreViewModel(
    private val userPreferencesRepository: UserPreferencesRepository,
) : KMMViewModel(),
    MVI<MoreViewModelContract.MyUiState, MoreViewModelContract.Action, MoreViewModelContract.SideEffect> 
    by mvi(MoreViewModelContract.MyUiState.defaultInstance()) {

    override fun onAction(event: MoreViewModelContract.Action) {
        when (event) {
            MoreViewModelContract.Action.ViewReady -> loadUserData()
            MoreViewModelContract.Action.ClickProfile -> navigate(MoreViewModelContract.SideEffect.NavigateToProfile)
            MoreViewModelContract.Action.ClickFavorites -> navigate(MoreViewModelContract.SideEffect.NavigateToFavorites)
            MoreViewModelContract.Action.ClickSettings -> navigate(MoreViewModelContract.SideEffect.NavigateToSettings)
            MoreViewModelContract.Action.ClickContactUs -> navigate(MoreViewModelContract.SideEffect.OpenUrl("https://wa.me/962700000000"))
            MoreViewModelContract.Action.Logout -> handleLogout()
        }
    }

    private fun loadUserData() {
        val token = userPreferencesRepository.getAuthToken()
        val isLoggedIn = !token.isNullOrBlank()
        val name = userPreferencesRepository.getUserName().orEmpty().ifBlank { if (isLoggedIn) "User" else "Guest" }
        val phone = userPreferencesRepository.getUserPhone().orEmpty().ifBlank { if (isLoggedIn) "-" else "Not logged in" }
        updateState { 
            copy(
                userName = name,
                userPhone = phone
            ) 
        }
    }

    private fun handleLogout() {
        viewModelScope.launch {
            updateState { copy(isLoggingOut = true) }
            userPreferencesRepository.clearAuthToken()
            userPreferencesRepository.clearUserIdentity()
            delay(800)
            navigate(MoreViewModelContract.SideEffect.NavigateToSplash)
        }
    }

    private fun navigate(effect: MoreViewModelContract.SideEffect) {
        viewModelScope.launch { emitEffect { effect } }
    }
}
