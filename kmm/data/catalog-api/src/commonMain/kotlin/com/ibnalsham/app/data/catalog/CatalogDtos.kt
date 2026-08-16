package com.ibnalsham.app.data.catalog

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CategoryDto(
    val id: Int,
    val name: String,
    @SerialName("icon") val icon: String? = null,
    @SerialName("image") val image: String? = null,
    @SerialName("banner") val banner: String? = null,
    @SerialName("products_count") val productsCount: Int? = null,
)
