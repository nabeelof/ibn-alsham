package com.ibnalsham.app.data.review

import com.ibnalsham.app.core.common.AppResult
import com.ibnalsham.app.data.network.error.DomainError
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.post

interface ReviewRepository {
    suspend fun productReviews(productId: Int): AppResult<DomainError, ReviewListResponseDto>
    suspend fun submitReview(): AppResult<DomainError, ReviewSubmitResponseDto>
}

@kotlinx.serialization.Serializable
data class ReviewListResponseDto(
    val data: List<kotlinx.serialization.json.JsonObject> = emptyList(),
    val success: Boolean = false,
    val status: Int = 0,
)

@kotlinx.serialization.Serializable
data class ReviewSubmitResponseDto(
    val result: Boolean = false,
    val message: String? = null,
)

class NetworkReviewRepository(
    private val httpClient: HttpClient
) : ReviewRepository {
    override suspend fun productReviews(productId: Int): AppResult<DomainError, ReviewListResponseDto> {
        return try {
            val response: ReviewListResponseDto = httpClient.get("reviews/product/$productId").body()
            AppResult.Success(response)
        } catch (e: Exception) {
            AppResult.Failure(DomainError.NetworkError)
        }
    }

    override suspend fun submitReview(): AppResult<DomainError, ReviewSubmitResponseDto> {
        return try {
            val response: ReviewSubmitResponseDto = httpClient.post("reviews/submit").body()
            AppResult.Success(response)
        } catch (e: Exception) {
            AppResult.Failure(DomainError.NetworkError)
        }
    }
}
