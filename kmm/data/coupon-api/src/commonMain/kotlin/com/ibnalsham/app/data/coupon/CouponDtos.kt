package com.ibnalsham.app.data.coupon

import kotlinx.serialization.Serializable

@Serializable
data class CouponApplyResponseDto(
    val result: Boolean = false,
    val message: String? = null,
)

@Serializable
data class CouponListResponseDto(
    val data: List<CouponDto> = emptyList(),
    val success: Boolean = false,
    val status: Int = 0,
)

@Serializable
data class CouponDto(
    val id: Int? = null,
    val code: String? = null,
)
