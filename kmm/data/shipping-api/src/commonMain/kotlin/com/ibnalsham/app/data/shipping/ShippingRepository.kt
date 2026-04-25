package com.ibnalsham.app.data.shipping

import com.ibnalsham.app.core.common.AppResult
import com.ibnalsham.app.data.network.error.DomainError
import io.ktor.client.HttpClient

interface ShippingRepository {
    suspend fun getShippingMethods(): AppResult<DomainError, List<ShippingMethodDto>>
}

class NetworkShippingRepository(
    private val httpClient: HttpClient
) : ShippingRepository {

    override suspend fun getShippingMethods(): AppResult<DomainError, List<ShippingMethodDto>> {
        // TODO: implement with actual network call
        return AppResult.Success(emptyList())
    }
}
