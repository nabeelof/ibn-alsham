package com.ibnalsham.app.ui.feature.profile

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.DeleteForever
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material.icons.rounded.Logout
import androidx.compose.material.icons.rounded.PersonOutline
import androidx.compose.material.icons.rounded.ReceiptLong
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.ibnalsham.app.feature.profile.presentation.viewModel.ProfileViewModel
import com.ibnalsham.app.feature.profile.presentation.viewModel.ProfileViewModelContract
import com.ibnalsham.app.library.data.cache.GatedFeature
import com.ibnalsham.app.ui.design.components.AnimatedCounter
import com.ibnalsham.app.ui.design.components.AppCard
import com.ibnalsham.app.ui.design.components.CircleIconButton
import com.ibnalsham.app.ui.design.components.ErrorState
import com.ibnalsham.app.ui.design.components.FullWidthButton
import com.ibnalsham.app.ui.design.components.LoadingState
import com.ibnalsham.app.ui.design.components.SecondaryButton
import com.ibnalsham.app.ui.design.components.SignInRequiredState
import com.ibnalsham.app.ui.design.gate.LocalAuthGate
import com.ibnalsham.app.ui.design.motion.enterFromBottom
import com.ibnalsham.app.ui.design.theme.AppTheme
import com.ibnalsham.app.ui.design.theme.Spacing
import com.ibnalsham.app.ui.design.designSystem.R as DesignR
import com.ibnalsham.app.ui.feature.profileimpl.R
import org.koin.androidx.compose.koinViewModel

@Composable
fun ProfileRoute(
    onNavigateBack: () -> Unit,
    onSignedOut: () -> Unit,
    onShowSnackbar: (String) -> Unit,
    viewModel: ProfileViewModel = koinViewModel(),
) {
    val state by viewModel.uiState.collectAsState()
    val gate = LocalAuthGate.current

    LaunchedEffect(Unit) {
        viewModel.onAction(ProfileViewModelContract.Action.ViewReady)
    }

    LaunchedEffect(Unit) {
        viewModel.sideEffects.collect { effect ->
            when (effect) {
                is ProfileViewModelContract.SideEffect.ShowSnackbar -> onShowSnackbar(effect.message)
                // Reaching profile without a session (expired token, deep link)
                // raises the gate in place instead of a hard redirect.
                ProfileViewModelContract.SideEffect.NavigateToAuth -> gate.prompt(GatedFeature.Profile)
                ProfileViewModelContract.SideEffect.NavigateBack -> onNavigateBack()
                ProfileViewModelContract.SideEffect.AccountDeleted -> onSignedOut()
            }
        }
    }

    ProfileScreen(
        state = state,
        onAction = viewModel::onAction,
        onNavigateBack = onNavigateBack,
        onSignedOut = onSignedOut,
    )
}

@Composable
private fun ProfileScreen(
    state: ProfileViewModelContract.MyUiState,
    onAction: (ProfileViewModelContract.Action) -> Unit,
    onNavigateBack: () -> Unit,
    onSignedOut: () -> Unit,
) {
    val colors = AppTheme.colors

    if (state.showDeleteDialog) {
        DeleteAccountDialog(
            isDeleting = state.isDeleting,
            onConfirm = { onAction(ProfileViewModelContract.Action.ConfirmDeleteAccount) },
            onDismiss = { onAction(ProfileViewModelContract.Action.DismissDeleteDialog) },
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
            .verticalScroll(rememberScrollState()),
    ) {
        ProfileHeader(
            name = state.profile?.name.orEmpty(),
            phone = state.profile?.phone.orEmpty(),
            onNavigateBack = onNavigateBack,
            onEdit = { onAction(ProfileViewModelContract.Action.StartEditing) },
            showEdit = !state.isEditing && state.profile != null,
        )

        when {
            state.requiresLogin -> SignInRequiredState(
                title = stringResource(DesignR.string.gate_profile_title),
                message = stringResource(DesignR.string.gate_profile_message),
                onSignIn = { onAction(ProfileViewModelContract.Action.Refresh) },
            )

            state.isLoading && state.profile == null -> LoadingState(
                modifier = Modifier.height(240.dp),
            )

            state.profile == null && state.error != null -> ErrorState(
                onRetry = { onAction(ProfileViewModelContract.Action.Refresh) },
                modifier = Modifier.height(320.dp),
            )

            else -> {
                Spacer(Modifier.height(Spacing.xl))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = Spacing.gutter)
                        .enterFromBottom(index = 0),
                    horizontalArrangement = Arrangement.spacedBy(Spacing.md),
                ) {
                    StatCard(
                        icon = Icons.Rounded.ReceiptLong,
                        value = state.profile?.ordersCount ?: 0,
                        label = stringResource(DesignR.string.profile_orders_count),
                        modifier = Modifier.weight(1f),
                    )
                    StatCard(
                        icon = Icons.Rounded.FavoriteBorder,
                        value = state.profile?.favoritesCount ?: 0,
                        label = stringResource(DesignR.string.profile_favorites_count),
                        modifier = Modifier.weight(1f),
                    )
                }

                AnimatedVisibility(visible = state.isEditing, enter = fadeIn(), exit = fadeOut()) {
                    EditNameCard(state = state, onAction = onAction)
                }

                Spacer(Modifier.height(Spacing.xxxl))

                SecondaryButton(
                    text = stringResource(DesignR.string.profile_logout),
                    onClick = {
                        onAction(ProfileViewModelContract.Action.Logout)
                        onSignedOut()
                    },
                    icon = Icons.Rounded.Logout,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = Spacing.gutter),
                )

                Spacer(Modifier.height(Spacing.md))

                SecondaryButton(
                    text = stringResource(DesignR.string.profile_delete_account),
                    onClick = { onAction(ProfileViewModelContract.Action.RequestDeleteAccount) },
                    icon = Icons.Rounded.DeleteForever,
                    destructive = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = Spacing.gutter),
                )
            }
        }

        Spacer(Modifier.height(Spacing.navBarClearance))
    }
}

