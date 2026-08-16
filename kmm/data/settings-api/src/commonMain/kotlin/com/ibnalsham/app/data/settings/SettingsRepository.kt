package com.ibnalsham.app.data.settings

import com.ibnalsham.app.core.common.AppResult
import com.ibnalsham.app.data.network.error.DomainError
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get

interface SettingsRepository {
    suspend fun languages(): AppResult<DomainError, LanguageListResponseDto>
    suspend fun currencies(): AppResult<DomainError, CurrencyListResponseDto>
    suspend fun businessSettings(): AppResult<DomainError, BusinessSettingsResponseDto>
}

class NetworkSettingsRepository(
    private val httpClient: HttpClient
) : SettingsRepository {
    override suspend fun languages(): AppResult<DomainError, LanguageListResponseDto> = get("languages")
    override suspend fun currencies(): AppResult<DomainError, CurrencyListResponseDto> = get("currencies")
    override suspend fun businessSettings(): AppResult<DomainError, BusinessSettingsResponseDto> = get("business-settings")

    private suspend inline fun <reified T> get(path: String): AppResult<DomainError, T> {
        return try {
            val response: T = httpClient.get(path).body()
            AppResult.Success(response)
        } catch (e: Exception) {
            AppResult.Failure(DomainError.NetworkError)
        }
    }
}
