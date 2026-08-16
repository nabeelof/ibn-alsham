package com.ibnalsham.app.data.coupon

import com.ibnalsham.app.core.common.AppResult
import com.ibnalsham.app.data.network.error.DomainError
import com.ibnalsham.app.feature.domain.CouponDomain
import com.ibnalsham.app.library.data.network.BaseResponse
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList

interface CouponRepository {
    /**
     * Validate a code against the current subtotal. The coupon is not consumed
     * here — it is redeemed when the order is placed.
     */
    suspend fun apply(code: String, subtotal: Double): AppResult<DomainError, CouponDomain>
    suspend fun remove(): AppResult<DomainError, Boolean>

    /** Coupons the signed-in customer can currently use. */
    suspend fun list(subtotal: Double = 0.0): AppResult<DomainError, ImmutableList<CouponDomain>>
}

class NetworkCouponRepository(
    private val httpClient: HttpClient,
) : CouponRepository {

    override suspend fun apply(code: String, subtotal: Double): AppResult<DomainError, CouponDomain> {
        return try {
            val response: BaseResponse<CouponDto> = httpClient.post("coupon-apply") {
                setBody(CouponApplyRequestDto(couponCode = code, subtotal = subtotal))
            }.body()

            val data = response.data
            if (response.success && data != null) {
                AppResult.Success(data.toDomain())
            } else {
                // The API sends the human-readable reason (expired, min order, …)
                AppResult.Failure(DomainError.Unknown(response.message))
            }
        } catch (e: Exception) {
            AppResult.Failure(DomainError.NetworkError)
        }
    }

    override suspend fun remove(): AppResult<DomainError, Boolean> {
        return try {
            httpClient.post("coupon-remove")
            AppResult.Success(true)
        } catch (e: Exception) {
            AppResult.Failure(DomainError.NetworkError)
        }
    }

    override suspend fun list(subtotal: Double): AppResult<DomainError, ImmutableList<CouponDomain>> {
        return try {
            val response: BaseResponse<List<CouponDto>> = httpClient.get("coupon-list") {
                parameter("subtotal", subtotal)
            }.body()

            val data = response.data
            if (response.success && data != null) {
                AppResult.Success(data.map { it.toDomain() }.toImmutableList())
            } else {
                AppResult.Failure(DomainError.Unknown(response.message ?: "Failed to fetch coupons"))
            }
        } catch (e: Exception) {
            AppResult.Failure(DomainError.NetworkError)
        }
    }
}
