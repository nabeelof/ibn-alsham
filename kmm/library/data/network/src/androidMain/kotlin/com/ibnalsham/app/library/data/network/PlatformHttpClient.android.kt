package com.ibnalsham.app.library.data.network

import io.ktor.client.HttpClient
import io.ktor.client.HttpClientConfig
import io.ktor.client.engine.okhttp.OkHttp
import okhttp3.Dns
import java.net.Inet4Address
import java.net.InetAddress

/**
 * Resolves hostnames with IPv4 addresses first.
 *
 * The API host publishes both A and AAAA records. On IPv4-only mobile networks
 * — still common in Jordan — an AAAA-first attempt stalls until it times out
 * before OkHttp falls back, which shows up as a blank home screen. Ordering
 * IPv4 first keeps those users on the fast path; dual-stack clients are
 * unaffected because both addresses reach the same server.
 */
private object Ipv4FirstDns : Dns {
    override fun lookup(hostname: String): List<InetAddress> {
        val addresses = Dns.SYSTEM.lookup(hostname)
        return addresses.sortedByDescending { it is Inet4Address }
    }
}

actual fun platformHttpClient(block: HttpClientConfig<*>.() -> Unit): HttpClient =
    HttpClient(OkHttp) {
        engine {
            config {
                dns(Ipv4FirstDns)
                retryOnConnectionFailure(true)
            }
        }
        block()
    }
