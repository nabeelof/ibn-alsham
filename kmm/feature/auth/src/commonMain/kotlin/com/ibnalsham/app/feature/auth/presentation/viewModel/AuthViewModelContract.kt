package com.ibnalsham.app.feature.auth.presentation.viewModel

import com.ibnalsham.app.data.network.error.DomainError
import com.ibnalsham.app.presentation.mvi.UiEvent
import com.ibnalsham.app.presentation.mvi.UiSideEffect
import com.ibnalsham.app.presentation.mvi.UiState

interface AuthViewModelContract {
    sealed class Action : UiEvent() {
        data class UpdatePhone(val phone: String) : Action()
        data class RequestOtp(val phone: String) : Action()
        data class UpdateOtp(val otp: String) : Action()
        data class VerifyOtp(val phone: String, val otp: String) : Action()
        data class UpdateName(val name: String) : Action()
        data class Register(val phone: String, val otp: String, val name: String) : Action()
        data object ResendOtp : Action()
        data object BackToPhone : Action()
    }

    enum class AuthStep { PHONE, OTP, REGISTER }

    data class MyUiState(
        val isLoading: Boolean = false,
        val step: AuthStep = AuthStep.PHONE,
        val phoneInput: String = "",
        val otpInput: String = "",
        val nameInput: String = "",
        val timerSeconds: Int = 0,
        val error: DomainError? = null
    ) : UiState {
        companion object {
            fun defaultInstance() = MyUiState()
        }
    }

    sealed class SideEffect : UiSideEffect {
        data object NavigateToHome : SideEffect()
        data class ShowSnackbar(val message: String) : SideEffect()
    }
}
