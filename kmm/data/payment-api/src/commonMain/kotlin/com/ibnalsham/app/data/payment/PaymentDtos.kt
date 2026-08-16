package com.ibnalsham.app.data.payment

import com.ibnalsham.app.feature.domain.PaymentMethodDomain
import com.ibnalsham.app.feature.domain.PaymentMethodType
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PaymentTypeDto(
    val key: String? = null,
    @SerialName("payment_type") val paymentType: String? = null,
    @SerialName("payment_type_key") val paymentTypeKey: String? = null,
    val image: String? = null,
    val name: String? = null,
    val title: String? = null,
    val description: String? = null,
    @SerialName("account_alias") val accountAlias: String? = null,
    @SerialName("requires_reference") val requiresReference: Boolean = false,
    @SerialName("is_online") val isOnline: Boolean = false,
    @SerialName("offline_payment_id") val offlinePaymentId: Int? = null,
    val details: String? = null,
)

fun PaymentTypeDto.toDomain(): PaymentMethodDomain {
    val resolvedKey = key ?: paymentTypeKey ?: paymentType
    return PaymentMethodDomain(
        type = PaymentMethodType.fromKey(resolvedKey),
        name = name ?: title ?: resolvedKey.orEmpty(),
        description = description ?: details,
        iconUrl = image,
        accountAlias = accountAlias,
        requiresReference = requiresReference,
        isOnline = isOnline,
    )
}

@Serializable
data class OrderCreateResponseDto(
    @SerialName("combined_order_id") val combinedOrderId: Int? = null,
    @SerialName("order_id") val orderId: Int? = null,
    @SerialName("order_number") val orderNumber: String? = null,
    val subtotal: Double = 0.0,
    @SerialName("discount_amount") val discountAmount: Double = 0.0,
    @SerialName("delivery_fee") val deliveryFee: Double = 0.0,
    val total: Double = 0.0,
    @SerialName("payment_method") val paymentMethod: String? = null,
    @SerialName("payment_status") val paymentStatus: String? = null,
    val result: Boolean = false,
    val message: String? = null,
) {
    val resolvedOrderId: Int? get() = orderId ?: combinedOrderId
}

@Serializable
data class CommonResponseDto(
    val result: Boolean = false,
    val success: Boolean = false,
    val message: String? = null,
)

@Serializable
data class PlaceOrderRequestDto(
    val type: String,
    val items: List<OrderItemRequestDto>,
    val delivery: DeliveryRequestDto? = null,
    val notes: String? = null,
    @SerialName("coupon_code") val couponCode: String? = null,
    @SerialName("payment_method") val paymentMethod: String = "cash",
    @SerialName("payment_reference") val paymentReference: String? = null,
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

@Serializable
data class CliqPaymentRequestDto(
    @SerialName("order_id") val orderId: Int,
    @SerialName("payment_reference") val paymentReference: String,
)
