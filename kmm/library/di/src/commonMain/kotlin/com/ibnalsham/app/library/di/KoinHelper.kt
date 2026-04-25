package com.ibnalsham.app.library.di

import org.koin.core.context.startKoin
import org.koin.core.Koin
import org.koin.core.parameter.parametersOf
import org.koin.core.component.KoinComponent
import org.koin.core.component.get

fun initKoin() {
    startKoin {
        modules(appModule)
    }
}

/**
 * Helper class for iOS to access Koin-managed instances.
 */
object KoinHelper : KoinComponent {
    fun <T : Any> get(clazz: kotlin.reflect.KClass<T>): T {
        return getKoin().get(clazz, null, null)
    }
}
