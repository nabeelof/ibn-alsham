package com.ibnalsham.app.data.order

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PlaceOrderRequestDto(
    @SerialName("items") val items: List<OrderItemDto>,
    @SerialName("type") val orderType: String, // pickup / delivery
    @SerialName("address") val address: String? = null,
    @SerialName("delivery") val delivery: DeliveryDataDto? = null,
    @SerialName("notes") val notes: String? = null,
    @SerialName("coupon_code") val couponCode: String? = null,
    @SerialName("payment_method") val paymentMethod: String = "cash",
    @SerialName("payment_reference") val paymentReference: String? = null,
)

@Serializable
data class DeliveryDataDto(
    @SerialName("address") val address: String,
    @SerialName("lat") val lat: Double,
    @SerialName("lng") val lng: Double,
)

@Serializable
data class OrderItemDto(
    @SerialName("product_id") val productId: Int,
    @SerialName("quantity") val quantity: Int,
    @SerialName("option_ids") val optionIds: List<Int> = emptyList(),
)

@Serializable
data class DeliveryFeeResponseDto(
    @SerialName("delivery_fee") val deliveryFee: Double,
    @SerialName("distance_km") val distanceKm: Double,
    @SerialName("is_valid") val isValid: Boolean,
)

@Serializable
data class OrderResponseDto(
    @SerialName("id") val id: Int,
    @SerialName("order_number") val orderNumber: String,
    @SerialName("type") val type: String = "pickup",
    @SerialName("status") val status: String,
    @SerialName("status_label") val statusLabel: String = "",
    @SerialName("subtotal") val subtotal: Double = 0.0,
    @SerialName("delivery_fee") val deliveryFee: Double = 0.0,
    @SerialName("discount_amount") val discountAmount: Double = 0.0,
    @SerialName("coupon_code") val couponCode: String? = null,
    @SerialName("total") val total: Double = 0.0,
    @SerialName("notes") val notes: String? = null,
    @SerialName("payment_method") val paymentMethod: String = "cash",
    @SerialName("payment_status") val paymentStatus: String = "unpaid",
    @SerialName("can_cancel") val canCancel: Boolean = false,
    @SerialName("cancellation_deadline") val cancellationDeadline: String? = null,
    @SerialName("estimated_time") val estimatedTime: Int? = null,
    @SerialName("created_at") val createdAt: String = "",
    @SerialName("confirmed_at") val confirmedAt: String? = null,
    @SerialName("needs_delivery_confirmation") val needsDeliveryConfirmation: Boolean = false,
    @SerialName("has_review") val hasReview: Boolean = false,
    @SerialName("rating") val rating: Int? = null,
    @SerialName("review_note") val reviewNote: String? = null,
    @SerialName("item_count") val itemCount: Int = 0,
)

@Serializable
data class OrderStatusResponseDto(
    @SerialName("id") val id: Int,
    @SerialName("status") val status: String,
    @SerialName("status_label") val statusLabel: String = "",
    @SerialName("can_cancel") val canCancel: Boolean = false,
    @SerialName("cancellation_deadline") val cancellationDeadline: String? = null,
    @SerialName("estimated_time") val estimatedTime: Int? = null,
    @SerialName("confirmed_at") val confirmedAt: String? = null,
    @SerialName("needs_delivery_confirmation") val needsDeliveryConfirmation: Boolean = false,
    @SerialName("has_review") val hasReview: Boolean = false,
)

@Serializable
data class ReviewRequestDto(
    @SerialName("rating") val rating: Int,
    @SerialName("review_note") val reviewNote: String? = null,
)

@Serializable
data class PurchaseHistoryResponseDto(
    @SerialName("data") val data: List<OrderResponseDto> = emptyList(),
    @SerialName("success") val success: Boolean = false,
    @SerialName("status") val status: Int = 0,
)

@Serializable
data class OrderDetailResponseDto(
    @SerialName("data") val data: List<OrderResponseDto> = emptyList(),
    @SerialName("success") val success: Boolean = false,
    @SerialName("status") val status: Int = 0,
)

@Serializable
data class ReOrderResponseDto(
    @SerialName("result") val result: Boolean = false,
    @SerialName("message") val message: String = "",
    @SerialName("order_id") val orderId: Int = 0,
)

@Serializable
data class OrderItemResponseDto(
    @SerialName("data") val data: List<OrderItemDto> = emptyList(),
    @SerialName("success") val success: Boolean = false,
    @SerialName("status") val status: Int = 0,
)

@Serializable
data class PurchasedDigitalProductResponseDto(
    @SerialName("data") val data: List<OrderItemDto> = emptyList(),
    @SerialName("success") val success: Boolean = false,
    @SerialName("status") val status: Int = 0,
)
