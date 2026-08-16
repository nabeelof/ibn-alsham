package com.ibnalsham.app.data.product

import com.ibnalsham.app.core.common.AppResult
import com.ibnalsham.app.data.network.error.DomainError
import com.ibnalsham.app.feature.domain.ProductDomain
import com.ibnalsham.app.library.data.network.BaseResponse
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList

interface ProductRepository {
    suspend fun getFeaturedProducts(): AppResult<DomainError, ImmutableList<ProductDto>>
    suspend fun getBestSellerProducts(): AppResult<DomainError, ImmutableList<ProductDto>>
    suspend fun getTodaysDealProducts(): AppResult<DomainError, ImmutableList<ProductDto>>
    suspend fun searchProducts(query: String?): AppResult<DomainError, ImmutableList<ProductDto>>
    suspend fun getProductDetailsBySlug(slug: String, userId: String = ""): AppResult<DomainError, ProductDetailsResponseDto>
    suspend fun getVariantPrice(slug: String, quantity: Int): AppResult<DomainError, VariantPriceDto>

    /**
     * Full product with its add-on groups, used by the product details screen.
     */
    suspend fun getProduct(productId: Int): AppResult<DomainError, ProductDomain>
}

class NetworkProductRepository(
    private val httpClient: HttpClient
) : ProductRepository {

    override suspend fun getFeaturedProducts(): AppResult<DomainError, ImmutableList<ProductDto>> =
        getList("products/featured")

    override suspend fun getBestSellerProducts(): AppResult<DomainError, ImmutableList<ProductDto>> =
        getList("products/best-seller")

    override suspend fun getTodaysDealProducts(): AppResult<DomainError, ImmutableList<ProductDto>> =
        getList("products/todays-deal")

    override suspend fun searchProducts(query: String?): AppResult<DomainError, ImmutableList<ProductDto>> {
        return try {
            val response: BaseResponse<List<ProductDto>> = httpClient.get("products/search") {
                if (!query.isNullOrBlank()) parameter("name", query)
            }.body()
            val data = response.data
            if (response.success && data != null) {
                AppResult.Success(data.toImmutableList())
            } else {
                AppResult.Failure(DomainError.Unknown(response.message ?: "Failed to search products"))
            }
        } catch (e: Exception) {
            AppResult.Failure(DomainError.NetworkError)
        }
    }

    override suspend fun getProductDetailsBySlug(slug: String, userId: String): AppResult<DomainError, ProductDetailsResponseDto> {
        return try {
            val response: ProductDetailsResponseDto = httpClient.get("products/$slug/$userId").body()
            if (response.success) {
                AppResult.Success(response)
            } else {
                AppResult.Failure(DomainError.Unknown("Failed to fetch product details"))
            }
        } catch (e: Exception) {
            AppResult.Failure(DomainError.NetworkError)
        }
    }

    override suspend fun getVariantPrice(slug: String, quantity: Int): AppResult<DomainError, VariantPriceDto> {
        return try {
            val response: VariantPriceResponseDto = httpClient.get("products/variant/price") {
                parameter("slug", slug)
                parameter("quantity", quantity)
            }.body()
            
            if (response.result && response.data != null) {
                AppResult.Success(response.data)
            } else {
                AppResult.Failure(DomainError.Unknown("Failed to fetch variant price"))
            }
        } catch (e: Exception) {
            AppResult.Failure(DomainError.NetworkError)
        }
    }

    override suspend fun getProduct(productId: Int): AppResult<DomainError, ProductDomain> {
        return try {
            val response: BaseResponse<ProductDetailsDto> = httpClient.get("products/$productId").body()
            val data = response.data
            if (response.success && data != null) {
                AppResult.Success(data.toDomain())
            } else {
                AppResult.Failure(DomainError.Unknown(response.message ?: "Failed to fetch product"))
            }
        } catch (e: Exception) {
            AppResult.Failure(DomainError.NetworkError)
        }
    }

    private suspend fun getList(path: String): AppResult<DomainError, ImmutableList<ProductDto>> {
        return try {
            val response: BaseResponse<List<ProductDto>> = httpClient.get(path).body()
            val data = response.data
            if (response.success && data != null) {
                AppResult.Success(data.toImmutableList())
            } else {
                AppResult.Failure(DomainError.Unknown(response.message ?: "Failed to fetch products"))
            }
        } catch (e: Exception) {
            AppResult.Failure(DomainError.NetworkError)
        }
    }
}
