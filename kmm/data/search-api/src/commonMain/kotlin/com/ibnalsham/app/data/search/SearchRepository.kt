package com.ibnalsham.app.data.search

import com.ibnalsham.app.core.common.AppResult
import com.ibnalsham.app.data.home.ProductDto
import com.ibnalsham.app.data.home.toDomain
import com.ibnalsham.app.data.network.error.DomainError
import com.ibnalsham.app.feature.domain.FilterOptionsDomain
import com.ibnalsham.app.feature.domain.ProductDomain
import com.ibnalsham.app.feature.domain.ProductFilterDomain
import com.ibnalsham.app.library.data.network.BaseResponse
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList

interface SearchRepository {
    suspend fun getSuggestions(query: String): AppResult<DomainError, ImmutableList<String>>

    /** Full-text search combined with the advanced filters from the filter sheet. */
    suspend fun search(
        query: String?,
        filter: ProductFilterDomain = ProductFilterDomain(),
        page: Int = 1,
    ): AppResult<DomainError, ImmutableList<ProductDomain>>

    /** Categories and JOD price bounds used to render the filter sheet. */
    suspend fun getFilterOptions(): AppResult<DomainError, FilterOptionsDomain>
}

class NetworkSearchRepository(
    private val httpClient: HttpClient,
) : SearchRepository {

    override suspend fun getSuggestions(query: String): AppResult<DomainError, ImmutableList<String>> {
        return try {
            val response: BaseResponse<List<String>> = httpClient.get("get-search-suggestions") {
                parameter("query_key", query)
            }.body()
            val data = response.data
            if (response.success && data != null) {
                AppResult.Success(data.toImmutableList())
            } else {
                AppResult.Failure(DomainError.Unknown(response.message ?: "Failed to fetch suggestions"))
            }
        } catch (e: Exception) {
            AppResult.Failure(DomainError.NetworkError)
        }
    }

    override suspend fun search(
        query: String?,
        filter: ProductFilterDomain,
        page: Int,
    ): AppResult<DomainError, ImmutableList<ProductDomain>> {
        return try {
            val response: BaseResponse<List<ProductDto>> = httpClient.get("products/search") {
                if (!query.isNullOrBlank()) parameter("name", query)
                filter.categoryId?.let { parameter("category_id", it) }
                filter.minPrice?.let { parameter("min_price", it) }
                filter.maxPrice?.let { parameter("max_price", it) }
                if (filter.onlyOffers) parameter("only_offers", 1)
                if (filter.onlyFeatured) parameter("only_featured", 1)
                parameter("sort", filter.sort.key)
                parameter("page", page)
            }.body()

            val data = response.data
            if (response.success && data != null) {
                AppResult.Success(data.map { it.toDomain() }.toImmutableList())
            } else {
                AppResult.Failure(DomainError.Unknown(response.message ?: "Failed to search products"))
            }
        } catch (e: Exception) {
            AppResult.Failure(DomainError.NetworkError)
        }
    }

    override suspend fun getFilterOptions(): AppResult<DomainError, FilterOptionsDomain> {
        return try {
            val response: BaseResponse<FilterOptionsDto> = httpClient.get("products/filters").body()
            val data = response.data
            if (response.success && data != null) {
                AppResult.Success(data.toDomain())
            } else {
                AppResult.Failure(DomainError.Unknown(response.message ?: "Failed to fetch filters"))
            }
        } catch (e: Exception) {
            AppResult.Failure(DomainError.NetworkError)
        }
    }
}
