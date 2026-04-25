package com.ibnalsham.app.data.cart

import com.ibnalsham.app.core.common.AppResult
import com.ibnalsham.app.data.network.error.DomainError
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.delete
import io.ktor.client.request.post
import io.ktor.client.request.setBody

interface CartApiRepository {
    suspend fun getCart(user: CartUserRef): AppResult<DomainError, CartResponseDto>
    suspend fun getCount(user: CartUserRef): AppResult<DomainError, CartCountDto>
    suspend fun addToCart(request: CartAddRequest): AppResult<DomainError, CartResponseDto>
    suspend fun processCart(request: CartProcessRequest): AppResult<DomainError, CartResponseDto>
    suspend fun summary(user: CartUserRef): AppResult<DomainError, CartSummaryDto>
    suspend fun deleteItem(id: Int, user: CartUserRef): AppResult<DomainError, Boolean>
}

class NetworkCartApiRepository(
    private val httpClient: HttpClient
) : CartApiRepository {

    override suspend fun getCart(user: CartUserRef): AppResult<DomainError, CartResponseDto> = postBody("carts", user)

    override suspend fun getCount(user: CartUserRef): AppResult<DomainError, CartCountDto> =
        postBody("cart-count", user)

    override suspend fun addToCart(request: CartAddRequest): AppResult<DomainError, CartResponseDto> =
        postBody("carts/add", request)

    override suspend fun processCart(request: CartProcessRequest): AppResult<DomainError, CartResponseDto> =
        postBody("carts/process", request)

    override suspend fun summary(user: CartUserRef): AppResult<DomainError, CartSummaryDto> =
        postBody("cart-summary", user)

    override suspend fun deleteItem(id: Int, user: CartUserRef): AppResult<DomainError, Boolean> {
        return try {
            httpClient.delete("carts/$id")
            AppResult.Success(true)
        } catch (e: Exception) {
            AppResult.Failure(DomainError.NetworkError)
        }
    }

    private suspend inline fun <reified T> postBody(path: String, body: Any): AppResult<DomainError, T> {
        return try {
            val response: T = httpClient.post(path) {
                setBody(body)
            }.body()
            AppResult.Success(response)
        } catch (e: Exception) {
            AppResult.Failure(DomainError.NetworkError)
        }
    }
}
