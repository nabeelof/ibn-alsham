package com.ibnalsham.app.data.payment

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PaymentTypeDto(
    @SerialName("payment_type") val paymentType: String? = null,
    @SerialName("payment_type_key") val paymentTypeKey: String? = null,
    val image: String? = null,
    val name: String? = null,
    val title: String? = null,
    @SerialName("offline_payment_id") val offlinePaymentId: Int? = null,
    val details: String? = null,
)

@Serializable
data class OrderCreateResponseDto(
    @SerialName("combined_order_id") val combinedOrderId: Int? = null,
    val result: Boolean = false,
    val message: String? = null,
)

@Serializable
data class CommonResponseDto(
    val result: Boolean = false,
    val message: String? = null,
)

@Serializable
data class PlaceOrderRequestDto(
    val type: String,
    val items: List<OrderItemRequestDto>,
    val delivery: DeliveryRequestDto? = null,
    val notes: String? = null,
    @SerialName("user_id") val userId: Int? = null,
)

@Serializable
data class OrderItemRequestDto(
    @SerialName("product_id") val productId: Int,
    val quantity: Int,
    @SerialName("option_ids") val optionIds: List<Int> = emptyList(),
)

@Serializable
data class DeliveryRequestDto(
    val address: String,
    val lat: Double,
    val lng: Double,
)
