package com.ibnalsham.app.ui.feature.auth

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Phone
import androidx.compose.material.icons.rounded.PersonOutline
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.ibnalsham.app.feature.auth.presentation.viewModel.AuthViewModelContract
import com.ibnalsham.app.feature.auth.presentation.viewModel.AuthViewModelContract.AuthStep
import com.ibnalsham.app.ui.design.components.FullWidthButton
import com.ibnalsham.app.ui.design.components.TextAction
import com.ibnalsham.app.ui.design.theme.AppTheme
import com.ibnalsham.app.ui.design.theme.Motion
import com.ibnalsham.app.ui.design.theme.Spacing
import com.ibnalsham.app.ui.feature.authimpl.R

/**
 * The phone → OTP → name flow, without any screen chrome around it.
 *
 * Shared by the full-screen sign-in route and the gate bottom sheet, so a guest
 * gets exactly the same three steps whichever way they entered. Steps slide
 * horizontally in the direction of travel, which is what makes "change number"
 * read as going back rather than as a new screen.
 */
@Composable
fun AuthFlow(
    state: AuthViewModelContract.MyUiState,
    onAction: (AuthViewModelContract.Action) -> Unit,
    modifier: Modifier = Modifier,
    showHeadings: Boolean = true,
) {
    val layoutDirection = LocalLayoutDirection.current
    // In RTL the flow visually advances leftward, so the slide has to flip too.
    val forward = if (layoutDirection == LayoutDirection.Rtl) -1 else 1

    AnimatedContent(
        targetState = state.step,
        modifier = modifier.fillMaxWidth(),
        transitionSpec = {
            val advancing = targetState.ordinal > initialState.ordinal
            val direction = if (advancing) forward else -forward
            val enter = slideInHorizontally(Motion.offsetSpring()) { it * direction } + fadeIn(Motion.fadeIn())
            val exit = slideOutHorizontally(Motion.offsetSpring()) { -it * direction } + fadeOut(Motion.fadeOut())
            (enter togetherWith exit).using(SizeTransform(clip = false))
        },
        label = "authStep",
    ) { step ->
        when (step) {
            AuthStep.PHONE -> PhoneStep(state, onAction, showHeadings)
            AuthStep.OTP -> OtpStep(state, onAction, showHeadings)
            AuthStep.REGISTER -> RegisterStep(state, onAction, showHeadings)
        }
    }
}

@Composable
private fun PhoneStep(
    state: AuthViewModelContract.MyUiState,
    onAction: (AuthViewModelContract.Action) -> Unit,
    showHeadings: Boolean,
) {
    Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
        if (showHeadings) {
            StepHeading(
                title = stringResource(R.string.login_title),
                message = stringResource(R.string.enter_phone_desc),
            )
        }

        AuthField(
            value = state.phoneInput,
            onValueChange = { onAction(AuthViewModelContract.Action.UpdatePhone(it.filter(Char::isDigit))) },
            label = stringResource(R.string.phone_label),
            leadingIcon = Icons.Rounded.Phone,
            keyboardType = KeyboardType.Phone,
        )

        Spacer(Modifier.height(Spacing.xxl))

        FullWidthButton(
            text = stringResource(R.string.send_otp),
            onClick = { onAction(AuthViewModelContract.Action.RequestOtp(state.phoneInput)) },
            enabled = state.phoneInput.length >= MIN_PHONE_LENGTH,
            loading = state.isLoading,
        )
    }
}

@Composable
private fun OtpStep(
    state: AuthViewModelContract.MyUiState,
    onAction: (AuthViewModelContract.Action) -> Unit,
    showHeadings: Boolean,
) {
    val colors = AppTheme.colors

    Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
        if (showHeadings) {
            StepHeading(
                title = stringResource(R.string.verify_title),
                message = stringResource(R.string.enter_otp_desc, state.phoneInput),
            )
        }

        OtpCodeInput(
            code = state.otpInput,
            onCodeChange = { input ->
                onAction(AuthViewModelContract.Action.UpdateOtp(input))
                // Auto-submit on the last digit — nobody should have to hunt for
                // a button after typing a four-digit code.
                if (input.length == OTP_LENGTH) {
                    onAction(AuthViewModelContract.Action.VerifyOtp(state.phoneInput, input))
                }
            },
            enabled = !state.isLoading,
        )

        Spacer(Modifier.height(Spacing.xl))

        if (state.isLoading) {
            com.ibnalsham.app.ui.design.components.PulsingDots()
        } else if (state.timerSeconds > 0) {
            Text(
                text = stringResource(R.string.resend_in, state.timerSeconds),
                style = MaterialTheme.typography.bodySmall,
                color = colors.textTertiary,
            )
        } else {
            TextAction(
                text = stringResource(R.string.resend_now),
                onClick = { onAction(AuthViewModelContract.Action.ResendOtp) },
            )
        }

        Spacer(Modifier.height(Spacing.sm))

        TextAction(
            text = stringResource(R.string.change_phone),
            onClick = { onAction(AuthViewModelContract.Action.BackToPhone) },
            color = colors.textSecondary,
        )
    }
}

