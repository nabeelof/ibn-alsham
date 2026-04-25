package com.ibnalsham.app.data.search

import com.ibnalsham.app.core.common.AppResult
import com.ibnalsham.app.data.network.error.DomainError
import com.ibnalsham.app.library.data.network.BaseResponse
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList

interface SearchRepository {
    suspend fun getSuggestions(query: String): AppResult<DomainError, ImmutableList<String>>
}

class NetworkSearchRepository(
    private val httpClient: HttpClient
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
}
