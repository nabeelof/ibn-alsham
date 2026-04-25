package com.ibnalsham.app.data.home

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class HomeResponseDto(
    @SerialName("banners") val banners: List<BannerDto>,
    @SerialName("categories") val categories: List<CategoryDto>,
    @SerialName("featured_products") val featuredProducts: List<ProductDto>,
    @SerialName("special_offers") val specialOffers: List<ProductDto>,
    @SerialName("announcement") val announcement: String? = null
)

@Serializable
data class BannerDto(
    @SerialName("id") val id: Int,
    @SerialName("title") val title: String? = null,
    @SerialName("image_url") val imageUrl: String,
    @SerialName("link_type") val linkType: String,
    @SerialName("link_value") val linkValue: String? = null
)

@Serializable
data class CategoryDto(
    @SerialName("id") val id: Int,
    @SerialName("name") val name: String,
    @SerialName("icon") val icon: String? = null
)

@Serializable
data class ProductDto(
    @SerialName("id") val id: Int,
    @SerialName("name") val name: String,
    @SerialName("description") val description: String? = null,
    @SerialName("price") val price: Double,
    @SerialName("discounted_price") val discountedPrice: Double? = null,
    @SerialName("image_url") val imageUrl: String? = null,
    @SerialName("category_name") val categoryName: String? = null
)
