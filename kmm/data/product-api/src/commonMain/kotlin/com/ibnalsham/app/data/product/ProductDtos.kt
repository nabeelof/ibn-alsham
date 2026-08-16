package com.ibnalsham.app.data.product

import com.ibnalsham.app.feature.domain.DiscountType
import com.ibnalsham.app.feature.domain.OptionDomain
import com.ibnalsham.app.feature.domain.OptionGroupDomain
import com.ibnalsham.app.feature.domain.OptionGroupType
import com.ibnalsham.app.feature.domain.ProductDomain
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

// ─── Product details (GET /products/{id}) ────────────────────────────────

@Serializable
data class ProductDetailsDto(
    val id: Int = 0,
    val name: String = "",
    val description: String? = null,
    val price: Double = 0.0,
    @SerialName("discounted_price") val discountedPrice: Double? = null,
    @SerialName("discount_type") val discountType: String? = null,
    @SerialName("discount_value") val discountValue: Double? = null,
    @SerialName("discount_percentage") val discountPercentage: Int? = null,
    @SerialName("offer_ends_at") val offerEndsAt: String? = null,
    @SerialName("image_url") val imageUrl: String? = null,
    @SerialName("thumbnail_image") val thumbnailImage: String? = null,
    @SerialName("main_price") val mainPrice: String? = null,
    @SerialName("stroked_price") val strokedPrice: String? = null,
    @SerialName("currency_symbol") val currencySymbol: String? = null,
    @SerialName("is_favorite") val isFavorite: Boolean = false,
    val category: ProductCategoryDto? = null,
    val photos: List<ProductPhotoDto> = emptyList(),
    @SerialName("option_groups") val optionGroups: List<OptionGroupDto> = emptyList(),
)

@Serializable
data class ProductCategoryDto(
    val id: Int? = null,
    val name: String? = null,
)

@Serializable
data class ProductPhotoDto(
    val variant: String? = null,
    val path: String? = null,
)

@Serializable
data class OptionGroupDto(
    val id: Int,
    val name: String = "",
    /** "checkbox" or "radio" */
    val type: String = "checkbox",
    @SerialName("is_required") val isRequired: Boolean = false,
    @SerialName("is_skippable") val isSkippable: Boolean = true,
    @SerialName("min_selections") val minSelections: Int = 0,
    @SerialName("max_selections") val maxSelections: Int? = null,
    val options: List<OptionDto> = emptyList(),
)

@Serializable
data class OptionDto(
    val id: Int,
    val name: String = "",
    /** 0 means a free add-on. */
    val price: Double = 0.0,
    @SerialName("default_selected") val defaultSelected: Boolean = false,
    @SerialName("is_user_changeable") val isUserChangeable: Boolean = true,
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
    @SerialName("image") val image: String? = null,
)

@Serializable
data class VariantPriceResponseDto(
    @SerialName("result") val result: Boolean,
    @SerialName("data") val data: VariantPriceDto? = null,
)

// ─── Mappers ─────────────────────────────────────────────────────────────

fun OptionDto.toDomain(groupId: Int? = null): OptionDomain = OptionDomain(
    id = id,
    name = name,
    price = price,
    defaultSelected = defaultSelected,
    isUserChangeable = isUserChangeable,
    groupId = groupId,
)

fun OptionGroupDto.toDomain(): OptionGroupDomain = OptionGroupDomain(
    id = id,
    name = name,
    type = OptionGroupType.fromString(type),
    isRequired = isRequired,
    isSkippable = isSkippable,
    minSelections = minSelections,
    maxSelections = maxSelections,
    options = options.map { it.toDomain(id) },
)

fun ProductDetailsDto.toDomain(): ProductDomain {
    // `detailsBySlug` sends prices as formatted strings; `show` sends numbers.
    val listPrice = strokedPrice?.toDoubleOrNull() ?: price
    val sellingPrice = mainPrice?.toDoubleOrNull() ?: discountedPrice

    return ProductDomain(
        id = id,
        name = name,
        description = description,
        price = if (listPrice > 0) listPrice else (sellingPrice ?: 0.0),
        discountedPrice = sellingPrice?.takeIf { listPrice > 0 && it < listPrice } ?: discountedPrice,
        image = imageUrl ?: thumbnailImage ?: photos.firstOrNull()?.path,
        categoryId = category?.id,
        categoryName = category?.name,
        optionGroups = optionGroups.map { it.toDomain() },
        discountType = DiscountType.fromString(discountType),
        discountValue = discountValue,
        discountPercentage = discountPercentage,
        offerEndsAt = offerEndsAt,
        isFavorite = isFavorite,
    )
}
