package com.ibnalsham.app.library.data.cache

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Everything a guest is not allowed to do.
 *
 * Each entry carries the copy shown on the sign-in sheet, so a gate looks
 * intentional ("sign in to keep your favourites") instead of a generic wall.
 */
enum class GatedFeature {
    Favorites,
    Orders,
    Profile,
    Checkout,
    Notifications,
    Reviews,
    Addresses,
    Wallet,
}

/**
 * Single source of truth for "is somebody signed in right now".
 *
 * Before this existed every screen called [UserPreferencesRepository.getAuthToken]
 * on its own, so a screen composed while signed out never learned about a later
 * sign-in. Reading [isSignedIn] gives callers a reactive answer instead.
 *
 * All writes to the auth token go through here — [signIn] and [signOut] are the
 * only supported way to change session state.
 */
class SessionManager(
    private val userPreferences: UserPreferencesRepository,
) {

    private val _session = MutableStateFlow(readSession())

    /** Emits a new value whenever the user signs in or out. */
    val session: StateFlow<Session> = _session.asStateFlow()

    val isSignedIn: Boolean
        get() = _session.value.isSignedIn

    val authToken: String?
        get() = _session.value.token

    fun signIn(token: String, name: String?, phone: String?) {
        userPreferences.saveAuthToken(token)
        name?.takeIf { it.isNotBlank() }?.let(userPreferences::saveUserName)
        phone?.takeIf { it.isNotBlank() }?.let(userPreferences::saveUserPhone)
        _session.value = readSession()
    }

    fun signOut() {
        userPreferences.clearAuthToken()
        userPreferences.clearUserIdentity()
        _session.value = Session.Guest
    }

    /** Re-reads persisted state. Used after a write that bypassed this class. */
    fun refresh() {
        _session.value = readSession()
    }

    /** True when [feature] is available to the current session. */
    fun canAccess(feature: GatedFeature): Boolean = isSignedIn

    private fun readSession(): Session {
        val token = userPreferences.getAuthToken()
        return if (token.isNullOrBlank()) {
            Session.Guest
        } else {
            Session.SignedIn(
                token = token,
                name = userPreferences.getUserName().orEmpty(),
                phone = userPreferences.getUserPhone().orEmpty(),
            )
        }
    }

    sealed class Session {
        abstract val isSignedIn: Boolean
        abstract val token: String?

        data object Guest : Session() {
            override val isSignedIn: Boolean = false
            override val token: String? = null
        }

        data class SignedIn(
            override val token: String,
            val name: String,
            val phone: String,
        ) : Session() {
            override val isSignedIn: Boolean = true
        }
    }
}
