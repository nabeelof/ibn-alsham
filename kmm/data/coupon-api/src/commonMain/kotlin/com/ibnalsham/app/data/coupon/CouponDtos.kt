package com.ibnalsham.app.data.coupon

import com.ibnalsham.app.feature.domain.CouponDomain
import com.ibnalsham.app.feature.domain.DiscountType
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CouponDto(
    val id: Int = 0,
    val code: String = "",
    val title: String = "",
    @SerialName("discount_type") val discountType: String = "percent",
    @SerialName("discount_value") val discountValue: Double = 0.0,
    @SerialName("max_discount") val maxDiscount: Double? = null,
    @SerialName("min_order_total") val minOrderTotal: Double = 0.0,
    @SerialName("discount_amount") val discountAmount: Double = 0.0,
    @SerialName("expires_at") val expiresAt: String? = null,
)

fun CouponDto.toDomain(): CouponDomain = CouponDomain(
    id = id,
    code = code,
    title = title,
    discountType = DiscountType.fromString(discountType),
    discountValue = discountValue,
    maxDiscount = maxDiscount,
    minOrderTotal = minOrderTotal,
    discountAmount = discountAmount,
    expiresAt = expiresAt,
)

@Serializable
data class CouponApplyRequestDto(
    @SerialName("coupon_code") val couponCode: String,
    val subtotal: Double,
)
