package com.ibnalsham.app.data.coupon

import com.ibnalsham.app.core.common.AppResult
import com.ibnalsham.app.data.network.error.DomainError
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody

interface CouponRepository {
    suspend fun apply(code: String): AppResult<DomainError, CouponApplyResponseDto>
    suspend fun remove(): AppResult<DomainError, CouponApplyResponseDto>
    suspend fun list(): AppResult<DomainError, CouponListResponseDto>
}

class NetworkCouponRepository(
    private val httpClient: HttpClient
) : CouponRepository {
    override suspend fun apply(code: String): AppResult<DomainError, CouponApplyResponseDto> {
        return try {
            val response: CouponApplyResponseDto = httpClient.post("coupon-apply") { setBody(mapOf("code" to code)) }.body()
            AppResult.Success(response)
        } catch (e: Exception) {
            AppResult.Failure(DomainError.NetworkError)
        }
    }

    override suspend fun remove(): AppResult<DomainError, CouponApplyResponseDto> {
        return try {
            val response: CouponApplyResponseDto = httpClient.post("coupon-remove").body()
            AppResult.Success(response)
        } catch (e: Exception) {
            AppResult.Failure(DomainError.NetworkError)
        }
    }

    override suspend fun list(): AppResult<DomainError, CouponListResponseDto> {
        return try {
            val response: CouponListResponseDto = httpClient.get("coupon-list").body()
            AppResult.Success(response)
        } catch (e: Exception) {
            AppResult.Failure(DomainError.NetworkError)
        }
    }
}
