package com.ibnalsham.app.data.notification

import com.ibnalsham.app.feature.domain.NotificationDomain
import com.ibnalsham.app.feature.domain.NotificationType
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonPrimitive

@Serializable
data class NotificationDto(
    val id: Int,
    val title: String = "",
    val body: String = "",
    val type: String? = null,
    val data: JsonObject? = null,
    @SerialName("is_read") val isRead: Boolean = false,
    @SerialName("created_at") val createdAt: String = "",
    @SerialName("created_at_human") val createdAtHuman: String = "",
)

@Serializable
data class UnreadCountDto(
    @SerialName("unread_count") val unreadCount: Int = 0,
)

@Serializable
data class DeviceTokenRequestDto(
    @SerialName("device_token") val deviceToken: String,
)

fun NotificationDto.toDomain(): NotificationDomain = NotificationDomain(
    id = id,
    title = title,
    body = body,
    type = NotificationType.fromString(type),
    isRead = isRead,
    createdAt = createdAt,
    createdAtHuman = createdAtHuman,
    orderId = data?.intField("order_id"),
    productId = data?.intField("product_id"),
)

private fun JsonObject.intField(key: String): Int? =
    this[key]?.jsonPrimitive?.content?.toIntOrNull()
