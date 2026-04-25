package com.ibnalsham.app.data.upload

import com.ibnalsham.app.core.common.AppResult
import com.ibnalsham.app.data.network.error.DomainError
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.post

interface UploadRepository {
    suspend fun list(): AppResult<DomainError, UploadListResponseDto>
    suspend fun delete(id: Int): AppResult<DomainError, Boolean>
}

class NetworkUploadRepository(
    private val httpClient: HttpClient
) : UploadRepository {
    override suspend fun list(): AppResult<DomainError, UploadListResponseDto> = get("file/all")

    override suspend fun delete(id: Int): AppResult<DomainError, Boolean> {
        return try {
            httpClient.get("file/delete/$id")
            AppResult.Success(true)
        } catch (e: Exception) {
            AppResult.Failure(DomainError.NetworkError)
        }
    }

    private suspend inline fun <reified T> get(path: String): AppResult<DomainError, T> {
        return try {
            val response: T = httpClient.get(path).body()
            AppResult.Success(response)
        } catch (e: Exception) {
            AppResult.Failure(DomainError.NetworkError)
        }
    }
}
