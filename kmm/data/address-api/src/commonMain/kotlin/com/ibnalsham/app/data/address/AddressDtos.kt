package com.ibnalsham.app.data.address

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AddressDto(
    val id: Int,
    val address: String,
    val city: String? = null,
    val area: String? = null,
    val lat: Double? = null,
    val lng: Double? = null,
    @SerialName("is_default") val isDefault: Boolean = false,
)

@Serializable
data class AddressRequestDto(
    val id: Int? = null,
    val address: String,
    val city: String? = null,
    val area: String? = null,
    val lat: Double? = null,
    val lng: Double? = null,
    @SerialName("is_default") val isDefault: Boolean? = null,
)
