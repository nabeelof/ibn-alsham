package com.ibnalsham.app.ui.feature.auth

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.ibnalsham.app.feature.auth.presentation.viewModel.AuthViewModel
import com.ibnalsham.app.feature.auth.presentation.viewModel.AuthViewModelContract
import com.ibnalsham.app.ui.design.theme.Spacing
import org.koin.androidx.compose.koinViewModel

/**
 * The sign-in flow as it appears inside the gate bottom sheet.
 *
 * Headings are suppressed because the sheet already explains why sign-in is
 * being asked for — repeating "Sign in" above the phone field would push the
 * keypad off-screen on shorter devices.
 *
 * [onSignedIn] fires on the view model's success effect; the gate controller
 * then replays whatever the user originally tapped.
 */
@Composable
fun GateAuthForm(
    onSignedIn: () -> Unit,
    modifier: Modifier = Modifier,
    onShowSnackbar: (String) -> Unit = {},
    viewModel: AuthViewModel = koinViewModel(),
) {
    val state by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.sideEffects.collect { effect ->
            when (effect) {
                AuthViewModelContract.SideEffect.NavigateToHome -> onSignedIn()
                is AuthViewModelContract.SideEffect.ShowSnackbar -> onShowSnackbar(effect.message)
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .imePadding(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        AuthFlow(state = state, onAction = viewModel::onAction, showHeadings = false)
        Spacer(Modifier.height(Spacing.md))
    }
}
