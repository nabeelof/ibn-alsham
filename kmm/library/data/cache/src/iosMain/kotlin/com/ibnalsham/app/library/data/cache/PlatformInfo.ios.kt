package com.ibnalsham.app.library.data.cache

import platform.Foundation.NSBundle
import platform.Foundation.NSLocale
import platform.Foundation.NSUserDefaults
import platform.Foundation.preferredLanguages

actual fun createPlatformInfo(): PlatformInfo = IosPlatformInfo()

private class IosPlatformInfo : PlatformInfo {

    override val appVersion: String
        get() = NSBundle.mainBundle.objectForInfoDictionaryKey("CFBundleShortVersionString") as? String
            ?: ""

    /**
     * iOS has no AppCompat-style per-app locale override, so the first
     * preferred language is the one being displayed.
     */
    override fun currentLanguageTag(): String? =
        NSLocale.preferredLanguages.firstOrNull() as? String
}
