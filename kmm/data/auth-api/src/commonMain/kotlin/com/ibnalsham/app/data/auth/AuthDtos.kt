package com.ibnalsham.app.data.auth

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SendOtpRequestDto(
    @SerialName("phone") val phone: String
)

@Serializable
data class VerifyOtpRequestDto(
    @SerialName("phone") val phone: String,
    @SerialName("code") val code: String
)

@Serializable
data class RegisterRequestDto(
    @SerialName("phone") val phone: String,
    @SerialName("code") val code: String,
    @SerialName("name") val name: String
)

@Serializable
data class AuthResponseDto(
    @SerialName("token") val token: String,
    @SerialName("user") val user: UserDto,
    @SerialName("is_new_user") val isNewUser: Boolean = false
)

@Serializable
data class UserDto(
    @SerialName("id") val id: Int,
    @SerialName("name") val name: String,
    @SerialName("phone") val phone: String
)
