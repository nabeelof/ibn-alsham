package com.ibnalsham.app.library.data.cache

import platform.Foundation.NSUserDefaults

actual class InMemoryUserPreferencesRepository actual constructor() : UserPreferencesRepository {
    private val defaults: NSUserDefaults = NSUserDefaults.standardUserDefaults

    override fun getViewType(): String = defaults.stringForKey(KEY_VIEW_TYPE) ?: DEFAULT_VIEW_TYPE

    override fun saveViewType(viewType: String) {
        defaults.setObject(viewType, forKey = KEY_VIEW_TYPE)
    }

    override fun getHomeCategoriesAsTabs(): Boolean {
        return if (defaults.objectForKey(KEY_HOME_CATEGORIES_AS_TABS) == null) {
            DEFAULT_HOME_CATEGORIES_AS_TABS
        } else {
            defaults.boolForKey(KEY_HOME_CATEGORIES_AS_TABS)
        }
    }

    override fun saveHomeCategoriesAsTabs(enabled: Boolean) {
        defaults.setBool(enabled, forKey = KEY_HOME_CATEGORIES_AS_TABS)
    }

    override fun getAuthToken(): String? = defaults.stringForKey(KEY_AUTH_TOKEN)

    override fun saveAuthToken(token: String) {
        defaults.setObject(token, forKey = KEY_AUTH_TOKEN)
    }

    override fun clearAuthToken() {
        defaults.removeObjectForKey(KEY_AUTH_TOKEN)
    }

    override fun getUserName(): String? = defaults.stringForKey(KEY_USER_NAME)

    override fun saveUserName(name: String) {
        defaults.setObject(name, forKey = KEY_USER_NAME)
    }

    override fun getUserPhone(): String? = defaults.stringForKey(KEY_USER_PHONE)

    override fun saveUserPhone(phone: String) {
        defaults.setObject(phone, forKey = KEY_USER_PHONE)
    }

    override fun clearUserIdentity() {
        defaults.removeObjectForKey(KEY_USER_NAME)
        defaults.removeObjectForKey(KEY_USER_PHONE)
    }

    private companion object {
        const val KEY_VIEW_TYPE = "view_type"
        const val KEY_HOME_CATEGORIES_AS_TABS = "home_categories_as_tabs"
        const val KEY_AUTH_TOKEN = "auth_token"
        const val KEY_USER_NAME = "user_name"
        const val KEY_USER_PHONE = "user_phone"
        const val DEFAULT_VIEW_TYPE = "GRID"
        const val DEFAULT_HOME_CATEGORIES_AS_TABS = false
    }
}
