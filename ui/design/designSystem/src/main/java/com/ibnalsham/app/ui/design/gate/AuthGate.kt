package com.ibnalsham.app.ui.design.gate

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import com.ibnalsham.app.library.data.cache.GatedFeature
import com.ibnalsham.app.library.data.cache.SessionManager

/**
 * Runs gated work, asking the user to sign in first when they are a guest.
 *
 * The contract that matters is [require]: the caller hands over what it wanted
 * to do, and it either happens now or happens after a successful sign-in. No
 * call site has to know which of those two it got, which is what keeps the
 * "return to where you were" behaviour from being re-implemented per screen.
 */
@Stable
class AuthGateController internal constructor(
    private val sessionManager: SessionManager,
) {

    /** The gate currently being shown, or null when the sheet is closed. */
    var pending: GatedFeature? by mutableStateOf(null)
        private set

    private var pendingAction: (() -> Unit)? = null

    /**
     * Runs [action] if the user may use [feature], otherwise raises the sign-in
     * sheet and runs it once they are signed in.
     */
    fun require(feature: GatedFeature, action: () -> Unit) {
        if (sessionManager.canAccess(feature)) {
            action()
        } else {
            pendingAction = action
            pending = feature
        }
    }

    /** Opens the sheet without queuing follow-up work. */
    fun prompt(feature: GatedFeature) {
        pendingAction = null
        pending = feature
    }

    /** Called by the host when sign-in completed inside the sheet. */
    internal fun onSignedIn() {
        val action = pendingAction
        pendingAction = null
        pending = null
        action?.invoke()
    }

    /** Called when the user dismissed the sheet without signing in. */
    internal fun onDismissed() {
        pendingAction = null
        pending = null
    }
}

@Composable
fun rememberAuthGateController(sessionManager: SessionManager): AuthGateController =
    remember(sessionManager) { AuthGateController(sessionManager) }

/**
 * Reachable from any screen so a gated tap does not need a callback threaded
 * down through every route.
 */
val LocalAuthGate = staticCompositionLocalOf<AuthGateController> {
    error("No AuthGateController provided. Wrap the app in AuthGateHost.")
}
