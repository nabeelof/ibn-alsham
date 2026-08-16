package com.ibnalsham.app.data.profile

import com.ibnalsham.app.feature.domain.UserProfileDomain
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ProfileDto(
    val id: Int = 0,
    val name: String = "",
    val phone: String = "",
    val avatar: String? = null,
    @SerialName("fcm_token") val fcmToken: String? = null,
)

@Serializable
data class ProfileCountersDto(
    @SerialName("cart_item_count") val cartItemCount: Int = 0,
    @SerialName("wishlist_item_count") val favoritesCount: Int = 0,
    @SerialName("orders_count") val ordersCount: Int = 0,
)

@Serializable
data class UpdateProfileRequestDto(
    val name: String? = null,
)

fun ProfileDto.toDomain(counters: ProfileCountersDto? = null): UserProfileDomain = UserProfileDomain(
    id = id,
    name = name,
    phone = phone,
    avatarUrl = avatar,
    ordersCount = counters?.ordersCount ?: 0,
    favoritesCount = counters?.favoritesCount ?: 0,
)
