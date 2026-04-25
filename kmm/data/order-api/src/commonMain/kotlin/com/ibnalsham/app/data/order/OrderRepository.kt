package com.ibnalsham.app.data.order

import com.ibnalsham.app.core.common.AppResult
import com.ibnalsham.app.library.data.network.BaseResponse
import com.ibnalsham.app.data.network.error.DomainError
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList

interface OrderRepository {
    suspend fun calculateDeliveryFee(lat: Double, lng: Double): AppResult<DomainError, DeliveryFeeResponseDto>
    suspend fun placeOrder(request: PlaceOrderRequestDto): AppResult<DomainError, OrderResponseDto>
    suspend fun getOrders(tab: String = "current"): AppResult<DomainError, ImmutableList<OrderResponseDto>>
    suspend fun getOrderStatus(orderId: Int): AppResult<DomainError, OrderStatusResponseDto>
    suspend fun cancelOrder(id: Int, reason: String? = null): AppResult<DomainError, Boolean>
    suspend fun confirmDelivery(orderId: Int): AppResult<DomainError, Boolean>
    suspend fun submitReview(orderId: Int, rating: Int, note: String?): AppResult<DomainError, Boolean>

    // Flutter Parity methods
    suspend fun getOrderList(page: Int = 1, paymentStatus: String = "", deliveryStatus: String = ""): AppResult<DomainError, PurchaseHistoryResponseDto>
    suspend fun getOrderDetails(id: Int): AppResult<DomainError, OrderDetailResponseDto>
    suspend fun reOrder(id: Int): AppResult<DomainError, ReOrderResponseDto>
    suspend fun getOrderItems(id: Int): AppResult<DomainError, OrderItemResponseDto>
    suspend fun getPurchasedDigitalProducts(page: Int = 1): AppResult<DomainError, PurchasedDigitalProductResponseDto>
}

class NetworkOrderRepository(
    private val httpClient: HttpClient,
) : OrderRepository {

    override suspend fun calculateDeliveryFee(lat: Double, lng: Double): AppResult<DomainError, DeliveryFeeResponseDto> {
        return try {
            val response: BaseResponse<DeliveryFeeResponseDto> = httpClient.get("delivery-fee") {
                parameter("lat", lat)
                parameter("lng", lng)
            }.body()

            val data = response.data
            if (response.success && data != null) {
                AppResult.Success(data)
            } else {
                AppResult.Failure(DomainError.Unknown(response.message ?: "Failed to calculate delivery fee"))
            }
        } catch (e: Exception) {
            AppResult.Failure(DomainError.NetworkError)
        }
    }

    override suspend fun placeOrder(request: PlaceOrderRequestDto): AppResult<DomainError, OrderResponseDto> {
        return try {
            val response: BaseResponse<OrderResponseDto> = httpClient.post("orders") {
                contentType(ContentType.Application.Json)
                setBody(request)
            }.body()

            val data = response.data
            if (response.success && data != null) {
                AppResult.Success(data)
            } else {
                AppResult.Failure(DomainError.Unknown(response.message ?: "Failed to place order"))
            }
        } catch (e: Exception) {
            AppResult.Failure(DomainError.NetworkError)
        }
    }

    override suspend fun getOrders(tab: String): AppResult<DomainError, ImmutableList<OrderResponseDto>> {
        return try {
            val response: BaseResponse<List<OrderResponseDto>> = httpClient.get("orders") {
                parameter("tab", tab)
            }.body()

            val data = response.data
            if (response.success && data != null) {
                AppResult.Success(data.toImmutableList())
            } else {
                AppResult.Failure(DomainError.Unknown(response.message ?: "Failed to fetch orders"))
            }
        } catch (e: Exception) {
            AppResult.Failure(DomainError.NetworkError)
        }
    }

    override suspend fun getOrderStatus(orderId: Int): AppResult<DomainError, OrderStatusResponseDto> {
        return try {
            val response: BaseResponse<OrderStatusResponseDto> = httpClient.get("orders/$orderId/status").body()

            val data = response.data
            if (response.success && data != null) {
                AppResult.Success(data)
            } else {
                AppResult.Failure(DomainError.Unknown(response.message ?: "Failed to fetch order status"))
            }
        } catch (e: Exception) {
            AppResult.Failure(DomainError.NetworkError)
        }
    }

    override suspend fun cancelOrder(id: Int, reason: String?): AppResult<DomainError, Boolean> {
        return try {
            httpClient.post("orders/$id/cancel") {
                contentType(ContentType.Application.Json)
                if (reason != null) {
                    setBody(mapOf("reason" to reason))
                }
            }
            AppResult.Success(true)
        } catch (e: Exception) {
            AppResult.Failure(DomainError.NetworkError)
        }
    }

    override suspend fun confirmDelivery(orderId: Int): AppResult<DomainError, Boolean> {
        return try {
            httpClient.post("orders/$orderId/confirm-delivery")
            AppResult.Success(true)
        } catch (e: Exception) {
            AppResult.Failure(DomainError.NetworkError)
        }
    }

    override suspend fun submitReview(orderId: Int, rating: Int, note: String?): AppResult<DomainError, Boolean> {
        return try {
            httpClient.post("orders/$orderId/review") {
                contentType(ContentType.Application.Json)
                setBody(ReviewRequestDto(rating = rating, reviewNote = note))
            }
            AppResult.Success(true)
        } catch (e: Exception) {
            AppResult.Failure(DomainError.NetworkError)
        }
    }

    override suspend fun getOrderList(page: Int, paymentStatus: String, deliveryStatus: String): AppResult<DomainError, PurchaseHistoryResponseDto> {
        return try {
            val response: PurchaseHistoryResponseDto = httpClient.get("purchase-history") {
                parameter("page", page)
                parameter("payment_status", paymentStatus)
                parameter("delivery_status", deliveryStatus)
            }.body()
            AppResult.Success(response)
        } catch (e: Exception) {
            AppResult.Failure(DomainError.NetworkError)
        }
    }

    override suspend fun getOrderDetails(id: Int): AppResult<DomainError, OrderDetailResponseDto> {
        return try {
            val response: OrderDetailResponseDto = httpClient.get("purchase-history-details/$id").body()
            AppResult.Success(response)
        } catch (e: Exception) {
            AppResult.Failure(DomainError.NetworkError)
        }
    }

    override suspend fun reOrder(id: Int): AppResult<DomainError, ReOrderResponseDto> {
        return try {
            val response: ReOrderResponseDto = httpClient.get("re-order/$id").body()
            AppResult.Success(response)
        } catch (e: Exception) {
            AppResult.Failure(DomainError.NetworkError)
        }
    }

    override suspend fun getOrderItems(id: Int): AppResult<DomainError, OrderItemResponseDto> {
        return try {
            val response: OrderItemResponseDto = httpClient.get("purchase-history-items/$id").body()
            AppResult.Success(response)
        } catch (e: Exception) {
            AppResult.Failure(DomainError.NetworkError)
        }
    }

    override suspend fun getPurchasedDigitalProducts(page: Int): AppResult<DomainError, PurchasedDigitalProductResponseDto> {
        return try {
            val response: PurchasedDigitalProductResponseDto = httpClient.get("digital/purchased-list") {
                parameter("page", page)
            }.body()
            AppResult.Success(response)
        } catch (e: Exception) {
            AppResult.Failure(DomainError.NetworkError)
        }
    }
}
