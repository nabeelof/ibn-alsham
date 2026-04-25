package com.ibnalsham.app.data.wallet

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class WalletBalanceDto(
    val balance: String? = null,
    @SerialName("currency_symbol") val currencySymbol: String? = null,
    val result: Boolean = false,
)

@Serializable
data class WalletHistoryDto(
    val data: List<kotlinx.serialization.json.JsonObject> = emptyList(),
    val result: Boolean = false,
    val status: Int = 0,
)
