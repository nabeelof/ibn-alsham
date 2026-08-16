package com.ibnalsham.app.feature.more.presentation.viewModel

import com.ibnalsham.app.library.data.cache.GatedFeature
import com.ibnalsham.app.library.data.cache.SessionManager
import com.ibnalsham.app.presentation.mvi.KMMViewModel
import com.ibnalsham.app.presentation.mvi.MVI
import com.ibnalsham.app.presentation.mvi.mvi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class MoreViewModel(
    private val sessionManager: SessionManager,
) : KMMViewModel(),
    MVI<MoreViewModelContract.MyUiState, MoreViewModelContract.Action, MoreViewModelContract.SideEffect>
    by mvi(MoreViewModelContract.MyUiState.defaultInstance()) {

    override fun onAction(event: MoreViewModelContract.Action) {
        when (event) {
            MoreViewModelContract.Action.ViewReady -> observeSession()

            MoreViewModelContract.Action.ClickProfile ->
                gated(GatedFeature.Profile, MoreViewModelContract.SideEffect.NavigateToProfile)

            MoreViewModelContract.Action.ClickFavorites ->
                gated(GatedFeature.Favorites, MoreViewModelContract.SideEffect.NavigateToFavorites)

            MoreViewModelContract.Action.ClickOrders ->
                gated(GatedFeature.Orders, MoreViewModelContract.SideEffect.NavigateToOrders)

            MoreViewModelContract.Action.ClickSettings ->
                emit(MoreViewModelContract.SideEffect.NavigateToSettings)

            MoreViewModelContract.Action.ClickContactUs ->
                emit(MoreViewModelContract.SideEffect.OpenUrl(WHATSAPP_URL))

            MoreViewModelContract.Action.ClickSignIn ->
                emit(MoreViewModelContract.SideEffect.RequireSignIn(GatedFeature.Profile))

            MoreViewModelContract.Action.Logout -> handleLogout()
        }
    }

    /**
     * Signing in from the gate sheet has to refresh this screen without a
     * re-entry, so the header follows the session flow rather than a one-shot read.
     */
    private fun observeSession() {
        viewModelScope.launch {
            sessionManager.session.collectLatest { session ->
                val signedIn = session as? SessionManager.Session.SignedIn
                updateState {
                    copy(
                        isSignedIn = signedIn != null,
                        userName = signedIn?.name?.ifBlank { null }.orEmpty(),
                        userPhone = signedIn?.phone.orEmpty(),
                    )
                }
            }
        }
    }

    /** Runs [onAllowed] when signed in, otherwise asks for the gate sheet. */
    private fun gated(feature: GatedFeature, onAllowed: MoreViewModelContract.SideEffect) {
        if (sessionManager.canAccess(feature)) {
            emit(onAllowed)
        } else {
            emit(MoreViewModelContract.SideEffect.RequireSignIn(feature))
        }
    }

    private fun handleLogout() {
        viewModelScope.launch {
            updateState { copy(isLoggingOut = true) }
            sessionManager.signOut()
            delay(LOGOUT_SETTLE_MS)
            updateState { copy(isLoggingOut = false) }
            emitEffect { MoreViewModelContract.SideEffect.NavigateToSplash }
        }
    }

    private fun emit(effect: MoreViewModelContract.SideEffect) {
        viewModelScope.launch { emitEffect { effect } }
    }

    private companion object {
        const val WHATSAPP_URL = "https://wa.me/962700000000"
        const val LOGOUT_SETTLE_MS = 400L
    }
}
