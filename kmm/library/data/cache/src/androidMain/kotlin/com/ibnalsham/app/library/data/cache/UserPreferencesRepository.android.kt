package com.ibnalsham.app.library.data.cache

import android.content.Context
import org.koin.core.context.GlobalContext

actual class InMemoryUserPreferencesRepository actual constructor() : UserPreferencesRepository {
    private val sharedPreferences by lazy {
        val context = GlobalContext.get().get<Context>()
        context.getSharedPreferences("ibn_alsham_user_preferences", Context.MODE_PRIVATE)
    }

    override fun getViewType(): String = sharedPreferences.getString(KEY_VIEW_TYPE, DEFAULT_VIEW_TYPE) ?: DEFAULT_VIEW_TYPE

    override fun saveViewType(viewType: String) {
        sharedPreferences.edit().putString(KEY_VIEW_TYPE, viewType).apply()
    }

    override fun getHomeCategoriesAsTabs(): Boolean =
        sharedPreferences.getBoolean(KEY_HOME_CATEGORIES_AS_TABS, DEFAULT_HOME_CATEGORIES_AS_TABS)

    override fun saveHomeCategoriesAsTabs(enabled: Boolean) {
        sharedPreferences.edit().putBoolean(KEY_HOME_CATEGORIES_AS_TABS, enabled).apply()
    }

    override fun getThemeMode(): String? = sharedPreferences.getString(KEY_THEME_MODE, null)

    override fun saveThemeMode(mode: String) {
        sharedPreferences.edit().putString(KEY_THEME_MODE, mode).apply()
    }

    override fun getAuthToken(): String? = sharedPreferences.getString(KEY_AUTH_TOKEN, null)

    override fun saveAuthToken(token: String) {
        sharedPreferences.edit().putString(KEY_AUTH_TOKEN, token).apply()
    }

    override fun clearAuthToken() {
        sharedPreferences.edit().remove(KEY_AUTH_TOKEN).apply()
    }

    override fun getUserName(): String? = sharedPreferences.getString(KEY_USER_NAME, null)

    override fun saveUserName(name: String) {
        sharedPreferences.edit().putString(KEY_USER_NAME, name).apply()
    }

    override fun getUserPhone(): String? = sharedPreferences.getString(KEY_USER_PHONE, null)

    override fun saveUserPhone(phone: String) {
        sharedPreferences.edit().putString(KEY_USER_PHONE, phone).apply()
    }

    override fun clearUserIdentity() {
        sharedPreferences.edit().remove(KEY_USER_NAME).remove(KEY_USER_PHONE).apply()
    }

    private companion object {
        const val KEY_VIEW_TYPE = "view_type"
        const val KEY_HOME_CATEGORIES_AS_TABS = "home_categories_as_tabs"
        const val KEY_THEME_MODE = "theme_mode"
        const val KEY_AUTH_TOKEN = "auth_token"
        const val KEY_USER_NAME = "user_name"
        const val KEY_USER_PHONE = "user_phone"
        const val DEFAULT_VIEW_TYPE = "GRID"
        const val DEFAULT_HOME_CATEGORIES_AS_TABS = false
    }
}
