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

    private companion object {
        const val KEY_VIEW_TYPE = "view_type"
        const val KEY_HOME_CATEGORIES_AS_TABS = "home_categories_as_tabs"
        const val DEFAULT_VIEW_TYPE = "GRID"
        const val DEFAULT_HOME_CATEGORIES_AS_TABS = false
    }
}
