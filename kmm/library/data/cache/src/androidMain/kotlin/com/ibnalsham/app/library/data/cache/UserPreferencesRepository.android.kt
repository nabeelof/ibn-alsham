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

    private companion object {
        const val KEY_VIEW_TYPE = "view_type"
        const val KEY_HOME_CATEGORIES_AS_TABS = "home_categories_as_tabs"
        const val DEFAULT_VIEW_TYPE = "GRID"
        const val DEFAULT_HOME_CATEGORIES_AS_TABS = false
    }
}
