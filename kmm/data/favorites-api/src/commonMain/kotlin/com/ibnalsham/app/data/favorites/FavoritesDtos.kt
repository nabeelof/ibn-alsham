package com.ibnalsham.app.data.favorites

import com.ibnalsham.app.feature.domain.DiscountType
import com.ibnalsham.app.feature.domain.ProductDomain
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class FavoriteProductDto(
    val id: Int,
    val name: String,
    val price: Double = 0.0,
    @SerialName("discounted_price") val discountedPrice: Double? = null,
    @SerialName("discount_type") val discountType: String? = null,
    @SerialName("discount_value") val discountValue: Double? = null,
    @SerialName("discount_percentage") val discountPercentage: Int? = null,
    @SerialName("has_discount") val hasDiscount: Boolean = false,
    val image: String? = null,
    @SerialName("image_url") val imageUrl: String? = null,
    @SerialName("is_favorite") val isFavorite: Boolean = true,
    val category: FavoriteCategoryDto? = null,
)

@Serializable
data class FavoriteCategoryDto(
    val id: Int,
    val name: String,
)

@Serializable
data class ToggleFavoriteRequestDto(
    @SerialName("product_id") val productId: Int,
)

@Serializable
data class ToggleFavoriteResponseDto(
    @SerialName("is_favorite") val isFavorite: Boolean = false,
)

fun FavoriteProductDto.toDomain(): ProductDomain = ProductDomain(
    id = id,
    name = name,
    price = price,
    discountedPrice = discountedPrice,
    image = imageUrl ?: image,
    categoryId = category?.id,
    categoryName = category?.name,
    discountType = DiscountType.fromString(discountType),
    discountValue = discountValue,
    discountPercentage = discountPercentage,
    isFavorite = true,
)
