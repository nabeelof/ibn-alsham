package com.ibnalsham.app.feature.settings.presentation.viewModel

import com.ibnalsham.app.library.data.cache.AppPreferences
import com.ibnalsham.app.library.data.cache.PlatformInfo
import com.ibnalsham.app.library.data.cache.ThemeMode
import com.ibnalsham.app.presentation.mvi.KMMViewModel
import com.ibnalsham.app.presentation.mvi.MVI
import com.ibnalsham.app.presentation.mvi.mvi
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

/**
 * Settings.
 *
 * Theme changes are written through [AppPreferences], whose flow the composition
 * root observes — so picking a mode re-themes the running app rather than taking
 * effect on next launch.
 */
class SettingsViewModel(
    private val appPreferences: AppPreferences,
    private val platformInfo: PlatformInfo,
) : KMMViewModel(),
    MVI<SettingsViewModelContract.MyUiState, SettingsViewModelContract.Action, SettingsViewModelContract.SideEffect>
    by mvi(SettingsViewModelContract.MyUiState.defaultInstance()) {

    override fun onAction(event: SettingsViewModelContract.Action) {
        when (event) {
            SettingsViewModelContract.Action.ViewReady -> {
                updateState {
                    copy(
                        language = AppLanguage.fromTag(platformInfo.currentLanguageTag()),
                        appVersion = platformInfo.appVersion,
                    )
                }
                observeTheme()
            }

            is SettingsViewModelContract.Action.SelectThemeMode -> selectTheme(event.mode)

            is SettingsViewModelContract.Action.SelectLanguage -> {
                updateState { copy(language = event.language) }
                emit(SettingsViewModelContract.SideEffect.ApplyLanguage(event.language.tag))
            }

            SettingsViewModelContract.Action.ClickPrivacyPolicy ->
                emit(SettingsViewModelContract.SideEffect.OpenUrl(PRIVACY_URL))

            SettingsViewModelContract.Action.ClickTerms ->
                emit(SettingsViewModelContract.SideEffect.OpenUrl(TERMS_URL))
        }
    }

    private fun observeTheme() {
        viewModelScope.launch {
            appPreferences.themeMode.collectLatest { mode ->
                updateState { copy(themeMode = mode) }
            }
        }
    }

    private fun selectTheme(mode: ThemeMode) {
        if (mode == currentState.themeMode) return
        appPreferences.setThemeMode(mode)
    }

    private fun emit(effect: SettingsViewModelContract.SideEffect) {
        viewModelScope.launch { emitEffect { effect } }
    }

    private companion object {
        const val PRIVACY_URL = "https://ibnalsham.prolabtech.net/privacy"
        const val TERMS_URL = "https://ibnalsham.prolabtech.net/terms"
    }
}
