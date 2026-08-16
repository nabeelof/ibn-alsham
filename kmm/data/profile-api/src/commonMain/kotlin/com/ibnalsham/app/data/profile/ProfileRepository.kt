package com.ibnalsham.app.data.profile

import com.ibnalsham.app.core.common.AppResult
import com.ibnalsham.app.data.network.error.DomainError
import com.ibnalsham.app.feature.domain.UserProfileDomain
import com.ibnalsham.app.library.data.network.BaseResponse
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.http.HttpStatusCode

interface ProfileRepository {
    suspend fun getProfile(): AppResult<DomainError, UserProfileDomain>
    suspend fun updateName(name: String): AppResult<DomainError, UserProfileDomain>
    suspend fun deleteAccount(): AppResult<DomainError, Boolean>
}

class NetworkProfileRepository(
    private val httpClient: HttpClient,
) : ProfileRepository {

    override suspend fun getProfile(): AppResult<DomainError, UserProfileDomain> {
        return try {
            val response: BaseResponse<ProfileDto> = httpClient.get("profile").body()
            val data = response.data
                ?: return AppResult.Failure(DomainError.Unknown(response.message ?: "Failed to load profile"))

            // Counters are a separate endpoint; a failure there must not hide the profile.
            val counters = runCatching {
                httpClient.get("profile/counters").body<BaseResponse<ProfileCountersDto>>().data
            }.getOrNull()

            AppResult.Success(data.toDomain(counters))
        } catch (e: ClientRequestException) {
            AppResult.Failure(e.toDomainError())
        } catch (e: Exception) {
            AppResult.Failure(DomainError.NetworkError)
        }
    }

    override suspend fun updateName(name: String): AppResult<DomainError, UserProfileDomain> {
        return try {
            val response: BaseResponse<ProfileDto> = httpClient.put("profile") {
                setBody(UpdateProfileRequestDto(name = name))
            }.body()

            val data = response.data
            if (response.success && data != null) {
                AppResult.Success(data.toDomain())
            } else {
                AppResult.Failure(DomainError.Unknown(response.message ?: "Failed to update profile"))
            }
        } catch (e: ClientRequestException) {
            AppResult.Failure(e.toDomainError())
        } catch (e: Exception) {
            AppResult.Failure(DomainError.NetworkError)
        }
    }

    override suspend fun deleteAccount(): AppResult<DomainError, Boolean> {
        return try {
            httpClient.delete("profile")
            AppResult.Success(true)
        } catch (e: ClientRequestException) {
            AppResult.Failure(e.toDomainError())
        } catch (e: Exception) {
            AppResult.Failure(DomainError.NetworkError)
        }
    }
}

private fun ClientRequestException.toDomainError(): DomainError =
    if (response.status == HttpStatusCode.Unauthorized) {
        DomainError.Unauthorized
    } else {
        DomainError.Unknown("Request failed: ${response.status.value}")
    }
