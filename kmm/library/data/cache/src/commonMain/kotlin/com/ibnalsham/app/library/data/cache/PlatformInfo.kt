package com.ibnalsham.app.library.data.cache

/**
 * The handful of host facts shared code needs but cannot read itself.
 *
 * Provided by [createPlatformInfo] so every platform gets a working default —
 * making this a host-registered dependency instead would leave any platform that
 * forgot to register it failing at resolution time.
 */
interface PlatformInfo {
    /** Version name shown in Settings, e.g. "1.0". */
    val appVersion: String

    /**
     * BCP-47 tag of the locale the app is currently displaying, or null when
     * the host has not overridden the system locale.
     */
    fun currentLanguageTag(): String?
}

expect fun createPlatformInfo(): PlatformInfo
