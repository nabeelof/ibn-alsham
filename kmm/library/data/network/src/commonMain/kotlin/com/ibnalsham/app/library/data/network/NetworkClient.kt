package com.ibnalsham.app.library.data.network

import com.ibnalsham.app.library.core.environment.EnvironmentConfig
import io.ktor.client.HttpClient
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.header
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

fun createHttpClient(json: Json): HttpClient {
    return HttpClient {
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
            // Default headers - we can add auth tokens here later
            header("Accept", "application/json")
        }
    }
}

val networkJson = Json {
    ignoreUnknownKeys = true
    isLenient = true
    encodeDefaults = true
    prettyPrint = true
}
