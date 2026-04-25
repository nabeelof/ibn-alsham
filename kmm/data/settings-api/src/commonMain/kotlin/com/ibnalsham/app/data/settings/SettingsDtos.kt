package com.ibnalsham.app.data.settings

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class LanguageDto(
    val id: Int,
    val name: String,
    val image: String? = null,
    val code: String,
    @SerialName("mobile_app_code") val mobileAppCode: String,
    val rtl: Boolean,
    @SerialName("is_default") val isDefault: Boolean,
)

@Serializable
data class LanguageListResponseDto(
    val data: List<LanguageDto> = emptyList(),
    val success: Boolean = false,
    val status: Int = 0,
)

@Serializable
data class CurrencyDto(
    val id: Int,
    val name: String,
    val code: String,
    val symbol: String,
    @SerialName("exchange_rate") val exchangeRate: Double,
    @SerialName("is_default") val isDefault: Boolean,
)

@Serializable
data class CurrencyListResponseDto(
    val data: List<CurrencyDto> = emptyList(),
    val success: Boolean = false,
    val status: Int = 0,
)

@Serializable
data class BusinessSettingDto(
    val type: String,
    val value: String? = null,
)

@Serializable
data class BusinessSettingsResponseDto(
    val data: List<BusinessSettingDto> = emptyList(),
    val success: Boolean = false,
    val status: Int = 0,
)
