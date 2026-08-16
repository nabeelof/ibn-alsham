package com.ibnalsham.app.data.favorites

import com.ibnalsham.app.core.common.AppResult
import com.ibnalsham.app.data.network.error.DomainError
import com.ibnalsham.app.feature.domain.ProductDomain
import com.ibnalsham.app.library.data.network.BaseResponse
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.HttpStatusCode
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList

interface FavoritesRepository {
    suspend fun getFavorites(): AppResult<DomainError, ImmutableList<ProductDomain>>

    /** @return the product's favourite state after the toggle. */
    suspend fun toggleFavorite(productId: Int): AppResult<DomainError, Boolean>
}

class NetworkFavoritesRepository(
    private val httpClient: HttpClient,
) : FavoritesRepository {

    override suspend fun getFavorites(): AppResult<DomainError, ImmutableList<ProductDomain>> {
        return try {
            val response: BaseResponse<List<FavoriteProductDto>> = httpClient.get("favorites").body()
            val data = response.data
            if (response.success && data != null) {
                AppResult.Success(data.map { it.toDomain() }.toImmutableList())
            } else {
                AppResult.Failure(DomainError.Unknown(response.message ?: "Failed to fetch favorites"))
            }
        } catch (e: ClientRequestException) {
            AppResult.Failure(
                if (e.response.status == HttpStatusCode.Unauthorized) {
                    DomainError.Unauthorized
                } else {
                    DomainError.Unknown("Request failed: ${e.response.status.value}")
                }
            )
        } catch (e: Exception) {
            AppResult.Failure(DomainError.NetworkError)
        }
    }

    override suspend fun toggleFavorite(productId: Int): AppResult<DomainError, Boolean> {
        return try {
            val response: BaseResponse<ToggleFavoriteResponseDto> = httpClient.post("favorites/toggle") {
                setBody(ToggleFavoriteRequestDto(productId = productId))
            }.body()

            val data = response.data
            if (response.success && data != null) {
                AppResult.Success(data.isFavorite)
            } else {
                AppResult.Failure(DomainError.Unknown(response.message ?: "Failed to update favorite"))
            }
        } catch (e: ClientRequestException) {
            AppResult.Failure(
                if (e.response.status == HttpStatusCode.Unauthorized) {
                    DomainError.Unauthorized
                } else {
                    DomainError.Unknown("Request failed: ${e.response.status.value}")
                }
            )
        } catch (e: Exception) {
            AppResult.Failure(DomainError.NetworkError)
        }
    }
}
