package com.ibnalsham.app.core.common

sealed class AppResult<out L, out R> {
    data class Failure<L>(val errorValue: L) : AppResult<L, Nothing>()
    data class Success<R>(val value: R) : AppResult<Nothing, R>()
}

inline fun <L, R> AppResult<L, R>.onSuccess(block: (R) -> Unit): AppResult<L, R> {
    if (this is AppResult.Success) block(value)
    return this
}

inline fun <L, R> AppResult<L, R>.onFailure(block: (L) -> Unit): AppResult<L, R> {
    if (this is AppResult.Failure) block(errorValue)
    return this
}

inline fun <L, R, T> AppResult<L, R>.map(transform: (R) -> T): AppResult<L, T> {
    return when (this) {
        is AppResult.Success -> AppResult.Success(transform(value))
        is AppResult.Failure -> AppResult.Failure(errorValue)
    }
}