@Composable
private fun ProfileHeader(
    name: String,
    phone: String,
    onNavigateBack: () -> Unit,
    onEdit: () -> Unit,
    showEdit: Boolean,
) {
    val colors = AppTheme.colors

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(colors.heroGradient)
            .statusBarsPadding()
            .padding(horizontal = Spacing.md)
            .padding(top = Spacing.sm, bottom = Spacing.xxxl),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            CircleIconButton(
                icon = Icons.AutoMirrored.Rounded.ArrowBack,
                contentDescription = stringResource(DesignR.string.back),
                onClick = onNavigateBack,
                onImage = true,
            )
            Spacer(Modifier.width(Spacing.sm))
            Text(
                text = stringResource(DesignR.string.profile_title),
                style = MaterialTheme.typography.titleLarge,
                color = Color.White,
                modifier = Modifier.weight(1f),
            )
            AnimatedVisibility(visible = showEdit, enter = fadeIn(), exit = fadeOut()) {
                CircleIconButton(
                    icon = Icons.Rounded.Edit,
                    contentDescription = stringResource(DesignR.string.edit),
                    onClick = onEdit,
                    onImage = true,
                )
            }
        }

        Spacer(Modifier.height(Spacing.xxl))

        Row(
            modifier = Modifier.padding(horizontal = Spacing.sm),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.22f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Rounded.PersonOutline,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(36.dp),
                )
            }

            Spacer(Modifier.width(Spacing.lg))

            Column {
                Text(
                    text = name.ifBlank { stringResource(DesignR.string.gate_guest_name) },
                    style = MaterialTheme.typography.headlineSmall,
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                if (phone.isNotBlank()) {
                    Text(
                        text = phone,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.85f),
                    )
                }
            }
        }
    }
}

@Composable
private fun StatCard(
    icon: ImageVector,
    value: Int,
    label: String,
    modifier: Modifier = Modifier,
) {
    val colors = AppTheme.colors

    AppCard(modifier = modifier) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Spacing.lg),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Icon(icon, contentDescription = null, tint = colors.brand, modifier = Modifier.size(24.dp))
            Spacer(Modifier.height(Spacing.sm))
            AnimatedCounter(
                value = value,
                style = MaterialTheme.typography.headlineMedium,
                color = colors.textPrimary,
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = colors.textSecondary,
            )
        }
    }
}

@Composable
private fun EditNameCard(
    state: ProfileViewModelContract.MyUiState,
    onAction: (ProfileViewModelContract.Action) -> Unit,
) {
    val colors = AppTheme.colors

    AppCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = Spacing.gutter)
            .padding(top = Spacing.md),
    ) {
        Column(modifier = Modifier.padding(Spacing.lg)) {
            OutlinedTextField(
                value = state.nameInput,
                onValueChange = { onAction(ProfileViewModelContract.Action.UpdateName(it)) },
                label = { Text(stringResource(DesignR.string.profile_name)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = MaterialTheme.shapes.small,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = colors.brand,
                    unfocusedBorderColor = colors.outline,
                    focusedLabelColor = colors.brand,
                    cursorColor = colors.brand,
                ),
            )

            Spacer(Modifier.height(Spacing.lg))

            Row(horizontalArrangement = Arrangement.spacedBy(Spacing.md)) {
                SecondaryButton(
                    text = stringResource(DesignR.string.cancel),
                    onClick = { onAction(ProfileViewModelContract.Action.CancelEditing) },
                    modifier = Modifier.weight(1f),
                )
                FullWidthButton(
                    text = stringResource(DesignR.string.save),
                    onClick = { onAction(ProfileViewModelContract.Action.SaveProfile) },
                    enabled = state.canSave,
                    loading = state.isSaving,
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

@Composable
private fun DeleteAccountDialog(
    isDeleting: Boolean,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    val colors = AppTheme.colors

    Dialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(MaterialTheme.shapes.large)
                .background(colors.surface)
                .padding(Spacing.xxl),
        ) {
            Text(
                text = stringResource(DesignR.string.profile_delete_account),
                style = MaterialTheme.typography.headlineSmall,
                color = colors.textPrimary,
            )
            Spacer(Modifier.height(Spacing.sm))
            Text(
                text = stringResource(DesignR.string.profile_delete_confirm),
                style = MaterialTheme.typography.bodyMedium,
                color = colors.textSecondary,
            )

            Spacer(Modifier.height(Spacing.xxl))

            Row(horizontalArrangement = Arrangement.spacedBy(Spacing.md)) {
                SecondaryButton(
                    text = stringResource(DesignR.string.cancel),
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f),
                )
                FullWidthButton(
                    text = stringResource(DesignR.string.delete),
                    onClick = onConfirm,
                    loading = isDeleting,
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}
