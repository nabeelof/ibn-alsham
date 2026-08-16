package com.ibnalsham.app

import android.app.Application
import com.ibnalsham.app.library.data.cache.UserPreferencesRepository
import com.ibnalsham.app.library.di.appModule
import com.ibnalsham.app.push.PushTokenSync
import org.koin.android.ext.android.get
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.dsl.module
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat

private val androidPrefsModule = module {
    single<UserPreferencesRepository> {
        SharedPreferencesUserPreferencesRepository(androidContext())
    }
}

class MainApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidLogger()
            androidContext(this@MainApplication)
            modules(appModule, androidPrefsModule)
        }

        // Set Arabic as default for first run
        if (AppCompatDelegate.getApplicationLocales().isEmpty) {
            AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags("ar"))
        }

        // Refresh the push token on every cold start so a re-installed or
        // restored device keeps receiving order notifications.
        PushTokenSync.syncIfSignedIn(
            context = this,
            userPreferences = get(),
            notificationRepository = get(),
        )
    }
}
