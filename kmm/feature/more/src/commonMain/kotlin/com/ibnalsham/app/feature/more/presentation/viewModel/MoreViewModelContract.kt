package com.ibnalsham.app.feature.more.presentation.viewModel

import com.ibnalsham.app.library.data.cache.GatedFeature
import com.ibnalsham.app.presentation.mvi.UiEvent
import com.ibnalsham.app.presentation.mvi.UiSideEffect
import com.ibnalsham.app.presentation.mvi.UiState

interface MoreViewModelContract {
    sealed class Action : UiEvent() {
        data object ViewReady : Action()
        data object ClickProfile : Action()
        data object ClickFavorites : Action()
        data object ClickOrders : Action()
        data object ClickSettings : Action()
        data object ClickContactUs : Action()
        data object ClickSignIn : Action()
        data object Logout : Action()
    }

    data class MyUiState(
        val userName: String = "",
        val userPhone: String = "",
        val isSignedIn: Boolean = false,
        val isLoggingOut: Boolean = false,
    ) : UiState {
        companion object {
            fun defaultInstance() = MyUiState()
        }
    }

    sealed class SideEffect : UiSideEffect {
        data object NavigateToProfile : SideEffect()
        data object NavigateToFavorites : SideEffect()
        data object NavigateToOrders : SideEffect()
        data object NavigateToSettings : SideEffect()
        data object NavigateToSplash : SideEffect() // Used after logout

        /** Guest tapped something they need an account for. */
        data class RequireSignIn(val feature: GatedFeature) : SideEffect()
        data class OpenUrl(val url: String) : SideEffect()
    }
}
