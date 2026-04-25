package com.ibnalsham.app.data.catalog

import com.ibnalsham.app.core.common.AppResult
import com.ibnalsham.app.data.network.error.DomainError
import com.ibnalsham.app.library.data.network.BaseResponse
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList

interface CatalogRepository {
    suspend fun getCategories(): AppResult<DomainError, ImmutableList<CategoryDto>>
    suspend fun getFeaturedCategories(): AppResult<DomainError, ImmutableList<CategoryDto>>
    suspend fun getTopCategories(): AppResult<DomainError, ImmutableList<CategoryDto>>
}

class NetworkCatalogRepository(
    private val httpClient: HttpClient
) : CatalogRepository {
    override suspend fun getCategories(): AppResult<DomainError, ImmutableList<CategoryDto>> = getList("categories")
    override suspend fun getFeaturedCategories(): AppResult<DomainError, ImmutableList<CategoryDto>> = getList("categories/featured")
    override suspend fun getTopCategories(): AppResult<DomainError, ImmutableList<CategoryDto>> = getList("categories/top")

    private suspend fun getList(path: String): AppResult<DomainError, ImmutableList<CategoryDto>> {
        return try {
            val response: BaseResponse<List<CategoryDto>> = httpClient.get(path).body()
            val data = response.data
            if (response.success && data != null) {
                AppResult.Success(data.toImmutableList())
            } else {
                AppResult.Failure(DomainError.Unknown(response.message ?: "Failed to fetch categories"))
            }
        } catch (e: Exception) {
            AppResult.Failure(DomainError.NetworkError)
        }
    }
}
