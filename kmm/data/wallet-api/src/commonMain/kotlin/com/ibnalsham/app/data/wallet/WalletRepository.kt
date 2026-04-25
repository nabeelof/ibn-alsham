package com.ibnalsham.app.data.wallet

import com.ibnalsham.app.core.common.AppResult
import com.ibnalsham.app.data.network.error.DomainError
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.post

interface WalletRepository {
    suspend fun balance(): AppResult<DomainError, WalletBalanceDto>
    suspend fun history(): AppResult<DomainError, WalletHistoryDto>
    suspend fun offlineRecharge(): AppResult<DomainError, Boolean>
}

class NetworkWalletRepository(
    private val httpClient: HttpClient
) : WalletRepository {
    override suspend fun balance(): AppResult<DomainError, WalletBalanceDto> = get("wallet/balance")
    override suspend fun history(): AppResult<DomainError, WalletHistoryDto> = get("wallet/history")
    override suspend fun offlineRecharge(): AppResult<DomainError, Boolean> {
        return try {
            httpClient.post("wallet/offline-recharge")
            AppResult.Success(false)
        } catch (e: Exception) {
            AppResult.Failure(DomainError.NetworkError)
        }
    }

    private suspend inline fun <reified T> get(path: String): AppResult<DomainError, T> {
        return try {
            val response: T = httpClient.get(path).body()
            AppResult.Success(response)
        } catch (e: Exception) {
            AppResult.Failure(DomainError.NetworkError)
        }
    }
}
