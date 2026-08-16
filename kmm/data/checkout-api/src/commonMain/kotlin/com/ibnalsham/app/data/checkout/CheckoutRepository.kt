package com.ibnalsham.app.data.checkout

import com.ibnalsham.app.core.common.AppResult
import com.ibnalsham.app.data.network.error.DomainError
import io.ktor.client.HttpClient

interface CheckoutRepository {
    suspend fun placeOrder(): AppResult<DomainError, CheckoutResponseDto>
}

class NetworkCheckoutRepository(
    private val httpClient: HttpClient
) : CheckoutRepository {

    override suspend fun placeOrder(): AppResult<DomainError, CheckoutResponseDto> {
        // TODO: Implement actual place order API call
        return AppResult.Success(CheckoutResponseDto(true, "Order placed"))
    }
}
