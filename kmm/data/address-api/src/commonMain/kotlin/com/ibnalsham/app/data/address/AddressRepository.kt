package com.ibnalsham.app.data.address

import com.ibnalsham.app.core.common.AppResult
import com.ibnalsham.app.data.network.error.DomainError
import com.ibnalsham.app.library.data.network.BaseResponse
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList

interface AddressRepository {
    suspend fun list(): AppResult<DomainError, ImmutableList<AddressDto>>
    suspend fun create(request: AddressRequestDto): AppResult<DomainError, AddressDto>
    suspend fun update(request: AddressRequestDto): AppResult<DomainError, AddressDto>
    suspend fun makeDefault(id: Int): AppResult<DomainError, AddressDto>
    suspend fun delete(id: Int): AppResult<DomainError, Boolean>
}

class NetworkAddressRepository(
    private val httpClient: HttpClient
) : AddressRepository {
    override suspend fun list(): AppResult<DomainError, ImmutableList<AddressDto>> {
        return try {
            val response: BaseResponse<List<AddressDto>> = httpClient.get("user/shipping/address").body()
            val data = response.data
            if (response.success && data != null) AppResult.Success(data.toImmutableList())
            else AppResult.Failure(DomainError.Unknown(response.message ?: "Failed to load addresses"))
        } catch (e: Exception) {
            AppResult.Failure(DomainError.NetworkError)
        }
    }

    override suspend fun create(request: AddressRequestDto): AppResult<DomainError, AddressDto> =
        post("user/shipping/create", request)

    override suspend fun update(request: AddressRequestDto): AppResult<DomainError, AddressDto> =
        post("user/shipping/update", request)

    override suspend fun makeDefault(id: Int): AppResult<DomainError, AddressDto> =
        post("user/shipping/make_default", mapOf("id" to id))

    override suspend fun delete(id: Int): AppResult<DomainError, Boolean> {
        return try {
            val response: BaseResponse<Map<String, Boolean>> = httpClient.get("user/shipping/delete/$id").body()
            AppResult.Success(response.success)
        } catch (e: Exception) {
            AppResult.Failure(DomainError.NetworkError)
        }
    }

    private suspend inline fun <reified T> post(path: String, body: Any): AppResult<DomainError, T> {
        return try {
            val response: T = httpClient.post(path) { setBody(body) }.body()
            AppResult.Success(response)
        } catch (e: Exception) {
            AppResult.Failure(DomainError.NetworkError)
        }
    }
}
