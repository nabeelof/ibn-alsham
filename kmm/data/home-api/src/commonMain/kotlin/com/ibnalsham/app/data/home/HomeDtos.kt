package com.ibnalsham.app.data.home

import com.ibnalsham.app.feature.domain.ActiveOrderDomain
import com.ibnalsham.app.feature.domain.BannerDomain
import com.ibnalsham.app.feature.domain.CategoryDomain
import com.ibnalsham.app.feature.domain.DiscountType
import com.ibnalsham.app.feature.domain.OrderStatus
import com.ibnalsham.app.feature.domain.ProductDomain
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class HomeResponseDto(
    @SerialName("banners") val banners: List<BannerDto> = emptyList(),
    @SerialName("categories") val categories: List<CategoryDto> = emptyList(),
    @SerialName("featured_products") val featuredProducts: List<ProductDto> = emptyList(),
    @SerialName("special_offers") val specialOffers: List<ProductDto> = emptyList(),
    @SerialName("most_ordered") val mostOrdered: List<ProductDto> = emptyList(),
    @SerialName("active_order") val activeOrder: ActiveOrderDto? = null,
    @SerialName("announcement") val announcement: String? = null,
)

@Serializable
data class BannerDto(
    @SerialName("id") val id: Int,
    @SerialName("title") val title: String? = null,
    @SerialName("image_url") val imageUrl: String? = null,
    @SerialName("link_type") val linkType: String? = null,
    @SerialName("link_value") val linkValue: String? = null,
)

@Serializable
data class CategoryDto(
    @SerialName("id") val id: Int,
    @SerialName("parent_id") val parentId: Int? = null,
    @SerialName("name") val name: String,
    @SerialName("icon") val icon: String? = null,
    @SerialName("image") val image: String? = null,
    @SerialName("products_count") val productsCount: Int = 0,
    @SerialName("children") val children: List<CategoryDto> = emptyList(),
)

@Serializable
data class ProductDto(
    @SerialName("id") val id: Int,
    @SerialName("name") val name: String,
    @SerialName("description") val description: String? = null,
    @SerialName("price") val price: Double,
    @SerialName("discounted_price") val discountedPrice: Double? = null,
    @SerialName("discount_type") val discountType: String? = null,
    @SerialName("discount_value") val discountValue: Double? = null,
    @SerialName("discount_percentage") val discountPercentage: Int? = null,
    @SerialName("has_discount") val hasDiscount: Boolean = false,
    @SerialName("offer_ends_at") val offerEndsAt: String? = null,
    @SerialName("image_url") val imageUrl: String? = null,
    @SerialName("category_id") val categoryId: Int? = null,
    @SerialName("category_name") val categoryName: String? = null,
    @SerialName("is_favorite") val isFavorite: Boolean = false,
    @SerialName("is_available") val isAvailable: Boolean = true,
)

@Serializable
data class ActiveOrderDto(
    @SerialName("id") val id: Int,
    @SerialName("order_number") val orderNumber: String = "",
    @SerialName("type") val type: String = "pickup",
    @SerialName("status") val status: String = "pending",
    @SerialName("status_label") val statusLabel: String = "",
    @SerialName("total") val total: Double = 0.0,
    @SerialName("estimated_time") val estimatedTime: Int? = null,
    @SerialName("created_at") val createdAt: String = "",
)

// ─── Mappers shared by home, menu and search ─────────────────────────────

fun ProductDto.toDomain(): ProductDomain = ProductDomain(
    id = id,
    name = name,
    description = description,
    price = price,
    discountedPrice = discountedPrice,
    image = imageUrl,
    categoryId = categoryId,
    categoryName = categoryName,
    discountType = DiscountType.fromString(discountType),
    discountValue = discountValue,
    discountPercentage = discountPercentage,
    offerEndsAt = offerEndsAt,
    isFavorite = isFavorite,
    isAvailable = isAvailable,
)

fun CategoryDto.toDomain(): CategoryDomain = CategoryDomain(
    id = id,
    name = name,
    image = icon ?: image,
    parentId = parentId,
    productsCount = productsCount,
    children = children.map { it.toDomain() },
)

fun BannerDto.toDomain(): BannerDomain = BannerDomain(
    id = id,
    imageUrl = imageUrl.orEmpty(),
    title = title,
    linkType = linkType,
    linkValue = linkValue,
)

fun ActiveOrderDto.toDomain(): ActiveOrderDomain = ActiveOrderDomain(
    id = id,
    orderNumber = orderNumber,
    type = type,
    status = OrderStatus.fromString(status),
    statusLabel = statusLabel,
    total = total,
    estimatedTime = estimatedTime,
    createdAt = createdAt,
)
