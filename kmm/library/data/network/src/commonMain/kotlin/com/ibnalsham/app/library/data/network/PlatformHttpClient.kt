package com.ibnalsham.app.library.data.network

import io.ktor.client.HttpClient
import io.ktor.client.HttpClientConfig

/**
 * Builds the platform's Ktor engine with the tweaks each one needs.
 * Shared plugins (JSON, logging, auth header) are configured by the caller.
 */
expect fun platformHttpClient(block: HttpClientConfig<*>.() -> Unit): HttpClient
