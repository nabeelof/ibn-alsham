package com.ibnalsham.app.data.cart

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CartItemDto(
    val id: Int,
    @SerialName("product_id") val productId: Int,
    @SerialName("product_name") val productName: String,
    @SerialName("product_thumbnail_image") val productThumbnailImage: String? = null,
    val price: String,
    val quantity: Int,
)

@Serializable
data class CartGroupDto(
    val name: String? = null,
    @SerialName("owner_id") val ownerId: Int? = null,
    @SerialName("sub_total") val subTotal: String? = null,
    @SerialName("cart_items") val cartItems: List<CartItemDto> = emptyList(),
)

@Serializable
data class CartResponseDto(
    @SerialName("grand_total") val grandTotal: String? = null,
    val data: List<CartGroupDto> = emptyList(),
)

@Serializable
data class CartCountDto(
    val status: Boolean = false,
    val count: Int = 0,
)

@Serializable
data class CartSummaryDto(
    @SerialName("sub_total") val subTotal: String? = null,
    val tax: String? = null,
    @SerialName("shipping_cost") val shippingCost: String? = null,
    val discount: String? = null,
    @SerialName("grand_total") val grandTotal: String? = null,
    @SerialName("grand_total_value") val grandTotalValue: Double? = null,
    @SerialName("coupon_code") val couponCode: String? = null,
    @SerialName("coupon_applied") val couponApplied: Boolean? = null,
)

@Serializable
data class CartAddRequest(
    val id: Int,
    val quantity: Int,
    val variant: String? = null,
    @SerialName("user_id") val userId: Int? = null,
    @SerialName("temp_user_id") val tempUserId: String? = null,
)

@Serializable
data class CartProcessRequest(
    @SerialName("cart_ids") val cartIds: String,
    @SerialName("cart_quantities") val cartQuantities: String,
    @SerialName("user_id") val userId: Int? = null,
    @SerialName("temp_user_id") val tempUserId: String? = null,
)

@Serializable
data class CartUserRef(
    @SerialName("user_id") val userId: Int? = null,
    @SerialName("temp_user_id") val tempUserId: String? = null,
)
