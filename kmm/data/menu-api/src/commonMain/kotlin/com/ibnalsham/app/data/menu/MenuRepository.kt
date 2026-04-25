package com.ibnalsham.app.data.menu

import com.ibnalsham.app.core.common.AppResult
import com.ibnalsham.app.data.home.ProductDto
import com.ibnalsham.app.library.data.network.BaseResponse
import com.ibnalsham.app.data.network.error.DomainError
import com.ibnalsham.app.feature.domain.CategoryDomain
import com.ibnalsham.app.feature.domain.ProductDomain
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList

interface MenuRepository {
    suspend fun getCategories(): AppResult<DomainError, ImmutableList<CategoryDomain>>
    suspend fun getProducts(categoryId: Int?, page: Int = 1): AppResult<DomainError, PaginatedProductsDomain>
}

data class PaginatedProductsDomain(
    val products: ImmutableList<ProductDomain>,
    val currentPage: Int,
    val lastPage: Int
)

class NetworkMenuRepository(
    private val httpClient: HttpClient
) : MenuRepository {

    override suspend fun getCategories(): AppResult<DomainError, ImmutableList<CategoryDomain>> {
        return try {
            val response: BaseResponse<List<CategoryWithCountDto>> = httpClient.get("categories").body()
            val data = response.data
            if (response.success && data != null) {
                AppResult.Success(data.map { it.toDomain() }.toImmutableList())
            } else {
                AppResult.Failure(DomainError.Unknown(response.message ?: "Failed to fetch categories"))
            }
        } catch (e: Exception) {
            AppResult.Failure(DomainError.NetworkError)
        }
    }

    override suspend fun getProducts(categoryId: Int?, page: Int): AppResult<DomainError, PaginatedProductsDomain> {
        return try {
            val response: ProductsListResponse = httpClient.get("products") {
                categoryId?.let { parameter("category_id", it) }
                parameter("page", page)
            }.body()

            val data = response.data
            if (response.success && data != null) {
                AppResult.Success(
                    PaginatedProductsDomain(
                        products = data.map { it.toDomain() }.toImmutableList(),
                        currentPage = response.pagination?.currentPage ?: page,
                        lastPage = response.pagination?.lastPage ?: page
                    )
                )
            } else {
                AppResult.Failure(DomainError.Unknown(response.message ?: "Failed to fetch products"))
            }
        } catch (e: Exception) {
            AppResult.Failure(DomainError.NetworkError)
        }
    }

    private fun CategoryWithCountDto.toDomain() = CategoryDomain(
        id = id,
        name = name,
        image = icon
    )

    private fun ProductDto.toDomain() = ProductDomain(
        id = id,
        name = name,
        price = price,
        image = imageUrl
    )
}
