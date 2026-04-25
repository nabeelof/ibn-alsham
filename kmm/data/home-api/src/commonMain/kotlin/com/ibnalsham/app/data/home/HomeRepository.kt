package com.ibnalsham.app.data.home

import com.ibnalsham.app.core.common.AppResult
import com.ibnalsham.app.library.data.network.BaseResponse
import com.ibnalsham.app.data.network.error.DomainError
import com.ibnalsham.app.feature.domain.BannerDomain
import com.ibnalsham.app.feature.domain.CategoryDomain
import com.ibnalsham.app.feature.domain.ProductDomain
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList

interface HomeRepository {
    suspend fun getHomeData(): AppResult<DomainError, HomeDataDomain>
}

data class HomeDataDomain(
    val banners: ImmutableList<BannerDomain>,
    val categories: ImmutableList<CategoryDomain>,
    val featuredProducts: ImmutableList<ProductDomain>,
    val specialOffers: ImmutableList<ProductDomain>,
    val announcement: String?
)

class NetworkHomeRepository(
    private val httpClient: HttpClient
) : HomeRepository {

    override suspend fun getHomeData(): AppResult<DomainError, HomeDataDomain> {
        return try {
            val response: BaseResponse<HomeResponseDto> = httpClient.get("home").body()
            val data = response.data
            if (response.success && data != null) {
                AppResult.Success(data.toDomain())
            } else {
                AppResult.Failure(DomainError.Unknown(response.message ?: "Failed to fetch home data"))
            }
        } catch (e: Exception) {
            AppResult.Failure(DomainError.NetworkError)
        }
    }

    private fun HomeResponseDto.toDomain() = HomeDataDomain(
        banners = banners.map { it.toDomain() }.toImmutableList(),
        categories = categories.map { it.toDomain() }.toImmutableList(),
        featuredProducts = featuredProducts.map { it.toDomain() }.toImmutableList(),
        specialOffers = specialOffers.map { it.toDomain() }.toImmutableList(),
        announcement = announcement
    )

    private fun BannerDto.toDomain() = BannerDomain(
        id = id,
        imageUrl = imageUrl,
        title = title
    )

    private fun CategoryDto.toDomain() = CategoryDomain(
        id = id,
        name = name,
        image = icon
    )

    private fun ProductDto.toDomain() = ProductDomain(
        id = id,
        name = name,
        price = price,
        image = imageUrl,
        categoryName = categoryName,
    )
}
