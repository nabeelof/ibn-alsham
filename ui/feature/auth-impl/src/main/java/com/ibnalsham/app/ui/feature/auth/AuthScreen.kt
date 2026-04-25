package com.ibnalsham.app.ui.feature.auth

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.ibnalsham.app.ui.feature.authimpl.R
import com.ibnalsham.app.feature.auth.presentation.viewModel.AuthViewModel
import com.ibnalsham.app.feature.auth.presentation.viewModel.AuthViewModelContract
import com.ibnalsham.app.feature.auth.presentation.viewModel.AuthViewModelContract.AuthStep
import org.koin.androidx.compose.koinViewModel

@Composable
fun AuthRoute(
    viewModel: AuthViewModel = koinViewModel(),
    onNavigateToHome: () -> Unit,
    onShowSnackbar: (String) -> Unit
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

    AuthScreen(
        state = state,
        onAction = viewModel::onAction
    )
}

@Composable
fun AuthScreen(
    state: AuthViewModelContract.MyUiState,
    onAction: (AuthViewModelContract.Action) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFAFAFA))
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Logo Space
        Box(
            modifier = Modifier
                .size(120.dp)
                .background(Color(0xFFC62828), RoundedCornerShape(16.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text(stringResource(R.string.app_name_ar), color = Color(0xFFFFD54F), fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleLarge)
        }

        Spacer(modifier = Modifier.height(48.dp))

        when (state.step) {
            AuthStep.PHONE -> PhoneStep(state, onAction)
            AuthStep.OTP -> OtpStep(state, onAction)
            AuthStep.REGISTER -> RegisterStep(state, onAction)
        }
    }
}

@Composable
fun PhoneStep(state: AuthViewModelContract.MyUiState, onAction: (AuthViewModelContract.Action) -> Unit) {
    Text(stringResource(R.string.login_title), fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleLarge)
    Spacer(modifier = Modifier.height(8.dp))
    Text(stringResource(R.string.enter_phone_desc), color = Color.Gray, textAlign = TextAlign.Center)
    
    Spacer(modifier = Modifier.height(32.dp))

    OutlinedTextField(
        value = state.phoneInput,
        onValueChange = { onAction(AuthViewModelContract.Action.UpdatePhone(it)) },
        label = { Text(stringResource(R.string.phone_label)) },
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color(0xFFC62828))
    )

    Spacer(modifier = Modifier.height(24.dp))

    Button(
        onClick = { onAction(AuthViewModelContract.Action.RequestOtp(state.phoneInput)) },
        modifier = Modifier.fillMaxWidth().height(50.dp),
        enabled = !state.isLoading,
        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFC62828)),
        shape = RoundedCornerShape(8.dp)
    ) {
        if (state.isLoading) CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
        else Text(stringResource(R.string.send_otp), color = Color.White, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun OtpStep(state: AuthViewModelContract.MyUiState, onAction: (AuthViewModelContract.Action) -> Unit) {
    Text(stringResource(R.string.verify_title), fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleLarge)
    Spacer(modifier = Modifier.height(8.dp))
    Text(stringResource(R.string.enter_otp_desc, state.phoneInput), color = Color.Gray, textAlign = TextAlign.Center)
    
    Spacer(modifier = Modifier.height(32.dp))

    OutlinedTextField(
        value = state.otpInput,
        onValueChange = { 
            onAction(AuthViewModelContract.Action.UpdateOtp(it))
            if (it.length == 4) onAction(AuthViewModelContract.Action.VerifyOtp(state.phoneInput, it))
        },
        label = { Text(stringResource(R.string.otp_label)) },
        modifier = Modifier.fillMaxWidth(0.5f),
        singleLine = true,
        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color(0xFFC62828))
    )

    Spacer(modifier = Modifier.height(24.dp))

    if (state.isLoading) {
        CircularProgressIndicator(color = Color(0xFFC62828))
    } else {
        if (state.timerSeconds > 0) {
            Text(stringResource(R.string.resend_in, state.timerSeconds), color = Color.Gray)
        } else {
            TextButton(onClick = { onAction(AuthViewModelContract.Action.ResendOtp) }) {
                Text(stringResource(R.string.resend_now), color = Color(0xFFC62828), fontWeight = FontWeight.Bold)
            }
        }
    }

    Spacer(modifier = Modifier.height(16.dp))

    TextButton(onClick = { onAction(AuthViewModelContract.Action.BackToPhone) }) {
        Text(stringResource(R.string.change_phone), color = Color.Gray)
    }
}

@Composable
fun RegisterStep(state: AuthViewModelContract.MyUiState, onAction: (AuthViewModelContract.Action) -> Unit) {
    Text(stringResource(R.string.complete_profile), fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleLarge)
    Spacer(modifier = Modifier.height(8.dp))
    Text(stringResource(R.string.welcome_new), color = Color.Gray, textAlign = TextAlign.Center)
    
    Spacer(modifier = Modifier.height(32.dp))

    OutlinedTextField(
        value = state.nameInput,
        onValueChange = { onAction(AuthViewModelContract.Action.UpdateName(it)) },
        label = { Text(stringResource(R.string.full_name)) },
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color(0xFFC62828))
    )

    Spacer(modifier = Modifier.height(24.dp))

    Button(
        onClick = { onAction(AuthViewModelContract.Action.Register(state.phoneInput, state.otpInput, state.nameInput)) },
        modifier = Modifier.fillMaxWidth().height(50.dp),
        enabled = !state.isLoading,
        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFC62828)),
        shape = RoundedCornerShape(8.dp)
    ) {
        if (state.isLoading) CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
        else Text(stringResource(R.string.create_account), color = Color.White, fontWeight = FontWeight.Bold)
    }
}
