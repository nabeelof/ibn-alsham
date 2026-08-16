package com.ibnalsham.app.data.auth

import com.ibnalsham.app.core.common.AppResult
import com.ibnalsham.app.library.data.network.BaseResponse
import com.ibnalsham.app.data.network.error.DomainError
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.post
import io.ktor.client.request.setBody

interface AuthRepository {
    suspend fun sendOtp(phone: String): AppResult<DomainError, String>
    suspend fun verifyOtp(phone: String, code: String): AppResult<DomainError, AuthResponseDto>
    suspend fun register(phone: String, code: String, name: String): AppResult<DomainError, AuthResponseDto>
}

class NetworkAuthRepository(
    private val httpClient: HttpClient
) : AuthRepository {

    override suspend fun sendOtp(phone: String): AppResult<DomainError, String> {
        return try {
            val response: BaseResponse<Unit> = httpClient.post("auth/send-otp") {
                setBody(SendOtpRequestDto(phone))
            }.body()
            
            if (response.success) {
                AppResult.Success(response.message ?: "OTP sent")
            } else {
                AppResult.Failure(DomainError.Unknown(response.message ?: "Failed to send OTP"))
            }
        } catch (e: Exception) {
            AppResult.Failure(DomainError.NetworkError)
        }
    }

    override suspend fun verifyOtp(phone: String, code: String): AppResult<DomainError, AuthResponseDto> {
        return try {
            val response: BaseResponse<AuthResponseDto> = httpClient.post("auth/verify-otp") {
                setBody(VerifyOtpRequestDto(phone, code))
            }.body()
            
            val data = response.data
            if (response.success && data != null) {
                AppResult.Success(data)
            } else {
                AppResult.Failure(DomainError.Unknown(response.message ?: "Invalid OTP"))
            }
        } catch (e: Exception) {
            AppResult.Failure(DomainError.NetworkError)
        }
    }

    override suspend fun register(phone: String, code: String, name: String): AppResult<DomainError, AuthResponseDto> {
        return try {
            val response: BaseResponse<AuthResponseDto> = httpClient.post("auth/register") {
                setBody(RegisterRequestDto(phone, code, name))
            }.body()
            
            val data = response.data
            if (response.success && data != null) {
                AppResult.Success(data)
            } else {
                AppResult.Failure(DomainError.Unknown(response.message ?: "Registration failed"))
            }
        } catch (e: Exception) {
            AppResult.Failure(DomainError.NetworkError)
        }
    }
}
