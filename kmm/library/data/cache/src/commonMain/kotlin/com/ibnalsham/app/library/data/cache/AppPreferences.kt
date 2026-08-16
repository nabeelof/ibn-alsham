package com.ibnalsham.app.library.data.cache

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/** How the app picks between the light and dark palettes. */
enum class ThemeMode {
    /** Follow the device setting. */
    System,
    Light,
    Dark,
    ;

    companion object {
        fun fromName(name: String?): ThemeMode =
            entries.firstOrNull { it.name == name } ?: System
    }
}

/**
 * Reactive app-level preferences that are not tied to an account.
 *
 * Theme lives here rather than in a view model because the choice has to reach
 * the composition root — the whole app re-themes, not one screen. Reading it as
 * a [StateFlow] is what makes the switch apply instantly instead of on restart.
 */
class AppPreferences(
    private val userPreferences: UserPreferencesRepository,
) {

    private val _themeMode = MutableStateFlow(ThemeMode.fromName(userPreferences.getThemeMode()))

    val themeMode: StateFlow<ThemeMode> = _themeMode.asStateFlow()

    fun setThemeMode(mode: ThemeMode) {
        userPreferences.saveThemeMode(mode.name)
        _themeMode.value = mode
    }
}
