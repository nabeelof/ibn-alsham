package com.ibnalsham.app.library.data.network

import io.ktor.client.HttpClient
import io.ktor.client.HttpClientConfig
import io.ktor.client.engine.darwin.Darwin

/**
 * NSURLSession already does Happy Eyeballs, so iOS needs no address-family
 * tuning — unlike the Android/OkHttp path.
 */
actual fun platformHttpClient(block: HttpClientConfig<*>.() -> Unit): HttpClient =
    HttpClient(Darwin) {
        block()
    }
