package com.ibnalsham.app.data.checkout

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CheckoutResponseDto(
    @SerialName("success") val success: Boolean,
    @SerialName("message") val message: String? = null
)
