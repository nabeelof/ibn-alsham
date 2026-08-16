package com.ibnalsham.app.library.data.network

import com.ibnalsham.app.library.core.environment.EnvironmentConfig
import com.ibnalsham.app.library.data.cache.UserPreferencesRepository
import io.ktor.client.HttpClient
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.header
import io.ktor.http.HttpHeaders
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

fun createHttpClient(
    json: Json,
    userPreferencesRepository: UserPreferencesRepository,
): HttpClient {
    return platformHttpClient {
        install(ContentNegotiation) {
            json(json)
        }
        
        install(Logging) {
            logger = object : Logger {
                override fun log(message: String) {
                    println("Ktor: $message")
                }
            }
            level = if (EnvironmentConfig.current.isDebug) LogLevel.ALL else LogLevel.INFO
        }
        
        defaultRequest {
            url(EnvironmentConfig.current.baseUrl)
            contentType(ContentType.Application.Json)
            header("Accept", "application/json")
            userPreferencesRepository.getAuthToken()
                ?.takeIf { it.isNotBlank() }
                ?.let { token ->
                    header(HttpHeaders.Authorization, "Bearer $token")
                }
        }
    }
}

val networkJson = Json {
    ignoreUnknownKeys = true
    isLenient = true
    encodeDefaults = true
    prettyPrint = true
}
