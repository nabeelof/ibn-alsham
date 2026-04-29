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

    override fun getAuthToken(): String? = prefs.getString("auth_token", null)

    override fun saveAuthToken(token: String) {
        prefs.edit().putString("auth_token", token).apply()
    }

    override fun clearAuthToken() {
        prefs.edit().remove("auth_token").apply()
    }

    override fun getUserName(): String? = prefs.getString("user_name", null)

    override fun saveUserName(name: String) {
        prefs.edit().putString("user_name", name).apply()
    }

    override fun getUserPhone(): String? = prefs.getString("user_phone", null)

    override fun saveUserPhone(phone: String) {
        prefs.edit().putString("user_phone", phone).apply()
    }

    override fun clearUserIdentity() {
        prefs.edit().remove("user_name").remove("user_phone").apply()
    }
}
