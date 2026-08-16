package com.ibnalsham.app.data.payment

import com.ibnalsham.app.core.common.AppResult
import com.ibnalsham.app.data.network.error.DomainError
import com.ibnalsham.app.feature.domain.PaymentMethodDomain
import com.ibnalsham.app.library.data.network.BaseResponse
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList

interface PaymentRepository {
    /** @param orderType "pickup" or "delivery" — some methods are type-specific. */
    suspend fun getPaymentMethods(orderType: String? = null): AppResult<DomainError, ImmutableList<PaymentMethodDomain>>
    suspend fun createOrder(request: PlaceOrderRequestDto): AppResult<DomainError, OrderCreateResponseDto>
    suspend fun payCod(orderId: Int): AppResult<DomainError, CommonResponseDto>
    suspend fun payCliq(orderId: Int, reference: String): AppResult<DomainError, CommonResponseDto>
}

class NetworkPaymentRepository(
    private val httpClient: HttpClient,
) : PaymentRepository {

    override suspend fun getPaymentMethods(
        orderType: String?,
    ): AppResult<DomainError, ImmutableList<PaymentMethodDomain>> {
        return try {
            val response: BaseResponse<List<PaymentTypeDto>> = httpClient.get("payment-types") {
                orderType?.let { parameter("type", it) }
            }.body()

            val data = response.data ?: return AppResult.Success(persistentListOf())
            AppResult.Success(data.map { it.toDomain() }.toImmutableList())
        } catch (e: Exception) {
            AppResult.Failure(DomainError.NetworkError)
        }
    }

    override suspend fun createOrder(
        request: PlaceOrderRequestDto,
    ): AppResult<DomainError, OrderCreateResponseDto> {
        return try {
            val response: OrderCreateResponseDto = httpClient.post("order/store") {
                setBody(request)
            }.body()

            if (response.result) {
                AppResult.Success(response)
            } else {
                AppResult.Failure(DomainError.Unknown(response.message ?: "Failed to place order"))
            }
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

    override suspend fun payCliq(orderId: Int, reference: String): AppResult<DomainError, CommonResponseDto> {
        return try {
            val response: CommonResponseDto = httpClient.post("payments/pay/cliq") {
                setBody(CliqPaymentRequestDto(orderId = orderId, paymentReference = reference))
            }.body()
            AppResult.Success(response)
        } catch (e: Exception) {
            AppResult.Failure(DomainError.NetworkError)
        }
    }
}
