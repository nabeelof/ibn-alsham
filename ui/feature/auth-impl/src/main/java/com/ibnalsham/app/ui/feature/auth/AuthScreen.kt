package com.ibnalsham.app.ui.feature.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.ibnalsham.app.feature.auth.presentation.viewModel.AuthViewModel
import com.ibnalsham.app.feature.auth.presentation.viewModel.AuthViewModelContract
import com.ibnalsham.app.ui.design.components.CircleIconButton
import com.ibnalsham.app.ui.design.components.TextAction
import com.ibnalsham.app.ui.design.motion.enterFromBottom
import com.ibnalsham.app.ui.design.theme.AppTheme
import com.ibnalsham.app.ui.design.theme.Spacing
import com.ibnalsham.app.ui.design.designSystem.R as DesignR
import com.ibnalsham.app.ui.feature.authimpl.R
import org.koin.androidx.compose.koinViewModel

/**
 * Full-screen sign-in.
 *
 * Still reachable (deep links, expired sessions, the More screen) but no longer
 * the only way in — most guests meet [GateAuthForm] inside the gate sheet and
 * never leave the screen they were on.
 */
@Composable
fun AuthRoute(
    viewModel: AuthViewModel = koinViewModel(),
    onNavigateToHome: () -> Unit,
    onShowSnackbar: (String) -> Unit,
    onDismiss: (() -> Unit)? = null,
) {
    val state by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.sideEffects.collect { effect ->
            when (effect) {
                AuthViewModelContract.SideEffect.NavigateToHome -> onNavigateToHome()
                is AuthViewModelContract.SideEffect.ShowSnackbar -> onShowSnackbar(effect.message)
            }
        }
    }

    AuthScreen(state = state, onAction = viewModel::onAction, onDismiss = onDismiss)
}

@Composable
fun AuthScreen(
    state: AuthViewModelContract.MyUiState,
    onAction: (AuthViewModelContract.Action) -> Unit,
    onDismiss: (() -> Unit)? = null,
) {
    val colors = AppTheme.colors

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background),
    ) {
        // Warm gradient block behind the brand mark, so the screen opens on the
        // same colour language as the rest of the app rather than plain grey.
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(280.dp)
                .background(colors.heroGradient),
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .verticalScroll(rememberScrollState())
                .imePadding()
                .navigationBarsPadding(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            if (onDismiss != null) {
                Box(modifier = Modifier.fillMaxWidth().padding(Spacing.md)) {
                    CircleIconButton(
                        icon = Icons.Rounded.Close,
                        contentDescription = stringResource(DesignR.string.close),
                        onClick = onDismiss,
                        onImage = true,
                        modifier = Modifier.align(Alignment.CenterStart),
                    )
                }
            } else {
                Spacer(Modifier.height(Spacing.huge))
            }

            Box(
                modifier = Modifier
                    .size(104.dp)
                    .clip(RoundedCornerShape(30.dp))
                    .background(colors.surface)
                    .enterFromBottom(index = 0),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = stringResource(R.string.app_name_ar),
                    style = MaterialTheme.typography.titleLarge,
                    color = colors.brand,
                    textAlign = TextAlign.Center,
                )
            }

            Spacer(Modifier.height(Spacing.xxxl))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Spacing.gutter)
                    .enterFromBottom(index = 1)
                    .clip(MaterialTheme.shapes.large)
                    .background(colors.surface)
                    .padding(Spacing.xxl),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                AuthFlow(state = state, onAction = onAction)
            }

            if (onDismiss != null) {
                Spacer(Modifier.height(Spacing.lg))
                TextAction(
                    text = stringResource(DesignR.string.gate_continue_browsing),
                    onClick = onDismiss,
                    color = colors.textSecondary,
                )
            }

            Spacer(Modifier.height(Spacing.huge))
        }
    }
}
