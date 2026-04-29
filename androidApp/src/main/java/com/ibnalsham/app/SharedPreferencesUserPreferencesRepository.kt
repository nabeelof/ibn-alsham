package com.ibnalsham.app

import android.content.Context
import com.ibnalsham.app.library.data.cache.UserPreferencesRepository

class SharedPreferencesUserPreferencesRepository(context: Context) : UserPreferencesRepository {
    private val prefs = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)

    override fun getViewType(): String = prefs.getString("view_type", "GRID") ?: "GRID"

    override fun saveViewType(viewType: String) {
        prefs.edit().putString("view_type", viewType).apply()
    }

    override fun getHomeCategoriesAsTabs(): Boolean = prefs.getBoolean("home_categories_as_tabs", false)

    override fun saveHomeCategoriesAsTabs(enabled: Boolean) {
        prefs.edit().putBoolean("home_categories_as_tabs", enabled).apply()
    }
}
