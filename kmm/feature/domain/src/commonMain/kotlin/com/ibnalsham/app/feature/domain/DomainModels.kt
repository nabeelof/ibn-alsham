package com.ibnalsham.app.feature.domain

data class CategoryDomain(val id: Int, val name: String, val image: String?)

data class ProductDomain(
    val id: Int,
    val name: String,
    val description: String? = null,
    val price: Double,
    val discountedPrice: Double? = null,
    val image: String?,
    val categoryName: String? = null,
    val optionGroups: List<OptionGroupDomain> = emptyList(),
) {
    val effectivePrice: Double get() = discountedPrice ?: price
    val hasDiscount: Boolean get() = discountedPrice != null && discountedPrice > 0
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
)

enum class OptionGroupType { CHECKBOX, RADIO }

data class OptionDomain(
    val id: Int,
    val name: String,
    val price: Double = 0.0,
    val defaultSelected: Boolean = false,
    val isUserChangeable: Boolean = true,
)

data class BannerDomain(val id: Int, val imageUrl: String, val title: String?)

data class OrderDomain(
    val id: Int,
    val orderNumber: String,
    val type: String, // pickup / delivery
    val status: OrderStatus,
    val statusLabel: String,
    val subtotal: Double,
    val deliveryFee: Double,
    val total: Double,
    val notes: String? = null,
    val paymentMethod: String = "cash",
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
