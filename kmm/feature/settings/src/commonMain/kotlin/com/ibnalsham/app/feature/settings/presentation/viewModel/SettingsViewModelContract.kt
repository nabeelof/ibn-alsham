package com.ibnalsham.app.feature.settings.presentation.viewModel

import com.ibnalsham.app.library.data.cache.ThemeMode
import com.ibnalsham.app.presentation.mvi.UiEvent
import com.ibnalsham.app.presentation.mvi.UiSideEffect
import com.ibnalsham.app.presentation.mvi.UiState

/** The languages the app ships translations for. */
enum class AppLanguage(val tag: String) {
    Arabic("ar"),
    English("en"),
    ;

    companion object {
        fun fromTag(tag: String?): AppLanguage =
            entries.firstOrNull { tag.orEmpty().startsWith(it.tag) } ?: Arabic
    }
}

interface SettingsViewModelContract {
    sealed class Action : UiEvent() {
        data object ViewReady : Action()
        data class SelectThemeMode(val mode: ThemeMode) : Action()
        data class SelectLanguage(val language: AppLanguage) : Action()
        data object ClickPrivacyPolicy : Action()
        data object ClickTerms : Action()
    }

    data class MyUiState(
        val themeMode: ThemeMode = ThemeMode.System,
        val language: AppLanguage = AppLanguage.Arabic,
        val appVersion: String = "",
    ) : UiState {
        companion object {
            fun defaultInstance() = MyUiState()
        }
    }

    sealed class SideEffect : UiSideEffect {
        /**
         * The platform applies the locale itself — on Android via
         * AppCompatDelegate, which recreates the activity.
         */
        data class ApplyLanguage(val tag: String) : SideEffect()
        data class OpenUrl(val url: String) : SideEffect()
    }
}
