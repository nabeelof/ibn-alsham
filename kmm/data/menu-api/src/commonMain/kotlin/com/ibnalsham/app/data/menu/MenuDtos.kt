package com.ibnalsham.app.data.menu

import com.ibnalsham.app.data.home.ProductDto
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CategoryWithCountDto(
    @SerialName("id") val id: Int,
    @SerialName("name") val name: String,
    @SerialName("icon") val icon: String? = null,
    @SerialName("products_count") val productsCount: Int
)

@Serializable
data class PaginationMetaDto(
    @SerialName("current_page") val currentPage: Int,
    @SerialName("last_page") val lastPage: Int,
    @SerialName("total") val total: Int? = null
)

@Serializable
data class ProductsListResponse(
    val success: Boolean = true,
    val data: List<ProductDto>? = null,
    val pagination: PaginationMetaDto? = null,
    val message: String? = null
)
