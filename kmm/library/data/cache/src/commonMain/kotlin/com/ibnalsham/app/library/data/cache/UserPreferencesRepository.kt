package com.ibnalsham.app.library.data.cache

interface UserPreferencesRepository {
    fun getViewType(): String
    fun saveViewType(viewType: String)
    fun getHomeCategoriesAsTabs(): Boolean
    fun saveHomeCategoriesAsTabs(enabled: Boolean)
}

expect class InMemoryUserPreferencesRepository() : UserPreferencesRepository
