package com.ibnalsham.app.library.data.cache

interface UserPreferencesRepository {
    fun getViewType(): String
    fun saveViewType(viewType: String)
    fun getHomeCategoriesAsTabs(): Boolean
    fun saveHomeCategoriesAsTabs(enabled: Boolean)

    /** Stored as [ThemeMode.name]; unset means [ThemeMode.System]. */
    fun getThemeMode(): String?
    fun saveThemeMode(mode: String)
    fun getAuthToken(): String?
    fun saveAuthToken(token: String)
    fun clearAuthToken()
    fun getUserName(): String?
    fun saveUserName(name: String)
    fun getUserPhone(): String?
    fun saveUserPhone(phone: String)
    fun clearUserIdentity()
}

expect class InMemoryUserPreferencesRepository() : UserPreferencesRepository
