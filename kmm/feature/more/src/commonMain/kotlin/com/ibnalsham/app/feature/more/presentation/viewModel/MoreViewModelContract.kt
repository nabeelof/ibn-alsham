package com.ibnalsham.app.feature.more.presentation.viewModel

import com.ibnalsham.app.presentation.mvi.UiEvent
import com.ibnalsham.app.presentation.mvi.UiSideEffect
import com.ibnalsham.app.presentation.mvi.UiState

interface MoreViewModelContract {
    sealed class Action : UiEvent() {
        data object ViewReady : Action()
        data object ClickProfile : Action()
        data object ClickFavorites : Action()
        data object ClickSettings : Action()
        data object ClickContactUs : Action()
        data object Logout : Action()
    }

    data class MyUiState(
        val userName: String = "",
        val userPhone: String = "",
        val isLoggingOut: Boolean = false
    ) : UiState {
        companion object {
            fun defaultInstance() = MyUiState()
        }
    }

    sealed class SideEffect : UiSideEffect {
        data object NavigateToProfile : SideEffect()
        data object NavigateToFavorites : SideEffect()
        data object NavigateToSettings : SideEffect()
        data object NavigateToSplash : SideEffect() // Used after logout
        data class OpenUrl(val url: String) : SideEffect()
    }
}
