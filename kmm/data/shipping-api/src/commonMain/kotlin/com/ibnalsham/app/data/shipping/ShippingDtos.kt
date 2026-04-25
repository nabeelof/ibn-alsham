package com.ibnalsham.app.data.shipping

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ShippingMethodDto(
    @SerialName("id") val id: Int,
    @SerialName("name") val name: String,
    @SerialName("cost") val cost: Double,
    @SerialName("estimated_days") val estimatedDays: String? = null
)
