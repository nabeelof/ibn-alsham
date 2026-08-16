package com.ibnalsham.app.feature.domain

data class CategoryDomain(
    val id: Int,
    val name: String,
    val image: String?,
    val parentId: Int? = null,
    val productsCount: Int = 0,
    val children: List<CategoryDomain> = emptyList(),
) {
    val hasChildren: Boolean get() = children.isNotEmpty()
    val isSubCategory: Boolean get() = parentId != null
}

/** How an offer on a product is expressed by the admin. */
enum class DiscountType {
    NONE, PERCENT, FIXED;

    companion object {
        fun fromString(value: String?): DiscountType = when (value?.lowercase()) {
            "percent" -> PERCENT
            "fixed" -> FIXED
            else -> NONE
        }
    }
}

data class ProductDomain(
    val id: Int,
    val name: String,
    val description: String? = null,
    val price: Double,
    val discountedPrice: Double? = null,
    val image: String?,
    val categoryId: Int? = null,
    val categoryName: String? = null,
    val optionGroups: List<OptionGroupDomain> = emptyList(),
    val discountType: DiscountType = DiscountType.NONE,
    val discountValue: Double? = null,
    val discountPercentage: Int? = null,
    val offerEndsAt: String? = null,
    val isFavorite: Boolean = false,
    val isAvailable: Boolean = true,
) {
    val effectivePrice: Double get() = if (hasDiscount) discountedPrice!! else price
    val hasDiscount: Boolean get() = discountedPrice != null && discountedPrice > 0 && discountedPrice < price

    /** Money saved versus the list price, in JOD. */
    val savedAmount: Double get() = if (hasDiscount) price - discountedPrice!! else 0.0

    /** Percentage off, derived when the API did not send one. */
    val savedPercentage: Int
        get() = discountPercentage
            ?: if (hasDiscount && price > 0) ((price - discountedPrice!!) / price * 100).toInt() else 0
}

data class OptionGroupDomain(
    val id: Int,
    val name: String,
    val type: OptionGroupType,
    val isRequired: Boolean,
    val isSkippable: Boolean,
    val minSelections: Int = 0,
    val maxSelections: Int? = null,
    val options: List<OptionDomain> = emptyList(),
) {
    /** A radio group, or a checkbox group capped at one pick. */
    val isSingleChoice: Boolean get() = type == OptionGroupType.RADIO || maxSelections == 1

    val hasPaidOptions: Boolean get() = options.any { it.price > 0 }

    fun isSelectionValid(selectedCount: Int): Boolean = when {
        isRequired && selectedCount < maxOf(minSelections, 1) -> false
        selectedCount < minSelections -> false
        maxSelections != null && selectedCount > maxSelections -> false
        else -> true
    }
}

enum class OptionGroupType {
    CHECKBOX, RADIO;

    companion object {
        fun fromString(value: String?): OptionGroupType =
            if (value?.lowercase() == "radio") RADIO else CHECKBOX
    }
}

data class OptionDomain(
    val id: Int,
    val name: String,
    val price: Double = 0.0,
    val defaultSelected: Boolean = false,
    val isUserChangeable: Boolean = true,
    val groupId: Int? = null,
) {
    val isFree: Boolean get() = price <= 0.0
}

data class BannerDomain(
    val id: Int,
    val imageUrl: String,
    val title: String?,
    val linkType: String? = null,
    val linkValue: String? = null,
)

// ─── Coupons ─────────────────────────────────────────────────────────────

data class CouponDomain(
    val id: Int,
    val code: String,
    val title: String,
    val discountType: DiscountType,
    val discountValue: Double,
    val maxDiscount: Double? = null,
    val minOrderTotal: Double = 0.0,
    /** Discount in JOD for the subtotal the coupon was validated against. */
    val discountAmount: Double = 0.0,
    val expiresAt: String? = null,
)

// ─── Payment ─────────────────────────────────────────────────────────────

/** Payment options offered in Jordan. */
enum class PaymentMethodType {
    CASH, CLIQ, CARD, WALLET;

    val key: String get() = name.lowercase()

    companion object {
        fun fromKey(value: String?): PaymentMethodType = when (value?.lowercase()) {
            "cliq" -> CLIQ
            "card" -> CARD
            "wallet" -> WALLET
            // "cod" is the legacy alias the API still accepts for cash on delivery
            else -> CASH
        }
    }
}

