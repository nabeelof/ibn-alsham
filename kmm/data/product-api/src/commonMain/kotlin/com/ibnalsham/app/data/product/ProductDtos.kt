package com.ibnalsham.app.data.product

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ProductDto(
    val id: Int,
    val name: String,
    @SerialName("image_url") val imageUrl: String? = null,
    val price: Double,
    @SerialName("discounted_price") val discountedPrice: Double? = null,
)

@Serializable
data class ProductDetailsDto(
    val id: Int,
    val name: String,
    val description: String? = null,
    @SerialName("thumbnail_image") val thumbnailImage: String? = null,
    @SerialName("main_price") val mainPrice: String? = null,
    @SerialName("stroked_price") val strokedPrice: String? = null,
    @SerialName("currency_symbol") val currencySymbol: String? = null,
)

@Serializable
data class ProductDetailsResponseDto(
    val data: List<ProductDetailsDto> = emptyList(),
    val success: Boolean = false,
    val status: Int = 0,
)

@Serializable
data class VariantPriceDto(
    @SerialName("price") val price: String,
    @SerialName("stock") val stock: Int = 0,
    @SerialName("stock_txt") val stockTxt: String? = null,
    @SerialName("image") val image: String? = null
)

@Serializable
data class VariantPriceResponseDto(
    @SerialName("result") val result: Boolean,
    @SerialName("data") val data: VariantPriceDto? = null
)