@Composable
private fun RegisterStep(
    state: AuthViewModelContract.MyUiState,
    onAction: (AuthViewModelContract.Action) -> Unit,
    showHeadings: Boolean,
) {
    Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
        if (showHeadings) {
            StepHeading(
                title = stringResource(R.string.complete_profile),
                message = stringResource(R.string.welcome_new),
            )
        }

        AuthField(
            value = state.nameInput,
            onValueChange = { onAction(AuthViewModelContract.Action.UpdateName(it)) },
            label = stringResource(R.string.full_name),
            leadingIcon = Icons.Rounded.PersonOutline,
            keyboardType = KeyboardType.Text,
        )

        Spacer(Modifier.height(Spacing.xxl))

        FullWidthButton(
            text = stringResource(R.string.create_account),
            onClick = {
                onAction(
                    AuthViewModelContract.Action.Register(state.phoneInput, state.otpInput, state.nameInput),
                )
            },
            enabled = state.nameInput.isNotBlank(),
            loading = state.isLoading,
        )
    }
}

@Composable
private fun StepHeading(title: String, message: String) {
    val colors = AppTheme.colors
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
        Text(
            text = title,
            style = MaterialTheme.typography.headlineMedium,
            color = colors.textPrimary,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(Spacing.sm))
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = colors.textSecondary,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(Spacing.xxl))
    }
}

@Composable
private fun AuthField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    leadingIcon: androidx.compose.ui.graphics.vector.ImageVector,
    keyboardType: KeyboardType,
) {
    val colors = AppTheme.colors
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        leadingIcon = { Icon(leadingIcon, contentDescription = null, tint = colors.brand) },
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
        shape = MaterialTheme.shapes.small,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = colors.brand,
            unfocusedBorderColor = colors.outline,
            focusedLabelColor = colors.brand,
            cursorColor = colors.brand,
        ),
    )
}

/**
 * Four separate digit cells backed by one hidden text field.
 *
 * A single field with four boxes drawn over it avoids the focus-juggling that
 * per-cell fields need, and lets paste and backspace behave normally. The
 * active cell scales up slightly so the caret position is obvious without
 * drawing a real cursor.
 */
@Composable
private fun OtpCodeInput(
    code: String,
    onCodeChange: (String) -> Unit,
    enabled: Boolean,
) {
    val colors = AppTheme.colors
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(Unit) { focusRequester.requestFocus() }

    Box {
        BasicTextField(
            value = code,
            onValueChange = { input ->
                onCodeChange(input.filter(Char::isDigit).take(OTP_LENGTH))
            },
            enabled = enabled,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
            cursorBrush = SolidColor(Color.Transparent),
            modifier = Modifier
                .size(1.dp)
                .focusRequester(focusRequester),
        )

        Row(
            horizontalArrangement = Arrangement.spacedBy(Spacing.md),
            modifier = Modifier.padding(vertical = Spacing.xs),
        ) {
            repeat(OTP_LENGTH) { index ->
                val digit = code.getOrNull(index)
                val isActive = index == code.length && enabled

                val borderColor by animateColorAsState(
                    targetValue = when {
                        isActive -> colors.brand
                        digit != null -> colors.brand.copy(alpha = 0.4f)
                        else -> colors.outline
                    },
                    animationSpec = Motion.snappy(),
                    label = "otpBorder",
                )
                val scale by animateFloatAsState(
                    targetValue = if (isActive) 1.06f else 1f,
                    animationSpec = Motion.bouncy(),
                    label = "otpScale",
                )

                Box(
                    modifier = Modifier
                        .scale(scale)
                        .size(width = 54.dp, height = 62.dp)
                        .clip(RoundedCornerShape(Spacing.lg))
                        .background(colors.surfaceSunken)
                        .border(2.dp, borderColor, RoundedCornerShape(Spacing.lg)),
                    contentAlignment = Alignment.Center,
                ) {
                    AnimatedContent(
                        targetState = digit,
                        transitionSpec = {
                            (fadeIn(Motion.fadeIn()) togetherWith fadeOut(Motion.fadeOut()))
                                .using(SizeTransform(clip = false))
                        },
                        label = "otpDigit",
                    ) { value ->
                        Text(
                            text = value?.toString().orEmpty(),
                            style = MaterialTheme.typography.headlineMedium,
                            color = colors.textPrimary,
                        )
                    }
                }
            }
        }
    }
}

private const val OTP_LENGTH = 4
private const val MIN_PHONE_LENGTH = 9
