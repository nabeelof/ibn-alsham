package com.ibnalsham.app.feature.auth.presentation.viewModel

import com.ibnalsham.app.presentation.mvi.KMMViewModel
import com.ibnalsham.app.presentation.mvi.MVI
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

import com.ibnalsham.app.data.auth.AuthRepository
import com.ibnalsham.app.core.common.onSuccess
import com.ibnalsham.app.core.common.onFailure
import com.ibnalsham.app.library.data.cache.UserPreferencesRepository
import com.ibnalsham.app.presentation.mvi.mvi

class AuthViewModel(
    private val authRepository: AuthRepository,
    private val userPreferencesRepository: UserPreferencesRepository,
) : KMMViewModel(), 
    MVI<AuthViewModelContract.MyUiState, AuthViewModelContract.Action, AuthViewModelContract.SideEffect> 
    by mvi(AuthViewModelContract.MyUiState.defaultInstance()) {

    override fun onAction(event: AuthViewModelContract.Action) {
        when (event) {
            is AuthViewModelContract.Action.UpdatePhone -> updateState { copy(phoneInput = event.phone, error = null) }
            is AuthViewModelContract.Action.UpdateOtp -> updateState { copy(otpInput = event.otp, error = null) }
            is AuthViewModelContract.Action.UpdateName -> updateState { copy(nameInput = event.name, error = null) }
            is AuthViewModelContract.Action.RequestOtp -> handleRequestOtp(event.phone)
            is AuthViewModelContract.Action.VerifyOtp -> handleVerifyOtp(event.phone, event.otp)
            is AuthViewModelContract.Action.Register -> handleRegister(event.phone, event.otp, event.name)
            AuthViewModelContract.Action.ResendOtp -> handleResendOtp()
            AuthViewModelContract.Action.BackToPhone -> updateState { copy(step = AuthViewModelContract.AuthStep.PHONE, otpInput = "") }
        }
    }

    private fun handleRequestOtp(phone: String) {
        if (phone.length < 9) {
            viewModelScope.launch { emitEffect { AuthViewModelContract.SideEffect.ShowSnackbar("رقم الهاتف غير صحيح") } }
            return
        }

        viewModelScope.launch {
            updateState { copy(isLoading = true, error = null) }
            
            authRepository.sendOtp(phone).onSuccess { message ->
                updateState { 
                    copy(
                        isLoading = false, 
                        step = AuthViewModelContract.AuthStep.OTP,
                        timerSeconds = 60
                    ) 
                }
                emitEffect { AuthViewModelContract.SideEffect.ShowSnackbar(message) }
                startTimer()
            }.onFailure { error ->
                updateState { copy(isLoading = false, error = error) }
            }
        }
    }

    private fun handleVerifyOtp(phone: String, otp: String) {
        if (otp.length < 4) return

        viewModelScope.launch {
            updateState { copy(isLoading = true, error = null) }
            
            authRepository.verifyOtp(phone, otp).onSuccess { response ->
                updateState { copy(isLoading = false) }
                userPreferencesRepository.saveAuthToken(response.token)
                userPreferencesRepository.saveUserName(response.user.name)
                userPreferencesRepository.saveUserPhone(response.user.phone)
                if (response.isNewUser) {
                    updateState { copy(step = AuthViewModelContract.AuthStep.REGISTER) }
                } else {
                    emitEffect { AuthViewModelContract.SideEffect.NavigateToHome }
                }
            }.onFailure { error ->
                updateState { copy(isLoading = false, error = error) }
            }
        }
    }

    private fun handleRegister(phone: String, otp: String, name: String) {
        if (name.isBlank()) {
            viewModelScope.launch { emitEffect { AuthViewModelContract.SideEffect.ShowSnackbar("الرجاء إدخال الاسم") } }
            return
        }

        viewModelScope.launch {
            updateState { copy(isLoading = true, error = null) }
            
            authRepository.register(phone, otp, name).onSuccess { response ->
                updateState { copy(isLoading = false) }
                userPreferencesRepository.saveAuthToken(response.token)
                userPreferencesRepository.saveUserName(response.user.name)
                userPreferencesRepository.saveUserPhone(response.user.phone)
                emitEffect { AuthViewModelContract.SideEffect.NavigateToHome }
            }.onFailure { error ->
                updateState { copy(isLoading = false, error = error) }
            }
        }
    }

    private fun handleResendOtp() {
        updateState { copy(timerSeconds = 60, otpInput = "") }
        startTimer()
        viewModelScope.launch { emitEffect { AuthViewModelContract.SideEffect.ShowSnackbar("تم إرسال رمز جديد (التجريبي: 1234)") } }
    }

    private fun startTimer() {
        viewModelScope.launch {
            while (currentState.timerSeconds > 0 && currentState.step == AuthViewModelContract.AuthStep.OTP) {
                delay(1000)
                updateState { copy(timerSeconds = timerSeconds - 1) }
            }
        }
    }

}
