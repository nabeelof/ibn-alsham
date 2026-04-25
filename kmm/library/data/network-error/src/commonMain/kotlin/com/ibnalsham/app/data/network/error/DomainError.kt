package com.ibnalsham.app.data.network.error

sealed class DomainError {
    data object NetworkError : DomainError()
    data object ServerError : DomainError()
    data object NotFound : DomainError()
    data object Unauthorized : DomainError()
    data class Unknown(val message: String?) : DomainError()
}