data class PaymentMethodDomain(
    val type: PaymentMethodType,
    val name: String,
    val description: String? = null,
    val iconUrl: String? = null,
    /** CliQ alias the customer transfers to. */
    val accountAlias: String? = null,
    val requiresReference: Boolean = false,
    val isOnline: Boolean = false,
) {
    val key: String get() = type.key
}

// ─── Orders ──────────────────────────────────────────────────────────────

data class OrderDomain(
    val id: Int,
    val orderNumber: String,
    val type: String, // pickup / delivery
    val status: OrderStatus,
    val statusLabel: String,
    val subtotal: Double,
    val deliveryFee: Double,
    val discountAmount: Double = 0.0,
    val couponCode: String? = null,
    val total: Double,
    val notes: String? = null,
    val paymentMethod: String = "cash",
    val paymentStatus: String = "unpaid",
    val canCancel: Boolean = false,
    val cancellationDeadline: String? = null,
    val estimatedTime: Int? = null,
    val createdAt: String,
    val confirmedAt: String? = null,
    val needsDeliveryConfirmation: Boolean = false,
    val hasReview: Boolean = false,
    val rating: Int? = null,
    val reviewNote: String? = null,
)

/** Compact in-flight order shown as a card on the home screen. */
data class ActiveOrderDomain(
    val id: Int,
    val orderNumber: String,
    val type: String,
    val status: OrderStatus,
    val statusLabel: String,
    val total: Double,
    val estimatedTime: Int? = null,
    val createdAt: String,
)

enum class OrderStatus {
    PENDING, CONFIRMED, PREPARING, READY,
    OUT_FOR_DELIVERY, DELIVERED, COMPLETED, CANCELLED;

    companion object {
        fun fromString(value: String): OrderStatus = when (value.lowercase()) {
            "pending" -> PENDING
            "confirmed" -> CONFIRMED
            "preparing" -> PREPARING
            "ready" -> READY
            "out_for_delivery" -> OUT_FOR_DELIVERY
            "delivered" -> DELIVERED
            "completed" -> COMPLETED
            "cancelled" -> CANCELLED
            else -> PENDING
        }
    }
}

// ─── Notifications ───────────────────────────────────────────────────────

enum class NotificationType {
    ORDER, PROMOTION, GENERAL;

    companion object {
        fun fromString(value: String?): NotificationType = when (value?.lowercase()) {
            "order" -> ORDER
            "promotion" -> PROMOTION
            else -> GENERAL
        }
    }
}

data class NotificationDomain(
    val id: Int,
    val title: String,
    val body: String,
    val type: NotificationType,
    val isRead: Boolean,
    val createdAt: String,
    val createdAtHuman: String,
    val orderId: Int? = null,
    val productId: Int? = null,
)

// ─── Profile ─────────────────────────────────────────────────────────────

data class UserProfileDomain(
    val id: Int,
    val name: String,
    val phone: String,
    val avatarUrl: String? = null,
    val ordersCount: Int = 0,
    val favoritesCount: Int = 0,
)

data class AddressDomain(
    val id: Int,
    val title: String,
    val address: String,
    val lat: Double? = null,
    val lng: Double? = null,
    val isDefault: Boolean = false,
)

// ─── Search filters ──────────────────────────────────────────────────────

enum class ProductSort {
    DEFAULT, PRICE_ASC, PRICE_DESC, NEWEST, BEST_SELLING;

    val key: String
        get() = when (this) {
            DEFAULT -> "default"
            PRICE_ASC -> "price_asc"
            PRICE_DESC -> "price_desc"
            NEWEST -> "newest"
            BEST_SELLING -> "best_selling"
        }
}

/** Everything the advanced search filter sheet can set. Prices are in JOD. */
data class ProductFilterDomain(
    val categoryId: Int? = null,
    val minPrice: Double? = null,
    val maxPrice: Double? = null,
    val onlyOffers: Boolean = false,
    val onlyFeatured: Boolean = false,
    val sort: ProductSort = ProductSort.DEFAULT,
) {
    val isActive: Boolean
        get() = categoryId != null || minPrice != null || maxPrice != null ||
            onlyOffers || onlyFeatured || sort != ProductSort.DEFAULT

    val activeCount: Int
        get() = listOf(
            categoryId != null,
            minPrice != null || maxPrice != null,
            onlyOffers,
            onlyFeatured,
            sort != ProductSort.DEFAULT,
        ).count { it }
}

/** Bounds the filter sheet renders from. */
data class FilterOptionsDomain(
    val categories: List<CategoryDomain> = emptyList(),
    val minPrice: Double = 0.0,
    val maxPrice: Double = 0.0,
)
