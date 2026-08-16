package com.ibnalsham.app.library.data.cache

import android.content.Context
import androidx.appcompat.app.AppCompatDelegate
import org.koin.core.context.GlobalContext

actual fun createPlatformInfo(): PlatformInfo = AndroidPlatformInfo()

private class AndroidPlatformInfo : PlatformInfo {

    private val context: Context by lazy { GlobalContext.get().get() }

    override val appVersion: String
        get() = runCatching {
            context.packageManager.getPackageInfo(context.packageName, 0).versionName
        }.getOrNull().orEmpty()

    /**
     * AppCompat's per-app locale rather than the device locale — that is what
     * the app actually displays once a language is chosen in Settings.
     */
    override fun currentLanguageTag(): String? =
        AppCompatDelegate.getApplicationLocales().toLanguageTags().takeIf { it.isNotBlank() }
}
