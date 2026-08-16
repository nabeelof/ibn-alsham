package com.ibnalsham.app.data.notification

import com.ibnalsham.app.core.common.AppResult
import com.ibnalsham.app.data.network.error.DomainError
import com.ibnalsham.app.feature.domain.NotificationDomain
import com.ibnalsham.app.library.data.network.BaseResponse
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.HttpStatusCode
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList

interface NotificationRepository {
    suspend fun getNotifications(page: Int = 1): AppResult<DomainError, ImmutableList<NotificationDomain>>
    suspend fun getUnreadCount(): AppResult<DomainError, Int>
    suspend fun markAllRead(): AppResult<DomainError, Boolean>
    suspend fun markRead(id: Int): AppResult<DomainError, Boolean>
    suspend fun delete(ids: List<Int>): AppResult<DomainError, Boolean>

    /** Register the FCM token so the backend can push to this device. */
    suspend fun registerDeviceToken(token: String): AppResult<DomainError, Boolean>
}

class NetworkNotificationRepository(
    private val httpClient: HttpClient,
) : NotificationRepository {

    override suspend fun getNotifications(page: Int): AppResult<DomainError, ImmutableList<NotificationDomain>> {
        return try {
            val response: BaseResponse<List<NotificationDto>> = httpClient.get("notifications") {
                parameter("page", page)
            }.body()

            val data = response.data
            if (response.success && data != null) {
                AppResult.Success(data.map { it.toDomain() }.toImmutableList())
            } else {
                AppResult.Failure(DomainError.Unknown(response.message ?: "Failed to fetch notifications"))
            }
        } catch (e: ClientRequestException) {
            AppResult.Failure(e.toDomainError())
        } catch (e: Exception) {
            AppResult.Failure(DomainError.NetworkError)
        }
    }

    override suspend fun getUnreadCount(): AppResult<DomainError, Int> {
        return try {
            val response: BaseResponse<UnreadCountDto> = httpClient.get("notifications/unread-count").body()
            val data = response.data
            if (response.success && data != null) {
                AppResult.Success(data.unreadCount)
            } else {
                AppResult.Failure(DomainError.Unknown(response.message ?: "Failed to fetch unread count"))
            }
        } catch (e: ClientRequestException) {
            AppResult.Failure(e.toDomainError())
        } catch (e: Exception) {
            AppResult.Failure(DomainError.NetworkError)
        }
    }

    override suspend fun markAllRead(): AppResult<DomainError, Boolean> = runBoolean {
        httpClient.post("notifications/mark-all-read")
    }

    override suspend fun markRead(id: Int): AppResult<DomainError, Boolean> = runBoolean {
        httpClient.post("notifications/$id/read")
    }

    override suspend fun delete(ids: List<Int>): AppResult<DomainError, Boolean> = runBoolean {
        httpClient.post("notifications/bulk-delete") {
            setBody(mapOf("ids" to ids))
        }
    }

    override suspend fun registerDeviceToken(token: String): AppResult<DomainError, Boolean> = runBoolean {
        httpClient.post("profile/update-device-token") {
            setBody(DeviceTokenRequestDto(deviceToken = token))
        }
    }

    private inline fun runBoolean(block: () -> Unit): AppResult<DomainError, Boolean> {
        return try {
            block()
            AppResult.Success(true)
        } catch (e: ClientRequestException) {
            AppResult.Failure(e.toDomainError())
        } catch (e: Exception) {
            AppResult.Failure(DomainError.NetworkError)
        }
    }
}

private fun ClientRequestException.toDomainError(): DomainError =
    if (response.status == HttpStatusCode.Unauthorized) {
        DomainError.Unauthorized
    } else {
        DomainError.Unknown("Request failed: ${response.status.value}")
    }
