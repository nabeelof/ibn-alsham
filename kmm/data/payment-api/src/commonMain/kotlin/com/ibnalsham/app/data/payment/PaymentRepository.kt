package com.ibnalsham.app.data.payment

import com.ibnalsham.app.core.common.AppResult
import com.ibnalsham.app.data.network.error.DomainError
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList

interface PaymentRepository {
    suspend fun getPaymentTypes(): AppResult<DomainError, ImmutableList<PaymentTypeDto>>
    suspend fun createOrder(request: PlaceOrderRequestDto): AppResult<DomainError, OrderCreateResponseDto>
    suspend fun payCod(orderId: Int): AppResult<DomainError, CommonResponseDto>
}

class NetworkPaymentRepository(
    private val httpClient: HttpClient
) : PaymentRepository {
    override suspend fun getPaymentTypes(): AppResult<DomainError, ImmutableList<PaymentTypeDto>> {
        return try {
            val data: List<PaymentTypeDto> = httpClient.get("payment-types").body()
            AppResult.Success(data.toImmutableList())
        } catch (e: Exception) {
            AppResult.Failure(DomainError.NetworkError)
        }
    }

    override suspend fun createOrder(request: PlaceOrderRequestDto): AppResult<DomainError, OrderCreateResponseDto> {
        return try {
            val response: OrderCreateResponseDto = httpClient.post("order/store") {
                setBody(request)
            }.body()
            AppResult.Success(response)
        } catch (e: Exception) {
            AppResult.Failure(DomainError.NetworkError)
        }
    }

    override suspend fun payCod(orderId: Int): AppResult<DomainError, CommonResponseDto> {
        return try {
            val response: CommonResponseDto = httpClient.post("payments/pay/cod") {
                setBody(mapOf("order_id" to orderId))
            }.body()
            AppResult.Success(response)
        } catch (e: Exception) {
            AppResult.Failure(DomainError.NetworkError)
        }
    }
}
